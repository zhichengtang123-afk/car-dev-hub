<template>
  <div class="knowledge-detail-page" v-loading="loading">
    <!-- 返回按钮 -->
    <div class="back-bar">
      <el-button text @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon>
        返回列表
      </el-button>
    </div>

    <div class="detail-container" v-if="knowledge">
      <!-- 左侧主要内容 -->
      <div class="main-content">
        <!-- 知识头部 -->
        <div class="knowledge-header">
          <div class="header-tags">
            <el-tag :type="getTypeTagType(knowledge.type)">{{ knowledge.type }}</el-tag>
            <el-tag type="info">{{ knowledge.domain }}</el-tag>
          </div>
          <h1 class="knowledge-title">{{ knowledge.title }}</h1>
          <div class="knowledge-meta">
            <div class="author-info">
              <el-avatar :size="40" :src="knowledge.authorAvatar">
                {{ knowledge.authorName?.charAt(0) }}
              </el-avatar>
              <div class="author-detail">
                <span class="author-name">{{ knowledge.authorName }}</span>
                <span class="author-dept">{{ knowledge.authorDepartment || '未设置部门' }}</span>
              </div>
            </div>
            <div class="meta-stats">
              <span class="stat-item">
                <el-icon><Clock /></el-icon>
                {{ formatTime(knowledge.createTime) }}
              </span>
              <span class="stat-item">
                <el-icon><View /></el-icon>
                {{ knowledge.viewCount }} 阅读
              </span>
              <span class="stat-item">
                <el-icon><Star /></el-icon>
                {{ knowledge.likeCount }} 点赞
              </span>
              <span class="stat-item">
                <el-icon><Collection /></el-icon>
                {{ knowledge.collectCount }} 收藏
              </span>
            </div>
          </div>
        </div>

        <!-- 知识摘要 -->
        <div class="knowledge-summary">
          <div class="summary-label">摘要</div>
          <p>{{ knowledge.summary }}</p>
        </div>

        <!-- 知识正文 -->
        <el-card class="knowledge-content">
          <MdPreview
            :modelValue="knowledge.content"
            :theme="'dark'"
            :previewTheme="'github'"
            :codeTheme="'atom'"
          />
        </el-card>

        <!-- 附件列表 -->
        <el-card class="attachments-card" v-if="knowledge.attachments?.length">
          <template #header>
            <span>📎 附件下载</span>
          </template>
          <div class="attachment-list">
            <div
              class="attachment-item"
              v-for="file in knowledge.attachments"
              :key="file.id"
            >
              <div class="file-icon">
                <el-icon :size="24"><Document /></el-icon>
              </div>
              <div class="file-info">
                <span class="file-name">{{ file.fileName }}</span>
                <span class="file-size">{{ formatFileSize(file.fileSize) }}</span>
              </div>
              <el-button type="primary" size="small" @click="handleDownload(file)">
                <el-icon><Download /></el-icon>
                下载
              </el-button>
            </div>
          </div>
        </el-card>

        <!-- 标签 -->
        <div class="knowledge-tags" v-if="knowledge.tags?.length">
          <span class="tags-label">标签：</span>
          <el-tag
            v-for="tag in knowledge.tags"
            :key="tag"
            size="small"
            type="info"
          >
            {{ tag }}
          </el-tag>
        </div>

        <!-- 操作栏 -->
        <div class="action-bar">
          <el-button
            :type="isLiked ? 'primary' : 'default'"
            @click="handleLike"
          >
            <el-icon><Star /></el-icon>
            {{ isLiked ? '已点赞' : '点赞' }}
          </el-button>
          <el-button
            :type="isCollected ? 'warning' : 'default'"
            @click="handleCollect"
          >
            <el-icon><Collection /></el-icon>
            {{ isCollected ? '已收藏' : '收藏' }}
          </el-button>
          <el-button @click="handleShare">
            <el-icon><Share /></el-icon>
            分享
          </el-button>
          <el-button
            v-if="isAuthor"
            @click="$router.push(`/knowledge/edit/${knowledge.id}`)"
          >
            <el-icon><Edit /></el-icon>
            编辑
          </el-button>
        </div>

        <!-- 评论区 -->
        <el-card class="comments-card">
          <template #header>
            <div class="comments-header">
              <span>💬 评论 ({{ comments.length }})</span>
            </div>
          </template>

          <!-- 发表评论 -->
          <div class="comment-form" v-if="userStore.isLoggedIn">
            <el-avatar :size="40" :src="userStore.userInfo?.avatar">
              {{ userStore.userInfo?.username?.charAt(0) }}
            </el-avatar>
            <div class="form-content">
              <el-input
                v-model="commentContent"
                type="textarea"
                :rows="3"
                placeholder="发表你的评论..."
              />
              <el-button type="primary" size="small" @click="handleSubmitComment">
                发表评论
              </el-button>
            </div>
          </div>
          <div class="login-prompt" v-else>
            <router-link to="/auth/login">登录</router-link> 后发表评论
          </div>

          <!-- 评论列表 -->
          <div class="comment-list">
            <div
              class="comment-item"
              v-for="comment in comments"
              :key="comment.id"
            >
              <el-avatar :size="40" :src="comment.userAvatar">
                {{ comment.userName?.charAt(0) }}
              </el-avatar>
              <div class="comment-content">
                <div class="comment-header">
                  <span class="username">{{ comment.userName }}</span>
                  <span class="time">{{ formatTime(comment.createTime) }}</span>
                </div>
                <p class="comment-text">{{ comment.content }}</p>
                <div class="comment-actions">
                  <el-button text size="small" @click="handleLikeComment(comment)">
                    <el-icon><Star /></el-icon>
                    {{ comment.likeCount }}
                  </el-button>
                  <el-button text size="small" @click="handleReply(comment)">
                    <el-icon><ChatLineSquare /></el-icon>
                    回复
                  </el-button>
                  <el-button
                    v-if="comment.userId === userStore.userInfo?.id"
                    text
                    size="small"
                    @click="handleDeleteComment(comment)"
                  >
                    删除
                  </el-button>
                </div>
              </div>
            </div>
          </div>

          <el-empty v-if="!comments.length" description="暂无评论，快来发表第一条评论吧" />
        </el-card>
      </div>

      <!-- 右侧边栏 -->
      <div class="sidebar">
        <!-- 作者信息 -->
        <el-card class="author-card">
          <div class="author-profile">
            <el-avatar :size="64" :src="knowledge.authorAvatar">
              {{ knowledge.authorName?.charAt(0) }}
            </el-avatar>
            <div class="profile-info">
              <h4>{{ knowledge.authorName }}</h4>
              <p>{{ knowledge.authorDepartment || '未设置部门' }}</p>
            </div>
          </div>
          <div class="author-stats">
            <div class="stat">
              <span class="value">128</span>
              <span class="label">知识</span>
            </div>
            <div class="stat">
              <span class="value">2.5k</span>
              <span class="label">点赞</span>
            </div>
            <div class="stat">
              <span class="value">1.2k</span>
              <span class="label">粉丝</span>
            </div>
          </div>
        </el-card>

        <!-- 相关知识 -->
        <el-card class="related-card">
          <template #header>
            <span>📖 相关知识</span>
          </template>
          <div class="related-list">
            <div
              class="related-item"
              v-for="item in relatedKnowledge"
              :key="item.id"
              @click="$router.push(`/knowledge/detail/${item.id}`)"
            >
              <h5>{{ item.title }}</h5>
              <div class="related-meta">
                <span><el-icon><View /></el-icon> {{ item.viewCount }}</span>
                <span><el-icon><Star /></el-icon> {{ item.likeCount }}</span>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 版本历史 -->
        <el-card class="version-card">
          <template #header>
            <span>📝 版本历史</span>
          </template>
          <el-timeline>
            <el-timeline-item
              v-for="version in versions"
              :key="version.id"
              :timestamp="formatTime(version.createTime)"
              placement="top"
            >
              <span class="version-label">V{{ version.version }}</span>
              <p class="version-desc">{{ version.description }}</p>
            </el-timeline-item>
          </el-timeline>
        </el-card>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  getKnowledgeDetail,
  likeKnowledge,
  unlikeKnowledge,
  collectKnowledge,
  uncollectKnowledge,
  getKnowledgeComments,
  createComment,
  deleteComment,
  downloadAttachment,
  getKnowledgeVersions,
  getRecommendKnowledge
} from '@/api/knowledge'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MdPreview } from 'md-editor-v3'
import 'md-editor-v3/lib/preview.css'
import dayjs from 'dayjs'
import {
  ArrowLeft, Clock, View, Star, Collection, Share, Edit, Download,
  Document, ChatLineSquare
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const knowledge = ref(null)
const comments = ref([])
const commentContent = ref('')
const isLiked = ref(false)
const isCollected = ref(false)

// 是否为作者
const isAuthor = computed(() => {
  return knowledge.value?.authorId === userStore.userInfo?.id
})

// 相关知识
const relatedKnowledge = ref([])

// 版本历史
const versions = ref([])

// 加载知识详情
async function loadKnowledge() {
  loading.value = true
  try {
    const res = await getKnowledgeDetail(route.params.id)
    if (res?.data) {
      knowledge.value = res.data
      isLiked.value = !!res.data.isLiked
      isCollected.value = !!res.data.isCollected
    }

    // 并发加载评论、版本历史、相关知识
    const [commentsRes, versionsRes, relatedRes] = await Promise.allSettled([
      getKnowledgeComments(route.params.id, { page: 1, size: 20 }),
      getKnowledgeVersions(route.params.id),
      getRecommendKnowledge({ limit: 3 })
    ])

    if (commentsRes.status === 'fulfilled' && commentsRes.value?.data) {
      const items = commentsRes.value.data.list || commentsRes.value.data
      comments.value = Array.isArray(items) ? items : []
    }

    if (versionsRes.status === 'fulfilled' && versionsRes.value?.data) {
      versions.value = versionsRes.value.data
    }

    if (relatedRes.status === 'fulfilled' && relatedRes.value?.data) {
      relatedKnowledge.value = relatedRes.value.data
    }
  } catch (error) {
    ElMessage.error('加载知识详情失败')
    router.back()
  } finally {
    loading.value = false
  }
}

// 加载评论
async function loadComments() {
  try {
    const res = await getKnowledgeComments(route.params.id, { page: 1, size: 20 })
    if (res?.data) {
      const items = res.data.list || res.data
      comments.value = Array.isArray(items) ? items : []
    }
  } catch (e) {
    console.error('加载评论失败:', e)
  }
}

// 点赞
async function handleLike() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    if (isLiked.value) {
      await unlikeKnowledge(knowledge.value.id)
      knowledge.value.likeCount--
    } else {
      await likeKnowledge(knowledge.value.id)
      knowledge.value.likeCount++
    }
    isLiked.value = !isLiked.value
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 收藏
async function handleCollect() {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  try {
    if (isCollected.value) {
      await uncollectKnowledge(knowledge.value.id)
      knowledge.value.collectCount--
    } else {
      await collectKnowledge(knowledge.value.id)
      knowledge.value.collectCount++
    }
    isCollected.value = !isCollected.value
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

// 分享
function handleShare() {
  const url = window.location.href
  navigator.clipboard.writeText(url)
  ElMessage.success('链接已复制到剪贴板')
}

// 下载附件
async function handleDownload(file) {
  try {
    const res = await downloadAttachment(file.id)
    const blob = new Blob([res.data])
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = file.fileName
    link.click()
    window.URL.revokeObjectURL(url)
  } catch (error) {
    ElMessage.error('下载失败')
  }
}

// 发表评论
async function handleSubmitComment() {
  if (!commentContent.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  try {
    await createComment(knowledge.value.id, { content: commentContent.value })
    commentContent.value = ''
    loadComments()
    ElMessage.success('评论成功')
  } catch (error) {
    ElMessage.error('评论失败')
  }
}

// 回复评论
function handleReply(comment) {
  commentContent.value = `@${comment.userName} `
}

// 点赞评论
function handleLikeComment(comment) {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  comment.likeCount++
}

// 删除评论
async function handleDeleteComment(comment) {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '提示')
    await deleteComment(knowledge.value.id, comment.id)
    loadComments()
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败')
    }
  }
}

// 工具函数
function getTypeTagType(type) {
  const typeMap = {
    '技术文档': 'primary',
    '设计方案': 'success',
    '故障解决方案': 'warning',
    '仿真数据': 'info',
    '测试报告': 'danger'
  }
  return typeMap[type] || ''
}

function formatTime(time) {
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

function formatFileSize(size) {
  if (size < 1024) return size + ' B'
  if (size < 1024 * 1024) return (size / 1024).toFixed(1) + ' KB'
  return (size / 1024 / 1024).toFixed(1) + ' MB'
}

onMounted(() => {
  loadKnowledge()
})
</script>

<style scoped>
.knowledge-detail-page {
  max-width: 1400px;
  margin: 0 auto;
}

.back-bar {
  margin-bottom: 16px;
}

.detail-container {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 24px;
}

/* 主内容区 */
.main-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

/* 知识头部 */
.knowledge-header {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 24px;
}

.header-tags {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}

.knowledge-title {
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 20px;
  line-height: 1.4;
}

.knowledge-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.author-info {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-detail {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.author-name {
  font-weight: 500;
  color: var(--text-primary);
}

.author-dept {
  font-size: 12px;
  color: var(--text-tertiary);
}

.meta-stats {
  display: flex;
  gap: 20px;
}

.stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
  color: var(--text-secondary);
  font-size: 14px;
}

/* 摘要 */
.knowledge-summary {
  background: var(--bg-tertiary);
  border-left: 4px solid var(--primary-color);
  padding: 16px 20px;
  border-radius: 0 var(--radius-md) var(--radius-md) 0;
}

.summary-label {
  font-weight: 600;
  color: var(--primary-color);
  margin-bottom: 8px;
}

.knowledge-summary p {
  color: var(--text-secondary);
  line-height: 1.8;
}

/* 正文内容 */
.knowledge-content {
  padding: 24px;
}

.knowledge-content :deep(.md-preview-wrapper) {
  background: transparent;
  color: var(--text-primary);
}

/* 附件 */
.attachment-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.attachment-item {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px;
  background: var(--bg-tertiary);
  border-radius: var(--radius-md);
}

.file-icon {
  width: 48px;
  height: 48px;
  background: var(--primary-bg);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--primary-color);
}

.file-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.file-name {
  font-weight: 500;
  color: var(--text-primary);
}

.file-size {
  font-size: 12px;
  color: var(--text-tertiary);
}

/* 标签 */
.knowledge-tags {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.tags-label {
  color: var(--text-secondary);
  font-size: 14px;
}

/* 操作栏 */
.action-bar {
  display: flex;
  gap: 12px;
  padding: 16px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
}

/* 评论区 */
.comment-form {
  display: flex;
  gap: 12px;
  padding-bottom: 24px;
  border-bottom: 1px solid var(--border-color);
  margin-bottom: 24px;
}

.form-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 12px;
  align-items: flex-end;
}

.form-content .el-input {
  width: 100%;
}

.login-prompt {
  text-align: center;
  padding: 24px;
  color: var(--text-secondary);
  border-bottom: 1px solid var(--border-color);
  margin-bottom: 24px;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.comment-item {
  display: flex;
  gap: 12px;
}

.comment-content {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.username {
  font-weight: 500;
  color: var(--text-primary);
}

.time {
  font-size: 12px;
  color: var(--text-tertiary);
}

.comment-text {
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 8px;
}

.comment-actions {
  display: flex;
  gap: 8px;
}

/* 侧边栏 */
.sidebar {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 作者卡片 */
.author-profile {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 12px;
  padding-bottom: 16px;
  border-bottom: 1px solid var(--border-color);
}

.profile-info h4 {
  color: var(--text-primary);
  margin-bottom: 4px;
}

.profile-info p {
  font-size: 12px;
  color: var(--text-tertiary);
}

.author-stats {
  display: flex;
  justify-content: space-around;
  padding-top: 16px;
}

.stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.stat .value {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.stat .label {
  font-size: 12px;
  color: var(--text-tertiary);
}

/* 相关知识 */
.related-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.related-item {
  padding: 12px;
  background: var(--bg-tertiary);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all var(--transition-fast);
}

.related-item:hover {
  background: var(--bg-hover);
}

.related-item h5 {
  color: var(--text-primary);
  font-size: 14px;
  margin-bottom: 8px;
  line-height: 1.4;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.related-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: var(--text-tertiary);
}

.related-meta span {
  display: flex;
  align-items: center;
  gap: 4px;
}

/* 版本历史 */
.version-label {
  display: inline-block;
  padding: 2px 8px;
  background: var(--primary-bg);
  color: var(--primary-color);
  border-radius: var(--radius-sm);
  font-size: 12px;
  font-weight: 500;
}

.version-desc {
  font-size: 13px;
  color: var(--text-secondary);
  margin-top: 4px;
}

/* 响应式 */
@media (max-width: 1024px) {
  .detail-container {
    grid-template-columns: 1fr;
  }
  
  .sidebar {
    order: -1;
    display: grid;
    grid-template-columns: repeat(3, 1fr);
    gap: 16px;
  }
}
</style>
