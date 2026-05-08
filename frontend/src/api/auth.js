import request from './request'

// 用户登录
export function login(data) {
    return request({
        url: '/auth/login',
        method: 'post',
        data
    })
}

// 用户注册
export function register(data) {
    return request({
        url: '/auth/register',
        method: 'post',
        data
    })
}

// 获取当前用户信息
export function getUserInfo() {
    return request({
        url: '/user/info',
        method: 'get'
    })
}

// 更新用户信息
export function updateUserInfo(data) {
    return request({
        url: '/user/info',
        method: 'put',
        data
    })
}

// 修改密码
export function changePassword(data) {
    return request({
        url: '/user/password',
        method: 'put',
        data
    })
}

// 获取用户列表（管理员）
export function getUserList(params) {
    return request({
        url: '/user/list',
        method: 'get',
        params
    })
}

// 更新用户状态
export function updateUserStatus(id, status) {
    return request({
        url: `/user/${id}/status`,
        method: 'put',
        data: { status }
    })
}

// 更新用户角色
export function updateUserRole(id, role) {
    return request({
        url: `/user/${id}/role`,
        method: 'put',
        data: { role }
    })
}

// 重置用户密码
export function resetUserPassword(id) {
    return request({
        url: `/user/${id}/reset-password`,
        method: 'post'
    })
}
