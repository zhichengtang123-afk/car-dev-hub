<template>
  <div class="app-layout">
    <!-- 顶部导航栏 -->
    <header class="header">
      <div class="header-container">
        <!-- Logo -->
        <div class="logo" @click="$router.push('/')">
          <div class="logo-icon">
            <el-icon :size="28"><Van /></el-icon>
          </div>
          <span class="logo-text">汽车研发知识共享平台</span>
        </div>

        <!-- 主导航菜单 -->
        <nav class="main-nav">
          <router-link 
            v-for="item in navItems" 
            :key="item.path" 
            :to="item.path"
            class="nav-item"
            :class="{ active: isActive(item.path) }"
          >
            <el-icon><component :is="item.icon" /></el-icon>
            <span>{{ item.title }}</span>
          </router-link>
        </nav>

        <!-- 右侧操作区 -->
        <div class="header-actions">
          <!-- 搜索框 -->
          <div class="search-box">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索知识、项目、问答..."
              prefix-icon="Search"
              @keyup.enter="handleSearch"
            />
          </div>

          <!-- 发布按钮 -->
          <el-button type="primary" @click="$router.push('/knowledge/create')" v-if="userStore.isLoggedIn">
            <el-icon><Plus /></el-icon>
            发布知识
          </el-button>

          <!-- 主题切换 -->
          <el-tooltip :content="appStore.isDark ? '切换到浅色模式' : '切换到深色模式'" placement="bottom">
            <el-button circle @click="appStore.toggleTheme">
              <el-icon><Sunny v-if="appStore.isDark" /><Moon v-else /></el-icon>
            </el-button>
          </el-tooltip>

          <!-- 通知 -->
          <el-popover
            v-if="userStore.isLoggedIn"
            placement="bottom"
            :width="300"
            trigger="click"
            @show="handlePopoverShow"
          >
            <template #reference>
              <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
                <el-button circle>
                  <el-icon><Bell /></el-icon>
                </el-button>
              </el-badge>
            </template>
            <div class="notification-panel">
              <div class="panel-header">
                <span>通知消息</span>
                <el-button text size="small" @click="handleMarkAllRead">全部已读</el-button>
              </div>
              <div class="notification-list" v-loading="loadingNotifications">
                <div 
                  v-for="item in notifications" 
                  :key="item.id" 
                  class="notification-item" 
                  :class="{ 'is-unread': item.isRead === 0 }"
                  @click="handleNotificationClick(item)"
                >
                  <div class="noti-title">{{ item.title }}</div>
                  <div class="noti-content">{{ item.content }}</div>
                  <div class="noti-time">{{ formatTime(item.createTime) }}</div>
                </div>
                <el-empty v-if="notifications.length === 0" description="暂无通知" :image-size="60" />
              </div>
            </div>
          </el-popover>

          <!-- 用户菜单 -->
          <template v-if="userStore.isLoggedIn">
            <el-dropdown trigger="click" @command="handleUserCommand">
              <div class="user-dropdown">
                <el-avatar :size="36" :src="userStore.userInfo?.avatar">
                  {{ userStore.userInfo?.username?.charAt(0) }}
                </el-avatar>
                <span class="username">{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</span>
                <el-icon><ArrowDown /></el-icon>
              </div>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="profile" :class="{ 'is-active': isMenuActive('/profile') }">
                    <el-icon><User /></el-icon>
                    个人中心
                  </el-dropdown-item>
                  <el-dropdown-item command="favorites" :class="{ 'is-active': isMenuActive('/favorites') }">
                    <el-icon><Star /></el-icon>
                    我的收藏
                  </el-dropdown-item>
                  <el-dropdown-item command="my-knowledge" :class="{ 'is-active': route.path === '/knowledge/list' && route.query.author === 'me' }">
                    <el-icon><Document /></el-icon>
                    我的知识
                  </el-dropdown-item>
                  <el-dropdown-item v-if="userStore.isAdmin || userStore.isExpert" divided command="audit" :class="{ 'is-active': isMenuActive('/admin/audit') }">
                    <el-icon><Check /></el-icon>
                    知识审核
                  </el-dropdown-item>
                  <el-dropdown-item v-if="userStore.isAdmin" command="user-manage" :class="{ 'is-active': isMenuActive('/user/list') }">
                    <el-icon><UserFilled /></el-icon>
                    用户管理
                  </el-dropdown-item>
                  <el-dropdown-item v-if="userStore.isAdmin" command="settings" :class="{ 'is-active': isMenuActive('/admin/settings') }">
                    <el-icon><Setting /></el-icon>
                    系统配置
                  </el-dropdown-item>
                  <el-dropdown-item divided command="logout">
                    <el-icon><SwitchButton /></el-icon>
                    退出登录
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
          <template v-else>
            <el-button @click="$router.push('/auth/login')">登录</el-button>
            <el-button type="primary" @click="$router.push('/auth/register')">注册</el-button>
          </template>
        </div>
      </div>
    </header>

    <!-- 主内容区 -->
    <main class="main-content" :class="{ 'no-transition': isNoAnimationPage }">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="footer-container">
        <div class="footer-links">
          <a href="#">关于我们</a>
          <a href="#">使用帮助</a>
          <a href="#">联系我们</a>
          <a href="#">隐私政策</a>
        </div>
        <div class="footer-copyright">
          © 2026 汽车研发知识共享平台 - 打造汽车行业知识共享生态
        </div>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useAppStore } from '@/stores/app'
