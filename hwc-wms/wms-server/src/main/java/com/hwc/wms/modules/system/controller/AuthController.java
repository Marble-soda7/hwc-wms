package com.hwc.wms.modules.system.controller;

import com.hwc.wms.common.result.Result;
import com.hwc.wms.common.utils.JwtUtils;
import com.hwc.wms.modules.system.entity.SysUser;
import com.hwc.wms.modules.system.security.UserDetailsImpl;
import com.hwc.wms.modules.system.service.SysMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 认证控制器
 */
@Tag(name = "认证管理")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private org.springframework.security.authentication.AuthenticationManager authenticationManager;
    @Resource
    private JwtUtils jwtUtils;
    @Resource
    private SysMenuService sysMenuService;

    @Operation(summary = "用户登录")
    @PostMapping("/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");

        // Spring Security 认证
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username, password);
        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(authenticationToken);
        } catch (org.springframework.security.core.AuthenticationException e) {
            return Result.fail("账号或密码错误");
        }
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 生成 JWT
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        String token = jwtUtils.generateToken(username, new HashMap<>());

        // 获取用户菜单
        List<?> menus = sysMenuService.getMenusByUserId(userDetails.getUser().getId());

        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("userInfo", buildUserInfo(userDetails.getUser()));
        result.put("menus", menus);
        return Result.ok(result);
    }

    @Operation(summary = "用户登出")
    @PostMapping("/logout")
    public Result<Void> logout() {
        SecurityContextHolder.clearContext();
        return Result.ok();
    }

    @Operation(summary = "获取当前用户信息")
    @GetMapping("/user-info")
    public Result<Map<String, Object>> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            return Result.unauthorized();
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Map<String, Object> result = new HashMap<>();
        result.put("userInfo", buildUserInfo(userDetails.getUser()));
        result.put("permissions", userDetails.getPermissions());
        return Result.ok(result);
    }

    private Map<String, Object> buildUserInfo(SysUser user) {
        Map<String, Object> info = new HashMap<>();
        info.put("id", user.getId());
        info.put("username", user.getUsername());
        info.put("nickname", user.getNickname());
        info.put("avatar", user.getAvatar());
        return info;
    }
}
