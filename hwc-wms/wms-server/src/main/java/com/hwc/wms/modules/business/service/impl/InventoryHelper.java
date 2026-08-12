package com.hwc.wms.modules.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hwc.wms.common.exception.BusinessException;
import com.hwc.wms.modules.business.entity.Inventory;
import com.hwc.wms.modules.business.entity.InventoryLog;
import com.hwc.wms.modules.business.mapper.InventoryLogMapper;
import com.hwc.wms.modules.business.mapper.InventoryMapper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * 库存操作助手 - 库存行 upsert、锁定/解锁/加减库存，并统一写库存流水
 */
@Component
public class InventoryHelper {

    @Resource
    private InventoryMapper inventoryMapper;
    @Resource
    private InventoryLogMapper inventoryLogMapper;

    /**
     * 按 (商品, 客户, 仓库, 库位, 批次) 查询库存行，批次为空时按 IS NULL 匹配
     */
    public Inventory findRow(Long productId, Long customerId, Long warehouseId,
                             Long locationId, String batchNo) {
        LambdaQueryWrapper<Inventory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Inventory::getProductId, productId)
                .eq(Inventory::getCustomerId, customerId)
                .eq(Inventory::getWarehouseId, warehouseId)
                .eq(Inventory::getLocationId, locationId);
        if (StringUtils.hasText(batchNo)) {
            wrapper.eq(Inventory::getBatchNo, batchNo);
        } else {
            wrapper.isNull(Inventory::getBatchNo);
        }
        return inventoryMapper.selectOne(wrapper);
    }

    /**
     * 增加在库与可用库存（入库上架、调整增加），返回更新后的库存行
     */
    public Inventory addStock(Long productId, Long customerId, Long warehouseId, Long locationId,
                              String batchNo, int delta, String changeType, String orderNo, String remark) {
        Inventory row = findRow(productId, customerId, warehouseId, locationId, batchNo);
        int before = row == null ? 0 : row.getQuantity();
        if (row == null) {
            row = new Inventory();
            row.setProductId(productId);
            row.setCustomerId(customerId);
            row.setWarehouseId(warehouseId);
            row.setLocationId(locationId);
            row.setBatchNo(StringUtils.hasText(batchNo) ? batchNo : null);
            row.setQuantity(delta);
            row.setAvailableQuantity(delta);
            row.setLockedQuantity(0);
            inventoryMapper.insert(row);
        } else {
            row.setQuantity(before + delta);
            row.setAvailableQuantity(row.getAvailableQuantity() + delta);
            inventoryMapper.updateById(row);
        }
        writeLog(productId, customerId, warehouseId, changeType, delta, before, before + delta, orderNo, remark);
        return row;
    }

    /**
     * 库存调整减少（校验可用库存充足）
     */
    public void deductStock(Long productId, Long customerId, Long warehouseId, Long locationId,
                            String batchNo, int delta, String orderNo, String remark) {
        Inventory row = findRow(productId, customerId, warehouseId, locationId, batchNo);
        if (row == null) {
            throw new BusinessException("该库位无该商品库存记录");
        }
        if (row.getAvailableQuantity() < delta) {
            throw new BusinessException("可用库存不足，当前可用 " + row.getAvailableQuantity());
        }
        int before = row.getQuantity();
        row.setQuantity(before - delta);
        row.setAvailableQuantity(row.getAvailableQuantity() - delta);
        inventoryMapper.updateById(row);
        writeLog(productId, customerId, warehouseId, "ADJUST", -delta, before, before - delta, orderNo, remark);
    }

    /**
     * 锁定库存（出库单创建时），流水记录锁定数量变动
     */
    public void lockStock(Inventory row, int delta, String orderNo) {
        int before = row.getLockedQuantity();
        row.setAvailableQuantity(row.getAvailableQuantity() - delta);
        row.setLockedQuantity(before + delta);
        inventoryMapper.updateById(row);
        writeLog(row.getProductId(), row.getCustomerId(), row.getWarehouseId(),
                "LOCK", delta, before, before + delta, orderNo, "出库锁定");
    }

    /**
     * 解锁库存（出库单取消时）
     */
    public void unlockStock(Inventory row, int delta, String orderNo) {
        int before = row.getLockedQuantity();
        row.setLockedQuantity(before - delta);
        row.setAvailableQuantity(row.getAvailableQuantity() + delta);
        inventoryMapper.updateById(row);
        writeLog(row.getProductId(), row.getCustomerId(), row.getWarehouseId(),
                "UNLOCK", -delta, before, before - delta, orderNo, "取消解锁");
    }

    /**
     * 出库发货扣减（从在库与锁定数量中扣减）
     */
    public void shipStock(Inventory row, int delta, String orderNo) {
        if (row.getLockedQuantity() < delta) {
            throw new BusinessException("锁定库存不足，无法发货");
        }
        int before = row.getQuantity();
        row.setLockedQuantity(row.getLockedQuantity() - delta);
        row.setQuantity(before - delta);
        inventoryMapper.updateById(row);
        writeLog(row.getProductId(), row.getCustomerId(), row.getWarehouseId(),
                "OUTBOUND", -delta, before, before - delta, orderNo, "出库发货");
    }

    private void writeLog(Long productId, Long customerId, Long warehouseId, String changeType,
                          int changeQuantity, int before, int after, String orderNo, String remark) {
        InventoryLog log = new InventoryLog();
        log.setProductId(productId);
        log.setCustomerId(customerId);
        log.setWarehouseId(warehouseId);
        log.setChangeType(changeType);
        log.setChangeQuantity(changeQuantity);
        log.setBeforeQuantity(before);
        log.setAfterQuantity(after);
        log.setOrderNo(orderNo);
        log.setRemark(remark);
        inventoryLogMapper.insert(log);
    }
}
