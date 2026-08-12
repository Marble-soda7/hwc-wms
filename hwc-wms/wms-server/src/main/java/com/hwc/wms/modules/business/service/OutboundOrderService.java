package com.hwc.wms.modules.business.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hwc.wms.modules.business.dto.OutboundOrderDTO;
import com.hwc.wms.modules.business.entity.OutboundOrder;
import com.hwc.wms.modules.business.entity.OutboundOrderItem;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 出库单 Service
 */
public interface OutboundOrderService extends IService<OutboundOrder> {

    /**
     * 分页查询出库单
     */
    Page<OutboundOrder> pageOutbounds(Page<OutboundOrder> page, String orderNo, String status,
                                      Long warehouseId, Long customerId,
                                      LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 出库单详情（含明细）
     */
    OutboundOrderDTO getDetail(Long id);

    /**
     * 获取下一个出库单号（CKD+日期+4位序号）
     */
    String getNextOrderNo();

    /**
     * 创建出库单（自动锁定库存，状态: 待拣货）
     */
    void createOrder(OutboundOrderDTO dto);

    /**
     * 修改出库单（仅待拣货状态，重新锁定库存）
     */
    void updateOrder(Long id, OutboundOrderDTO dto);

    /**
     * 拣货（全量拣货：拣货数量必须等于下单数量）
     */
    void pick(Long id, List<OutboundOrderItem> pickItems);

    /**
     * 发货（已拣货 -> 已发货，扣减库存并记录快递信息）
     */
    void ship(Long id, OutboundOrder shipInfo);

    /**
     * 取消出库单（仅待拣货/拣货中状态，解锁库存）
     */
    void cancel(Long id);
}
