package com.hwc.wms.modules.business.dto;

import lombok.Data;

/**
 * 库存调整 DTO
 */
@Data
public class InventoryAdjustDTO {

    /** 商品ID */
    private Long productId;

    /** 客户（货主）ID */
    private Long customerId;

    /** 仓库ID */
    private Long warehouseId;

    /** 库位ID */
    private Long locationId;

    /** 批次号 */
    private String batchNo;

    /** 调整方式: INCREASE-增加 DECREASE-减少 */
    private String type;

    /** 调整数量 */
    private Integer quantity;

    /** 调整原因（如: 盘点调整/报损/其他） */
    private String reason;
}
