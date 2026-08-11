package com.hwc.wms.modules.business.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hwc.wms.modules.business.entity.Location;

/**
 * 库位 Service
 */
public interface LocationService extends IService<Location> {

    /**
     * 分页查询库位（支持按仓库筛选、编码/区域/货道搜索）
     */
    Page<Location> pageLocations(Page<Location> page, Long warehouseId, String keyword);

    /**
     * 新增库位（校验同仓库下编码唯一）
     */
    void saveLocation(Location location);

    /**
     * 修改库位
     */
    void updateLocation(Location location);

    /**
     * 获取下一个库位编码
     */
    String getNextCode();
}