import { getNotificationList, getUnreadCount, markAsRead, markAllAsRead } from '@/api/notification'
import dayjs from 'dayjs'
import { Van, Plus, Bell, ArrowDown, User, UserFilled, Star, Document, Setting, SwitchButton, Search, Sunny, Moon, Check } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const appStore = useAppStore()

const searchKeyword = ref('')

// 需要禁用动画的页面路径前缀
const noAnimationPrefixes = ['/admin/audit', '/user/list', '/admin/settings']
const isNoAnimationPage = computed(() => {
  return route.path && noAnimationPrefixes.some(prefix => route.path.startsWith(prefix))
})

// 导航菜单项
const navItems = computed(() => {
  const items = [
    { path: '/dashboard', title: '首页', icon: 'HomeFilled' },
    { path: '/knowledge/list', title: '知识库', icon: 'Document' },
    { path: '/project/list', title: '项目', icon: 'Folder' },
    { path: '/discussion/list', title: '问答', icon: 'ChatDotRound' },
    { path: '/statistics', title: '统计', icon: 'DataAnalysis' }
  ]
  return items
})

// 判断当前路由是否激活
function isActive(path) {
  if (path === '/dashboard') {
    return route.path === '/' || route.path === '/dashboard'
  }
  return route.path.startsWith(path.replace('/list', ''))
}

// 判断下拉菜单项是否激活
function isMenuActive(path) {
  return route.path === path || route.path.startsWith(path)
}

// 搜索
function handleSearch() {
  if (searchKeyword.value.trim()) {
    appStore.addSearchHistory(searchKeyword.value)
    router.push({ path: '/knowledge/list', query: { keyword: searchKeyword.value } })
  }
}

// 用户菜单命令
function handleUserCommand(command) {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'favorites':
      router.push('/favorites')
      break
    case 'my-knowledge':
      router.push('/knowledge/list?author=me')
      break
    case 'audit':
      router.push('/admin/audit')
      break
    case 'user-manage':
      router.push('/user/list')
      break
    case 'settings':
      router.push('/admin/settings')
      break
    case 'logout':
      userStore.logout()
      router.push('/auth/login')
      break
  }
}

// 通知逻辑
const unreadCount = ref(0)
const notifications = ref([])
const loadingNotifications = ref(false)
let pollingInterval = null

async function fetchUnreadCount() {
  if (userStore.isLoggedIn) {
    try {
      const res = await getUnreadCount()
      unreadCount.value = res.data || 0
    } catch (error) {
      console.error('获取未读数量失败', error)
    }
  }
}

async function handlePopoverShow() {
  loadingNotifications.value = true
  try {
    const res = await getNotificationList({ page: 1, size: 10 })
    if (res?.data) {
      notifications.value = res.data.list || []
    }
  } catch (error) {
    console.error('获取通知失败', error)
  } finally {
    loadingNotifications.value = false
  }
}

async function handleMarkAllRead() {
  try {
    await markAllAsRead()
    notifications.value.forEach(item => item.isRead = 1)
    unreadCount.value = 0
  } catch (error) {
    console.error('标记全部已读失败', error)
  }
}

async function handleNotificationClick(item) {
  if (item.isRead === 0) {
    try {
      await markAsRead(item.id)
      item.isRead = 1
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    } catch (error) {
      console.error('标记已读失败', error)
    }
  }
  if (item.referenceId) {
    router.push(`/knowledge/detail/${item.referenceId}`)
  }
}

function formatTime(time) {
  return dayjs(time).format('MM-DD HH:mm')
}

