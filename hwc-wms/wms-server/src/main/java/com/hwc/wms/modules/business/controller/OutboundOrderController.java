package com.hwc.wms.modules.business.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hwc.wms.common.result.PageResult;
import com.hwc.wms.common.result.Result;
import com.hwc.wms.modules.business.dto.OutboundOrderDTO;
import com.hwc.wms.modules.business.entity.OutboundOrder;
import com.hwc.wms.modules.business.entity.OutboundOrderItem;
import com.hwc.wms.modules.business.service.OutboundOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 出库管理
 */
@Tag(name = "出库管理")
@RestController
@RequestMapping("/api/outbound")
public class OutboundOrderController {

    @Resource
    private OutboundOrderService outboundOrderService;

    @Operation(summary = "分页查询出库单")
    @GetMapping("/page")
    public Result<PageResult<OutboundOrder>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        Page<OutboundOrder> p = new Page<>(page, pageSize);
        Page<OutboundOrder> result = outboundOrderService.pageOutbounds(p, orderNo, status, warehouseId, customerId, startTime, endTime);
        return Result.ok(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @Operation(summary = "出库单详情（含明细）")
    @GetMapping("/{id}")
    public Result<OutboundOrderDTO> getById(@PathVariable Long id) {
        return Result.ok(outboundOrderService.getDetail(id));
    }

    @Operation(summary = "获取下一个出库单号")
    @GetMapping("/next-code")
    public Result<String> nextCode() {
        return Result.ok(outboundOrderService.getNextOrderNo());
    }

    @Operation(summary = "创建出库单（仅管理员，自动锁定库存）")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Result<Void> create(@RequestBody OutboundOrderDTO dto) {
        outboundOrderService.createOrder(dto);
        return Result.ok();
    }

    @Operation(summary = "修改出库单（仅管理员，待拣货状态，重新锁定库存）")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody OutboundOrderDTO dto) {
        outboundOrderService.updateOrder(id, dto);
        return Result.ok();
    }

    @Operation(summary = "拣货（仅管理员，全量拣货）")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/pick")
    public Result<Void> pick(@PathVariable Long id, @RequestBody(required = false) List<OutboundOrderItem> items) {
        outboundOrderService.pick(id, items);
        return Result.ok();
    }

    @Operation(summary = "发货（仅管理员，扣减库存并记录快递信息）")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/ship")
    public Result<Void> ship(@PathVariable Long id, @RequestBody OutboundOrder shipInfo) {
        outboundOrderService.ship(id, shipInfo);
        return Result.ok();
    }

    @Operation(summary = "取消出库单（仅管理员，解锁库存）")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        outboundOrderService.cancel(id);
        return Result.ok();
    }
}
