package com.hwc.wms.modules.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.hwc.wms.modules.system.entity.SysUser;

/**
 * 用户 Service
 */
public interface SysUserService extends IService<SysUser> {

    Page<SysUser> pageUsers(Page<SysUser> page, String username);

    void saveUser(SysUser user);

    void updateUser(SysUser user);
}
