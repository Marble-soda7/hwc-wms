package com.hwc.wms.modules.business.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hwc.wms.common.result.PageResult;
import com.hwc.wms.common.result.Result;
import com.hwc.wms.modules.business.entity.Product;
import com.hwc.wms.modules.business.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 商品管理
 */
@Tag(name = "商品管理")
@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Resource
    private ProductService productService;

    @Operation(summary = "分页查询商品")
    @GetMapping("/page")
    public Result<PageResult<Product>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String keyword) {
        Page<Product> p = new Page<>(page, pageSize);
        Page<Product> result = productService.pageProducts(p, keyword);
        return Result.ok(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @Operation(summary = "商品列表（不分页，供下拉选择）")
    @GetMapping("/list")
    public Result<List<Product>> list() {
        return Result.ok(productService.list());
    }

    @Operation(summary = "商品详情")
    @GetMapping("/{id}")
    public Result<Product> getById(@PathVariable Long id) {
        return Result.ok(productService.getById(id));
    }

    @Operation(summary = "获取下一个SKU编码")
    @GetMapping("/next-code")
    public Result<String> nextCode() {
        return Result.ok(productService.getNextSkuCode());
    }

    @Operation(summary = "新增商品（仅管理员）")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Result<Void> save(@RequestBody Product product) {
        productService.saveProduct(product);
        return Result.ok();
    }

    @Operation(summary = "修改商品（仅管理员）")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping
    public Result<Void> update(@RequestBody Product product) {
        productService.updateProduct(product);
        return Result.ok();
    }

    @Operation(summary = "删除商品（仅管理员）")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{ids}")
    public Result<Void> delete(@PathVariable List<Long> ids) {
        productService.removeByIds(ids);
        return Result.ok();
    }
}
