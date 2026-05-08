<template>
  <div class="home-page">
    <!-- Hero 区域 -->
    <section class="hero-section">
      <div class="hero-content">
        <h1 class="hero-title">
          汽车研发知识共享平台
        </h1>
        <p class="hero-desc">
          整合研发全流程知识资源，打造汽车行业知识共享生态
        </p>
        <div class="hero-search">
          <div class="search-wrapper">
            <el-icon class="search-icon"><Search /></el-icon>
            <input
              v-model="searchKeyword"
              type="text"
              class="search-input"
              placeholder="搜索技术文档、设计方案、故障解决方案..."
              @keyup.enter="handleSearch"
            />
            <button class="search-btn" @click="handleSearch">
              搜索
            </button>
          </div>
        </div>
        <div class="hero-tags">
          <span class="tag-label">热门搜索：</span>
          <el-tag
            v-for="tag in hotTags"
            :key="tag"
            @click="searchKeyword = tag; handleSearch()"
          >
            {{ tag }}
          </el-tag>
        </div>
      </div>
      <div class="hero-stats">
        <div class="stat-item" v-for="stat in stats" :key="stat.label">
          <span class="stat-value">{{ stat.value }}</span>
          <span class="stat-label">{{ stat.label }}</span>
        </div>
      </div>
    </section>

    <!-- 快速入口 -->
    <section class="quick-entry">
      <div class="entry-card" v-for="entry in quickEntries" :key="entry.title" @click="$router.push(entry.path)">
        <div class="entry-icon" :style="{ background: entry.gradient }">
          <el-icon :size="28"><component :is="entry.icon" /></el-icon>
        </div>
        <div class="entry-info">
          <h3>{{ entry.title }}</h3>
          <p>{{ entry.desc }}</p>
        </div>
        <el-icon class="entry-arrow"><ArrowRight /></el-icon>
      </div>
    </section>

    <!-- 主内容区 -->
    <div class="main-sections">
      <!-- 左侧：最新知识 -->
      <section class="section knowledge-section">
        <div class="section-header">
          <h2>📚 最新知识</h2>
          <router-link to="/knowledge/list" class="view-more">
            查看更多 <el-icon><ArrowRight /></el-icon>
          </router-link>
        </div>
        <div class="knowledge-list">
          <article class="knowledge-card" v-for="item in latestKnowledge" :key="item.id" @click="$router.push(`/knowledge/detail/${item.id}`)">
            <div class="card-header">
              <el-tag size="small" :type="getTypeTagType(item.type)">{{ item.type }}</el-tag>
              <span class="domain">{{ item.domain }}</span>
            </div>
            <h3 class="card-title">{{ item.title }}</h3>
            <p class="card-summary">{{ item.summary }}</p>
            <div class="card-footer">
              <div class="author">
                <el-avatar :size="24" :src="item.authorAvatar">{{ item.authorName?.charAt(0) }}</el-avatar>
                <span>{{ item.authorName }}</span>
              </div>
              <div class="card-stats">
                <span><el-icon><View /></el-icon> {{ item.viewCount }}</span>
                <span><el-icon><Star /></el-icon> {{ item.likeCount }}</span>
              </div>
            </div>
          </article>
        </div>
      </section>

      <!-- 右侧 -->
      <aside class="sidebar">
        <!-- 热门问答 -->
        <section class="section">
          <div class="section-header">
            <h2>🔥 热门问答</h2>
            <router-link to="/discussion/list" class="view-more">更多</router-link>
          </div>
          <div class="discussion-list">
            <div class="discussion-item" v-for="item in hotDiscussions" :key="item.id" @click="$router.push(`/discussion/detail/${item.id}`)">
              <el-tag :type="item.isResolved ? 'success' : 'warning'" size="small">
                {{ item.isResolved ? '已解决' : '待解决' }}
              </el-tag>
              <span class="title">{{ item.title }}</span>
              <span class="reply-count">{{ item.replyCount }} 回复</span>
            </div>
          </div>
        </section>

        <!-- 贡献排行 -->
        <section class="section">
          <div class="section-header">
            <h2>🏆 贡献排行</h2>
          </div>
          <div class="ranking-list">
            <div class="ranking-item" v-for="(user, index) in topContributors" :key="user.id">
              <span class="rank" :class="getRankClass(index)">{{ index + 1 }}</span>
              <el-avatar :size="32" :src="user.avatar">{{ (user.name || user.username)?.charAt(0) }}</el-avatar>
              <div class="user-info">
                <span class="name">{{ user.name || user.username }}</span>
                <span class="dept">{{ user.department || '未知部门' }}</span>
              </div>
              <span class="score">{{ user.score || user.count }}</span>
            </div>
          </div>
        </section>

        <!-- 领域分布 -->
        <section class="section">
          <div class="section-header">
            <h2>📊 知识领域分布</h2>
          </div>
          <div class="chart-container">
            <v-chart :option="domainChartOption" autoresize />
          </div>
        </section>
      </aside>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart } from 'echarts/charts'
