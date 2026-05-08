import { createRouter, createWebHistory } from 'vue-router'

// 布局组件 - 使用懒加载
const MainLayout = () => import('@/layouts/MainLayout.vue')
const AuthLayout = () => import('@/layouts/AuthLayout.vue')

const routes = [
    // 认证相关路由
    {
        path: '/auth',
        component: AuthLayout,
        redirect: '/auth/login',
        children: [
            {
                path: 'login',
                name: 'Login',
                component: () => import('@/views/auth/Login.vue'),
                meta: { title: '登录', guest: true }
            },
            {
                path: 'register',
                name: 'Register',
                component: () => import('@/views/auth/Register.vue'),
                meta: { title: '注册', guest: true }
            }
        ]
    },

    // 主应用路由
    {
        path: '/',
        component: MainLayout,
        redirect: '/dashboard',
        children: [
            // 仪表盘
            {
                path: 'dashboard',
                name: 'Dashboard',
                component: () => import('@/views/dashboard/Index.vue'),
                meta: { title: '工作台', icon: 'Odometer' }
            },

            // 知识资源管理
            {
                path: 'knowledge',
                name: 'Knowledge',
                redirect: '/knowledge/list',
                meta: { title: '知识资源', icon: 'Document' },
                children: [
                    {
                        path: 'list',
                        name: 'KnowledgeList',
                        component: () => import('@/views/knowledge/List.vue'),
                        meta: { title: '知识列表' }
                    },
                    {
                        path: 'create',
                        name: 'KnowledgeCreate',
                        component: () => import('@/views/knowledge/Create.vue'),
                        meta: { title: '上传知识', requireAuth: true }
                    },
                    {
                        path: 'detail/:id',
                        name: 'KnowledgeDetail',
                        component: () => import('@/views/knowledge/Detail.vue'),
                        meta: { title: '知识详情' }
                    },
                    {
                        path: 'edit/:id',
                        name: 'KnowledgeEdit',
                        component: () => import('@/views/knowledge/Create.vue'),
                        meta: { title: '编辑知识', requireAuth: true }
                    }
                ]
            },

            // 项目管理
            {
                path: 'project',
                name: 'Project',
                redirect: '/project/list',
                meta: { title: '项目管理', icon: 'Folder' },
                children: [
                    {
                        path: 'list',
                        name: 'ProjectList',
                        component: () => import('@/views/project/List.vue'),
                        meta: { title: '项目列表' }
                    },
                    {
                        path: 'detail/:id',
                        name: 'ProjectDetail',
                        component: () => import('@/views/project/Detail.vue'),
                        meta: { title: '项目详情' }
                    }
                ]
            },

            // 讨论区
            {
                path: 'discussion',
                name: 'Discussion',
                redirect: '/discussion/list',
                meta: { title: '讨论区', icon: 'ChatDotRound' },
                children: [
                    {
                        path: 'list',
                        name: 'DiscussionList',
                        component: () => import('@/views/discussion/List.vue'),
                        meta: { title: '问答列表' }
                    },
                    {
                        path: 'create',
                        name: 'DiscussionCreate',
                        component: () => import('@/views/discussion/Create.vue'),
                        meta: { title: '发起提问', requireAuth: true }
                    },
                    {
                        path: 'detail/:id',
                        name: 'DiscussionDetail',
                        component: () => import('@/views/discussion/Detail.vue'),
                        meta: { title: '问题详情' }
                    }
                ]
            },

            // 数据统计
            {
                path: 'statistics',
                name: 'Statistics',
                component: () => import('@/views/statistics/Index.vue'),
                meta: { title: '数据统计', icon: 'DataAnalysis', roles: ['ADMIN', 'EXPERT'] }
            },

            // 用户管理（管理员）
            {
                path: 'user',
                name: 'UserManagement',
                redirect: '/user/list',
                meta: { title: '用户管理', icon: 'User', roles: ['ADMIN'] },
                children: [
                    {
                        path: 'list',
                        name: 'UserList',
                        component: () => import('@/views/user/List.vue'),
                        meta: { title: '用户列表' }
                    }
                ]
            },

            // 知识审核（管理员）
            {
                path: 'admin/audit',
                name: 'KnowledgeAudit',
                component: () => import('@/views/admin/Audit.vue'),
                meta: { title: '知识审核', icon: 'Check', roles: ['ADMIN', 'EXPERT'] }
            },

            // 系统配置（管理员）
            {
                path: 'admin/settings',
                name: 'SystemSettings',
                component: () => import('@/views/admin/Settings.vue'),
                meta: { title: '系统配置', icon: 'Setting', roles: ['ADMIN'] }
            },

            // 个人中心
            {
                path: 'profile',
                name: 'Profile',
                component: () => import('@/views/profile/Index.vue'),
                meta: { title: '个人中心', requireAuth: true }
            },

            // 我的收藏
            {
                path: 'favorites',
                name: 'Favorites',
                component: () => import('@/views/favorites/Index.vue'),
                meta: { title: '我的收藏', requireAuth: true }
            }
        ]
    },

    // 404
    {
        path: '/:pathMatch(.*)*',
        name: 'NotFound',
        component: () => import('@/views/error/404.vue'),
        meta: { title: '页面不存在' }
    }
]

const router = createRouter({
    history: createWebHistory(),
    routes,
    scrollBehavior(to, from, savedPosition) {
        if (savedPosition) {
            return savedPosition
        } else {
            return { top: 0 }
        }
    }
})

// 路由守卫
router.beforeEach((to, from, next) => {
    // 设置页面标题
    document.title = `${to.meta.title || '汽车研发知识共享平台'} - 汽车研发知识共享平台`

    // 从 localStorage 直接获取 token，避免 pinia 循环依赖
    const token = localStorage.getItem('token')
    const userInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')

    // 需要认证的页面
    if (to.meta.requireAuth && !token) {
        next({ name: 'Login', query: { redirect: to.fullPath } })
        return
    }

    // 已登录用户访问登录/注册页
    if (to.meta.guest && token) {
        next({ name: 'Dashboard' })
        return
    }

    // 角色权限检查
    if (to.meta.roles && token) {
        const userRole = userInfo?.role
        if (!to.meta.roles.includes(userRole)) {
            next({ name: 'Dashboard' })
            return
        }
    }

    next()
})

export default router
