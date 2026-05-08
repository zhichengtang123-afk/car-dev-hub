import request from './request'

// 获取知识列表
export function getKnowledgeList(params) {
    return request({
        url: '/knowledge/list',
        method: 'get',
        params
    })
}

// 获取知识详情
export function getKnowledgeDetail(id) {
    return request({
        url: `/knowledge/${id}`,
        method: 'get'
    })
}

// 创建知识
export function createKnowledge(data) {
    return request({
        url: '/knowledge',
        method: 'post',
        data
    })
}

// 更新知识
export function updateKnowledge(id, data) {
    return request({
        url: `/knowledge/${id}`,
        method: 'put',
        data
    })
}

// 删除知识
export function deleteKnowledge(id) {
    return request({
        url: `/knowledge/${id}`,
        method: 'delete'
    })
}

// 上传附件
export function uploadAttachment(file, onProgress) {
    const formData = new FormData()
    formData.append('file', file)

    return request({
        url: '/knowledge/upload',
        method: 'post',
        data: formData,
        headers: {
            'Content-Type': 'multipart/form-data'
        },
        onUploadProgress: (progressEvent) => {
            if (onProgress) {
                const percent = Math.round((progressEvent.loaded * 100) / progressEvent.total)
                onProgress(percent)
            }
        }
    })
}

// 下载附件
export function downloadAttachment(id) {
    return request({
        url: `/knowledge/attachment/${id}/download`,
        method: 'get',
        responseType: 'blob'
    })
}

// 搜索知识
export function searchKnowledge(params) {
    return request({
        url: '/knowledge/search',
        method: 'get',
        params
    })
}

// 获取知识版本历史
export function getKnowledgeVersions(id) {
    return request({
        url: `/knowledge/${id}/versions`,
        method: 'get'
    })
}

// 回滚版本
export function rollbackVersion(id, versionId) {
    return request({
        url: `/knowledge/${id}/rollback/${versionId}`,
        method: 'post'
    })
}

// 点赞知识
export function likeKnowledge(id) {
    return request({
        url: `/knowledge/${id}/like`,
        method: 'post'
    })
}

// 取消点赞
export function unlikeKnowledge(id) {
    return request({
        url: `/knowledge/${id}/like`,
        method: 'delete'
    })
}

// 收藏知识
export function collectKnowledge(id, folderId) {
    return request({
        url: `/knowledge/${id}/collect`,
        method: 'post',
        data: { folderId }
    })
}

// 取消收藏
export function uncollectKnowledge(id) {
    return request({
        url: `/knowledge/${id}/collect`,
        method: 'delete'
    })
}

// 获取我的收藏
export function getMyCollections(params) {
    return request({
        url: '/knowledge/collections',
        method: 'get',
        params
    })
}

// 获取知识评论
export function getKnowledgeComments(id, params) {
    return request({
        url: `/knowledge/${id}/comments`,
        method: 'get',
        params
    })
}

// 发表评论
export function createComment(knowledgeId, data) {
    return request({
        url: `/knowledge/${knowledgeId}/comments`,
        method: 'post',
        data
    })
}

// 删除评论
export function deleteComment(knowledgeId, commentId) {
    return request({
        url: `/knowledge/${knowledgeId}/comments/${commentId}`,
        method: 'delete'
    })
}

// 获取热门知识
export function getHotKnowledge(params) {
    return request({
        url: '/knowledge/hot',
        method: 'get',
        params
    })
}

// 获取推荐知识
export function getRecommendKnowledge(params) {
    return request({
        url: '/knowledge/recommend',
        method: 'get',
        params
    })
}

// 获取知识类型选项
export function getKnowledgeTypes() {
    return request({
        url: '/knowledge/types',
        method: 'get'
    })
}

// 获取知识领域选项
export function getKnowledgeDomains() {
    return request({
        url: '/knowledge/domains',
        method: 'get'
    })
}

// 获取热门标签
export function getHotTags(params) {
    return request({
        url: '/knowledge/tags/hot',
        method: 'get',
        params
    })
}
