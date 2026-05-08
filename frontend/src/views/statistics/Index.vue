<template>
  <div class="statistics-page">
    <div class="page-header">
      <h1 class="page-title">数据统计</h1>
      <el-date-picker
        v-model="dateRange"
        type="daterange"
        start-placeholder="开始日期"
        end-placeholder="结束日期"
        @change="handleDateChange"
      />
    </div>

    <!-- 统计概览 -->
    <div class="stats-overview">
      <div class="stat-card" v-for="stat in overviewStats" :key="stat.title">
        <div class="stat-icon" :style="{ background: stat.gradient }">
          <el-icon :size="24"><component :is="stat.icon" /></el-icon>
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ stat.value }}</span>
          <span class="stat-title">{{ stat.title }}</span>
        </div>
        <div class="stat-trend" :class="stat.trend > 0 ? 'up' : 'down'">
          <span>{{ stat.trend > 0 ? '+' : '' }}{{ stat.trend }}%</span>
          <span class="period">较上周</span>
        </div>
      </div>
    </div>

    <!-- 图表区域 -->
    <el-row :gutter="20" class="chart-row">
      <el-col :span="16">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>知识增长趋势</span>
              <el-radio-group v-model="trendType" size="small">
                <el-radio-button label="day">按日</el-radio-button>
                <el-radio-button label="week">按周</el-radio-button>
                <el-radio-button label="month">按月</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart-container large">
            <v-chart :option="trendChartOption" autoresize />
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="chart-card">
          <template #header>知识领域分布</template>
          <div class="chart-container large">
            <v-chart :option="domainChartOption" autoresize />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" class="chart-row">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>知识类型分布</template>
          <div class="chart-container">
            <v-chart :option="typeChartOption" autoresize />
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>用户活跃度趋势</template>
          <div class="chart-container">
            <v-chart :option="activeUserChartOption" autoresize />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 排行榜区域 -->
    <el-row :gutter="20">
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>🔥 热门知识 Top10</span>
            </div>
          </template>
          <div class="ranking-list">
            <div class="ranking-item" v-for="(item, index) in hotKnowledge" :key="item.id">
              <span class="rank" :class="getRankClass(index)">{{ index + 1 }}</span>
              <span class="title">{{ item.title }}</span>
              <span class="count">{{ item.viewCount }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>🏆 贡献排行 Top10</span>
            </div>
          </template>
          <div class="ranking-list">
            <div class="ranking-item" v-for="(user, index) in contributionRanking" :key="user.id">
              <span class="rank" :class="getRankClass(index)">{{ index + 1 }}</span>
              <el-avatar :size="28" :src="user.avatar">{{ user.username?.charAt(0) }}</el-avatar>
              <span class="name">{{ user.realName || user.username }}</span>
              <span class="count">{{ user.score }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card>
          <template #header>
            <div class="card-header">
              <span>🏢 部门贡献 Top10</span>
            </div>
          </template>
          <div class="ranking-list">
            <div class="ranking-item" v-for="(dept, index) in departmentRanking" :key="dept.name">
              <span class="rank" :class="getRankClass(index)">{{ index + 1 }}</span>
              <span class="name">{{ dept.name }}</span>
              <span class="count">{{ dept.count }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, PieChart, BarChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import { Document, User, Folder, TrendCharts, View, ChatDotRound } from '@element-plus/icons-vue'
import {
  getDashboardData,
  getKnowledgeTrend,
  getDomainDistribution,
  getTypeDistribution,
  getContributionRanking,
  getHotKnowledge,
  getDepartmentRanking,
  getActiveUserStats
} from '@/api/statistics'

use([CanvasRenderer, LineChart, PieChart, BarChart, GridComponent, TooltipComponent, LegendComponent])

const dateRange = ref(null)
const trendType = ref('day')

// 概览统计
const overviewStats = ref([
  { title: '知识总量', value: '-', trend: 0, icon: 'Document', gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
  { title: '本周新增', value: '-', trend: 0, icon: 'TrendCharts', gradient: 'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)' },
  { title: '活跃用户', value: '-', trend: 0, icon: 'User', gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
  { title: '搜索次数', value: '-', trend: 0, icon: 'View', gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' },
  { title: '讨论数量', value: '-', trend: 0, icon: 'ChatDotRound', gradient: 'linear-gradient(135deg, #fa8231 0%, #ffcc00 100%)' },
  { title: '项目数量', value: '-', trend: 0, icon: 'Folder', gradient: 'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)' }
])

// 趋势图表数据
const trendData = ref([])
const trendChartOption = computed(() => ({
  tooltip: { trigger: 'axis', backgroundColor: 'rgba(22, 27, 34, 0.95)', borderColor: '#30363d', textStyle: { color: '#e6edf3' } },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'category', data: trendData.value.map(i => i.date), axisLine: { lineStyle: { color: '#30363d' } }, axisLabel: { color: '#8b949e' } },
  yAxis: { type: 'value', axisLine: { show: false }, splitLine: { lineStyle: { color: '#21262d' } }, axisLabel: { color: '#8b949e' } },
  series: [
    { name: '新增知识', type: 'line', smooth: true, data: trendData.value.map(i => i.count), areaStyle: { color: { type: 'linear', x: 0, y: 0, x2: 0, y2: 1, colorStops: [{ offset: 0, color: 'rgba(24, 144, 255, 0.4)' }, { offset: 1, color: 'rgba(24, 144, 255, 0.05)' }] } }, lineStyle: { color: '#1890ff', width: 3 }, itemStyle: { color: '#1890ff' } }
  ]
}))

// 领域分布
const domainDistData = ref([])
const domainColors = ['#667eea', '#38ef7d', '#f5576c', '#4facfe', '#fa8231', '#a855f7', '#ec4899', '#14b8a6']
const domainChartOption = computed(() => ({
  tooltip: { trigger: 'item' },
  series: [{ type: 'pie', radius: ['40%', '70%'], center: ['50%', '50%'], data: domainDistData.value.map((item, i) => ({
    value: item.count || item.value,
    name: item.name || item.domain,
    itemStyle: { color: domainColors[i % domainColors.length] }
  })), label: { color: '#8b949e' }, itemStyle: { borderRadius: 8, borderColor: '#161b22', borderWidth: 2 } }]
}))

// 类型分布
const typeDistData = ref([])
const typeChartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'value', axisLine: { show: false }, splitLine: { lineStyle: { color: '#21262d' } }, axisLabel: { color: '#8b949e' } },
  yAxis: { type: 'category', data: typeDistData.value.map(i => i.name || i.type), axisLine: { lineStyle: { color: '#30363d' } }, axisLabel: { color: '#8b949e' } },
  series: [{ type: 'bar', data: typeDistData.value.map(i => i.count || i.value), itemStyle: { color: { type: 'linear', x: 0, y: 0, x2: 1, y2: 0, colorStops: [{ offset: 0, color: '#667eea' }, { offset: 1, color: '#764ba2' }] }, borderRadius: [0, 4, 4, 0] }, barWidth: 20 }]
}))

// 用户活跃度
const activeUserData = ref([])
const activeUserChartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: { type: 'category', data: activeUserData.value.map(i => i.date), axisLine: { lineStyle: { color: '#30363d' } }, axisLabel: { color: '#8b949e' } },
  yAxis: { type: 'value', axisLine: { show: false }, splitLine: { lineStyle: { color: '#21262d' } }, axisLabel: { color: '#8b949e' } },
  series: [{ type: 'bar', data: activeUserData.value.map(i => i.count), itemStyle: { color: '#1890ff', borderRadius: [4, 4, 0, 0] }, barWidth: 30 }]
}))

