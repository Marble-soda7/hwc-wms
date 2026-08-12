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
 * 库存流水实体（无逻辑删除列，不继承 BaseEntity）
 */
@Data
@TableName("inventory_log")
public class InventoryLog implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 商品ID */
    private Long productId;

    /** 客户ID */
    private Long customerId;

    /** 仓库ID */
    private Long warehouseId;

    /** 变动类型: INBOUND-入库 OUTBOUND-出库 ADJUST-盘点调整 LOCK-锁定 UNLOCK-解锁 */
    private String changeType;

    /** 变动数量（正数为增加，负数为减少） */
    private Integer changeQuantity;

    /** 变动前数量 */
    private Integer beforeQuantity;

    /** 变动后数量 */
    private Integer afterQuantity;

    /** 关联业务单号 */
    private String orderNo;

    /** 备注 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
