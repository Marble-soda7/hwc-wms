import request from './request'

// 分页查询入库单
export function pageInbounds(params) {
  return request({
    url: '/inbound/page',
    method: 'get',
    params
  })
}

// 入库单详情（含明细）
export function getInbound(id) {
  return request({
    url: `/inbound/${id}`,
    method: 'get'
  })
}

// 获取下一个入库单号
export function getNextInboundNo() {
  return request({
    url: '/inbound/next-code',
    method: 'get'
  })
}

// 创建入库单（仅管理员）
export function addInbound(data) {
  return request({
    url: '/inbound',
    method: 'post',
    data
  })
}

// 修改入库单（仅管理员）
export function updateInbound(id, data) {
  return request({
    url: `/inbound/${id}`,
    method: 'put',
    data
  })
}

// 收货（仅管理员）
export function receiveInbound(id, items) {
  return request({
    url: `/inbound/${id}/receive`,
    method: 'post',
    data: items
  })
}

// 上架（仅管理员）
export function putawayInbound(id, items) {
  return request({
    url: `/inbound/${id}/putaway`,
    method: 'post',
    data: items
  })
}

// 取消入库单（仅管理员）
export function cancelInbound(id) {
  return request({
    url: `/inbound/${id}/cancel`,
    method: 'post'
  })
}
