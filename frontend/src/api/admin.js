import request from './request'

/**
 * 获取审核列表
 */
export function getAuditList(status, page = 1, size = 10) {
  // mapped status values: 'pending' => 1, 'approved' => 2, 'rejected' => 3
  let finalStatus;
  if (status === 'pending') finalStatus = 1;
  else if (status === 'approved') finalStatus = 2;
  else if (status === 'rejected') finalStatus = 3;

  return request({
    url: '/admin/audit/list',
    method: 'get',
    params: {
      status: finalStatus,
      page,
      size
    }
  })
}

/**
 * 审核通过
 */
export function approveKnowledge(id) {
  return request({
    url: `/admin/audit/${id}/approve`,
    method: 'post'
  })
}

/**
 * 审核拒绝
 */
export function rejectKnowledge(id, reason) {
  return request({
    url: `/admin/audit/${id}/reject`,
    method: 'post',
    data: { reason }
  })
}

/**
 * 保存系统设置
 */
export function saveSystemSettings(data) {
  return request({
    url: '/admin/settings/system',
    method: 'post',
    data
  })
}

/**
 * 获取所有后台设置
 */
export function getAllSettings() {
  return request({
    url: '/admin/settings/all',
    method: 'get'
  })
}

/**
 * 保存所有后台设置
 */
export function saveAllSettings(data) {
  return request({
    url: '/admin/settings/all',
    method: 'post',
    data
  })
}
