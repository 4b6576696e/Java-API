package com.API.JWT.security.jwt;

import com.API.JWT.model.User;
import com.API.JWT.security.service.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getRequestURL().toString().endsWith("/api/auth/sign-in")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String token = jwtUtils.extractJWTFromCookie(request);

            if (token != null && jwtUtils.isValidToken(token)) {
                String userName = jwtUtils.extractUserName(token);

                User user = (User) userDetailsService.loadUserByUsername(userName);

                System.out.println(user.getAuthorities());

                var newUser = new UsernamePasswordAuthenticationToken(
                        user.getName(),
                        null,
                        user.getAuthorities()
                );

                SecurityContextHolder.getContext().setAuthentication(newUser);
            }
        } catch (Exception ex) {
            System.out.println(ex);
        }

        filterChain.doFilter(request, response);
    }
}
