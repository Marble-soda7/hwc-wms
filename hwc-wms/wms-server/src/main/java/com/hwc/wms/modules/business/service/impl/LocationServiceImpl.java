package com.hwc.wms.modules.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hwc.wms.common.exception.BusinessException;
import com.hwc.wms.modules.business.entity.Location;
import com.hwc.wms.modules.business.mapper.LocationMapper;
import com.hwc.wms.modules.business.service.LocationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;

/**
 * 库位 Service 实现
 */
@Service
public class LocationServiceImpl extends ServiceImpl<LocationMapper, Location> implements LocationService {

    @Resource
    private LocationMapper locationMapper;

    @Override
    public Page<Location> pageLocations(Page<Location> page, Long warehouseId, String keyword) {
        LambdaQueryWrapper<Location> wrapper = new LambdaQueryWrapper<>();
        if (warehouseId != null) {
            wrapper.eq(Location::getWarehouseId, warehouseId);
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w
                    .like(Location::getCode, keyword)
                    .or()
                    .like(Location::getZone, keyword)
                    .or()
                    .like(Location::getAisle, keyword));
        }
        wrapper.orderByAsc(Location::getWarehouseId, Location::getCode);
        return locationMapper.selectPage(page, wrapper);
    }

    @Override
    @Transactional
    public void saveLocation(Location location) {
        // 校验必填字段
        if (location.getWarehouseId() == null) {
            throw new BusinessException("请选择所属仓库");
        }
        if (!StringUtils.hasText(location.getCode())) {
            throw new BusinessException("请输入库位编码");
        }
        // 检查同仓库下编码唯一性
        Long count = locationMapper.selectCount(
                new LambdaQueryWrapper<Location>()
                        .eq(Location::getWarehouseId, location.getWarehouseId())
                        .eq(Location::getCode, location.getCode()));
        if (count > 0) {
            throw new BusinessException("该仓库下已存在相同编码的库位");
        }
        location.setStatus(location.getStatus() != null ? location.getStatus() : 1);
        locationMapper.insert(location);
    }

    @Override
    public String getNextCode() {
        LambdaQueryWrapper<Location> wrapper = new LambdaQueryWrapper<>();
        wrapper.likeRight(Location::getCode, "KW");
        wrapper.orderByDesc(Location::getCode);
        wrapper.last("LIMIT 1");
        Location lastLocation = locationMapper.selectOne(wrapper);

        if (lastLocation == null || lastLocation.getCode() == null) {
            return "KW00001";
        }
        try {
            int nextNum = Integer.parseInt(lastLocation.getCode().substring(2)) + 1;
            return "KW" + String.format("%05d", nextNum);
        } catch (NumberFormatException e) {
            throw new BusinessException("库位编码格式异常，请联系管理员");
        }
    }

    @Override
    @Transactional
    public void updateLocation(Location location) {
        Location exist = locationMapper.selectById(location.getId());
        if (exist == null) {
            throw new BusinessException("库位不存在");
        }
        // 检查同仓库下编码唯一性（排除自身）
        Long count = locationMapper.selectCount(
                new LambdaQueryWrapper<Location>()
                        .eq(Location::getWarehouseId, location.getWarehouseId())
                        .eq(Location::getCode, location.getCode())
                        .ne(Location::getId, location.getId()));
        if (count > 0) {
            throw new BusinessException("该仓库下已存在相同编码的库位");
        }
        locationMapper.updateById(location);
    }
}
