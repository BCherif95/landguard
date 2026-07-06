package com.laboussole.infrastructure.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.laboussole.domain.port.out.TokenIssuer;
import com.laboussole.interfaces.rest.error.ApiError;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String HEADER = "Authorization";
    private static final String PREFIX = "Bearer ";

    private final TokenIssuer tokenIssuer;
    private final ObjectMapper objectMapper;

    JwtAuthenticationFilter(TokenIssuer tokenIssuer, ObjectMapper objectMapper) {
        this.tokenIssuer = tokenIssuer;
        this.objectMapper = objectMapper;
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
            var verified = tokenIssuer.verifyAccessToken(token);
            if (verified.isPresent()) {
                var principal = new AuthenticatedPrincipal(verified.get().userId(), verified.get().role());
                var authority = new org.springframework.security.core.authority.SimpleGrantedAuthority(
                        verified.get().role().authority());
                var auth = new UsernamePasswordAuthenticationToken(principal, null, List.of(authority));
                SecurityContextHolder.getContext().setAuthentication(auth);
            } else if (!PublicEndpoints.matches(request.getRequestURI())) {
                // A token was presented but is expired or invalid. Public endpoints are
                // exempt so a stale Authorization header cannot break login/refresh.
                writeExpiredSession(request, response);
                return;
            }
        }
        chain.doFilter(request, response);
    }

    private void writeExpiredSession(HttpServletRequest request, HttpServletResponse response) throws IOException {
        var error = ApiError.of(
                HttpStatus.UNAUTHORIZED.value(),
                "SESSION_EXPIRED",
                "Votre session a expiré, veuillez vous reconnecter.",
                request.getRequestURI());
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.getWriter().write(objectMapper.writeValueAsString(error));
    }
}
