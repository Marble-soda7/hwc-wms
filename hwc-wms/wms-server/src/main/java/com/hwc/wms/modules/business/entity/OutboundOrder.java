package com.hwc.wms.modules.business.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.hwc.wms.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 出库单实体
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("outbound_order")
public class OutboundOrder extends BaseEntity {

    /** 出库单号 */
    private String orderNo;

    /** 客户（货主）ID */
    private Long customerId;

    /** 仓库ID */
    private Long warehouseId;

    /** 出库类型: SALE-销售出库 TRANSFER-调拨出库 RETURN-退货出库 */
    private String orderType;

    /** 状态: WAIT_PICK-待拣货 PICKING-拣货中 PICKED-已拣货 SHIPPED-已发货 CANCELLED-已取消 */
    private String status;

    /** 收货人姓名 */
    private String receiverName;

    /** 收货人电话 */
    private String receiverPhone;

    /** 收货人地址 */
    private String receiverAddress;

    /** 快递公司 */
    private String expressCompany;

    /** 快递单号 */
    private String expressNo;

    /** 备注 */
    private String remark;

    /** 创建人 */
    private String createUser;
}
