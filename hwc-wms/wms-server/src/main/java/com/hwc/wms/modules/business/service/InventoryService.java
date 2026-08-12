package com.hwc.wms.modules.business.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hwc.wms.modules.business.dto.InventoryAdjustDTO;
import com.hwc.wms.modules.business.entity.Inventory;
import com.hwc.wms.modules.business.entity.InventoryLog;

import java.time.LocalDateTime;

/**
 * 库存 Service
 */
public interface InventoryService {

    /**
     * 分页查询库存（支持商品关键字、低库存预警筛选）
     */
    Page<Inventory> pageInventory(Page<Inventory> page, Long warehouseId, Long customerId,
                                  Long locationId, String batchNo, String keyword, Boolean warnOnly);

    /**
     * 库存调整（增加/减少，写 ADJUST 流水）
     */
    Inventory adjust(InventoryAdjustDTO dto);

    /**
     * 分页查询库存流水
     */
    Page<InventoryLog> pageLogs(Page<InventoryLog> page, String productKeyword, String changeType,
                                String orderNo, LocalDateTime startTime, LocalDateTime endTime);
}
