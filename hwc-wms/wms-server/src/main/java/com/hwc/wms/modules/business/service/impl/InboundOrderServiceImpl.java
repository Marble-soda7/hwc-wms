package com.hwc.wms.modules.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hwc.wms.common.exception.BusinessException;
import com.hwc.wms.modules.business.dto.InboundOrderDTO;
import com.hwc.wms.modules.business.entity.InboundOrder;
import com.hwc.wms.modules.business.entity.InboundOrderItem;
import com.hwc.wms.modules.business.entity.Location;
import com.hwc.wms.modules.business.entity.Product;
import com.hwc.wms.modules.business.mapper.InboundOrderItemMapper;
import com.hwc.wms.modules.business.mapper.InboundOrderMapper;
import com.hwc.wms.modules.business.mapper.LocationMapper;
import com.hwc.wms.modules.business.mapper.ProductMapper;
import com.hwc.wms.modules.business.service.InboundOrderService;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 入库单 Service 实现
 */
@Service
public class InboundOrderServiceImpl extends ServiceImpl<InboundOrderMapper, InboundOrder>
        implements InboundOrderService {

    @Resource
    private InboundOrderItemMapper inboundOrderItemMapper;
    @Resource
    private InventoryHelper inventoryHelper;
    @Resource
    private ProductMapper productMapper;
    @Resource
    private LocationMapper locationMapper;

    @Override
    public Page<InboundOrder> pageInbounds(Page<InboundOrder> page, String orderNo, String status,
                                           Long warehouseId, Long customerId,
                                           LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<InboundOrder> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(orderNo)) {
            wrapper.like(InboundOrder::getOrderNo, orderNo);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(InboundOrder::getStatus, status);
        }
        if (warehouseId != null) {
            wrapper.eq(InboundOrder::getWarehouseId, warehouseId);
        }
        if (customerId != null) {
            wrapper.eq(InboundOrder::getCustomerId, customerId);
        }
        if (startTime != null) {
            wrapper.ge(InboundOrder::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(InboundOrder::getCreateTime, endTime);
        }
        wrapper.orderByDesc(InboundOrder::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public InboundOrderDTO getDetail(Long id) {
        InboundOrder order = baseMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("入库单不存在");
        }
        InboundOrderDTO dto = new InboundOrderDTO();
        BeanUtils.copyProperties(order, dto);
        dto.setItems(inboundOrderItemMapper.selectList(
                new LambdaQueryWrapper<InboundOrderItem>()
                        .eq(InboundOrderItem::getInboundOrderId, id)
                        .orderByAsc(InboundOrderItem::getId)));
        return dto;
    }

    @Override
    public String getNextOrderNo() {
        String prefix = "RKD" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        LambdaQueryWrapper<InboundOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(InboundOrder::getOrderNo, prefix);
        wrapper.orderByDesc(InboundOrder::getOrderNo);
        wrapper.last("LIMIT 1");
        InboundOrder last = baseMapper.selectOne(wrapper);
        if (last == null || last.getOrderNo() == null) {
            return prefix + "0001";
        }
        try {
            int nextNum = Integer.parseInt(last.getOrderNo().substring(prefix.length())) + 1;
            return prefix + String.format("%04d", nextNum);
        } catch (NumberFormatException e) {
            throw new BusinessException("入库单号格式异常，请联系管理员");
        }
    }

    @Override
    @Transactional
    public void createOrder(InboundOrderDTO dto) {
        validateOrder(dto);
        InboundOrder order = new InboundOrder();
        order.setOrderNo(getNextOrderNo());
        order.setCustomerId(dto.getCustomerId());
        order.setWarehouseId(dto.getWarehouseId());
        order.setOrderType(StringUtils.hasText(dto.getOrderType()) ? dto.getOrderType() : "PURCHASE");
        order.setStatus("PENDING");
        order.setExpectArriveTime(dto.getExpectArriveTime());
        order.setRemark(dto.getRemark());
        order.setCreateUser(currentUsername());
        baseMapper.insert(order);
        saveItems(order.getId(), dto.getItems());
    }

    @Override
    @Transactional
    public void updateOrder(Long id, InboundOrderDTO dto) {
        InboundOrder order = baseMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("入库单不存在");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException("仅待收货状态的入库单可修改");
        }
        validateOrder(dto);
        order.setCustomerId(dto.getCustomerId());
        order.setWarehouseId(dto.getWarehouseId());
        order.setOrderType(dto.getOrderType());
        order.setExpectArriveTime(dto.getExpectArriveTime());
        order.setRemark(dto.getRemark());
        baseMapper.updateById(order);
        // 替换明细
        inboundOrderItemMapper.delete(new LambdaQueryWrapper<InboundOrderItem>()
                .eq(InboundOrderItem::getInboundOrderId, id));
        saveItems(id, dto.getItems());
    }

    @Override
    @Transactional
    public void receive(Long id, List<InboundOrderItem> receiveItems) {
        InboundOrder order = baseMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("入库单不存在");
        }
        if (!"PENDING".equals(order.getStatus())) {
            throw new BusinessException("仅待收货状态的入库单可收货");
        }
        List<InboundOrderItem> items = inboundOrderItemMapper.selectList(
                new LambdaQueryWrapper<InboundOrderItem>().eq(InboundOrderItem::getInboundOrderId, id));
        validateSubmitItems(receiveItems, items);
        for (InboundOrderItem item : items) {
            Integer actual = findSubmitQuantity(receiveItems, item.getId());
            if (actual == null) {
                // 未提交该明细则按预期数量收货
                actual = item.getExpectQuantity();
            }
            if (actual < 0) {
                throw new BusinessException("收货数量不能为负数");
            }
            item.setActualQuantity(actual);
            inboundOrderItemMapper.updateById(item);
        }
        order.setActualArriveTime(LocalDateTime.now());
        order.setStatus("RECEIVED");
        baseMapper.updateById(order);
    }

    @Override
    @Transactional
    public void putaway(Long id, List<InboundOrderItem> putawayItems) {
        InboundOrder order = baseMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("入库单不存在");
        }
        if (!"RECEIVED".equals(order.getStatus())) {
            throw new BusinessException("仅已收货状态的入库单可上架");
        }
        List<InboundOrderItem> items = inboundOrderItemMapper.selectList(
                new LambdaQueryWrapper<InboundOrderItem>().eq(InboundOrderItem::getInboundOrderId, id));
        validateSubmitItems(putawayItems, items);
        for (InboundOrderItem item : items) {
            int actual = item.getActualQuantity() == null ? 0 : item.getActualQuantity();
            if (actual <= 0) {
                continue;
            }
            // 上架库位：优先取提交值，其次取明细已有库位
            Long locationId = findSubmitLocation(putawayItems, item.getId());
            if (locationId == null) {
                locationId = item.getLocationId();
            }
            if (locationId == null) {
                throw new BusinessException("商品「" + productName(item.getProductId()) + "」请选择上架库位");
            }
            validateLocation(order.getWarehouseId(), locationId);
            item.setLocationId(locationId);
            inboundOrderItemMapper.updateById(item);
            inventoryHelper.addStock(item.getProductId(), order.getCustomerId(), order.getWarehouseId(),
                    locationId, item.getBatchNo(), actual, "INBOUND", order.getOrderNo(), "入库上架");
        }
        order.setStatus("COMPLETED");
        baseMapper.updateById(order);
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        InboundOrder order = baseMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("入库单不存在");
        }
        if (!"PENDING".equals(order.getStatus()) && !"RECEIVED".equals(order.getStatus())) {
            throw new BusinessException("仅待收货/已收货状态的入库单可取消");
        }
        order.setStatus("CANCELLED");
        baseMapper.updateById(order);
    }

    // ========== 私有方法 ==========

    private void validateOrder(InboundOrderDTO dto) {
        if (dto.getCustomerId() == null) {
            throw new BusinessException("请选择货主/客户");
        }
        if (dto.getWarehouseId() == null) {
            throw new BusinessException("请选择入库仓库");
        }
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BusinessException("请添加入库商品明细");
        }
        for (InboundOrderItem item : dto.getItems()) {
            if (item.getProductId() == null) {
                throw new BusinessException("请选择入库商品");
            }
            if (item.getExpectQuantity() == null || item.getExpectQuantity() <= 0) {
                throw new BusinessException("商品「" + productName(item.getProductId()) + "」的预计数量必须大于0");
            }
            if (productMapper.selectById(item.getProductId()) == null) {
                throw new BusinessException("商品「" + productName(item.getProductId()) + "」不存在");
            }
        }
    }

    private void saveItems(Long orderId, List<InboundOrderItem> items) {
        for (InboundOrderItem item : items) {
            InboundOrderItem newItem = new InboundOrderItem();
            newItem.setInboundOrderId(orderId);
            newItem.setProductId(item.getProductId());
            newItem.setExpectQuantity(item.getExpectQuantity());
            newItem.setActualQuantity(0);
            newItem.setLocationId(item.getLocationId());
            newItem.setBatchNo(item.getBatchNo());
            newItem.setRemark(item.getRemark());
            inboundOrderItemMapper.insert(newItem);
        }
    }

    /**
     * 校验提交的明细ID都属于该订单，防止无效提交被静默忽略
     */
    private void validateSubmitItems(List<InboundOrderItem> submitItems, List<InboundOrderItem> orderItems) {
        if (submitItems == null) {
            return;
        }
        for (InboundOrderItem submit : submitItems) {
            boolean found = orderItems.stream()
                    .anyMatch(i -> submit.getId() != null && submit.getId().equals(i.getId()));
            if (!found) {
                throw new BusinessException("提交的明细不存在或不属于该入库单");
            }
        }
    }

    private Integer findSubmitQuantity(List<InboundOrderItem> submitItems, Long itemId) {
        if (submitItems == null) {
            return null;
        }
        for (InboundOrderItem submit : submitItems) {
            if (submit.getId() != null && submit.getId().equals(itemId)) {
                return submit.getActualQuantity();
            }
        }
        return null;
    }

    private Long findSubmitLocation(List<InboundOrderItem> submitItems, Long itemId) {
        if (submitItems == null) {
            return null;
        }
        for (InboundOrderItem submit : submitItems) {
            if (submit.getId() != null && submit.getId().equals(itemId)) {
                return submit.getLocationId();
            }
        }
        return null;
    }

    private void validateLocation(Long warehouseId, Long locationId) {
        Location location = locationMapper.selectById(locationId);
        if (location == null) {
            throw new BusinessException("上架库位不存在");
        }
        if (!warehouseId.equals(location.getWarehouseId())) {
            throw new BusinessException("上架库位不属于该仓库");
        }
    }

    private String productName(Long productId) {
        Product product = productMapper.selectById(productId);
        return product == null ? "未知商品" : product.getName();
    }

    private String currentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null ? authentication.getName() : "system";
    }
}
