<template>
  <div class="user-list-page">
    <div class="page-header">
      <h1 class="page-title">用户管理</h1>
    </div>

    <!-- 筛选区 -->
    <el-card class="filter-card">
      <el-row :gutter="16">
        <el-col :span="6">
          <el-input v-model="searchKeyword" placeholder="搜索用户名/姓名/手机号" prefix-icon="Search" clearable />
        </el-col>
        <el-col :span="4">
          <el-select v-model="filterRole" placeholder="角色" clearable style="width: 100%">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="技术专家" value="EXPERT" />
            <el-option label="研发人员" value="DEVELOPER" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 100%">
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-col>
        <el-col :span="3">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 用户列表 -->
    <el-card>
      <el-table :data="userList" v-loading="loading" style="width: 100%">
        <el-table-column label="用户" min-width="200">
          <template #default="{ row }">
            <div class="user-cell">
              <el-avatar :size="40" :src="row.avatar">
                {{ row.username?.charAt(0) }}
              </el-avatar>
              <div class="user-info">
                <span class="username">{{ row.realName || row.username }}</span>
                <span class="email">{{ row.email }}</span>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="phone" label="手机号" width="140" />
        <el-table-column prop="department" label="部门" width="120" />
        <el-table-column prop="role" label="角色" width="120">
          <template #default="{ row }">
            <el-tag :type="getRoleType(row.role)">{{ getRoleLabel(row.role) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-switch v-model="row.status" :active-value="1" :inactive-value="0" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="注册时间" width="120">
          <template #default="{ row }">
            {{ formatDate(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="showRoleDialog(row)">
              修改角色
            </el-button>
            <el-button type="warning" link size="small" @click="handleResetPassword(row)">
              重置密码
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          :page-size="pageSize"
          :total="total"
          layout="total, prev, pager, next"
          @current-change="handlePageChange"
        />
      </div>
    </el-card>

    <!-- 修改角色对话框 -->
    <el-dialog v-model="roleDialogVisible" title="修改用户角色" width="400px" destroy-on-close>
      <el-form label-width="80px">
        <el-form-item label="用户">
          <span>{{ currentUser?.realName || currentUser?.username }}</span>
        </el-form-item>
        <el-form-item label="角色">
          <el-radio-group v-model="newRole">
            <el-radio value="ADMIN">管理员</el-radio>
            <el-radio value="EXPERT">技术专家</el-radio>
            <el-radio value="DEVELOPER">研发人员</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="roleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdateRole">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getUserList, updateUserStatus, updateUserRole, resetUserPassword } from '@/api/auth'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'

defineOptions({
  name: 'UserList'
})

const loading = ref(false)
const searchKeyword = ref('')
const filterRole = ref('')
const filterStatus = ref(null)
const currentPage = ref(1)
const pageSize = 10
const total = ref(0)
const userList = ref([])

const roleDialogVisible = ref(false)
const currentUser = ref(null)
const newRole = ref('')

// 加载数据
onMounted(() => {
  fetchUsers()
})

// 获取用户列表
async function fetchUsers() {
  loading.value = true
  try {
    const res = await getUserList({
      keyword: searchKeyword.value,
      role: filterRole.value,
      status: filterStatus.value,
      page: currentPage.value,
      size: pageSize
    })
    if (res?.data) {
      userList.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  fetchUsers()
}

function handlePageChange(page) {
  currentPage.value = page
  fetchUsers()
}

async function handleStatusChange(row) {
  try {
    await updateUserStatus(row.id, row.status)
    ElMessage.success(row.status ? '已启用' : '已禁用')
  } catch {
    row.status = row.status ? 0 : 1
    ElMessage.error('操作失败')
  }
}

function showRoleDialog(user) {
  currentUser.value = user
  newRole.value = user.role
  roleDialogVisible.value = true
}

async function handleUpdateRole() {
  try {
    await updateUserRole(currentUser.value.id, newRole.value)
    currentUser.value.role = newRole.value
    roleDialogVisible.value = false
    ElMessage.success('角色更新成功')
  } catch {
    ElMessage.error('更新失败')
  }
}

async function handleResetPassword(user) {
  try {
    await ElMessageBox.confirm(`确定要重置用户 ${user.realName || user.username} 的密码吗？`, '提示')
    await resetUserPassword(user.id)
    ElMessage.success('密码已重置为 123456')
  } catch (error) {
    if (error !== 'cancel') ElMessage.error('重置失败')
  }
}

function getRoleType(role) {
  const types = { ADMIN: 'danger', EXPERT: 'warning', DEVELOPER: '' }
  return types[role] || ''
}

function getRoleLabel(role) {
  const labels = { ADMIN: '管理员', EXPERT: '技术专家', DEVELOPER: '研发人员' }
  return labels[role] || role
}

function formatDate(date) {
  return dayjs(date).format('YYYY-MM-DD')
}
</script>

<style scoped>
.user-list-page {
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

.user-cell {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.username {
  font-weight: 500;
  color: var(--text-primary);
}

.email {
  font-size: 12px;
  color: var(--text-tertiary);
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
