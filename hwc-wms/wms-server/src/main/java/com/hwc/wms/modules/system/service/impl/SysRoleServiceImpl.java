package com.hwc.wms.modules.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hwc.wms.common.exception.BusinessException;
import com.hwc.wms.modules.system.entity.SysRole;
import com.hwc.wms.modules.system.entity.SysRoleMenu;
import com.hwc.wms.modules.system.mapper.SysRoleMapper;
import com.hwc.wms.modules.system.mapper.SysRoleMenuMapper;
import com.hwc.wms.modules.system.service.SysRoleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 角色 Service 实现
 */
@Service
public class SysRoleServiceImpl extends ServiceImpl<SysRoleMapper, SysRole> implements SysRoleService {

    @Resource
    private SysRoleMapper sysRoleMapper;
    @Resource
    private SysRoleMenuMapper sysRoleMenuMapper;

    @Override
    public List<SysRole> listRoles() {
        return sysRoleMapper.selectList(null);
    }

    @Override
    @Transactional
    public void saveRole(SysRole role) {
        sysRoleMapper.insert(role);
        // 保存角色菜单关联
        if (role.getMenuIds() != null && !role.getMenuIds().isEmpty()) {
            saveRoleMenus(role.getId(), role.getMenuIds());
        }
    }

    @Override
    @Transactional
    public void updateRole(SysRole role) {
        SysRole exist = sysRoleMapper.selectById(role.getId());
        if (exist == null) {
            throw new BusinessException("角色不存在");
        }
        sysRoleMapper.updateById(role);
        // 更新角色菜单关联
        sysRoleMapper.deleteRoleMenus(role.getId());
        if (role.getMenuIds() != null && !role.getMenuIds().isEmpty()) {
            saveRoleMenus(role.getId(), role.getMenuIds());
        }
    }

    private void saveRoleMenus(Long roleId, List<Long> menuIds) {
        for (Long menuId : menuIds) {
            SysRoleMenu rm = new SysRoleMenu();
            rm.setRoleId(roleId);
            rm.setMenuId(menuId);
            sysRoleMenuMapper.insert(rm);
        }
    }
}
