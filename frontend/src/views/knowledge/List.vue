<template>
  <div class="knowledge-list-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">知识资源</h1>
      <el-button type="primary" @click="$router.push('/knowledge/create')">
        <el-icon><Plus /></el-icon>
        上传知识
      </el-button>
    </div>

    <!-- 搜索和筛选区 -->
    <el-card class="filter-card">
      <div class="search-row">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索知识标题、内容、标签..."
          prefix-icon="Search"
          size="large"
          clearable
          class="search-input"
          @keyup.enter="handleSearch"
        />
        <el-button type="primary" size="large" @click="handleSearch">
          搜索
        </el-button>
        <el-button size="large" @click="toggleAdvanced">
          高级筛选
          <el-icon :class="{ 'rotate-180': showAdvanced }"><ArrowDown /></el-icon>
        </el-button>
      </div>

      <!-- 高级筛选 -->
      <el-collapse-transition>
        <div v-show="showAdvanced" class="advanced-filter">
          <el-row :gutter="16">
            <el-col :span="6">
              <el-select v-model="filters.type" placeholder="知识类型" clearable style="width: 100%">
                <el-option
                  v-for="item in knowledgeTypes"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-col>
            <el-col :span="6">
              <el-select v-model="filters.domain" placeholder="所属领域" clearable style="width: 100%">
                <el-option
                  v-for="item in domains"
                  :key="item.value"
                  :label="item.label"
                  :value="item.value"
                />
              </el-select>
            </el-col>
            <el-col :span="6">
              <el-select v-model="filters.sort" placeholder="排序方式" style="width: 100%">
                <el-option label="最新发布" value="createTime" />
                <el-option label="最多浏览" value="viewCount" />
                <el-option label="最多点赞" value="likeCount" />
                <el-option label="最多收藏" value="collectCount" />
              </el-select>
            </el-col>
            <el-col :span="6">
              <el-date-picker
                v-model="filters.dateRange"
                type="daterange"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                style="width: 100%"
              />
            </el-col>
          </el-row>
        </div>
      </el-collapse-transition>
    </el-card>

    <!-- 搜索历史和热门标签 -->
    <div class="quick-access">
      <div class="search-history" v-if="appStore.searchHistory.length">
        <span class="label">搜索历史：</span>
        <el-tag
          v-for="keyword in appStore.searchHistory.slice(0, 5)"
          :key="keyword"
          size="small"
          @click="quickSearch(keyword)"
        >
          {{ keyword }}
        </el-tag>
        <el-link type="info" @click="appStore.clearSearchHistory">清空</el-link>
      </div>
      <div class="hot-tags">
        <span class="label">热门标签：</span>
        <el-tag
          v-for="tag in hotTags"
          :key="tag"
          size="small"
          type="info"
          @click="quickSearch(tag)"
        >
          {{ tag }}
        </el-tag>
      </div>
    </div>

    <!-- 知识列表 -->
    <div class="knowledge-grid" v-loading="loading">
      <div
        class="knowledge-card"
        v-for="item in knowledgeList"
        :key="item.id"
        @click="$router.push(`/knowledge/detail/${item.id}`)"
      >
        <div class="card-header">
          <div class="header-left">
            <el-tag size="small" :type="getTypeTagType(item.type)">
              {{ item.type }}
            </el-tag>
            <span class="domain">{{ item.domain }}</span>
          </div>
          <span class="publish-time">{{ formatTime(item.createTime) }}</span>
        </div>
        <h3 class="card-title">{{ item.title }}</h3>
        <p class="card-summary">{{ item.summary }}</p>
        <div class="card-tags" v-if="item.tags?.length">
          <el-tag
            v-for="tag in item.tags.slice(0, 3)"
            :key="tag"
            size="small"
            type="info"
          >
            {{ tag }}
          </el-tag>
        </div>
        <div class="card-footer">
          <div class="author">
            <el-avatar :size="24" :src="item.authorAvatar">
              {{ item.authorName?.charAt(0) }}
            </el-avatar>
            <span>{{ item.authorName }}</span>
          </div>
          <div class="stats">
            <span><el-icon><View /></el-icon> {{ item.viewCount }}</span>
            <span><el-icon><Star /></el-icon> {{ item.likeCount }}</span>
            <span><el-icon><Collection /></el-icon> {{ item.collectCount }}</span>
          </div>
        </div>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty v-if="!loading && !knowledgeList.length" description="暂无知识资源" />

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next, jumper"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useUserStore } from '@/stores/user'
import { getKnowledgeList, getHotTags } from '@/api/knowledge'
import dayjs from 'dayjs'
import { Plus, ArrowDown, View, Star, Collection } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const userStore = useUserStore()

const loading = ref(false)
const showAdvanced = ref(false)
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = 12
const total = ref(0)

// 筛选条件
const filters = reactive({
  type: '',
  domain: '',
  sort: 'createTime',
  dateRange: null
})

