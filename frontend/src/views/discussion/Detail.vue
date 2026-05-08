<template>
  <div class="discussion-detail-page" v-loading="loading">
    <div class="back-bar">
      <el-button text @click="$router.back()">
        <el-icon><ArrowLeft /></el-icon>
        返回列表
      </el-button>
    </div>

    <div class="detail-container" v-if="discussion">
      <!-- 问题主体 -->
      <div class="question-section">
        <div class="question-header">
          <el-tag :type="discussion.isResolved ? 'success' : 'warning'" size="large">
            {{ discussion.isResolved ? '已解决' : '待解决' }}
          </el-tag>
          <h1 class="question-title">{{ discussion.title }}</h1>
        </div>

        <div class="question-meta">
          <el-avatar :size="40" :src="discussion.authorAvatar">
            {{ discussion.authorName?.charAt(0) }}
          </el-avatar>
          <div class="meta-info">
            <span class="author">{{ discussion.authorName }}</span>
            <span class="time">发布于 {{ formatTime(discussion.createTime) }}</span>
          </div>
          <div class="meta-stats">
            <span><el-icon><View /></el-icon> {{ discussion.viewCount }} 浏览</span>
            <span><el-icon><ChatLineSquare /></el-icon> {{ discussion.replyCount }} 回复</span>
          </div>
        </div>

        <div class="question-content">
          <p>{{ discussion.content }}</p>
        </div>

        <div class="question-tags" v-if="discussion.tags?.length">
          <el-tag
            v-for="tag in discussion.tags"
            :key="tag"
            type="info"
            size="small"
          >
            {{ tag }}
          </el-tag>
        </div>
      </div>

      <!-- 回复列表 -->
      <div class="replies-section">
        <div class="section-header">
          <h2>{{ replies.length }} 个回复</h2>
          <el-radio-group v-model="sortBy" size="small">
            <el-radio-button label="time">按时间</el-radio-button>
            <el-radio-button label="like">按点赞</el-radio-button>
          </el-radio-group>
        </div>

        <div class="reply-list">
          <div
            class="reply-item"
            v-for="reply in sortedReplies"
            :key="reply.id"
            :class="{ accepted: reply.isAccepted }"
          >
            <div class="reply-left">
              <el-avatar :size="40" :src="reply.authorAvatar">
                {{ reply.authorName?.charAt(0) }}
              </el-avatar>
            </div>

            <div class="reply-main">
              <div class="reply-header">
                <span class="author">{{ reply.authorName }}</span>
                <el-tag v-if="reply.isAccepted" type="success" size="small" effect="dark" round style="margin-left: 8px;">
                  已采纳
                </el-tag>
                <span class="time">{{ formatTime(reply.createTime) }}</span>
              </div>

              <div class="reply-content">
                <p>{{ reply.content }}</p>
              </div>

              <div class="reply-actions">
                <el-button text size="small" @click="handleLikeReply(reply)">
                  <el-icon :color="reply.isLiked ? 'var(--primary-color)' : ''">
                    <svg viewBox="0 0 1024 1024" xmlns="http://www.w3.org/2000/svg">
                      <path fill="currentColor" d="M801.171 349.091c-1.455-66.909-58.182-119.273-126.546-119.273H532.364V127.418c0-34.909-29.091-64-64-64-18.909 0-36.364 9.455-46.545 25.455l-151.273 243.636c-8.727 13.818-14.545 29.818-14.545 46.545v456.727c0 34.909 29.091 64 64 64h363.636c53.818 0 100.364-36.364 113.455-88.727l58.182-261.818c1.455-6.545 2.182-13.091 2.182-20.364V349.091z" />
                      <path fill="currentColor" d="M218.182 349.091v549.818H98.909c-18.909 0-34.909-16-34.909-34.909V384c0-18.909 16-34.909 34.909-34.909h119.273z" />
                    </svg>
                  </el-icon>
                  赞 ({{ reply.likeCount }})
                </el-button>
                <el-button text size="small" @click="showReplyInput(reply)">
                  <el-icon><ChatLineSquare /></el-icon> 回复
                </el-button>
                <el-button
                  v-if="isQuestionAuthor && !discussion.isResolved"
                  text
                  size="small"
                  type="success"
                  @click="handleAccept(reply)"
                >
                  <el-icon><Check /></el-icon> 采纳
                </el-button>
                <el-button
                  v-if="reply.authorId === userStore.userInfo?.id"
                  text
                  size="small"
                  type="danger"
                  @click="handleDeleteReply(reply)"
                >
                  删除
                </el-button>
              </div>

              <!-- 子回复 -->
              <div class="sub-replies" v-if="reply.children?.length">
                <div class="sub-reply" v-for="sub in reply.children" :key="sub.id">
                  <el-avatar :size="24" :src="sub.authorAvatar">
                    {{ sub.authorName?.charAt(0) }}
                  </el-avatar>
                  <div class="sub-content">
                    <span class="author">{{ sub.authorName }}</span>
                    <span class="text">{{ sub.content }}</span>
                    <span class="time">{{ formatTime(sub.createTime) }}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <el-empty v-if="!replies.length" description="暂无回复，快来抢沙发" />
      </div>

      <!-- 发表回复 -->
      <div class="reply-form-section" v-if="userStore.isLoggedIn">
        <h3>发表回复</h3>
        <el-input
          v-model="replyContent"
          type="textarea"
          :rows="4"
          placeholder="写下你的回答..."
        />
        <div class="form-actions">
          <el-button type="primary" @click="handleSubmitReply" :loading="submitting">
            发表回复
          </el-button>
        </div>
      </div>
      <div class="login-prompt" v-else>
        <router-link to="/auth/login">登录</router-link> 后参与讨论
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import {
  getDiscussionDetail,
  getDiscussionReplies,
  createReply,
  deleteReply,
  acceptReply,
  likeReply
} from '@/api/discussion'
import { ElMessage, ElMessageBox } from 'element-plus'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import { ArrowLeft, View, ChatLineSquare, Top, Check } from '@element-plus/icons-vue'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const discussion = ref(null)
const replies = ref([])
const replyContent = ref('')
const sortBy = ref('time')
const submitting = ref(false)

