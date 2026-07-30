package com.hwc.wms.modules.business.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hwc.wms.common.result.PageResult;
import com.hwc.wms.common.result.Result;
import com.hwc.wms.modules.business.entity.Warehouse;
import com.hwc.wms.modules.business.service.WarehouseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 仓库管理
 */
@Tag(name = "仓库管理")
@RestController
@RequestMapping("/api/warehouse")
public class WarehouseController {

    @Resource
    private WarehouseService warehouseService;

    @Operation(summary = "分页查询仓库")
    @GetMapping("/page")
    public Result<PageResult<Warehouse>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword) {
        Page<Warehouse> p = new Page<>(page, pageSize);
        Page<Warehouse> result = warehouseService.pageWarehouses(p, keyword);
        return Result.ok(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @Operation(summary = "仓库列表（不分页，供下拉选择）")
    @GetMapping("/list")
    public Result<List<Warehouse>> list() {
        return Result.ok(warehouseService.list());
    }

    @Operation(summary = "仓库详情")
    @GetMapping("/{id}")
    public Result<Warehouse> getById(@PathVariable Long id) {
        return Result.ok(warehouseService.getById(id));
    }

    @Operation(summary = "获取下一个仓库编码")
    @GetMapping("/next-code")
    public Result<String> nextCode() {
        return Result.ok(warehouseService.getNextCode());
    }

    @Operation(summary = "新增仓库（仅管理员）")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Result<Void> save(@RequestBody Warehouse warehouse) {
        warehouseService.saveWarehouse(warehouse);
        return Result.ok();
    }

    @Operation(summary = "修改仓库（仅管理员）")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping
    public Result<Void> update(@RequestBody Warehouse warehouse) {
        warehouseService.updateWarehouse(warehouse);
        return Result.ok();
    }

    @Operation(summary = "删除仓库（仅管理员）")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{ids}")
    public Result<Void> delete(@PathVariable List<Long> ids) {
        warehouseService.removeByIds(ids);
        return Result.ok();
    }
}
