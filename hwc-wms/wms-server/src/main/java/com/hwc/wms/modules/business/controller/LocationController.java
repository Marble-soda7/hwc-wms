package com.hwc.wms.modules.business.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hwc.wms.common.result.PageResult;
import com.hwc.wms.common.result.Result;
import com.hwc.wms.modules.business.entity.Location;
import com.hwc.wms.modules.business.service.LocationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 库位管理
 */
@Tag(name = "库位管理")
@RestController
@RequestMapping("/api/location")
public class LocationController {

    @Resource
    private LocationService locationService;

    @Operation(summary = "分页查询库位")
    @GetMapping("/page")
    public Result<PageResult<Location>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String keyword) {
        Page<Location> p = new Page<>(page, pageSize);
        Page<Location> result = locationService.pageLocations(p, warehouseId, keyword);
        return Result.ok(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @Operation(summary = "库位列表（不分页，可指定仓库筛选）")
    @GetMapping("/list")
    public Result<List<Location>> list(@RequestParam(required = false) Long warehouseId) {
        if (warehouseId != null) {
            return Result.ok(locationService.lambdaQuery()
                    .eq(Location::getWarehouseId, warehouseId)
                    .eq(Location::getStatus, 1)
                    .list());
        }
        return Result.ok(locationService.list());
    }

    @Operation(summary = "库位详情")
    @GetMapping("/{id}")
    public Result<Location> getById(@PathVariable Long id) {
        return Result.ok(locationService.getById(id));
    }

    @Operation(summary = "获取下一个库位编码")
    @GetMapping("/next-code")
    public Result<String> nextCode() {
        return Result.ok(locationService.getNextCode());
    }

    @Operation(summary = "新增库位（仅管理员）")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Result<Void> save(@RequestBody Location location) {
        locationService.saveLocation(location);
        return Result.ok();
    }

    @Operation(summary = "修改库位（仅管理员）")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping
    public Result<Void> update(@RequestBody Location location) {
        locationService.updateLocation(location);
        return Result.ok();
    }

    @Operation(summary = "删除库位（仅管理员）")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{ids}")
    public Result<Void> delete(@PathVariable List<Long> ids) {
        locationService.removeByIds(ids);
        return Result.ok();
    }
}
