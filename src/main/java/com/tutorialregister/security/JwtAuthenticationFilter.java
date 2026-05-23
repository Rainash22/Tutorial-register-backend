package com.tutorialregister.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    public JwtAuthenticationFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                String token = jwtUtil.extractTokenFromBearer(authHeader);
                if (token != null && jwtUtil.isTokenValid(token)) {
                    String username = jwtUtil.extractUsername(token);
                    String roles = jwtUtil.extractRoles(token);

                    String[] roleArray = roles.isEmpty() ? new String[]{} : roles.split(",");
                    String[] authorities = new String[roleArray.length];
                    for (int i = 0; i < roleArray.length; i++) {
                        authorities[i] = "ROLE_" + roleArray[i].trim();
                    }

                    UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                            username,
                            null,
                            AuthorityUtils.createAuthorityList(authorities)
                        );
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                log.error("Cannot set user authentication: {}", e.getMessage());
            }
        }

        filterChain.doFilter(request, response);
    }
}
