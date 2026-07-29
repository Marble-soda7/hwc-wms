package com.hwc.wms.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hwc.wms.modules.system.entity.SysRole;

import java.util.List;

/**
 * 角色 Service
 */
public interface SysRoleService extends IService<SysRole> {

    List<SysRole> listRoles();

    void saveRole(SysRole role);

    void updateRole(SysRole role);
}
