<template>
  <div class="project-detail-page" v-loading="loading">
    <div class="back-bar">
      <el-button text @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon>
        返回列表
      </el-button>
    </div>

    <div class="detail-container" v-if="project">
      <!-- 项目头部 -->
      <div class="project-header">
        <div class="header-left">
          <div class="project-icon" :style="{ background: getProjectColor(project.id) }">
            <el-icon :size="32"><Folder /></el-icon>
          </div>
          <div class="header-info">
            <h1 class="project-name">{{ project.name }}</h1>
            <div class="project-meta">
              <el-tag :type="getStatusType(project.status)">
                {{ getStatusText(project.status) }}
              </el-tag>
              <span class="meta-item">
                <el-icon><Calendar /></el-icon>
                {{ formatDate(project.startDate) }} ~ {{ formatDate(project.endDate) }}
              </span>
              <span class="meta-item">
                <el-icon><User /></el-icon>
                负责人：{{ project.leaderName }}
              </span>
            </div>
          </div>
        </div>
        <div class="header-actions" v-if="isLeader || userStore.isAdmin">
          <el-button @click="showEditDialog">编辑</el-button>
          <el-button type="danger" @click="handleDelete">删除</el-button>
        </div>
      </div>

      <!-- 项目描述 -->
      <el-card class="desc-card">
        <template #header>
          <span>项目描述</span>
        </template>
        <p>{{ project.description || '暂无描述' }}</p>
      </el-card>

      <!-- 标签页 -->
      <el-tabs v-model="activeTab">
        <!-- 关联知识 -->
        <el-tab-pane label="关联知识" name="knowledge">
          <el-card shadow="never">
            <template #header>
              <div class="card-header" style="display: flex; justify-content: space-between; align-items: center;">
                <span style="font-weight: 500;">共 {{ knowledgeList.length }} 条关联知识</span>
                <el-button type="primary" size="small" @click="showLinkDialog">
                  <el-icon><Plus /></el-icon>
                  添加关联
                </el-button>
              </div>
            </template>
            
            <el-table :data="knowledgeList" style="width: 100%">
              <el-table-column prop="title" label="标题" min-width="200">
                <template #default="{ row }">
                  <el-link type="primary" @click="$router.push(`/knowledge/detail/${row.id}`)">
                    {{ row.title }}
                  </el-link>
                </template>
              </el-table-column>
              <el-table-column prop="type" label="类型" width="120">
                <template #default="{ row }">
                  <el-tag size="small">{{ row.type }}</el-tag>
                </template>
              </el-table-column>
              <el-table-column prop="domain" label="领域" width="120" />
              <el-table-column prop="author" label="作者" width="100">
                <template #default="{ row }">
                  {{ row.authorName }}
                </template>
              </el-table-column>
              <el-table-column prop="createTime" label="创建时间" width="120">
                <template #default="{ row }">
                  {{ formatDate(row.createTime) }}
                </template>
              </el-table-column>
              <el-table-column label="操作" width="80" fixed="right">
                <template #default="{ row }">
                  <el-button type="danger" link size="small" @click="handleUnlink(row)">
                    取消关联
                  </el-button>
                </template>
              </el-table-column>
            </el-table>
          </el-card>
        </el-tab-pane>

        <!-- 项目成员 -->
        <el-tab-pane label="项目成员" name="members">
          <div class="tab-header">
            <span>共 {{ members.length }} 名成员</span>
            <el-button type="primary" size="small" @click="showAddMemberDialog" v-if="isLeader">
              <el-icon><Plus /></el-icon>
              添加成员
            </el-button>
          </div>
          
          <div class="member-grid">
            <div class="member-card" v-for="member in members" :key="member.id">
              <el-avatar :size="56" :src="member.avatar">
                {{ member.username?.charAt(0) }}
              </el-avatar>
              <div class="member-info">
                <h4>{{ member.realName || member.username }}</h4>
                <p>{{ member.department || '未设置部门' }}</p>
                <el-tag v-if="member.id === project.leaderId" type="warning" size="small">
                  负责人
                </el-tag>
              </div>
              <el-button
                v-if="isLeader && member.id !== project.leaderId"
                type="danger"
                text
                size="small"
                @click="handleRemoveMember(member)"
              >
                移除
              </el-button>
            </div>
          </div>
        </el-tab-pane>

        <!-- 知识类型分布 -->
        <el-tab-pane label="数据统计" name="stats">
          <div class="stats-content">
            <el-row :gutter="20">
              <el-col :span="12">
                <el-card>
                  <template #header>知识类型分布</template>
                  <div class="chart-container">
                    <v-chart :option="typeChartOption" autoresize />
                  </div>
                </el-card>
              </el-col>
              <el-col :span="12">
                <el-card>
                  <template #header>知识增长趋势</template>
                  <div class="chart-container">
                    <v-chart :option="trendChartOption" autoresize />
                  </div>
                </el-card>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <!-- 编辑项目对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑项目" width="600px" destroy-on-close>
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="项目名称">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="项目描述">
          <el-input v-model="editForm.description" type="textarea" :rows="4" />
        </el-form-item>
        <el-form-item label="开始日期">
          <el-date-picker v-model="editForm.startDate" type="date" style="width: 100%" />
        </el-form-item>
        <el-form-item label="结束日期">
          <el-date-picker v-model="editForm.endDate" type="date" style="width: 100%" />
        </el-form-item>
        <el-form-item label="项目状态">
          <el-select v-model="editForm.status" style="width: 100%">
            <el-option :value="0" label="规划中" />
            <el-option :value="1" label="进行中" />
            <el-option :value="2" label="已完成" />
            <el-option :value="3" label="已归档" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSaveEdit" :loading="editLoading">保存</el-button>
      </template>
    </el-dialog>

    <!-- 关联知识对话框 -->
    <el-dialog v-model="linkDialogVisible" title="关联知识" width="700px" destroy-on-close>
      <el-input
        v-model="linkSearchKeyword"
        placeholder="搜索知识标题..."
        prefix-icon="Search"
        clearable
        @keyup.enter="searchLinkKnowledge"
        style="margin-bottom: 16px"
      />
      <el-table :data="linkKnowledgeResults" v-loading="linkSearching" max-height="400">
        <el-table-column prop="title" label="标题" min-width="200" />
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag size="small">{{ row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="authorName" label="作者" width="100" />
        <el-table-column label="操作" width="80">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              size="small"
              :disabled="knowledgeList.some(k => k.id === row.id)"
              @click="handleLinkKnowledge(row)"
            >
              {{ knowledgeList.some(k => k.id === row.id) ? '已关联' : '关联' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>

    <!-- 添加成员对话框 -->
    <el-dialog v-model="addMemberDialogVisible" title="添加成员" width="500px" destroy-on-close>
      <el-select
        v-model="selectedUserId"
        filterable
        remote
        :remote-method="searchUsers"
        placeholder="搜索用户名或姓名..."
        :loading="userSearching"
        style="width: 100%"
      >
        <el-option
          v-for="u in userSearchResults"
          :key="u.id"
          :label="(u.realName || u.username) + (u.department ? ` (${u.department})` : '')"
          :value="u.id"
          :disabled="members.some(m => m.id === u.id)"
        />
      </el-select>
      <template #footer>
        <el-button @click="addMemberDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddMember" :disabled="!selectedUserId">添加</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  getProjectDetail,
  updateProject,
  deleteProject,
  getProjectKnowledge,
  getProjectMembers,
  addProjectMember,
  removeProjectMember,
  unlinkKnowledgeFromProject,
  linkKnowledgeToProject
} from '@/api/project'
import { getKnowledgeList } from '@/api/knowledge'
import { ElMessage, ElMessageBox } from 'element-plus'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { PieChart, LineChart } from 'echarts/charts'
import { GridComponent, TooltipComponent, LegendComponent } from 'echarts/components'
import VChart from 'vue-echarts'
import dayjs from 'dayjs'
import { ArrowLeft, Folder, Calendar, User, Plus } from '@element-plus/icons-vue'

use([CanvasRenderer, PieChart, LineChart, GridComponent, TooltipComponent, LegendComponent])

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const activeTab = ref('knowledge')
const project = ref(null)
const knowledgeList = ref([])
const members = ref([])

// 是否为项目负责人
const isLeader = computed(() => {
  return project.value?.leaderId === userStore.userInfo?.id
})

// 加载项目详情
async function loadProject() {
  loading.value = true
  try {
    const res = await getProjectDetail(route.params.id)
    if (res?.data) {
      project.value = res.data
    }

    // 并发加载关联知识和成员
    const [knowledgeRes, membersRes] = await Promise.allSettled([
      getProjectKnowledge(route.params.id, { page: 1, size: 20 }),
      getProjectMembers(route.params.id)
    ])

    if (knowledgeRes.status === 'fulfilled' && knowledgeRes.value?.data) {
      const items = knowledgeRes.value.data.list || knowledgeRes.value.data
      knowledgeList.value = Array.isArray(items) ? items : []
    }

    if (membersRes.status === 'fulfilled' && membersRes.value?.data) {
      members.value = membersRes.value.data
    }
  } catch (error) {
    ElMessage.error('加载项目详情失败')
    router.back()
  } finally {
    loading.value = false
  }
}

// 图表配置
const typeChartOption = computed(() => ({
  tooltip: { trigger: 'item' },
  series: [{
    type: 'pie',
    radius: ['40%', '70%'],
    data: [
      { value: 12, name: '设计方案', itemStyle: { color: '#667eea' } },
      { value: 8, name: '技术文档', itemStyle: { color: '#38ef7d' } },
      { value: 6, name: '测试报告', itemStyle: { color: '#f5576c' } },
      { value: 4, name: '故障解决方案', itemStyle: { color: '#4facfe' } }
    ]
  }]
}))

const trendChartOption = computed(() => ({
  tooltip: { trigger: 'axis' },
  grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
  xAxis: {
    type: 'category',
    data: ['10月', '11月', '12月', '1月'],
    axisLine: { lineStyle: { color: '#30363d' } },
    axisLabel: { color: '#8b949e' }
  },
  yAxis: {
    type: 'value',
    axisLine: { show: false },
    splitLine: { lineStyle: { color: '#21262d' } },
    axisLabel: { color: '#8b949e' }
  },
  series: [{
    data: [5, 12, 8, 5],
    type: 'line',
    smooth: true,
    areaStyle: {
      color: {
        type: 'linear',
        x: 0, y: 0, x2: 0, y2: 1,
        colorStops: [
          { offset: 0, color: 'rgba(24, 144, 255, 0.4)' },
          { offset: 1, color: 'rgba(24, 144, 255, 0.05)' }
        ]
      }
    },
    lineStyle: { color: '#1890ff' }
  }]
}))

// ---- 编辑项目 ----
const editDialogVisible = ref(false)
const editLoading = ref(false)
const editForm = ref({ name: '', description: '', startDate: null, endDate: null, status: 0 })

function showEditDialog() {
  editForm.value = {
    name: project.value.name,
    description: project.value.description,
    startDate: project.value.startDate,
    endDate: project.value.endDate,
    status: project.value.status
  }
  editDialogVisible.value = true
}

async function handleSaveEdit() {
  editLoading.value = true
  try {
    await updateProject(project.value.id, editForm.value)
    ElMessage.success('保存成功')
    editDialogVisible.value = false
    loadProject()
  } catch (error) {
    ElMessage.error('保存失败')
  } finally {
    editLoading.value = false
  }
}

async function handleDelete() {
  try {
    await ElMessageBox.confirm('确定要删除该项目吗？删除后无法恢复', '警告', { type: 'warning' })
    await deleteProject(project.value.id)
    ElMessage.success('删除成功')
    router.push('/project/list')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// ---- 关联知识 ----
const linkDialogVisible = ref(false)
const linkSearchKeyword = ref('')
const linkKnowledgeResults = ref([])
const linkSearching = ref(false)

function showLinkDialog() {
  linkSearchKeyword.value = ''
  linkKnowledgeResults.value = []
  linkDialogVisible.value = true
  searchLinkKnowledge()
}

async function searchLinkKnowledge() {
  linkSearching.value = true
  try {
    const res = await getKnowledgeList({ keyword: linkSearchKeyword.value, page: 1, size: 20 })
    if (res?.data) {
      linkKnowledgeResults.value = res.data.list || []
    }
  } catch (e) {
    ElMessage.error('搜索失败')
  } finally {
    linkSearching.value = false
  }
}

async function handleLinkKnowledge(row) {
  try {
    await linkKnowledgeToProject(project.value.id, row.id)
    knowledgeList.value.push(row)
    ElMessage.success('关联成功')
  } catch (e) {
    ElMessage.error('关联失败')
  }
}

async function handleUnlink(row) {
  try {
    await ElMessageBox.confirm('确定取消关联该知识？', '提示')
    await unlinkKnowledgeFromProject(project.value.id, row.id)
    knowledgeList.value = knowledgeList.value.filter(k => k.id !== row.id)
    ElMessage.success('取消关联成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// ---- 添加成员 ----
const addMemberDialogVisible = ref(false)
const selectedUserId = ref(null)
const userSearchResults = ref([])
const userSearching = ref(false)

function showAddMemberDialog() {
  selectedUserId.value = null
  userSearchResults.value = []
  addMemberDialogVisible.value = true
}

async function searchUsers(query) {
  if (!query) { userSearchResults.value = []; return }
  userSearching.value = true
  try {
    const { default: request } = await import('@/api/request')
    const userRes = await request({ url: '/admin/users', method: 'get', params: { keyword: query, page: 1, size: 20 } })
    if (userRes?.data) {
      userSearchResults.value = userRes.data.list || userRes.data || []
    }
  } catch (e) {
    userSearchResults.value = []
  } finally {
    userSearching.value = false
  }
}

async function handleAddMember() {
  if (!selectedUserId.value) return
  try {
    await addProjectMember(project.value.id, selectedUserId.value)
    ElMessage.success('添加成功')
    addMemberDialogVisible.value = false
    loadProject()
  } catch (e) {
    ElMessage.error('添加失败')
  }
}

async function handleRemoveMember(member) {
  try {
    await ElMessageBox.confirm(`确定移除成员 ${member.realName || member.username}？`, '提示')
    await removeProjectMember(project.value.id, member.id)
    members.value = members.value.filter(m => m.id !== member.id)
    ElMessage.success('移除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 工具函数
function getStatusType(status) {
  const types = ['info', 'primary', 'success', 'warning']
  return types[status] || 'info'
}

function getStatusText(status) {
  const texts = ['规划中', '进行中', '已完成', '已归档']
  return texts[status] || '未知'
}

function getProjectColor(id) {
  const colors = [
    'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)'
  ]
  return colors[id % colors.length]
}

function formatDate(date) {
  if (!date) return '未设置'
  return dayjs(date).format('YYYY-MM-DD')
}

onMounted(() => {
  loadProject()
})
</script>

<style scoped>
.project-detail-page {
  max-width: 1200px;
  margin: 0 auto;
}

.back-bar {
  margin-bottom: 16px;
}

/* 项目头部 */
.project-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 24px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  margin-bottom: 24px;
}

.header-left {
  display: flex;
  gap: 20px;
}

.project-icon {
  width: 64px;
  height: 64px;
  border-radius: var(--radius-lg);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
}

.project-name {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 12px;
}

.project-meta {
  display: flex;
  align-items: center;
  gap: 16px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
  color: var(--text-secondary);
  font-size: 14px;
}

.header-actions {
  display: flex;
  gap: 12px;
}

.desc-card {
  margin-bottom: 24px;
}

.desc-card p {
  color: var(--text-secondary);
  line-height: 1.8;
}

/* 标签页 */
.tab-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  color: var(--text-secondary);
}

/* 成员网格 */
.member-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.member-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 20px;
  background: var(--bg-tertiary);
  border-radius: var(--radius-md);
  text-align: center;
}

.member-info h4 {
  color: var(--text-primary);
  margin-bottom: 4px;
}

.member-info p {
  font-size: 12px;
  color: var(--text-tertiary);
  margin-bottom: 8px;
}

/* 图表 */
.chart-container {
  height: 300px;
}

.chart-container > * {
  width: 100%;
  height: 100%;
}

/* 响应式 */
@media (max-width: 1024px) {
  .member-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 768px) {
  .project-header {
    flex-direction: column;
    gap: 16px;
  }
  
  .member-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