// 是否为问题发布者
const isQuestionAuthor = computed(() => {
  return discussion.value?.authorId === userStore.userInfo?.id
})

// 排序后的回复
const sortedReplies = computed(() => {
  const list = [...replies.value]
  if (sortBy.value === 'like') {
    return list.sort((a, b) => b.likeCount - a.likeCount)
  }
  return list.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))
})

// 加载讨论详情
async function loadDiscussion() {
  loading.value = true
  try {
    const res = await getDiscussionDetail(route.params.id)
    if (res?.data) {
      discussion.value = res.data
    }

    // 加载回复
    const repliesRes = await getDiscussionReplies(route.params.id, { page: 1, size: 50 })
    if (repliesRes?.data) {
      const items = repliesRes.data.list || repliesRes.data
      replies.value = Array.isArray(items) ? items : []
    }
  } catch (error) {
    ElMessage.error('加载失败')
    router.back()
  } finally {
    loading.value = false
  }
}

// 显示回复输入框
function showReplyInput(reply) {
  replyContent.value = `@${reply.authorName} `
}

// 点赞回复
async function handleLikeReply(reply) {
  if (!userStore.isLoggedIn) {
    ElMessage.warning('请先登录')
    return
  }
  reply.isLiked = !reply.isLiked
  reply.likeCount += reply.isLiked ? 1 : -1
}

// 采纳回复
async function handleAccept(reply) {
  try {
    await ElMessageBox.confirm('确定采纳这个回复为最佳答案？', '提示')
    await acceptReply(discussion.value.id, reply.id)
    reply.isAccepted = true
    discussion.value.isResolved = true
    ElMessage.success('采纳成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 删除回复
async function handleDeleteReply(reply) {
  try {
    await ElMessageBox.confirm('确定删除这条回复？', '提示')
    await deleteReply(discussion.value.id, reply.id)
    replies.value = replies.value.filter(r => r.id !== reply.id)
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

// 发表回复
async function handleSubmitReply() {
  if (!replyContent.value.trim()) {
    ElMessage.warning('请输入回复内容')
    return
  }

  submitting.value = true
  try {
    await createReply(discussion.value.id, { content: replyContent.value })
    replyContent.value = ''
    loadDiscussion()
    ElMessage.success('回复成功')
  } catch (error) {
    ElMessage.error('回复失败')
  } finally {
    submitting.value = false
  }
}

// 格式化时间
function formatTime(time) {
  return dayjs(time).fromNow()
}

onMounted(() => {
  loadDiscussion()
})
</script>

<style scoped>
.discussion-detail-page {
  max-width: 900px;
  margin: 0 auto;
}

.back-bar {
  margin-bottom: 16px;
}

/* 问题部分 */
.question-section {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 24px;
  margin-bottom: 24px;
}

.question-header {
  display: flex;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 20px;
}

.question-title {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  line-height: 1.4;
}

.question-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  padding-bottom: 20px;
  border-bottom: 1px solid var(--border-color);
  margin-bottom: 20px;
}

.meta-info {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.author {
  font-weight: 500;
  color: var(--text-primary);
}

.time {
  font-size: 13px;
  color: var(--text-tertiary);
}

.meta-stats {
  margin-left: auto;
  display: flex;
  gap: 16px;
  font-size: 14px;
  color: var(--text-secondary);
}

.meta-stats span {
  display: flex;
  align-items: center;
  gap: 4px;
}

.question-content {
  color: var(--text-secondary);
  line-height: 1.8;
  white-space: pre-wrap;
}

.question-tags {
  display: flex;
  gap: 8px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid var(--border-color);
}

/* 回复部分 */
.replies-section {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 24px;
  margin-bottom: 24px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 24px;
}

.section-header h2 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
}

.reply-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.reply-item {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: var(--bg-tertiary);
  border-radius: var(--radius-md);
  border: 1px solid transparent;
  transition: all var(--transition-fast);
}

.reply-item.accepted {
  border-color: var(--success-color);
  background: rgba(82, 196, 26, 0.05);
}

.reply-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
}

.reply-main {
  flex: 1;
}

.reply-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 12px;
}

.reply-header .author {
  font-weight: 500;
  color: var(--text-primary);
}

.reply-header .time {
  margin-left: auto;
  font-size: 12px;
  color: var(--text-tertiary);
}

.reply-content {
  color: var(--text-secondary);
  line-height: 1.8;
  white-space: pre-wrap;
  margin-bottom: 12px;
}

.reply-actions {
  display: flex;
  gap: 8px;
}

/* 子回复 */
.sub-replies {
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.sub-reply {
  display: flex;
  align-items: flex-start;
  gap: 8px;
}

.sub-content {
  font-size: 14px;
}

.sub-content .author {
  color: var(--primary-color);
  margin-right: 8px;
}

.sub-content .text {
  color: var(--text-secondary);
}

.sub-content .time {
  margin-left: 12px;
  color: var(--text-tertiary);
  font-size: 12px;
}

/* 回复表单 */
.reply-form-section {
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  padding: 24px;
}

.reply-form-section h3 {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary);
  margin-bottom: 16px;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.login-prompt {
  text-align: center;
  padding: 32px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  color: var(--text-secondary);
}
</style>
