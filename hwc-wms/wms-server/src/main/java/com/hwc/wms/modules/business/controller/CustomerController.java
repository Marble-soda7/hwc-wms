package com.hwc.wms.modules.business.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hwc.wms.common.result.PageResult;
import com.hwc.wms.common.result.Result;
import com.hwc.wms.modules.business.entity.Customer;
import com.hwc.wms.modules.business.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 客户管理
 */
@Tag(name = "客户管理")
@RestController
@RequestMapping("/api/customer")
public class CustomerController {

    @Resource
    private CustomerService customerService;

    @Operation(summary = "分页查询客户")
    @GetMapping("/page")
    public Result<PageResult<Customer>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword) {
        Page<Customer> p = new Page<>(page, pageSize);
        Page<Customer> result = customerService.pageCustomers(p, keyword);
        return Result.ok(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @Operation(summary = "客户列表（不分页，供下拉选择）")
    @GetMapping("/list")
    public Result<List<Customer>> list() {
        return Result.ok(customerService.list());
    }

    @Operation(summary = "客户详情")
    @GetMapping("/{id}")
    public Result<Customer> getById(@PathVariable Long id) {
        return Result.ok(customerService.getById(id));
    }

    @Operation(summary = "新增客户（仅管理员）")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Result<Void> save(@RequestBody Customer customer) {
        customerService.saveCustomer(customer);
        return Result.ok();
    }

    @Operation(summary = "获取下一个客户编码")
    @GetMapping("/next-code")
    public Result<String> nextCode() {
        return Result.ok(customerService.getNextCode());
    }

    @Operation(summary = "修改客户（仅管理员）")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping
    public Result<Void> update(@RequestBody Customer customer) {
        customerService.updateCustomer(customer);
        return Result.ok();
    }

    @Operation(summary = "删除客户（仅管理员）")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{ids}")
    public Result<Void> delete(@PathVariable List<Long> ids) {
        customerService.removeByIds(ids);
        return Result.ok();
    }
}
