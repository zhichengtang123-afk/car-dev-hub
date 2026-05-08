import request from './request'

// 获取项目列表
export function getProjectList(params) {
    return request({
        url: '/project/list',
        method: 'get',
        params
    })
}

// 获取项目详情
export function getProjectDetail(id) {
    return request({
        url: `/project/${id}`,
        method: 'get'
    })
}

// 创建项目
export function createProject(data) {
    return request({
        url: '/project',
        method: 'post',
        data
    })
}

// 更新项目
export function updateProject(id, data) {
    return request({
        url: `/project/${id}`,
        method: 'put',
        data
    })
}

// 删除项目
export function deleteProject(id) {
    return request({
        url: `/project/${id}`,
        method: 'delete'
    })
}

// 获取项目成员
export function getProjectMembers(id) {
    return request({
        url: `/project/${id}/members`,
        method: 'get'
    })
}

// 添加项目成员
export function addProjectMember(id, userId) {
    return request({
        url: `/project/${id}/members`,
        method: 'post',
        data: { userId }
    })
}

// 移除项目成员
export function removeProjectMember(id, userId) {
    return request({
        url: `/project/${id}/members/${userId}`,
        method: 'delete'
    })
}

// 获取项目关联的知识
export function getProjectKnowledge(id, params) {
    return request({
        url: `/project/${id}/knowledge`,
        method: 'get',
        params
    })
}

// 关联知识到项目
export function linkKnowledgeToProject(projectId, knowledgeId) {
    return request({
        url: `/project/${projectId}/knowledge/${knowledgeId}`,
        method: 'post'
    })
}

// 取消关联
export function unlinkKnowledgeFromProject(projectId, knowledgeId) {
    return request({
        url: `/project/${projectId}/knowledge/${knowledgeId}`,
        method: 'delete'
    })
}
