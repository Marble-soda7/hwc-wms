package com.hwc.wms.modules.business.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hwc.wms.common.result.PageResult;
import com.hwc.wms.common.result.Result;
import com.hwc.wms.modules.business.dto.InboundOrderDTO;
import com.hwc.wms.modules.business.entity.InboundOrder;
import com.hwc.wms.modules.business.entity.InboundOrderItem;
import com.hwc.wms.modules.business.service.InboundOrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 入库管理
 */
@Tag(name = "入库管理")
@RestController
@RequestMapping("/api/inbound")
public class InboundOrderController {

    @Resource
    private InboundOrderService inboundOrderService;

    @Operation(summary = "分页查询入库单")
    @GetMapping("/page")
    public Result<PageResult<InboundOrder>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String orderNo,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long customerId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        Page<InboundOrder> p = new Page<>(page, pageSize);
        Page<InboundOrder> result = inboundOrderService.pageInbounds(p, orderNo, status, warehouseId, customerId, startTime, endTime);
        return Result.ok(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @Operation(summary = "入库单详情（含明细）")
    @GetMapping("/{id}")
    public Result<InboundOrderDTO> getById(@PathVariable Long id) {
        return Result.ok(inboundOrderService.getDetail(id));
    }

    @Operation(summary = "获取下一个入库单号")
    @GetMapping("/next-code")
    public Result<String> nextCode() {
        return Result.ok(inboundOrderService.getNextOrderNo());
    }

    @Operation(summary = "创建入库单（仅管理员）")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Result<Void> create(@RequestBody InboundOrderDTO dto) {
        inboundOrderService.createOrder(dto);
        return Result.ok();
    }

    @Operation(summary = "修改入库单（仅管理员，待收货状态）")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody InboundOrderDTO dto) {
        inboundOrderService.updateOrder(id, dto);
        return Result.ok();
    }

    @Operation(summary = "收货（仅管理员）")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/receive")
    public Result<Void> receive(@PathVariable Long id, @RequestBody(required = false) List<InboundOrderItem> items) {
        inboundOrderService.receive(id, items);
        return Result.ok();
    }

    @Operation(summary = "上架（仅管理员，选择库位并增加库存）")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/putaway")
    public Result<Void> putaway(@PathVariable Long id, @RequestBody(required = false) List<InboundOrderItem> items) {
        inboundOrderService.putaway(id, items);
        return Result.ok();
    }

    @Operation(summary = "取消入库单（仅管理员）")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/{id}/cancel")
    public Result<Void> cancel(@PathVariable Long id) {
        inboundOrderService.cancel(id);
        return Result.ok();
    }
}