// 排行榜数据
const hotKnowledge = ref([])
const contributionRanking = ref([])
const departmentRanking = ref([])

// 加载数据
onMounted(async () => {
  const [dashRes, trendRes, domainRes, hotRes, contribRes, deptRes, typeRes, activeRes] = await Promise.allSettled([
    getDashboardData(),
    getKnowledgeTrend({ type: trendType.value }),
    getDomainDistribution(),
    getHotKnowledge({ limit: 10 }),
    getContributionRanking({ limit: 10 }),
    getDepartmentRanking({ limit: 10 }),
    getTypeDistribution(),
    getActiveUserStats({ days: 7 })
  ])

  if (dashRes.status === 'fulfilled' && dashRes.value?.data) {
    const d = dashRes.value.data
    overviewStats.value = [
      { title: '知识总量', value: d.knowledgeCount?.toLocaleString() || '0', trend: d.knowledgeTrend || 0, icon: 'Document', gradient: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)' },
      { title: '本周新增', value: d.weeklyKnowledgeCount?.toLocaleString() || '0', trend: d.weeklyKnowledgeTrend || 0, icon: 'TrendCharts', gradient: 'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)' },
      { title: '活跃用户', value: d.userCount?.toLocaleString() || '0', trend: d.userTrend || 0, icon: 'User', gradient: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)' },
      { title: '搜索次数', value: d.searchCount?.toLocaleString() || '0', trend: d.searchTrend || 0, icon: 'View', gradient: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)' },
      { title: '讨论数量', value: d.discussionCount?.toLocaleString() || '0', trend: d.discussionTrend || 0, icon: 'ChatDotRound', gradient: 'linear-gradient(135deg, #fa8231 0%, #ffcc00 100%)' },
      { title: '项目数量', value: d.projectCount?.toLocaleString() || '0', trend: d.projectTrend || 0, icon: 'Folder', gradient: 'linear-gradient(135deg, #a18cd1 0%, #fbc2eb 100%)' }
    ]
  }

  if (trendRes.status === 'fulfilled' && trendRes.value?.data) {
    trendData.value = trendRes.value.data
  }

  if (domainRes.status === 'fulfilled' && domainRes.value?.data) {
    domainDistData.value = domainRes.value.data
  }

  if (hotRes.status === 'fulfilled' && hotRes.value?.data) {
    hotKnowledge.value = hotRes.value.data
  }

  if (contribRes.status === 'fulfilled' && contribRes.value?.data) {
    contributionRanking.value = contribRes.value.data
  }

  if (deptRes.status === 'fulfilled' && deptRes.value?.data) {
    departmentRanking.value = deptRes.value.data
  }

  if (typeRes.status === 'fulfilled' && typeRes.value?.data) {
    typeDistData.value = typeRes.value.data
  }

  if (activeRes.status === 'fulfilled' && activeRes.value?.data) {
    activeUserData.value = activeRes.value.data
  }
})

