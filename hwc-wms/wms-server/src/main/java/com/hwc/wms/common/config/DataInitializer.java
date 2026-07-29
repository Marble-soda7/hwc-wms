package com.hwc.wms.common.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hwc.wms.modules.system.entity.*;
import com.hwc.wms.modules.system.mapper.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 数据初始化 - 确保admin密码正确 & 管理员角色拥有全部菜单
 */
@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    @Resource
    private SysUserMapper sysUserMapper;
    @Resource
    private SysRoleMapper sysRoleMapper;
    @Resource
    private SysMenuMapper sysMenuMapper;
    @Resource
    private SysRoleMenuMapper sysRoleMenuMapper;
    @Resource
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        // 1. 重置 admin 密码为 admin123
        SysUser admin = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, "admin"));
        if (admin != null) {
            String newPwd = passwordEncoder.encode("admin123");
            admin.setPassword(newPwd);
            sysUserMapper.updateById(admin);
            log.info("已重置 admin 用户密码");
        }

        // 2. 给管理员角色分配所有菜单权限
        SysRole adminRole = sysRoleMapper.selectOne(
                new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, "ADMIN"));
        if (adminRole != null) {
            // 清除旧关联
            sysRoleMapper.deleteRoleMenus(adminRole.getId());
            // 查询所有菜单
            List<SysMenu> allMenus = sysMenuMapper.selectList(null);
            for (SysMenu menu : allMenus) {
                SysRoleMenu rm = new SysRoleMenu();
                rm.setRoleId(adminRole.getId());
                rm.setMenuId(menu.getId());
                sysRoleMenuMapper.insert(rm);
            }
            log.info("已分配 {} 个菜单给管理员角色", allMenus.size());
        }
    }
}
