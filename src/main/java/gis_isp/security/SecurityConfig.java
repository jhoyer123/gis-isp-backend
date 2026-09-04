package gis_isp.security;

import gis_isp.security.filter.JwtFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CorsConfigurationSource corsConfigurationSource
    ) throws Exception {
        http
                // Habilitamos CORS usando la configuración que ya tienes en CorsConfig
                .cors(cors -> cors.configurationSource(corsConfigurationSource))

                // Sin sesiones de servidor: cada petición se autentica con el JWT de la cookie
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                // CSRF se desactiva porque no usamos sesiones basadas en cookie de sesión
                // de Spring; usamos JWT.
                .csrf(AbstractHttpConfigurer::disable)

                .authorizeHttpRequests(auth -> auth
                        // Rutas públicas: cualquiera puede entrar sin estar logueado
                        .requestMatchers("/api/auth/login").permitAll()
                        // Solo el POST a /api/users (registro) es público
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                        // Todo lo demás requiere estar autenticado
                        .anyRequest().authenticated()
                )

                // Insertamos nuestro filtro JWT antes del filtro estándar de usuario/password
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}