package com.hwc.wms.modules.business.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hwc.wms.modules.business.entity.Warehouse;

/**
 * 仓库 Service
 */
public interface WarehouseService extends IService<Warehouse> {

    /**
     * 分页查询仓库（支持按名称、编码或国家搜索）
     */
    Page<Warehouse> pageWarehouses(Page<Warehouse> page, String keyword);

    /**
     * 新增仓库
     */
    void saveWarehouse(Warehouse warehouse);

    /**
     * 修改仓库
     */
    void updateWarehouse(Warehouse warehouse);

    /**
     * 获取下一个仓库编码
     */
    String getNextCode();
}
