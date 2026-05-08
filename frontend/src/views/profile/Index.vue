<template>
  <div class="profile-page">
    <div class="page-header">
      <h1 class="page-title">个人中心</h1>
    </div>

    <el-row :gutter="24">
      <!-- 左侧个人信息 -->
      <el-col :span="8">
        <el-card class="profile-card">
          <div class="avatar-section">
            <el-upload
              class="avatar-uploader"
              action="/api/user/avatar"
              :headers="{ Authorization: userStore.token ? `Bearer ${userStore.token}` : '' }"
              :show-file-list="false"
              :before-upload="beforeAvatarUpload"
              :on-success="handleAvatarSuccess"
              :on-error="handleAvatarError"
            >
              <el-avatar :size="100" :src="userStore.userInfo?.avatar">
                {{ userStore.userInfo?.username?.charAt(0) }}
              </el-avatar>
              <div class="avatar-overlay">
                <el-icon><Camera /></el-icon>
              </div>
            </el-upload>
            <h2 class="username">{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</h2>
            <el-tag :type="getRoleType(userStore.userInfo?.role)">
              {{ getRoleLabel(userStore.userInfo?.role) }}
            </el-tag>
          </div>

          <el-divider />

          <div class="info-list">
            <div class="info-item">
              <el-icon><User /></el-icon>
              <span class="label">用户名</span>
              <span class="value">{{ userStore.userInfo?.username }}</span>
            </div>
            <div class="info-item">
              <el-icon><Phone /></el-icon>
              <span class="label">手机号</span>
              <span class="value">{{ userStore.userInfo?.phone }}</span>
            </div>
            <div class="info-item">
              <el-icon><Message /></el-icon>
              <span class="label">邮箱</span>
              <span class="value">{{ userStore.userInfo?.email }}</span>
            </div>
            <div class="info-item">
              <el-icon><OfficeBuilding /></el-icon>
              <span class="label">部门</span>
              <span class="value">{{ userStore.userInfo?.department || '未设置' }}</span>
            </div>
            <div class="info-item">
              <el-icon><Postcard /></el-icon>
              <span class="label">职位</span>
              <span class="value">{{ userStore.userInfo?.position || '未设置' }}</span>
            </div>
          </div>
        </el-card>

        <!-- 贡献统计 -->
        <el-card class="stats-card">
          <template #header>我的贡献</template>
          <div class="stats-grid">
            <div class="stat-item">
              <span class="value">{{ myStats.knowledgeCount }}</span>
              <span class="label">知识</span>
            </div>
            <div class="stat-item">
              <span class="value">{{ myStats.likeCount }}</span>
              <span class="label">获赞</span>
            </div>
            <div class="stat-item">
              <span class="value">{{ myStats.collectCount }}</span>
              <span class="label">被收藏</span>
            </div>
            <div class="stat-item">
              <span class="value">{{ myStats.replyCount }}</span>
              <span class="label">回答</span>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧表单 -->
      <el-col :span="16">
        <el-tabs v-model="activeTab">
          <!-- 基本信息 -->
          <el-tab-pane label="基本信息" name="info">
            <el-card>
              <el-form :model="profileForm" :rules="profileRules" ref="profileFormRef" label-width="100px">
                <el-form-item label="真实姓名" prop="realName">
                  <el-input v-model="profileForm.realName" placeholder="请输入真实姓名" />
                </el-form-item>
                <el-form-item label="手机号" prop="phone">
                  <el-input v-model="profileForm.phone" placeholder="请输入手机号" />
                </el-form-item>
                <el-form-item label="邮箱" prop="email">
                  <el-input v-model="profileForm.email" placeholder="请输入邮箱" />
                </el-form-item>
                <el-form-item label="部门" prop="department">
                  <el-input v-model="profileForm.department" placeholder="请输入部门" />
                </el-form-item>
                <el-form-item label="职位" prop="position">
                  <el-input v-model="profileForm.position" placeholder="请输入职位" />
                </el-form-item>
                <el-form-item label="个人简介">
                  <el-input v-model="profileForm.bio" type="textarea" :rows="4" placeholder="介绍一下自己" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleUpdateProfile" :loading="saving">保存修改</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-tab-pane>

          <!-- 修改密码 -->
          <el-tab-pane label="修改密码" name="password">
            <el-card>
              <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px">
                <el-form-item label="原密码" prop="oldPassword">
                  <el-input v-model="passwordForm.oldPassword" type="password" show-password placeholder="请输入原密码" />
                </el-form-item>
                <el-form-item label="新密码" prop="newPassword">
                  <el-input v-model="passwordForm.newPassword" type="password" show-password placeholder="请输入新密码" />
                </el-form-item>
                <el-form-item label="确认密码" prop="confirmPassword">
                  <el-input v-model="passwordForm.confirmPassword" type="password" show-password placeholder="请确认新密码" />
                </el-form-item>
                <el-form-item>
                  <el-button type="primary" @click="handleChangePassword" :loading="changing">修改密码</el-button>
                </el-form-item>
              </el-form>
            </el-card>
          </el-tab-pane>

          <!-- 我的知识 -->
          <el-tab-pane label="我的知识" name="knowledge">
            <el-card>
              <div class="my-knowledge-list">
                <div class="knowledge-item" v-for="item in myKnowledge" :key="item.id" @click="$router.push(`/knowledge/detail/${item.id}`)">
                  <div class="item-info">
                    <h4>{{ item.title }}</h4>
                    <div class="item-meta">
                      <el-tag size="small">{{ item.type }}</el-tag>
                      <span>{{ formatDate(item.createTime) }}</span>
                    </div>
                  </div>
                  <div class="item-stats">
                    <span><el-icon><View /></el-icon> {{ item.viewCount }}</span>
                    <span><el-icon><Star /></el-icon> {{ item.likeCount }}</span>
                  </div>
                </div>
              </div>
              <el-empty v-if="!myKnowledge.length" description="暂无知识" />
            </el-card>
          </el-tab-pane>
        </el-tabs>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { changePassword } from '@/api/auth'
import { getUserContributionStats } from '@/api/statistics'
import { getKnowledgeList } from '@/api/knowledge'
import { ElMessage } from 'element-plus'
import dayjs from 'dayjs'
import { Camera, User, Phone, Message, OfficeBuilding, Postcard, View, Star } from '@element-plus/icons-vue'

const userStore = useUserStore()
const activeTab = ref('info')
const profileFormRef = ref()
const passwordFormRef = ref()
const saving = ref(false)
const changing = ref(false)

// 统计数据
const myStats = ref({ knowledgeCount: 0, likeCount: 0, collectCount: 0, replyCount: 0 })

// 我的知识
const myKnowledge = ref([])

// 加载个人数据
onMounted(async () => {
  const [statsRes, knowledgeRes] = await Promise.allSettled([
    getUserContributionStats(),
    getKnowledgeList({ authorId: userStore.userInfo?.id, page: 1, size: 10 })
  ])

  if (statsRes.status === 'fulfilled' && statsRes.value?.data) {
    myStats.value = statsRes.value.data
  }

  if (knowledgeRes.status === 'fulfilled' && knowledgeRes.value?.data) {
    const items = knowledgeRes.value.data.list || knowledgeRes.value.data
    myKnowledge.value = Array.isArray(items) ? items : []
  }
})

// 个人信息表单
const profileForm = reactive({
  realName: userStore.userInfo?.realName || '',
  phone: userStore.userInfo?.phone || '',
  email: userStore.userInfo?.email || '',
  department: userStore.userInfo?.department || '',
  position: userStore.userInfo?.position || '',
  avatar: userStore.userInfo?.avatar || '',
  bio: ''
})

const profileRules = {
  phone: [{ pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }],
  email: [{ type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }]
}

// 密码表单
const passwordForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })

