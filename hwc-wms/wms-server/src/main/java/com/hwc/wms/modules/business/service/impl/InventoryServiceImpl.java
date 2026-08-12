package com.hwc.wms.modules.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hwc.wms.common.exception.BusinessException;
import com.hwc.wms.modules.business.dto.InventoryAdjustDTO;
import com.hwc.wms.modules.business.entity.Inventory;
import com.hwc.wms.modules.business.entity.InventoryLog;
import com.hwc.wms.modules.business.entity.Product;
import com.hwc.wms.modules.business.mapper.InventoryLogMapper;
import com.hwc.wms.modules.business.mapper.InventoryMapper;
import com.hwc.wms.modules.business.mapper.ProductMapper;
import com.hwc.wms.modules.business.service.InventoryService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 库存 Service 实现
 */
@Service
public class InventoryServiceImpl implements InventoryService {

    @Resource
    private InventoryMapper inventoryMapper;
    @Resource
    private InventoryLogMapper inventoryLogMapper;
    @Resource
    private InventoryHelper inventoryHelper;
    @Resource
    private ProductMapper productMapper;

    @Override
    public Page<Inventory> pageInventory(Page<Inventory> page, Long warehouseId, Long customerId,
                                         Long locationId, String batchNo, String keyword, Boolean warnOnly) {
        return inventoryMapper.selectInventoryPage(page, warehouseId, customerId, locationId, batchNo, keyword, warnOnly);
    }

    @Override
    @Transactional
    public Inventory adjust(InventoryAdjustDTO dto) {
        if (dto.getProductId() == null) {
            throw new BusinessException("请选择调整商品");
        }
        if (dto.getCustomerId() == null) {
            throw new BusinessException("请选择货主/客户");
        }
        if (dto.getWarehouseId() == null) {
            throw new BusinessException("请选择仓库");
        }
        if (dto.getLocationId() == null) {
            throw new BusinessException("请选择库位");
        }
        if (dto.getQuantity() == null || dto.getQuantity() <= 0) {
            throw new BusinessException("调整数量必须大于0");
        }
        if (!"INCREASE".equals(dto.getType()) && !"DECREASE".equals(dto.getType())) {
            throw new BusinessException("请选择调整方式");
        }
        if (!StringUtils.hasText(dto.getReason())) {
            throw new BusinessException("请填写调整原因");
        }
        Product product = productMapper.selectById(dto.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if ("INCREASE".equals(dto.getType())) {
            return inventoryHelper.addStock(dto.getProductId(), dto.getCustomerId(), dto.getWarehouseId(),
                    dto.getLocationId(), dto.getBatchNo(), dto.getQuantity(), "ADJUST", null, dto.getReason());
        }
        inventoryHelper.deductStock(dto.getProductId(), dto.getCustomerId(), dto.getWarehouseId(),
                dto.getLocationId(), dto.getBatchNo(), dto.getQuantity(), null, dto.getReason());
        return inventoryHelper.findRow(dto.getProductId(), dto.getCustomerId(), dto.getWarehouseId(),
                dto.getLocationId(), dto.getBatchNo());
    }

    @Override
    public Page<InventoryLog> pageLogs(Page<InventoryLog> page, String productKeyword, String changeType,
                                       String orderNo, LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<InventoryLog> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(changeType)) {
            wrapper.eq(InventoryLog::getChangeType, changeType);
        }
        if (StringUtils.hasText(orderNo)) {
            wrapper.like(InventoryLog::getOrderNo, orderNo);
        }
        if (startTime != null) {
            wrapper.ge(InventoryLog::getCreateTime, startTime);
        }
        if (endTime != null) {
            wrapper.le(InventoryLog::getCreateTime, endTime);
        }
        if (StringUtils.hasText(productKeyword)) {
            List<Long> productIds = productMapper.selectList(
                            new LambdaQueryWrapper<Product>()
                                    .like(Product::getName, productKeyword)
                                    .or()
                                    .like(Product::getSkuCode, productKeyword)
                                    .or()
                                    .like(Product::getBarcode, productKeyword))
                    .stream().map(Product::getId).collect(Collectors.toList());
            if (productIds.isEmpty()) {
                return new Page<>(page.getCurrent(), page.getSize());
            }
            wrapper.in(InventoryLog::getProductId, productIds);
        }
        wrapper.orderByDesc(InventoryLog::getCreateTime);
        return inventoryLogMapper.selectPage(page, wrapper);
    }
}
