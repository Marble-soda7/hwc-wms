package com.hwc.wms.modules.system.security;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.hwc.wms.modules.system.entity.SysUser;
import com.hwc.wms.modules.system.mapper.SysUserMapper;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * Spring Security 用户加载服务
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Resource
    private SysUserMapper sysUserMapper;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username));
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        if (user.getStatus() != null && user.getStatus() == 0) {
            throw new UsernameNotFoundException("用户已被禁用: " + username);
        }

        List<String> permissions = sysUserMapper.getUserPermissions(user.getId());
        // 同时加载角色编码，加上 ROLE_ 前缀，便于 @PreAuthorize("hasRole('ADMIN')") 使用
        List<String> roleCodes = sysUserMapper.getUserRoleCodes(user.getId());
        for (String roleCode : roleCodes) {
            permissions.add("ROLE_" + roleCode);
        }
        return new UserDetailsImpl(user, permissions);
    }
}
