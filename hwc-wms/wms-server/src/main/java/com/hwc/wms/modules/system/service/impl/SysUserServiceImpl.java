package com.hwc.wms.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hwc.wms.common.exception.BusinessException;
import com.hwc.wms.modules.system.entity.SysRole;
import com.hwc.wms.modules.system.entity.SysUser;
import com.hwc.wms.modules.system.mapper.SysRoleMapper;
import com.hwc.wms.modules.system.mapper.SysUserMapper;
import com.hwc.wms.modules.system.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户 Service 实现
 */
@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    @Resource
    private SysUserMapper sysUserMapper;
    @Resource
    private SysRoleMapper sysRoleMapper;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public Page<SysUser> pageUsers(Page<SysUser> page, String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(username)) {
            wrapper.like(SysUser::getUsername, username);
        }
        wrapper.orderByDesc(SysUser::getCreateTime);
        Page<SysUser> result = sysUserMapper.selectPage(page, wrapper);
        // 填充角色信息
        for (SysUser user : result.getRecords()) {
            List<Long> roleIds = sysRoleMapper.getRoleIdsByUserId(user.getId());
            if (!roleIds.isEmpty()) {
                List<SysRole> roles = sysRoleMapper.selectBatchIds(roleIds);
                user.setRoles(roles);
            }
        }
        return result;
    }

    @Override
    @Transactional
    public void saveUser(SysUser user) {
        // 检查用户名唯一性
        Long count = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, user.getUsername()));
        if (count > 0) {
            throw new BusinessException("用户名已存在");
        }
        // 加密密码
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        sysUserMapper.insert(user);
    }

    @Override
    @Transactional
    public void updateUser(SysUser user) {
        SysUser exist = sysUserMapper.selectById(user.getId());
        if (exist == null) {
            throw new BusinessException("用户不存在");
        }
        // 如果密码为空，不修改密码
        if (!StringUtils.hasText(user.getPassword())) {
            user.setPassword(null);
        } else {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        sysUserMapper.updateById(user);
    }
}
