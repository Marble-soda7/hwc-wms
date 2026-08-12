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
 * 入库单明细实体（无逻辑删除列，不继承 BaseEntity）
 */
@Data
@TableName("inbound_order_item")
public class InboundOrderItem implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 入库单ID */
    private Long inboundOrderId;

    /** 商品ID */
    private Long productId;

    /** 预计数量 */
    private Integer expectQuantity;

    /** 实际收货数量 */
    private Integer actualQuantity;

    /** 上架库位ID */
    private Long locationId;

    /** 批次号 */
    private String batchNo;

    /** 备注 */
    private String remark;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
}
