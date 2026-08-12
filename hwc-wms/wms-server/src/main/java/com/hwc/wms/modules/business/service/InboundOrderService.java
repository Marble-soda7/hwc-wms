package com.hwc.wms.modules.business.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hwc.wms.modules.business.dto.InboundOrderDTO;
import com.hwc.wms.modules.business.entity.InboundOrder;
import com.hwc.wms.modules.business.entity.InboundOrderItem;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 入库单 Service
 */
public interface InboundOrderService extends IService<InboundOrder> {

    /**
     * 分页查询入库单
     */
    Page<InboundOrder> pageInbounds(Page<InboundOrder> page, String orderNo, String status,
                                    Long warehouseId, Long customerId,
                                    LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 入库单详情（含明细）
     */
    InboundOrderDTO getDetail(Long id);

    /**
     * 获取下一个入库单号（RKD+日期+4位序号）
     */
    String getNextOrderNo();

    /**
     * 创建入库单（状态: 待收货）
     */
    void createOrder(InboundOrderDTO dto);

    /**
     * 修改入库单（仅待收货状态）
     */
    void updateOrder(Long id, InboundOrderDTO dto);

    /**
     * 收货（待收货 -> 已收货，记录实际数量，不影响库存）
     */
    void receive(Long id, List<InboundOrderItem> receiveItems);

    /**
     * 上架（已收货 -> 已完成，选择库位并增加库存）
     */
    void putaway(Long id, List<InboundOrderItem> putawayItems);

    /**
     * 取消入库单（仅待收货/已收货状态）
     */
    void cancel(Long id);
}
