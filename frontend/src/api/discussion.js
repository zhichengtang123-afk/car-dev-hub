import request from './request'

// 获取讨论列表
export function getDiscussionList(params) {
    return request({
        url: '/discussion/list',
        method: 'get',
        params
    })
}

// 获取讨论详情
export function getDiscussionDetail(id) {
    return request({
        url: `/discussion/${id}`,
        method: 'get'
    })
}

// 创建讨论
export function createDiscussion(data) {
    return request({
        url: '/discussion',
        method: 'post',
        data
    })
}

// 更新讨论
export function updateDiscussion(id, data) {
    return request({
        url: `/discussion/${id}`,
        method: 'put',
        data
    })
}

// 删除讨论
export function deleteDiscussion(id) {
    return request({
        url: `/discussion/${id}`,
        method: 'delete'
    })
}

// 获取讨论回复
export function getDiscussionReplies(id, params) {
    return request({
        url: `/discussion/${id}/replies`,
        method: 'get',
        params
    })
}

// 创建回复
export function createReply(discussionId, data) {
    return request({
        url: `/discussion/${discussionId}/replies`,
        method: 'post',
        data
    })
}

// 删除回复
export function deleteReply(discussionId, replyId) {
    return request({
        url: `/discussion/${discussionId}/replies/${replyId}`,
        method: 'delete'
    })
}

// 采纳回复
export function acceptReply(discussionId, replyId) {
    return request({
        url: `/discussion/${discussionId}/replies/${replyId}/accept`,
        method: 'post'
    })
}

// 点赞回复
export function likeReply(discussionId, replyId) {
    return request({
        url: `/discussion/${discussionId}/replies/${replyId}/like`,
        method: 'post'
    })
}

// 取消点赞回复
export function unlikeReply(discussionId, replyId) {
    return request({
        url: `/discussion/${discussionId}/replies/${replyId}/like`,
        method: 'delete'
    })
}
