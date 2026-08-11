import request from './request'

// 分页查询商品
export function pageProducts(params) {
  return request({
    url: '/product/page',
    method: 'get',
    params
  })
}

// 商品列表（不分页，用于下拉选择）
export function listProducts() {
  return request({
    url: '/product/list',
    method: 'get'
  })
}

// 商品详情
export function getProduct(id) {
  return request({
    url: `/product/${id}`,
    method: 'get'
  })
}

// 获取下一个SKU编码
export function getNextSkuCode() {
  return request({
    url: '/product/next-code',
    method: 'get'
  })
}

// 新增商品（仅管理员）
export function addProduct(data) {
  return request({
    url: '/product',
    method: 'post',
    data
  })
}

// 修改商品（仅管理员）
export function updateProduct(data) {
  return request({
    url: '/product',
    method: 'put',
    data
  })
}

// 删除商品（仅管理员）
export function deleteProduct(ids) {
  return request({
    url: `/product/${ids}`,
    method: 'delete'
  })
}
