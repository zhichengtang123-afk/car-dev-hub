import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { login, register, getUserInfo, updateUserInfo } from '@/api/auth'
import { ElMessage } from 'element-plus'

export const useUserStore = defineStore('user', () => {
    // 状态
    const token = ref(localStorage.getItem('token') || '')
    const userInfo = ref(JSON.parse(localStorage.getItem('userInfo') || 'null'))

    // 计算属性
    const isLoggedIn = computed(() => !!token.value)
    const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')
    const isExpert = computed(() => userInfo.value?.role === 'EXPERT')

    // 登录
    async function loginAction(credentials) {
        try {
            const res = await login(credentials)
            if (res.code === 200) {
                token.value = res.data.token
                userInfo.value = res.data.user
                localStorage.setItem('token', res.data.token)
                localStorage.setItem('userInfo', JSON.stringify(res.data.user))
                ElMessage.success('登录成功')
                return true
            } else {
                ElMessage.error(res.message || '登录失败')
                return false
            }
        } catch (error) {
            ElMessage.error('登录失败，请检查网络')
            return false
        }
    }

    // 注册
    async function registerAction(userData) {
        try {
            const res = await register(userData)
            if (res.code === 200) {
                ElMessage.success('注册成功，请登录')
                return true
            } else {
                ElMessage.error(res.message || '注册失败')
                return false
            }
        } catch (error) {
            ElMessage.error('注册失败，请检查网络')
            return false
        }
    }

    // 获取用户信息
    async function fetchUserInfo() {
        try {
            const res = await getUserInfo()
            if (res.code === 200) {
                userInfo.value = res.data
                localStorage.setItem('userInfo', JSON.stringify(res.data))
                return true
            }
            return false
        } catch (error) {
            return false
        }
    }

    // 更新用户信息
    async function updateProfile(data) {
        try {
            const res = await updateUserInfo(data)
            if (res.code === 200) {
                userInfo.value = { ...userInfo.value, ...data }
                localStorage.setItem('userInfo', JSON.stringify(userInfo.value))
                ElMessage.success('更新成功')
                return true
            } else {
                ElMessage.error(res.message || '更新失败')
                return false
            }
        } catch (error) {
            ElMessage.error('更新失败，请检查网络')
            return false
        }
    }

    // 登出
    function logout() {
        token.value = ''
        userInfo.value = null
        localStorage.removeItem('token')
        localStorage.removeItem('userInfo')
        ElMessage.success('已退出登录')
    }

    return {
        token,
        userInfo,
        isLoggedIn,
        isAdmin,
        isExpert,
        loginAction,
        registerAction,
        fetchUserInfo,
        updateProfile,
        logout
    }
})
