package com.hwc.wms.modules.business.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hwc.wms.common.result.PageResult;
import com.hwc.wms.common.result.Result;
import com.hwc.wms.modules.business.dto.InventoryAdjustDTO;
import com.hwc.wms.modules.business.entity.Inventory;
import com.hwc.wms.modules.business.entity.InventoryLog;
import com.hwc.wms.modules.business.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 库存管理
 */
@Tag(name = "库存管理")
@RestController
@RequestMapping("/api/inventory")
public class InventoryController {

    @Resource
    private InventoryService inventoryService;

    @Operation(summary = "分页查询库存")
    @GetMapping("/page")
    public Result<PageResult<Inventory>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) String batchNo,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean warnOnly) {
        Page<Inventory> p = new Page<>(page, pageSize);
        Page<Inventory> result = inventoryService.pageInventory(p, warehouseId, customerId, locationId, batchNo, keyword, warnOnly);
        return Result.ok(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @Operation(summary = "库存调整（仅管理员）")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/adjust")
    public Result<Inventory> adjust(@RequestBody InventoryAdjustDTO dto) {
        return Result.ok(inventoryService.adjust(dto));
    }

    @Operation(summary = "分页查询库存流水")
    @GetMapping("/logs/page")
    public Result<PageResult<InventoryLog>> logsPage(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String productKeyword,
            @RequestParam(required = false) String changeType,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        Page<InventoryLog> p = new Page<>(page, pageSize);
        Page<InventoryLog> result = inventoryService.pageLogs(p, productKeyword, changeType, orderNo, startTime, endTime);
        return Result.ok(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }
}