function handleDateChange() {
  // TODO: 根据日期范围重新加载数据
}

function getRankClass(index) {
  if (index === 0) return 'gold'
  if (index === 1) return 'silver'
  if (index === 2) return 'bronze'
  return ''
}
</script>

<style scoped>
.statistics-page {
  max-width: 1600px;
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

/* 概览统计 */
.stats-overview {
  display: grid;
  grid-template-columns: repeat(6, 1fr);
  gap: 16px;
  margin-bottom: 24px;
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  transition: all var(--transition-normal);
}

.stat-card:hover {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-glow);
}

.stat-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.stat-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.stat-value {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
}

.stat-title {
  font-size: 13px;
  color: var(--text-secondary);
}

.stat-trend {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  font-size: 14px;
  font-weight: 500;
}

.stat-trend.up { color: var(--success-color); }
.stat-trend.down { color: var(--danger-color); }

.period {
  font-size: 12px;
  color: var(--text-tertiary);
  font-weight: 400;
}

/* 图表 */
.chart-row {
  margin-bottom: 20px;
}

.chart-card {
  height: 420px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart-container {
  height: 280px;
}

.chart-container.large {
  height: 320px;
}

.chart-container > * {
  width: 100%;
  height: 100%;
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
  padding: 8px 12px;
  background: var(--bg-tertiary);
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
}

.ranking-item:hover {
  background: var(--bg-hover);
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
  background: var(--bg-secondary);
  color: var(--text-secondary);
  flex-shrink: 0;
}

.rank.gold {
  background: linear-gradient(135deg, #ffd700 0%, #ffb700 100%);
  color: #1a1a1a;
}

.rank.silver {
  background: linear-gradient(135deg, #c0c0c0 0%, #a0a0a0 100%);
  color: #1a1a1a;
}

.rank.bronze {
  background: linear-gradient(135deg, #cd7f32 0%, #b87333 100%);
  color: white;
}

.ranking-item .title,
.ranking-item .name {
  flex: 1;
  font-size: 14px;
  color: var(--text-primary);
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.ranking-item .count {
  font-size: 14px;
  font-weight: 600;
  color: var(--primary-color);
}

/* 响应式 */
@media (max-width: 1400px) {
  .stats-overview {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
