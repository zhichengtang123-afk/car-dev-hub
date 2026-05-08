<template>
  <div class="project-list-page">
    <div class="page-header">
      <h1 class="page-title">项目管理</h1>
      <el-button type="primary" @click="showCreateDialog">
        <el-icon><Plus /></el-icon>
        新建项目
      </el-button>
    </div>

    <!-- 筛选区 -->
    <el-card class="filter-card">
      <el-row :gutter="16">
        <el-col :span="8">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索项目名称..."
            prefix-icon="Search"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-col>
        <el-col :span="6">
          <el-select v-model="filterStatus" placeholder="项目状态" clearable style="width: 100%">
            <el-option label="规划中" :value="0" />
            <el-option label="进行中" :value="1" />
            <el-option label="已完成" :value="2" />
            <el-option label="已归档" :value="3" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 项目列表 -->
    <div class="project-grid" v-loading="loading">
      <div
        class="project-card"
        v-for="project in projectList"
        :key="project.id"
        @click="$router.push(`/project/detail/${project.id}`)"
      >
        <div class="card-header">
          <div class="project-icon" :style="{ background: getProjectColor(project.id) }">
            <el-icon :size="24"><Folder /></el-icon>
          </div>
          <el-tag :type="getStatusType(project.status)" size="small">
            {{ getStatusText(project.status) }}
          </el-tag>
        </div>
        
        <h3 class="project-name">{{ project.name }}</h3>
        <p class="project-desc">{{ project.description || '暂无描述' }}</p>
        
        <div class="project-info">
          <div class="info-item">
            <el-icon><User /></el-icon>
            <span>负责人：{{ project.leader?.realName || project.leader?.username }}</span>
          </div>
          <div class="info-item">
            <el-icon><Calendar /></el-icon>
            <span>{{ formatDate(project.startDate) }} ~ {{ formatDate(project.endDate) }}</span>
          </div>
        </div>

        <div class="project-stats">
          <div class="stat">
            <span class="value">{{ project.knowledgeCount || 0 }}</span>
            <span class="label">知识</span>
          </div>
          <div class="stat">
            <span class="value">{{ project.memberCount || 0 }}</span>
            <span class="label">成员</span>
          </div>
        </div>

        <div class="project-members" v-if="project.members?.length">
          <el-avatar
            v-for="(member, index) in project.members.slice(0, 5)"
            :key="member.id"
            :size="28"
            :src="member.avatar"
            :style="{ marginLeft: index > 0 ? '-8px' : '0' }"
          >
            {{ member.username?.charAt(0) }}
          </el-avatar>
          <span v-if="project.members.length > 5" class="more-members">
            +{{ project.members.length - 5 }}
          </span>
        </div>
      </div>
    </div>

    <el-empty v-if="!loading && !projectList.length" description="暂无项目" />

    <!-- 分页 -->
    <div class="pagination-wrapper" v-if="total > pageSize">
      <el-pagination
        v-model:current-page="currentPage"
        :page-size="pageSize"
        :total="total"
        layout="prev, pager, next"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 新建项目对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      title="新建项目"
      width="600px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="projectForm"
        :rules="formRules"
        label-width="100px"
      >
        <el-form-item label="项目名称" prop="name">
          <el-input v-model="projectForm.name" placeholder="请输入项目名称" />
        </el-form-item>
        <el-form-item label="项目描述" prop="description">
          <el-input
            v-model="projectForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入项目描述"
          />
        </el-form-item>
        <el-form-item label="负责人" prop="leaderId">
          <el-select v-model="projectForm.leaderId" placeholder="选择负责人" style="width: 100%">
            <el-option
              v-for="user in userList"
              :key="user.id"
              :label="user.realName || user.username"
              :value="user.id"
            />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="开始日期" prop="startDate">
              <el-date-picker
                v-model="projectForm.startDate"
                type="date"
                placeholder="选择开始日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期" prop="endDate">
              <el-date-picker
                v-model="projectForm.endDate"
                type="date"
                placeholder="选择结束日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="项目状态" prop="status">
          <el-radio-group v-model="projectForm.status">
            <el-radio :value="0">规划中</el-radio>
            <el-radio :value="1">进行中</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleCreateProject" :loading="submitting">
          创建
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getProjectList, createProject } from '@/api/project'
import { getUserList } from '@/api/auth'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { Plus, Folder, User, Calendar } from '@element-plus/icons-vue'

