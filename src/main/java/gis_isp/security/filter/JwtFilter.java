package gis_isp.security.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import gis_isp.security.jwt.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Buscar la cookie "accessToken" entre todas las cookies de la petición
        String token = extractTokenFromCookies(request);

        // Si hay token, intentar validarlo
        if (token != null) {
            try {
                Claims claims = jwtProvider.validateAndGetClaims(token);

                UUID userId = UUID.fromString(claims.getSubject());

                List<GrantedAuthority> authorities = Collections.emptyList();

                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userId,       // "principal": quién es el usuario (su id)
                        null,         // credentials: no las necesitamos aquí
                        authorities   // roles/permisos (vacío por ahora)
                );

                // Dejar al usuario "autenticado" para el resto de la petición
                SecurityContextHolder.getContext().setAuthentication(authentication);

            } catch (JwtException e) {
                // Token inválido o expirado -> simplemente no autenticamos.
                // No lanzamos error aquí; dejamos que Spring Security decida
                // si la ruta requiere autenticación o no.
                SecurityContextHolder.clearContext();
            }
        }

        // Seguir con la cadena de filtros / llegar al controller
        filterChain.doFilter(request, response);
    }

    private String extractTokenFromCookies(HttpServletRequest request) {
        if (request.getCookies() == null) {
            return null;
        }

        // Optimizado con Streams para mantener un estilo limpio y declarativo
        return Arrays.stream(request.getCookies())
                .filter(cookie -> "accessToken".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // 1. Si van a loguearse, saltarse este filtro JWT por completo
        if ("/api/auth/login".equals(path)) {
            return true;
        }

        // 2. Si van a registrarse (POST /api/users), saltarse este filtro JWT por completo
        if ("/api/users".equals(path) && "POST".equalsIgnoreCase(method)) {
            return true;
        }

        return false;
    }

}