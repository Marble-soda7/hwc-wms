package com.hwc.wms.modules.business.entity;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 库存实体（无逻辑删除列，不继承 BaseEntity）
 */
@Data
@TableName("inventory")
public class Inventory implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

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

    /** 在库数量 */
    private Integer quantity;

    /** 可用数量 */
    private Integer availableQuantity;

    /** 锁定数量（已锁定未发货） */
    private Integer lockedQuantity;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
