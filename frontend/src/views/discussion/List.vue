<template>
  <div class="discussion-list-page">
    <div class="page-header">
      <h1 class="page-title">讨论区</h1>
      <el-button type="primary" @click="$router.push('/discussion/create')">
        <el-icon><Plus /></el-icon>
        发起提问
      </el-button>
    </div>

    <!-- 筛选区 -->
    <el-card class="filter-card">
      <el-row :gutter="16">
        <el-col :span="10">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索问题..."
            prefix-icon="Search"
            clearable
            @keyup.enter="handleSearch"
          />
        </el-col>
        <el-col :span="4">
          <el-select v-model="filterStatus" placeholder="状态" clearable style="width: 100%">
            <el-option label="全部" value="" />
            <el-option label="未解决" value="0" />
            <el-option label="已解决" value="1" />
          </el-select>
        </el-col>
        <el-col :span="4">
          <el-select v-model="filterSort" placeholder="排序" style="width: 100%">
            <el-option label="最新发布" value="createTime" />
            <el-option label="最多回复" value="replyCount" />
            <el-option label="最多浏览" value="viewCount" />
          </el-select>
        </el-col>
        <el-col :span="2">
          <el-button type="primary" @click="handleSearch">搜索</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 讨论列表 -->
    <div class="discussion-list" v-loading="loading">
      <div
        class="discussion-item"
        v-for="item in discussionList"
        :key="item.id"
        @click="$router.push(`/discussion/detail/${item.id}`)"
      >
        <div class="item-left">
          <el-avatar :size="48" :src="item.authorAvatar">
            {{ item.authorName?.charAt(0) }}
          </el-avatar>
        </div>
        
        <div class="item-main">
          <div class="item-header">
            <el-tag v-if="item.isResolved" type="success" size="small">已解决</el-tag>
            <el-tag v-else type="warning" size="small">未解决</el-tag>
            <h3 class="item-title">{{ item.title }}</h3>
          </div>
          
          <p class="item-content">{{ item.content }}</p>
          
          <div class="item-meta">
            <span class="author">{{ item.authorName }}</span>
            <span class="time">{{ formatTime(item.createTime) }}</span>
            <span class="tags" v-if="item.tags?.length">
              <el-tag
                v-for="tag in item.tags.slice(0, 3)"
                :key="tag"
                type="info"
                size="small"
              >
                {{ tag }}
              </el-tag>
            </span>
          </div>
        </div>
        
        <div class="item-stats">
          <div class="stat">
            <span class="value">{{ item.viewCount }}</span>
            <span class="label">浏览</span>
          </div>
          <div class="stat">
            <span class="value">{{ item.replyCount }}</span>
            <span class="label">回复</span>
          </div>
        </div>
      </div>
    </div>

    <el-empty v-if="!loading && !discussionList.length" description="暂无讨论" />

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
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getDiscussionList } from '@/api/discussion'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'
import { Plus } from '@element-plus/icons-vue'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const loading = ref(false)
const searchKeyword = ref('')
const filterStatus = ref('')
const filterSort = ref('createTime')
const currentPage = ref(1)
const pageSize = 10
const total = ref(0)
const discussionList = ref([])

// 加载数据
onMounted(() => {
  fetchDiscussions()
})

// 搜索
function handleSearch() {
  currentPage.value = 1
  fetchDiscussions()
}

// 获取讨论列表
async function fetchDiscussions() {
  loading.value = true
  try {
    const res = await getDiscussionList({
      keyword: searchKeyword.value,
      resolved: filterStatus.value === 'resolved' ? true : filterStatus.value === 'pending' ? false : undefined,
      page: currentPage.value,
      size: pageSize
    })
    if (res?.data) {
      discussionList.value = res.data.list || []
      total.value = res.data.total || 0
    }
  } catch (error) {
    console.error('获取讨论列表失败:', error)
  } finally {
    loading.value = false
  }
}

// 分页
function handlePageChange(page) {
  currentPage.value = page
  fetchDiscussions()
}

// 格式化时间
function formatTime(time) {
  return dayjs(time).fromNow()
}
</script>

<style scoped>
.discussion-list-page {
  max-width: 1000px;
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

/* 讨论列表 */
.discussion-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.discussion-item {
  display: flex;
  gap: 16px;
  padding: 20px;
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-lg);
  cursor: pointer;
  transition: all var(--transition-normal);
}

.discussion-item:hover {
  border-color: var(--primary-color);
  box-shadow: var(--shadow-glow);
}

.item-left {
  flex-shrink: 0;
}

.item-main {
  flex: 1;
  min-width: 0;
}

.item-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.item-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.4;
}

.item-content {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 12px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.item-meta {
  display: flex;
  align-items: center;
  gap: 16px;
  font-size: 13px;
  color: var(--text-tertiary);
}

.tags {
  display: flex;
  gap: 6px;
}

.item-stats {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding-left: 24px;
  border-left: 1px solid var(--border-color);
  flex-shrink: 0;
}

.stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
  min-width: 60px;
}

.stat .value {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
}

.stat .label {
  font-size: 12px;
  color: var(--text-tertiary);
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 32px;
}
</style>
