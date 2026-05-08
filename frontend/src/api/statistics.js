import request from './request'

// 获取知识统计
export function getKnowledgeStats() {
    return request({
        url: '/statistics/knowledge',
        method: 'get'
    })
}

// 获取用户贡献统计
export function getUserContributionStats(params) {
    return request({
        url: '/statistics/contribution',
        method: 'get',
        params
    })
}

// 获取系统使用统计
export function getSystemStats() {
    return request({
        url: '/statistics/system',
        method: 'get'
    })
}

// 获取知识领域分布
export function getDomainDistribution() {
    return request({
        url: '/statistics/domain-distribution',
        method: 'get'
    })
}

// 获取知识类型分布
export function getTypeDistribution() {
    return request({
        url: '/statistics/type-distribution',
        method: 'get'
    })
}

// 获取知识增长趋势
export function getKnowledgeTrend(params) {
    return request({
        url: '/statistics/knowledge-trend',
        method: 'get',
        params
    })
}

// 获取用户贡献排行榜
export function getContributionRanking(params) {
    return request({
        url: '/statistics/contribution-ranking',
        method: 'get',
        params
    })
}

// 获取热门知识排行榜
export function getHotKnowledgeRanking(params) {
    return request({
        url: '/statistics/hot-knowledge',
        method: 'get',
        params
    })
}

// 获取活跃用户统计
export function getActiveUserStats(params) {
    return request({
        url: '/statistics/active-users',
        method: 'get',
        params
    })
}

// 获取仪表盘数据
export function getDashboardData() {
    return request({
        url: '/statistics/dashboard',
        method: 'get'
    })
}

// 获取热门知识排行（统计页使用）
export function getHotKnowledge(params) {
    return request({
        url: '/statistics/hot-knowledge',
        method: 'get',
        params
    })
}

// 获取部门贡献排行
export function getDepartmentRanking(params) {
    return request({
        url: '/statistics/department-ranking',
        method: 'get',
        params
    })
}
