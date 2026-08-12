package com.hwc.wms.modules.business.dto;

import com.hwc.wms.modules.business.entity.OutboundOrderItem;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 出库单提交/详情 DTO（订单信息 + 明细列表）
 */
@Data
public class OutboundOrderDTO {

    private Long id;

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

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 出库明细 */
    private List<OutboundOrderItem> items;
}