onMounted(() => {
  fetchUnreadCount()
  pollingInterval = setInterval(fetchUnreadCount, 60000)
})

onUnmounted(() => {
  if (pollingInterval) clearInterval(pollingInterval)
})
</script>

<style scoped>
.app-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
  background: var(--bg-primary);
}

/* 顶部导航 */
.header {
  position: sticky;
  top: 0;
  z-index: 100;
  background: var(--bg-secondary);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid var(--border-color);
}

.header-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  height: 64px;
  display: flex;
  align-items: center;
  gap: 20px;
}

/* Logo */
.logo {
  display: flex;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  flex-shrink: 0;
}

.logo-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, var(--primary-color) 0%, #8b5cf6 100%);
  border-radius: var(--radius-md);
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
}

.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  white-space: nowrap;
}

/* 主导航 */
.main-nav {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1 1 auto;
  min-width: 0;
  overflow-x: auto;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  color: var(--text-secondary);
  text-decoration: none;
  font-size: 15px;
  font-weight: 500;
  border-radius: var(--radius-md);
  transition: all var(--transition-fast);
  white-space: nowrap;
  flex-shrink: 0;
}

.nav-item:hover {
  color: var(--text-primary);
  background: var(--bg-hover);
}

.nav-item.active {
  color: var(--primary-color);
  background: var(--primary-bg);
}

/* 右侧操作区 */
.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-shrink: 0;
}

.search-box {
  width: 220px;
}

.search-box :deep(.el-input__wrapper) {
  background: var(--bg-tertiary);
  border-radius: 20px;
  box-shadow: none;
  border: 1px solid transparent;
}

.search-box :deep(.el-input__wrapper):focus-within {
  border-color: var(--primary-color);
}

.notification-badge {
  cursor: pointer;
}

/* 用户下拉 */
.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 4px 8px 4px 4px;
  border-radius: var(--radius-lg);
  transition: all var(--transition-fast);
}

.user-dropdown:hover {
  background: var(--bg-hover);
}

.username {
  color: var(--text-primary);
  font-weight: 500;
  font-size: 14px;
}

.user-dropdown .el-icon {
  color: var(--text-tertiary);
  font-size: 12px;
}

/* 主内容区 */
.main-content {
  flex: 1;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
  padding: 32px 24px;
}

/* 页脚 */
.footer {
  background: var(--bg-secondary);
  border-top: 1px solid var(--border-color);
  padding: 24px 0;
  margin-top: auto;
}

.footer-container {
  max-width: 1400px;
  margin: 0 auto;
  padding: 0 24px;
  text-align: center;
}

.footer-links {
  display: flex;
  justify-content: center;
  gap: 32px;
  margin-bottom: 16px;
}

.footer-links a {
  color: var(--text-secondary);
  text-decoration: none;
  font-size: 14px;
  transition: color var(--transition-fast);
}

.footer-links a:hover {
  color: var(--primary-color);
}

.footer-copyright {
  color: var(--text-tertiary);
  font-size: 13px;
}

/* 页面过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

/* 响应式 */
@media (max-width: 1200px) {
  .search-box {
    width: 200px;
  }
  
  .logo-text {
    display: none;
  }
}

@media (max-width: 768px) {
  .main-nav {
    display: none;
  }
  
  .search-box {
    width: 160px;
  }
}

/* 禁用动画时覆盖过渡属性 */
.main-content.no-transition .fade-enter-active,
.main-content.no-transition .fade-leave-active {
  transition: none !important;
  opacity: 1 !important;
}
.main-content.no-transition .fade-enter-from,
.main-content.no-transition .fade-leave-to {
  opacity: 1 !important;
}

/* 通知下拉框 */
.notification-panel {
  display: flex;
  flex-direction: column;
}
.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 8px 12px;
  border-bottom: 1px solid var(--border-color);
  font-weight: bold;
}
.notification-list {
  max-height: 300px;
  overflow-y: auto;
}
.notification-item {
  padding: 12px;
  border-bottom: 1px solid var(--border-color);
  cursor: pointer;
  transition: background var(--transition-fast);
}
.notification-item:hover {
  background: var(--bg-hover);
}
.notification-item.is-unread {
  background: var(--primary-bg);
}
.noti-title {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  margin-bottom: 4px;
}
.noti-content {
  font-size: 13px;
  color: var(--text-secondary);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  margin-bottom: 4px;
}
.noti-time {
  font-size: 12px;
  color: var(--text-tertiary);
}
</style>