import { TooltipComponent, LegendComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import { Search, ArrowRight, View, Star, Document, Folder, ChatDotRound, Upload } from '@element-plus/icons-vue'
import { getDashboardData, getContributionRanking, getDomainDistribution } from '@/api/statistics'
import { getKnowledgeList, getHotTags } from '@/api/knowledge'
import { getDiscussionList } from '@/api/discussion'

use([CanvasRenderer, PieChart, TooltipComponent, LegendComponent])

const router = useRouter()
const appStore = useAppStore()
const searchKeyword = ref('')

// 热门标签
const hotTags = ref([])

// 统计数据
const stats = ref([
  { value: '-', label: '知识文档' },
  { value: '-', label: '活跃用户' },
  { value: '-', label: '研发项目' },
  { value: '-', label: '技术问答' }
])

// 快速入口
const quickEntries = [
  { title: '知识库', desc: '浏览技术文档、设计方案', path: '/knowledge/list', icon: 'Document', gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
  { title: '项目空间', desc: '查看研发项目进展', path: '/project/list', icon: 'Folder', gradient: 'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)' },
  { title: '技术问答', desc: '解决技术难题', path: '/discussion/list', icon: 'ChatDotRound', gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
  { title: '上传知识', desc: '分享你的经验', path: '/knowledge/create', icon: 'Upload', gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' }
]

// 最新知识
const latestKnowledge = ref([])

// 热门问答
const hotDiscussions = ref([])

// 贡献排行
const topContributors = ref([])

// 领域分布数据
const domainData = ref([])
const domainColors = ['#667eea', '#38ef7d', '#f5576c', '#4facfe', '#fa8231', '#a855f7', '#ec4899', '#14b8a6']

// 领域分布图表
const domainChartOption = computed(() => ({
  tooltip: { trigger: 'item', formatter: '{b}: {c} ({d}%)' },
  legend: { top: 'bottom', textStyle: { color: 'var(--text-secondary)' } },
  series: [{
    type: 'pie',
    radius: ['40%', '60%'],
    center: ['50%', '40%'],
    data: domainData.value.map((item, index) => ({
      value: item.count || item.value,
      name: item.name || item.domain,
      itemStyle: { color: domainColors[index % domainColors.length] }
    })),
    label: { show: false },
    itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 }
  }]
}))

// 加载仪表盘数据
onMounted(async () => {
  // 并发请求所有数据
  const [dashboardRes, tagsRes, knowledgeRes, discussionRes, rankingRes, domainRes] = await Promise.allSettled([
    getDashboardData(),
    getHotTags({ limit: 5 }),
    getKnowledgeList({ page: 1, size: 4, sort: 'latest' }),
    getDiscussionList({ page: 1, size: 4 }),
    getContributionRanking({ limit: 5 }),
    getDomainDistribution()
  ])

  // 仪表盘统计
  if (dashboardRes.status === 'fulfilled' && dashboardRes.value?.data) {
    const d = dashboardRes.value.data
    stats.value = [
      { value: d.knowledgeCount?.toLocaleString() || '0', label: '知识文档' },
      { value: d.userCount?.toLocaleString() || '0', label: '活跃用户' },
      { value: d.projectCount?.toLocaleString() || '0', label: '研发项目' },
      { value: d.discussionCount?.toLocaleString() || '0', label: '技术问答' }
    ]
  }

  // 热门标签
  if (tagsRes.status === 'fulfilled' && tagsRes.value?.data) {
    hotTags.value = tagsRes.value.data
  }

  // 最新知识
  if (knowledgeRes.status === 'fulfilled' && knowledgeRes.value?.data) {
    const items = knowledgeRes.value.data.list || knowledgeRes.value.data
    latestKnowledge.value = Array.isArray(items) ? items : []
  }

  // 热门问答
  if (discussionRes.status === 'fulfilled' && discussionRes.value?.data) {
    const items = discussionRes.value.data.list || discussionRes.value.data
    hotDiscussions.value = Array.isArray(items) ? items : []
  }

  // 贡献排行
  if (rankingRes.status === 'fulfilled' && rankingRes.value?.data) {
    topContributors.value = rankingRes.value.data
  }

  // 领域分布
  if (domainRes.status === 'fulfilled' && domainRes.value?.data) {
    domainData.value = domainRes.value.data
  }
})

// 搜索
function handleSearch() {
  if (searchKeyword.value.trim()) {
    appStore.addSearchHistory(searchKeyword.value)
    router.push({ path: '/knowledge/list', query: { keyword: searchKeyword.value } })
  }
}

function getTypeTagType(type) {
  const typeMap = { '技术文档': 'primary', '设计方案': 'success', '故障解决方案': 'warning', '仿真数据': 'info', '测试报告': 'danger' }
  return typeMap[type] || ''
}

function getRankClass(index) {
  if (index === 0) return 'gold'
  if (index === 1) return 'silver'
  if (index === 2) return 'bronze'
  return ''
}
</script>

<style scoped>
.home-page {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

/* Hero 区域 */
.hero-section {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.15) 0%, rgba(139, 92, 246, 0.15) 100%);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-xl);
  padding: 48px;
  text-align: center;
}

.hero-title {
  font-size: 42px;
  font-weight: 800;
  background: linear-gradient(135deg, var(--primary-color) 0%, #8b5cf6 50%, #f5576c 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  margin-bottom: 16px;
}

.hero-desc {
  font-size: 18px;
  color: var(--text-secondary);
  margin-bottom: 32px;
}

.hero-search {
  max-width: 640px;
  margin: 0 auto 24px;
}

.search-wrapper {
  display: flex;
  align-items: center;
  background: var(--bg-card);
  border: 2px solid var(--border-color);
  border-radius: 50px;
  padding: 6px 6px 6px 20px;
  transition: all var(--transition-fast);
}

.search-wrapper:focus-within {
  border-color: var(--primary-color);
  box-shadow: 0 0 0 4px rgba(24, 144, 255, 0.1);
}

.search-icon {
  font-size: 20px;
  color: var(--text-tertiary);
  margin-right: 12px;
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  border: none;
  outline: none;
  background: transparent;
  font-size: 16px;
  color: var(--text-primary);
  padding: 8px 0;
}

.search-input::placeholder {
  color: var(--text-tertiary);
}

.search-btn {
  flex-shrink: 0;
  padding: 12px 28px;
  background: linear-gradient(135deg, var(--primary-color) 0%, var(--primary-dark) 100%);
  color: white;
  border: none;
  border-radius: 50px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all var(--transition-fast);
}

.search-btn:hover {
  transform: scale(1.02);
  box-shadow: 0 4px 12px rgba(24, 144, 255, 0.4);
}

.hero-tags {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  flex-wrap: wrap;
}

.tag-label {
  color: var(--text-tertiary);
  font-size: 14px;
}

.hero-tags .el-tag {
  cursor: pointer;
  transition: all var(--transition-fast);
  background: var(--bg-tertiary);
  border-color: var(--border-color);
  color: var(--text-secondary);
}

.hero-tags .el-tag:hover {
  background: var(--primary-bg);
  border-color: var(--primary-color);
  color: var(--primary-color);
}

.hero-stats {
  display: flex;
  justify-content: center;
  gap: 64px;
  margin-top: 40px;
  padding-top: 32px;
  border-top: 1px solid var(--border-color);
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 8px;
}

.stat-value {
  font-size: 32px;
  font-weight: 800;
  color: var(--text-primary);
}

.stat-label {
  font-size: 14px;
  color: var(--text-tertiary);
}

/* 快速入口 */
.quick-entry {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.entry-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-normal);
}

.entry-card:hover {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-glow);
  transform: translateY(-2px);
}

