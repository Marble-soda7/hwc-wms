package com.hwc.wms.modules.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hwc.wms.common.result.PageResult;
import com.hwc.wms.common.result.Result;
import com.hwc.wms.modules.system.entity.SysUser;
import com.hwc.wms.modules.system.service.SysUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户管理
 */
@Tag(name = "用户管理")
@RestController
@RequestMapping("/api/sys-user")
public class SysUserController {

    @Resource
    private SysUserService sysUserService;

    @Operation(summary = "分页查询用户")
    @GetMapping("/page")
    public Result<PageResult<SysUser>> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(required = false) String username) {
        Page<SysUser> p = new Page<>(page, pageSize);
        Page<SysUser> result = sysUserService.pageUsers(p, username);
        return Result.ok(PageResult.of(result.getRecords(), result.getTotal(), page, pageSize));
    }

    @Operation(summary = "用户详情")
    @GetMapping("/{id}")
    public Result<SysUser> getById(@PathVariable Long id) {
        return Result.ok(sysUserService.getById(id));
    }

    @Operation(summary = "新增用户")
    @PostMapping
    public Result<Void> save(@RequestBody SysUser user) {
        sysUserService.saveUser(user);
        return Result.ok();
    }

    @Operation(summary = "修改用户")
    @PutMapping
    public Result<Void> update(@RequestBody SysUser user) {
        sysUserService.updateUser(user);
        return Result.ok();
    }

    @Operation(summary = "删除用户")
    @DeleteMapping("/{ids}")
    public Result<Void> delete(@PathVariable List<Long> ids) {
        sysUserService.removeByIds(ids);
        return Result.ok();
    }
}
