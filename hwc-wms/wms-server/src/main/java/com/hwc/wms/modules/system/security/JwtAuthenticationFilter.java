package com.hwc.wms.modules.system.security;

import com.hwc.wms.common.utils.JwtUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * JWT 认证过滤器
 */
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String headerValue = request.getHeader(jwtUtils.getHeader());
        String token = jwtUtils.extractToken(headerValue);

        // 调试日志：记录每个需要认证的请求
        String uri = request.getRequestURI();
        log.info("JWT过滤 - URI: {}, 有Token: {}", uri, StringUtils.hasText(token));

        if (StringUtils.hasText(token) && jwtUtils.validateToken(token)) {
            String username = jwtUtils.getUsernameFromToken(token);
            log.info("JWT过滤 - Token验证通过, username: {}", username);

            // 防止 token 中用户名为空导致 loadUserByUsername(null) 抛异常
            if (!StringUtils.hasText(username)) {
                log.warn("JWT过滤 - 用户名为空，跳过认证 - URI: {}", uri);
                filterChain.doFilter(request, response);
                return;
            }

            UserDetailsImpl userDetails = (UserDetailsImpl) userDetailsService.loadUserByUsername(username);
            if (userDetails != null) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.debug("JWT过滤 - 认证成功, username: {}, 权限数: {}", username, userDetails.getAuthorities().size());
            }
        } else if (StringUtils.hasText(token)) {
            log.warn("JWT过滤 - Token验证失败 - URI: {}, Token前20位: {}", uri, token.substring(0, Math.min(20, token.length())));
        }

        filterChain.doFilter(request, response);
    }
}
