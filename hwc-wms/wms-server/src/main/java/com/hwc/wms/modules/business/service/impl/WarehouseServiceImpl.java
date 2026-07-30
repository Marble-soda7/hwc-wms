package com.hwc.wms.modules.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hwc.wms.common.exception.BusinessException;
import com.hwc.wms.modules.business.entity.Warehouse;
import com.hwc.wms.modules.business.mapper.WarehouseMapper;
import com.hwc.wms.modules.business.service.WarehouseService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * 仓库 Service 实现
 */
@Service
public class WarehouseServiceImpl extends ServiceImpl<WarehouseMapper, Warehouse> implements WarehouseService {

    @Resource
    private WarehouseMapper warehouseMapper;

    @Override
    public Page<Warehouse> pageWarehouses(Page<Warehouse> page, String keyword) {
        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(Warehouse::getName, keyword)
                    .or()
                    .like(Warehouse::getCode, keyword)
                    .or()
                    .like(Warehouse::getCountry, keyword));
        }
        wrapper.orderByDesc(Warehouse::getCreateTime);
        return warehouseMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public void saveWarehouse(Warehouse warehouse) {
        // 自动生成仓库编码: CK00001, CK00002 ...
        warehouse.setCode(getNextCode());
        warehouse.setStatus(warehouse.getStatus() != null ? warehouse.getStatus() : 1);
        warehouseMapper.insert(warehouse);
    }

    @Override
    public String getNextCode() {
        LambdaQueryWrapper<Warehouse> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(Warehouse::getCode, "CK");
        wrapper.orderByDesc(Warehouse::getCode);
        wrapper.last("LIMIT 1");
        Warehouse lastWarehouse = warehouseMapper.selectOne(wrapper);

        if (lastWarehouse == null || lastWarehouse.getCode() == null) {
            return "CK00001";
        }
        try {
            int nextNum = Integer.parseInt(lastWarehouse.getCode().substring(2)) + 1;
            return "CK" + String.format("%05d", nextNum);
        } catch (NumberFormatException e) {
            throw new BusinessException("仓库编码格式异常，请联系管理员");
        }
    }

    @Override
    @Transactional
    public void updateWarehouse(Warehouse warehouse) {
        Warehouse exist = warehouseMapper.selectById(warehouse.getId());
        if (exist == null) {
            throw new BusinessException("仓库不存在");
        }
        // 检查编码是否与其他仓库重复
        Long count = warehouseMapper.selectCount(
                new LambdaQueryWrapper<Warehouse>()
                        .eq(Warehouse::getCode, warehouse.getCode())
                        .ne(Warehouse::getId, warehouse.getId()));
        if (count > 0) {
            throw new BusinessException("仓库编码已被其他仓库使用");
        }
        warehouseMapper.updateById(warehouse);
    }
}