const loading = ref(false)
const searchKeyword = ref('')
const filterStatus = ref(null)
const currentPage = ref(1)
const pageSize = 12
const total = ref(0)
const projectList = ref([])

// 新建项目
const createDialogVisible = ref(false)
const formRef = ref()
const submitting = ref(false)

const projectForm = reactive({
  name: '',
  description: '',
  leaderId: null,
  startDate: null,
  endDate: null,
  status: 0
})

const formRules = {
  name: [
    { required: true, message: '请输入项目名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  leaderId: [
    { required: true, message: '请选择负责人', trigger: 'change' }
  ]
}

// 用户列表（用于选择负责人）
const userList = ref([])

// 加载数据
onMounted(async () => {
  fetchProjects()
  // 加载用户列表
  try {
    const res = await getUserList({ page: 1, size: 100 })
    if (res?.data) {
      userList.value = res.data.list || res.data || []
    }
  } catch (e) {
    console.error('获取用户列表失败:', e)
  }
})

// 搜索
function handleSearch() {
  currentPage.value = 1
  fetchProjects()
}

// 获取项目列表
async function fetchProjects() {
  loading.value = true
  try {
    const res = await getProjectList({
      keyword: searchKeyword.value,
      status: filterStatus.value,
      page: currentPage.value,
      size: pageSize
    })
    if (res?.data) {
      projectList.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取项目列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 分页
function handlePageChange(page) {
  currentPage.value = page
  fetchProjects()
}

// 显示新建对话框
function showCreateDialog() {
  createDialogVisible.value = true
}

// 创建项目
async function handleCreateProject() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    await createProject(projectForm)
    ElMessage.success('项目创建成功')
    createDialogVisible.value = false
    fetchProjects()
  } catch (error) {
    ElMessage.error('创建失败')
  } finally {
    submitting.value = false
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
    'linear-gradient(135deg, #11998e 0%, #38ef7d 100%)',
    'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    'linear-gradient(135deg, #fa8231 0%, #ffcc00 100%)'
  ]
  return colors[id % colors.length]
}

function formatDate(date) {
  if (!date) return '未设置'
  return dayjs(date).format('YYYY-MM-DD')
}
</script>

<style scoped>
.project-list-page {
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

.filter-card {
  margin-bottom: 24px;
}

/* 项目网格 */
.project-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
  min-height: 300px;
}

.project-card {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 24px;
  cursor: pointer;
  transition: all var(--transition-normal);
}

.project-card:hover {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-glow);
  transform: translateY(-4px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.project-icon {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.project-name {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 8px;
  line-height: 1.4;
}

.project-desc {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 16px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.project-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: var(--text-tertiary);
}

.project-stats {
  display: flex;
  gap: 24px;
  padding: 12px 0;
  border-top: 1px solid var(--border-color);
  border-bottom: 1px solid var(--border-color);
  margin-bottom: 16px;
}

.stat {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.stat .value {
  font-size: 20px;
  font-weight: 700;
  color: var(--primary-color);
}

.stat .label {
  font-size: 12px;
  color: var(--text-tertiary);
}

.project-members {
  display: flex;
  align-items: center;
}

.project-members :deep(.el-avatar) {
  border: 2px solid var(--bg-card);
}

.more-members {
  margin-left: 8px;
  font-size: 12px;
  color: var(--text-tertiary);
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}

/* 响应式 */
@media (max-width: 1200px) {
  .project-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .project-grid {
    grid-template-columns: 1fr;
  }
}
</style>
