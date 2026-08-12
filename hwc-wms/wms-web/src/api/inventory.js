import request from './request'

// 分页查询库存
export function pageInventory(params) {
  return request({
    url: '/inventory/page',
    method: 'get',
    params
  })
}

// 库存调整（仅管理员）
export function adjustInventory(data) {
  return request({
    url: '/inventory/adjust',
    method: 'post',
    data
  })
}

// 分页查询库存流水
export function pageInventoryLogs(params) {
  return request({
    url: '/inventory/logs/page',
    method: 'get',
    params
  })
}
