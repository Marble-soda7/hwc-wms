import request from './request'

// 分页查询库位
export function pageLocations(params) {
  return request({
    url: '/location/page',
    method: 'get',
    params
  })
}

// 库位列表（不分页，可指定仓库筛选）
export function listLocations(warehouseId) {
  return request({
    url: '/location/list',
    method: 'get',
    params: { warehouseId }
  })
}

// 库位详情
export function getLocation(id) {
  return request({
    url: `/location/${id}`,
    method: 'get'
  })
}

// 获取下一个库位编码
export function getNextLocationCode() {
  return request({
    url: '/location/next-code',
    method: 'get'
  })
}

// 新增库位（仅管理员）
export function addLocation(data) {
  return request({
    url: '/location',
    method: 'post',
    data
  })
}

// 修改库位（仅管理员）
export function updateLocation(data) {
  return request({
    url: '/location',
    method: 'put',
    data
  })
}

// 删除库位（仅管理员）
export function deleteLocation(ids) {
  return request({
    url: `/location/${ids}`,
    method: 'delete'
  })
}