.entry-icon {
  width: 56px;
  height: 56px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.entry-info {
  flex: 1;
}

.entry-info h3 {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.entry-info p {
  font-size: 13px;
  color: var(--text-tertiary);
}

.entry-arrow {
  color: var(--text-tertiary);
  transition: transform var(--transition-fast);
}

.entry-card:hover .entry-arrow {
  transform: translateX(4px);
  color: var(--primary-color);
}

/* 主内容区 */
.main-sections {
  display: grid;
  grid-template-columns: 1fr 360px;
  gap: 24px;
}

.section {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.section-header h2 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.view-more {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--text-secondary);
  font-size: 14px;
  text-decoration: none;
  transition: color var(--transition-fast);
}

.view-more:hover {
  color: var(--primary-color);
}

/* 知识列表 */
.knowledge-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.knowledge-card {
  padding: 20px;
  background: var(--bg-tertiary);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.knowledge-card:hover {
  background: var(--bg-hover);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.domain {
  font-size: 12px;
  color: var(--text-tertiary);
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
  line-height: 1.4;
}

.card-summary {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.author {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-secondary);
}

.card-stats {
  display: flex;
  gap: 16px;
  font-size: 13px;
  color: var(--text-tertiary);
}

.card-stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 侧边栏 */
.sidebar {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 问答列表 */
.discussion-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.discussion-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: var(--bg-tertiary);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.discussion-item:hover {
  background: var(--bg-hover);
}

.discussion-item .title {
  flex: 1;
  font-size: 14px;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.reply-count {
  font-size: 12px;
  color: var(--text-tertiary);
  white-space: nowrap;
}

/* 排行榜 */
.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 0;
}

.rank {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 12px;
  font-weight: 600;
  background: var(--bg-tertiary);
  color: var(--text-secondary);
}

.rank.gold { background: linear-gradient(135deg, #ffd700, #ffb700); color: #1a1a1a; }
.rank.silver { background: linear-gradient(135deg, #c0c0c0, #a0a0a0); color: #1a1a1a; }
.rank.bronze { background: linear-gradient(135deg, #cd7f32, #b87333); color: white; }

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-info .name {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.user-info .dept {
  font-size: 12px;
  color: var(--text-tertiary);
}

.score {
  font-size: 14px;
  font-weight: 600;
  color: var(--primary-color);
}

/* 图表 */
.chart-container {
  height: 250px;
}

.chart-container > * {
  width: 100%;
  height: 100%;
}

/* 响应式 */
@media (max-width: 1200px) {
  .quick-entry {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .main-sections {
    grid-template-columns: 1fr;
  }
  
  .sidebar {
    display: grid;
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .hero-section {
    padding: 32px 20px;
  }
  
  .hero-title {
    font-size: 28px;
  }
  
  .hero-stats {
    gap: 32px;
    flex-wrap: wrap;
  }
  
  .quick-entry {
    grid-template-columns: 1fr;
  }
  
  .sidebar {
    grid-template-columns: 1fr;
  }
}
</style>
