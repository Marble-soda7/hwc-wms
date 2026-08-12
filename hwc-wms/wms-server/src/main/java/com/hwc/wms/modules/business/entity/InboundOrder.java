package com.hwc.wms.modules.business.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hwc.wms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * 入库单实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("inbound_order")
public class InboundOrder extends BaseEntity {

    /** 入库单号 */
    private String orderNo;

    /** 客户（货主）ID */
    private Long customerId;

    /** 仓库ID */
    private Long warehouseId;

    /** 入库类型: PURCHASE-采购入库 RETURN-退货入库 TRANSFER-调拨入库 */
    private String orderType;

    /** 状态: PENDING-待收货 RECEIVED-已收货 COMPLETED-已完成 CANCELLED-已取消 */
    private String status;

    /** 预计到货时间 */
    private LocalDateTime expectArriveTime;

    /** 实际到货时间 */
    private LocalDateTime actualArriveTime;

    /** 备注 */
    private String remark;

    /** 创建人 */
    private String createUser;
}
