package com.laboussole.infrastructure.security;

import com.laboussole.domain.port.out.TokenIssuer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final TokenIssuer tokenIssuer;

    JwtAuthenticationFilter(TokenIssuer tokenIssuer) {
        this.tokenIssuer = tokenIssuer;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain chain) throws ServletException, IOException {
        var header = request.getHeader(HEADER);
        String token = null;

        if (header != null && header.startsWith(PREFIX)) {
            token = header.substring(PREFIX.length());
        } else {
            // Check query parameter for SSE
            token = request.getParameter("token");
        }

        if (token != null) {
            tokenIssuer.verifyAccessToken(token).ifPresent(verified -> {
                var principal = new AuthenticatedPrincipal(verified.userId(), verified.role());
                var authority = new org.springframework.security.core.authority.SimpleGrantedAuthority(
                        verified.role().authority());
                var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of(authority));
                SecurityContextHolder.getContext().setAuthentication(auth);
            });
        }
        chain.doFilter(request, response);
    }
}
