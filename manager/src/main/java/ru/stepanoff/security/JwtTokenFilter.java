package ru.stepanoff.security;

import com.auth0.jwt.exceptions.JWTVerificationException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.stepanoff.service.JwtTokenService;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtTokenFilter extends OncePerRequestFilter {
    private static final int TOKEN_PREFIX_LENGTH = "Bearer ".length();

    private final JwtTokenService jwtTokenService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String tokenWithPrefix = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (tokenWithPrefix == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = tokenWithPrefix.substring(TOKEN_PREFIX_LENGTH);
        try {
            String userName = jwtTokenService.getUserName(token);
            List<? extends GrantedAuthority> authorities = jwtTokenService.getAuthorities(token)
                    .stream()
                    .map(SimpleGrantedAuthority::new)
                    .toList();

            UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                    userName,
                    null,
                    authorities
            );
            SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);

            filterChain.doFilter(request, response);
        } catch (JWTVerificationException e) {
            filterChain.doFilter(request, response);
        }
    }
}
