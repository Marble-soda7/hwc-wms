package com.hwc.wms.modules.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hwc.wms.modules.system.entity.SysMenu;
import com.hwc.wms.modules.system.mapper.SysMenuMapper;
import com.hwc.wms.modules.system.service.SysMenuService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 菜单 Service 实现
 */
@Service
public class SysMenuServiceImpl extends ServiceImpl<SysMenuMapper, SysMenu> implements SysMenuService {

    @Resource
    private SysMenuMapper sysMenuMapper;

    @Override
    public List<SysMenu> getMenuTree() {
        List<SysMenu> allMenus = sysMenuMapper.selectList(null);
        return buildTree(allMenus);
    }

    @Override
    public List<SysMenu> getMenusByUserId(Long userId) {
        List<SysMenu> userMenus = sysMenuMapper.getMenusByUserId(userId);
        return buildTree(userMenus);
    }

    /**
     * 构建菜单树
     */
    private List<SysMenu> buildTree(List<SysMenu> menus) {
        List<SysMenu> tree = new ArrayList<>();
        for (SysMenu menu : menus) {
            if (menu.getParentId() == 0) {
                tree.add(menu);
            }
        }
        for (SysMenu parent : tree) {
            parent.setChildren(getChildren(parent.getId(), menus));
        }
        return tree.stream()
                .sorted(Comparator.comparing(SysMenu::getSort))
                .collect(Collectors.toList());
    }

    private List<SysMenu> getChildren(Long parentId, List<SysMenu> allMenus) {
        List<SysMenu> children = new ArrayList<>();
        for (SysMenu menu : allMenus) {
            if (parentId.equals(menu.getParentId())) {
                children.add(menu);
            }
        }
        children.sort(Comparator.comparing(SysMenu::getSort));
        return children;
    }
}
