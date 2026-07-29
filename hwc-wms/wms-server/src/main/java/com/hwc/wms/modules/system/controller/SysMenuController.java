package com.hwc.wms.modules.system.controller;

import com.hwc.wms.common.result.Result;
import com.hwc.wms.modules.system.entity.SysMenu;
import com.hwc.wms.modules.system.service.SysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 菜单管理
 */
@Tag(name = "菜单管理")
@RestController
@RequestMapping("/api/sys-menu")
public class SysMenuController {

    @Resource
    private SysMenuService sysMenuService;

    @Operation(summary = "菜单树")
    @GetMapping("/tree")
    public Result<List<SysMenu>> tree() {
        return Result.ok(sysMenuService.getMenuTree());
    }

    @Operation(summary = "菜单详情")
    @GetMapping("/{id}")
    public Result<SysMenu> getById(@PathVariable Long id) {
        return Result.ok(sysMenuService.getById(id));
    }

    @Operation(summary = "新增菜单")
    @PostMapping
    public Result<Void> save(@RequestBody SysMenu menu) {
        sysMenuService.save(menu);
        return Result.ok();
    }

    @Operation(summary = "修改菜单")
    @PutMapping
    public Result<Void> update(@RequestBody SysMenu menu) {
        sysMenuService.updateById(menu);
        return Result.ok();
    }

    @Operation(summary = "删除菜单")
    @DeleteMapping("/{ids}")
    public Result<Void> delete(@PathVariable List<Long> ids) {
        sysMenuService.removeByIds(ids);
        return Result.ok();
    }
}
