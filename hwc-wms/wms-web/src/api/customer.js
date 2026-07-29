import request from './request'

// 分页查询客户
export function pageCustomers(params) {
  return request({
    url: '/customer/page',
    method: 'get',
    params
  })
}

// 客户列表（不分页，用于下拉选择）
export function listCustomers() {
  return request({
    url: '/customer/list',
    method: 'get'
  })
}

// 客户详情
export function getCustomer(id) {
  return request({
    url: `/customer/${id}`,
    method: 'get'
  })
}

// 获取下一个客户编码（仅管理员）
export function getNextCode() {
  return request({
    url: '/customer/next-code',
    method: 'get'
  })
}

// 新增客户（仅管理员）
export function addCustomer(data) {
  return request({
    url: '/customer',
    method: 'post',
    data
  })
}

// 修改客户（仅管理员）
export function updateCustomer(data) {
  return request({
    url: '/customer',
    method: 'put',
    data
  })
}

// 删除客户（仅管理员）
export function deleteCustomer(ids) {
  return request({
    url: `/customer/${ids}`,
    method: 'delete'
  })
}
