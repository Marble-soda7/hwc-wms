package com.hwc.wms.modules.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hwc.wms.common.exception.BusinessException;
import com.hwc.wms.modules.business.dto.OutboundOrderDTO;
import com.hwc.wms.modules.business.entity.Inventory;
import com.hwc.wms.modules.business.entity.OutboundOrder;
import com.hwc.wms.modules.business.entity.OutboundOrderItem;
import com.hwc.wms.modules.business.entity.Product;
import com.hwc.wms.modules.business.mapper.InventoryMapper;
import com.hwc.wms.modules.business.mapper.OutboundOrderItemMapper;
import com.hwc.wms.modules.business.mapper.OutboundOrderMapper;
import com.hwc.wms.modules.business.mapper.ProductMapper;
import com.hwc.wms.modules.business.service.OutboundOrderService;
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
 * 出库单 Service 实现
 */
@Service
public class OutboundOrderServiceImpl extends ServiceImpl<OutboundOrderMapper, OutboundOrder>
        implements OutboundOrderService {

    @Resource
    private OutboundOrderItemMapper outboundOrderItemMapper;
    @Resource
    private InventoryMapper inventoryMapper;
    @Resource
    private InventoryHelper inventoryHelper;
    @Resource
    private ProductMapper productMapper;

    @Override
    public Page<OutboundOrder> pageOutbounds(Page<OutboundOrder> page, String orderNo, String status,
                                             Long warehouseId, Long customerId,
                                             LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<OutboundOrder> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(orderNo)) {
            wrapper.like(OutboundOrder::getOrderNo, orderNo);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(OutboundOrder::getStatus, status);
        }
        if (warehouseId != null) {
            wrapper.eq(OutboundOrder::getWarehouseId, warehouseId);
        }
        if (customerId != null) {
            wrapper.eq(OutboundOrder::getCustomerId, customerId);
        }
        if (startTime != null) {
            wrapper.ge(OutboundOrder::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(OutboundOrder::getCreateTime, endTime);
        }
        wrapper.orderByDesc(OutboundOrder::getCreateTime);
        return baseMapper.selectPage(page, wrapper);
    }

    @Override
    public OutboundOrderDTO getDetail(Long id) {
        OutboundOrder order = baseMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("出库单不存在");
        }
        OutboundOrderDTO dto = new OutboundOrderDTO();
        BeanUtils.copyProperties(order, dto);
        dto.setItems(outboundOrderItemMapper.selectList(
                new LambdaQueryWrapper<OutboundOrderItem>()
                        .eq(OutboundOrderItem::getOutboundOrderId, id)
                        .orderByAsc(OutboundOrderItem::getId)));
        return dto;
    }

    @Override
    public String getNextOrderNo() {
        String prefix = "CKD" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        LambdaQueryWrapper<OutboundOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(OutboundOrder::getOrderNo, prefix);
        wrapper.orderByDesc(OutboundOrder::getOrderNo);
        wrapper.last("LIMIT 1");
        OutboundOrder last = baseMapper.selectOne(wrapper);
        if (last == null || last.getOrderNo() == null) {
            return prefix + "0001";
        }
        try {
            int nextNum = Integer.parseInt(last.getOrderNo().substring(prefix.length())) + 1;
            return prefix + String.format("%04d", nextNum);
        } catch (NumberFormatException e) {
            throw new BusinessException("出库单号格式异常，请联系管理员");
        }
    }

    @Override
    @Transactional
    public void createOrder(OutboundOrderDTO dto) {
        validateOrder(dto);
        OutboundOrder order = new OutboundOrder();
        order.setOrderNo(getNextOrderNo());
        order.setCustomerId(dto.getCustomerId());
        order.setWarehouseId(dto.getWarehouseId());
        order.setOrderType(StringUtils.hasText(dto.getOrderType()) ? dto.getOrderType() : "SALE");
        order.setStatus("WAIT_PICK");
        order.setReceiverName(dto.getReceiverName());
        order.setReceiverPhone(dto.getReceiverPhone());
        order.setReceiverAddress(dto.getReceiverAddress());
        order.setRemark(dto.getRemark());
        order.setCreateUser(currentUsername());
        baseMapper.insert(order);
        saveItemsWithLock(order, dto.getItems());
    }

    @Override
    @Transactional
    public void updateOrder(Long id, OutboundOrderDTO dto) {
        OutboundOrder order = baseMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("出库单不存在");
        }
        if (!"WAIT_PICK".equals(order.getStatus())) {
            throw new BusinessException("仅待拣货状态的出库单可修改");
        }
        validateOrder(dto);
        // 1. 解锁旧明细的锁定库存
        List<OutboundOrderItem> oldItems = outboundOrderItemMapper.selectList(
                new LambdaQueryWrapper<OutboundOrderItem>().eq(OutboundOrderItem::getOutboundOrderId, id));
        for (OutboundOrderItem oldItem : oldItems) {
            unlockItem(oldItem, order);
        }
        // 2. 替换明细
        outboundOrderItemMapper.delete(new LambdaQueryWrapper<OutboundOrderItem>()
                .eq(OutboundOrderItem::getOutboundOrderId, id));
        // 3. 更新主单并重新锁定库存（库存不足时整体回滚）
        order.setCustomerId(dto.getCustomerId());
        order.setWarehouseId(dto.getWarehouseId());
        order.setOrderType(dto.getOrderType());
        order.setReceiverName(dto.getReceiverName());
        order.setReceiverPhone(dto.getReceiverPhone());
        order.setReceiverAddress(dto.getReceiverAddress());
        order.setRemark(dto.getRemark());
        baseMapper.updateById(order);
        saveItemsWithLock(order, dto.getItems());
    }

    @Override
    @Transactional
    public void pick(Long id, List<OutboundOrderItem> pickItems) {
        OutboundOrder order = baseMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("出库单不存在");
        }
        if (!"WAIT_PICK".equals(order.getStatus()) && !"PICKING".equals(order.getStatus())) {
            throw new BusinessException("当前状态不可拣货");
        }
        List<OutboundOrderItem> items = outboundOrderItemMapper.selectList(
                new LambdaQueryWrapper<OutboundOrderItem>().eq(OutboundOrderItem::getOutboundOrderId, id));
        validateSubmitItems(pickItems, items);
        for (OutboundOrderItem item : items) {
            OutboundOrderItem submitted = findSubmitItem(pickItems, item.getId());
            if (submitted == null) {
                continue;
            }
            int picked = submitted.getPickedQuantity() == null ? item.getQuantity() : submitted.getPickedQuantity();
            if (picked != item.getQuantity()) {
                throw new BusinessException("商品「" + productName(item.getProductId()) + "」拣货数量必须等于下单数量（全量拣货）");
            }
            item.setPickedQuantity(picked);
            outboundOrderItemMapper.updateById(item);
        }
        // 全部拣完置为已拣货，否则为拣货中
        boolean allPicked = items.stream().allMatch(i ->
                i.getPickedQuantity() != null && i.getPickedQuantity() >= i.getQuantity());
        order.setStatus(allPicked ? "PICKED" : "PICKING");
        baseMapper.updateById(order);
    }

    @Override
    @Transactional
    public void ship(Long id, OutboundOrder shipInfo) {
        OutboundOrder order = baseMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("出库单不存在");
        }
        if (!"PICKED".equals(order.getStatus())) {
            throw new BusinessException("仅已拣货状态的出库单可发货");
        }
        if (shipInfo == null || !StringUtils.hasText(shipInfo.getExpressCompany())
                || !StringUtils.hasText(shipInfo.getExpressNo())) {
            throw new BusinessException("请填写快递公司和快递单号");
        }
        List<OutboundOrderItem> items = outboundOrderItemMapper.selectList(
                new LambdaQueryWrapper<OutboundOrderItem>().eq(OutboundOrderItem::getOutboundOrderId, id));
        for (OutboundOrderItem item : items) {
            if (item.getPickedQuantity() == null || item.getPickedQuantity() <= 0) {
                continue;
            }
            shipItem(item, order);
        }
        order.setExpressCompany(shipInfo.getExpressCompany());
        order.setExpressNo(shipInfo.getExpressNo());
        order.setStatus("SHIPPED");
        baseMapper.updateById(order);
    }

    @Override
    @Transactional
    public void cancel(Long id) {
        OutboundOrder order = baseMapper.selectById(id);
        if (order == null) {
            throw new BusinessException("出库单不存在");
        }
        if (!"WAIT_PICK".equals(order.getStatus()) && !"PICKING".equals(order.getStatus())) {
            throw new BusinessException("仅待拣货/拣货中状态的出库单可取消");
        }
        List<OutboundOrderItem> items = outboundOrderItemMapper.selectList(
                new LambdaQueryWrapper<OutboundOrderItem>().eq(OutboundOrderItem::getOutboundOrderId, id));
        for (OutboundOrderItem item : items) {
            unlockItem(item, order);
        }
        order.setStatus("CANCELLED");
        baseMapper.updateById(order);
    }

    // ========== 私有方法 ==========

    private void validateOrder(OutboundOrderDTO dto) {
        if (dto.getCustomerId() == null) {
            throw new BusinessException("请选择货主/客户");
        }
        if (dto.getWarehouseId() == null) {
            throw new BusinessException("请选择出库仓库");
        }
        if (!StringUtils.hasText(dto.getReceiverName())) {
            throw new BusinessException("请填写收货人姓名");
        }
        if (dto.getItems() == null || dto.getItems().isEmpty()) {
            throw new BusinessException("请添加出库商品明细");
        }
        for (OutboundOrderItem item : dto.getItems()) {
            if (item.getProductId() == null) {
                throw new BusinessException("请选择出库商品");
            }
            if (item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new BusinessException("商品「" + productName(item.getProductId()) + "」的出库数量必须大于0");
            }
            if (productMapper.selectById(item.getProductId()) == null) {
                throw new BusinessException("商品「" + productName(item.getProductId()) + "」不存在");
            }
        }
    }

    /**
     * 保存明细并锁定库存：按库存行创建顺序 FIFO 分配，可用不足则抛异常回滚
     */
    private void saveItemsWithLock(OutboundOrder order, List<OutboundOrderItem> items) {
        for (OutboundOrderItem item : items) {
            OutboundOrderItem newItem = new OutboundOrderItem();
            newItem.setOutboundOrderId(order.getId());
            newItem.setProductId(item.getProductId());
            newItem.setQuantity(item.getQuantity());
            newItem.setPickedQuantity(0);
            newItem.setBatchNo(item.getBatchNo());
            newItem.setRemark(item.getRemark());
            newItem.setLocationId(lockItem(item.getProductId(), item.getQuantity(), order));
            outboundOrderItemMapper.insert(newItem);
        }
    }

    /**
     * 锁定指定商品库存，返回主分配库位ID
     */
    private Long lockItem(Long productId, int need, OutboundOrder order) {
        List<Inventory> rows = inventoryMapper.selectList(
                new LambdaQueryWrapper<Inventory>()
                        .eq(Inventory::getProductId, productId)
                        .eq(Inventory::getCustomerId, order.getCustomerId())
                        .eq(Inventory::getWarehouseId, order.getWarehouseId())
                        .gt(Inventory::getAvailableQuantity, 0)
                        .orderByAsc(Inventory::getId));
        int totalAvailable = rows.stream().mapToInt(Inventory::getAvailableQuantity).sum();
        if (totalAvailable < need) {
            throw new BusinessException("商品「" + productName(productId) + "」库存不足，可用 "
                    + totalAvailable + "，需要 " + need);
        }
        int remaining = need;
        Long primaryLocation = null;
        for (Inventory row : rows) {
            if (remaining <= 0) {
                break;
            }
            int take = Math.min(row.getAvailableQuantity(), remaining);
            inventoryHelper.lockStock(row, take, order.getOrderNo());
            if (primaryLocation == null) {
                primaryLocation = row.getLocationId();
            }
            remaining -= take;
        }
        return primaryLocation;
    }

    /**
     * 解锁指定商品在库的锁定库存（取消/修改时，按数量 FIFO 释放）
     */
    private void unlockItem(OutboundOrderItem item, OutboundOrder order) {
        int remaining = item.getQuantity();
        List<Inventory> rows = inventoryMapper.selectList(
                new LambdaQueryWrapper<Inventory>()
                        .eq(Inventory::getProductId, item.getProductId())
                        .eq(Inventory::getCustomerId, order.getCustomerId())
                        .eq(Inventory::getWarehouseId, order.getWarehouseId())
                        .gt(Inventory::getLockedQuantity, 0)
                        .orderByAsc(Inventory::getId));
        for (Inventory row : rows) {
            if (remaining <= 0) {
                break;
            }
            int take = Math.min(row.getLockedQuantity(), remaining);
            inventoryHelper.unlockStock(row, take, order.getOrderNo());
            remaining -= take;
        }
        if (remaining > 0) {
            throw new BusinessException("商品「" + productName(item.getProductId()) + "」锁定库存不足，无法解锁");
        }
    }

    /**
     * 发货扣减：从锁定库存中按数量 FIFO 扣减并写出库流水
     */
    private void shipItem(OutboundOrderItem item, OutboundOrder order) {
        int remaining = item.getPickedQuantity();
        List<Inventory> rows = inventoryMapper.selectList(
                new LambdaQueryWrapper<Inventory>()
                        .eq(Inventory::getProductId, item.getProductId())
                        .eq(Inventory::getCustomerId, order.getCustomerId())
                        .eq(Inventory::getWarehouseId, order.getWarehouseId())
                        .gt(Inventory::getLockedQuantity, 0)
                        .orderByAsc(Inventory::getId));
        for (Inventory row : rows) {
            if (remaining <= 0) {
                break;
            }
            int take = Math.min(row.getLockedQuantity(), remaining);
            inventoryHelper.shipStock(row, take, order.getOrderNo());
            remaining -= take;
        }
        if (remaining > 0) {
            throw new BusinessException("商品「" + productName(item.getProductId()) + "」锁定库存不足，无法发货");
        }
    }

    /**
     * 校验提交的明细ID都属于该订单，防止无效提交被静默忽略
     */
    private void validateSubmitItems(List<OutboundOrderItem> submitItems, List<OutboundOrderItem> orderItems) {
        if (submitItems == null) {
            return;
        }
        for (OutboundOrderItem submit : submitItems) {
            boolean found = orderItems.stream()
                    .anyMatch(i -> submit.getId() != null && submit.getId().equals(i.getId()));
            if (!found) {
                throw new BusinessException("提交的明细不存在或不属于该出库单");
            }
        }
    }

    private OutboundOrderItem findSubmitItem(List<OutboundOrderItem> pickItems, Long itemId) {
        if (pickItems == null) {
            return null;
        }
        for (OutboundOrderItem submit : pickItems) {
            if (submit.getId() != null && submit.getId().equals(itemId)) {
                return submit;
            }
        }
        return null;
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
