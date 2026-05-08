import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'

export const useAppStore = defineStore('app', () => {
    // 主题（dark / light）
    const theme = ref(localStorage.getItem('theme') || 'dark')

    // 是否为深色主题
    const isDark = computed(() => theme.value === 'dark')

    // 侧边栏折叠状态
    const sidebarCollapsed = ref(false)

    // 全局加载状态
    const loading = ref(false)

    // 搜索历史
    const searchHistory = ref(JSON.parse(localStorage.getItem('searchHistory') || '[]'))

    // 初始化主题
    function initTheme() {
        const savedTheme = localStorage.getItem('theme')
        if (savedTheme) {
            theme.value = savedTheme
        } else {
            // 根据系统偏好设置
            const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
            theme.value = prefersDark ? 'dark' : 'light'
        }
        applyTheme()
    }

    // 应用主题
    function applyTheme() {
        document.documentElement.setAttribute('data-theme', theme.value)
        localStorage.setItem('theme', theme.value)
    }

    // 切换主题
    function toggleTheme() {
        theme.value = theme.value === 'dark' ? 'light' : 'dark'
        applyTheme()
    }

    // 设置主题
    function setTheme(newTheme) {
        theme.value = newTheme
        applyTheme()
    }

    // 切换侧边栏
    function toggleSidebar() {
        sidebarCollapsed.value = !sidebarCollapsed.value
    }

    // 设置加载状态
    function setLoading(status) {
        loading.value = status
    }

    // 添加搜索历史
    function addSearchHistory(keyword) {
        if (!keyword.trim()) return

        // 移除重复项
        const index = searchHistory.value.indexOf(keyword)
        if (index > -1) {
            searchHistory.value.splice(index, 1)
        }

        // 添加到开头
        searchHistory.value.unshift(keyword)

        // 最多保留 10 条
        if (searchHistory.value.length > 10) {
            searchHistory.value.pop()
        }

        localStorage.setItem('searchHistory', JSON.stringify(searchHistory.value))
    }

    // 清空搜索历史
    function clearSearchHistory() {
        searchHistory.value = []
        localStorage.removeItem('searchHistory')
    }

    // 初始化时应用主题
    initTheme()

    return {
        theme,
        isDark,
        sidebarCollapsed,
        loading,
        searchHistory,
        initTheme,
        toggleTheme,
        setTheme,
        toggleSidebar,
        setLoading,
        addSearchHistory,
        clearSearchHistory
    }
})
