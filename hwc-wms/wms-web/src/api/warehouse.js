import request from './request'

// 分页查询仓库
export function pageWarehouses(params) {
  return request({
    url: '/warehouse/page',
    method: 'get',
    params
  })
}

// 仓库列表（不分页，用于下拉选择）
export function listWarehouses() {
  return request({
    url: '/warehouse/list',
    method: 'get'
  })
}

// 仓库详情
export function getWarehouse(id) {
  return request({
    url: `/warehouse/${id}`,
    method: 'get'
  })
}

// 获取下一个仓库编码
export function getNextCode() {
  return request({
    url: '/warehouse/next-code',
    method: 'get'
  })
}

// 新增仓库（仅管理员）
export function addWarehouse(data) {
  return request({
    url: '/warehouse',
    method: 'post',
    data
  })
}

// 修改仓库（仅管理员）
export function updateWarehouse(data) {
  return request({
    url: '/warehouse',
    method: 'put',
    data
  })
}

// 删除仓库（仅管理员）
export function deleteWarehouse(ids) {
  return request({
    url: `/warehouse/${ids}`,
    method: 'delete'
  })
}