const validateConfirmPassword = (rule, value, callback) => {
  if (value !== passwordForm.newPassword) {
    callback(new Error('两次输入的密码不一致'))
  } else {
    callback()
  }
}

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 8, message: '密码至少8位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    { validator: validateConfirmPassword, trigger: 'blur' }
  ]
}

async function handleUpdateProfile() {
  const valid = await profileFormRef.value.validate().catch(() => false)
  if (!valid) return

  saving.value = true
  const success = await userStore.updateProfile(profileForm)
  saving.value = false
}

async function handleChangePassword() {
  const valid = await passwordFormRef.value.validate().catch(() => false)
  if (!valid) return

  changing.value = true
  try {
    await changePassword(passwordForm)
    ElMessage.success('密码修改成功，请重新登录')
    userStore.logout()
  } catch {
    ElMessage.error('修改失败，请检查原密码是否正确')
  } finally {
    changing.value = false
  }
}

function beforeAvatarUpload(file) {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2
  if (!isImage) ElMessage.error('只能上传图片文件')
  if (!isLt2M) ElMessage.error('图片大小不能超过 2MB')
  return isImage && isLt2M
}

function handleAvatarSuccess(res, file) {
  if (res.code === 200) {
    const avatarUrl = res.data.url
    profileForm.avatar = avatarUrl
    // Update local userStore and submit automatically if needed
    userStore.userInfo.avatar = avatarUrl
    profileFormRef.value.validate().then(() => {
      userStore.updateProfile(profileForm).then(() => {
        ElMessage.success('头像更新成功')
      })
    })
  } else {
    ElMessage.error(res.message || '上传失败')
  }
}

function handleAvatarError(error) {
  ElMessage.error('图片上传失败: ' + error.message)
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
.profile-page {
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 24px;
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
}

/* 个人信息卡片 */
.profile-card {
  margin-bottom: 20px;
}

.avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
}

.avatar-uploader {
  position: relative;
  cursor: pointer;
}

.avatar-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity var(--transition-fast);
  color: white;
  font-size: 24px;
}

.avatar-uploader:hover .avatar-overlay {
  opacity: 1;
}

.username {
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.info-item .el-icon {
  color: var(--text-tertiary);
}

.info-item .label {
  width: 60px;
  color: var(--text-secondary);
  font-size: 14px;
}

.info-item .value {
  flex: 1;
  color: var(--text-primary);
  font-size: 14px;
}

/* 统计卡片 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.stat-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 16px;
  background: var(--bg-tertiary);
  border-radius: var(--radius-md);
}

.stat-item .value {
  font-size: 24px;
  font-weight: 700;
  color: var(--primary-color);
}

.stat-item .label {
  font-size: 12px;
  color: var(--text-tertiary);
}

/* 我的知识 */
.my-knowledge-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.knowledge-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: var(--bg-tertiary);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.knowledge-item:hover {
  background: var(--bg-hover);
}

.item-info h4 {
  color: var(--text-primary);
  margin-bottom: 8px;
}

.item-meta {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 12px;
  color: var(--text-tertiary);
}

.item-stats {
  display: flex;
  gap: 16px;
  font-size: 14px;
  color: var(--text-secondary);
}

.item-stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}
</style>
