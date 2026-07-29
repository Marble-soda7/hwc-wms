package com.hwc.wms.modules.system.controller;

import com.hwc.wms.common.result.Result;
import com.hwc.wms.modules.system.entity.SysRole;
import com.hwc.wms.modules.system.mapper.SysRoleMapper;
import com.hwc.wms.modules.system.service.SysRoleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * 角色管理
 */
@Tag(name = "角色管理")
@RestController
@RequestMapping("/api/sys-role")
public class SysRoleController {

    @Resource
    private SysRoleService sysRoleService;
    @Resource
    private SysRoleMapper sysRoleMapper;

    @Operation(summary = "角色列表")
    @GetMapping
    public Result<List<SysRole>> list() {
        List<SysRole> roles = sysRoleService.listRoles();
        // 填充每个角色的菜单ID
        for (SysRole role : roles) {
            role.setMenuIds(sysRoleMapper.getMenuIdsByRoleId(role.getId()));
        }
        return Result.ok(roles);
    }

    @Operation(summary = "角色详情")
    @GetMapping("/{id}")
    public Result<SysRole> getById(@PathVariable Long id) {
        SysRole role = sysRoleService.getById(id);
        if (role != null) {
            role.setMenuIds(sysRoleMapper.getMenuIdsByRoleId(role.getId()));
        }
        return Result.ok(role);
    }

    @Operation(summary = "新增角色")
    @PostMapping
    public Result<Void> save(@RequestBody SysRole role) {
        sysRoleService.saveRole(role);
        return Result.ok();
    }

    @Operation(summary = "修改角色")
    @PutMapping
    public Result<Void> update(@RequestBody SysRole role) {
        sysRoleService.updateRole(role);
        return Result.ok();
    }

    @Operation(summary = "删除角色")
    @DeleteMapping("/{ids}")
    public Result<Void> delete(@PathVariable List<Long> ids) {
        sysRoleService.removeByIds(ids);
        return Result.ok();
    }
}
