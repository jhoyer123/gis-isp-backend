package gis_isp.security.filter;

import gis_isp.security.cookie.AuthCookies;
import gis_isp.user.UserRepository;
import gis_isp.user.UserStatus;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import gis_isp.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final AuthCookies authCookies;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain) throws ServletException, IOException {
        String token = authCookies.extract(request, AuthCookies.ACCESS);
        if (token != null) {
            try {
                var claims = jwtProvider.validateAndGetClaims(token);
                if (claims.get("purpose") != null) throw new JwtException("Token de propósito especial");
                UUID userId = UUID.fromString(claims.getSubject());

                userRepository.findByIdWithRole(userId)
                        .filter(u -> UserStatus.ACTIVE.equals(u.getStatus()))   // enum: UserStatus.ACTIVE
                        .ifPresent(u -> {
                            List<GrantedAuthority> authorities = new ArrayList<>();
                            authorities.add(new SimpleGrantedAuthority("ROLE_" + u.getRole().getName()));
                            userRepository.findPermissionCodesByRoleId(u.getRole().getId())
                                    .forEach(code -> authorities.add(new SimpleGrantedAuthority(code)));
                            SecurityContextHolder.getContext().setAuthentication(
                                    new UsernamePasswordAuthenticationToken(u.getId(), null, authorities));
                        });
            } catch (JwtException | IllegalArgumentException e) {
                SecurityContextHolder.clearContext();
            }
        }
        chain.doFilter(request, response);
    }

}