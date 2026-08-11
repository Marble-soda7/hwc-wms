import request from './request'

// 分类树（含children）
export function getCategoryTree() {
  return request({
    url: '/category/tree',
    method: 'get'
  })
}

// 分类列表（不分页，用于下拉选择）
export function listCategories() {
  return request({
    url: '/category/list',
    method: 'get'
  })
}

// 分类详情
export function getCategory(id) {
  return request({
    url: `/category/${id}`,
    method: 'get'
  })
}

// 新增分类（仅管理员）
export function addCategory(data) {
  return request({
    url: '/category',
    method: 'post',
    data
  })
}

// 修改分类（仅管理员）
export function updateCategory(data) {
  return request({
    url: '/category',
    method: 'put',
    data
  })
}

// 删除分类（仅管理员）
export function deleteCategory(ids) {
  return request({
    url: `/category/${ids}`,
    method: 'delete'
  })
}
