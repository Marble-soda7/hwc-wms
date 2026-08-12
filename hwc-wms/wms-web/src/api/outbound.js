import request from './request'

// 分页查询出库单
export function pageOutbounds(params) {
  return request({
    url: '/outbound/page',
    method: 'get',
    params
  })
}

// 出库单详情（含明细）
export function getOutbound(id) {
  return request({
    url: `/outbound/${id}`,
    method: 'get'
  })
}

// 获取下一个出库单号
export function getNextOutboundNo() {
  return request({
    url: '/outbound/next-code',
    method: 'get'
  })
}

// 创建出库单（仅管理员，自动锁定库存）
export function addOutbound(data) {
  return request({
    url: '/outbound',
    method: 'post',
    data
  })
}

// 修改出库单（仅管理员）
export function updateOutbound(id, data) {
  return request({
    url: `/outbound/${id}`,
    method: 'put',
    data
  })
}

// 拣货（仅管理员）
export function pickOutbound(id, items) {
  return request({
    url: `/outbound/${id}/pick`,
    method: 'post',
    data: items
  })
}

// 发货（仅管理员）
export function shipOutbound(id, data) {
  return request({
    url: `/outbound/${id}/ship`,
    method: 'post',
    data
  })
}

// 取消出库单（仅管理员）
export function cancelOutbound(id) {
  return request({
    url: `/outbound/${id}/cancel`,
    method: 'post'
  })
}
