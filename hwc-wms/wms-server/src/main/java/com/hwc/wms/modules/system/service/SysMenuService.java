package com.hwc.wms.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hwc.wms.modules.system.entity.SysMenu;

import java.util.List;

/**
 * 菜单 Service
 */
public interface SysMenuService extends IService<SysMenu> {

    List<SysMenu> getMenuTree();

    List<SysMenu> getMenusByUserId(Long userId);
}
