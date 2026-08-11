package com.hwc.wms.modules.business.controller;

import com.hwc.wms.common.result.Result;
import com.hwc.wms.modules.business.entity.Category;
import com.hwc.wms.modules.business.service.CategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * 商品分类管理
 */
@Tag(name = "商品分类管理")
@RestController
@RequestMapping("/api/category")
public class CategoryController {

    @Resource
    private CategoryService categoryService;

    @Operation(summary = "分类树（含children）")
    @GetMapping("/tree")
    public Result<List<Map<String, Object>>> tree() {
        return Result.ok(categoryService.getCategoryTree());
    }

    @Operation(summary = "分类列表（不分页，供下拉选择）")
    @GetMapping("/list")
    public Result<List<Category>> list() {
        return Result.ok(categoryService.list());
    }

    @Operation(summary = "分类详情")
    @GetMapping("/{id}")
    public Result<Category> getById(@PathVariable Long id) {
        return Result.ok(categoryService.getById(id));
    }

    @Operation(summary = "新增分类（仅管理员）")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public Result<Void> save(@RequestBody Category category) {
        categoryService.saveCategory(category);
        return Result.ok();
    }

    @Operation(summary = "修改分类（仅管理员）")
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping
    public Result<Void> update(@RequestBody Category category) {
        categoryService.updateCategory(category);
        return Result.ok();
    }

    @Operation(summary = "删除分类（仅管理员）")
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{ids}")
    public Result<Void> delete(@PathVariable List<Long> ids) {
        categoryService.removeByIds(ids);
        return Result.ok();
    }
}