// 知识类型选项
const knowledgeTypes = [
  { label: '技术文档', value: '技术文档' },
  { label: '设计方案', value: '设计方案' },
  { label: '故障解决方案', value: '故障解决方案' },
  { label: '仿真数据', value: '仿真数据' },
  { label: '测试报告', value: '测试报告' },
  { label: '其他', value: '其他' }
]

// 领域选项
const domains = [
  { label: '底盘工程', value: '底盘工程' },
  { label: '电子电控', value: '电子电控' },
  { label: '软件算法', value: '软件算法' },
  { label: '动力系统', value: '动力系统' },
  { label: '整车设计', value: '整车设计' },
  { label: '生产工艺', value: '生产工艺' },
  { label: '质量管控', value: '质量管控' },
  { label: '其他', value: '其他' }
]

// 热门标签
const hotTags = ref([])

// 知识列表
const knowledgeList = ref([])

// 切换高级筛选
function toggleAdvanced() {
  showAdvanced.value = !showAdvanced.value
}

// 搜索
function handleSearch() {
  if (searchKeyword.value.trim()) {
    appStore.addSearchHistory(searchKeyword.value)
  }
  currentPage.value = 1
  fetchKnowledgeList()
}

// 快速搜索
function quickSearch(keyword) {
  searchKeyword.value = keyword
  handleSearch()
}

// 获取知识列表
async function fetchKnowledgeList() {
  loading.value = true
  try {
    const params = {
      keyword: searchKeyword.value,
      type: filters.type,
      domain: filters.domain,
      sort: filters.sort,
      page: currentPage.value,
      size: pageSize
    }
    if (route.query.author === 'me' && userStore.userInfo?.id) {
      params.authorId = userStore.userInfo.id
    }
    const res = await getKnowledgeList(params)
    if (res?.data) {
      knowledgeList.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取知识列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 分页变化
function handlePageChange(page) {
  currentPage.value = page
  fetchKnowledgeList()
}

// 获取类型标签样式
function getTypeTagType(type) {
  const typeMap = {
    '技术文档': 'primary',
    '设计方案': 'success',
    '故障解决方案': 'warning',
    '仿真数据': 'info',
    '测试报告': 'danger',
    '其他': ''
  }
  return typeMap[type] || ''
}

// 格式化时间
function formatTime(time) {
  return dayjs(time).format('YYYY-MM-DD')
}

// 监听路由参数
watch(() => route.query, (query) => {
  if (query.keyword !== undefined) {
    searchKeyword.value = query.keyword
  }
  if (query.author === 'me') {
    // 可以在这里做一些特定的UI重置或标题修改
  }
  currentPage.value = 1
  fetchKnowledgeList()
}, { immediate: true })

// 初始化
onMounted(async () => {
  // 加载热门标签
  try {
    const res = await getHotTags({ limit: 10 })
    if (res?.data) {
      hotTags.value = res.data
    }
  } catch (e) {
    console.error('获取热门标签失败:', e)
  }
})
</script>

<style scoped>
.knowledge-list-page {
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
}

/* 筛选卡片 */
.filter-card {
  margin-bottom: 24px;
}

.search-row {
  display: flex;
  gap: 12px;
}

.search-input {
  flex: 1;
}

.rotate-180 {
  transform: rotate(180deg);
  transition: transform 0.3s;
}

.advanced-filter {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--border-color);
}

/* 快速访问 */
.quick-access {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 24px;
}

.search-history,
.hot-tags {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.label {
  color: var(--text-secondary);
  font-size: 14px;
}

.search-history :deep(.el-tag),
.hot-tags :deep(.el-tag) {
  cursor: pointer;
  transition: all var(--transition-fast);
}

.search-history :deep(.el-tag):hover,
.hot-tags :deep(.el-tag):hover {
  background: var(--primary-bg);
  border-color: var(--primary-color);
  color: var(--primary-color);
}

/* 知识网格 */
.knowledge-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  min-height: 400px;
}

.knowledge-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 20px;
  cursor: pointer;
  transition: all var(--transition-normal);
  position: relative;
  display: flex;
  flex-direction: column;
}

.knowledge-card:hover {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-glow);
  transform: translateY(-4px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}


.domain {
  font-size: 12px;
  color: var(--text-tertiary);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

.publish-time {
  font-size: 12px;
  color: var(--text-tertiary);
}

.card-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-summary {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 12px;
  flex: 1;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-tags {
  display: flex;
  gap: 6px;
  margin-bottom: 16px;
  flex-wrap: wrap;
}

.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 12px;
  border-top: 1px solid var(--border-color);
}

.author {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-secondary);
}

.stats {
  display: flex;
  gap: 12px;
  font-size: 12px;
  color: var(--text-tertiary);
}

.stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}



/* 分页 */
.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

/* 响应式 */
@media (max-width: 1200px) {
  .knowledge-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .knowledge-grid {
    grid-template-columns: 1fr;
  }
  
  .search-row {
    flex-direction: column;
  }
}
</style>
