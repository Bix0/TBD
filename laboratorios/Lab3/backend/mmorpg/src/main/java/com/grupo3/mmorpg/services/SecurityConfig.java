package com.grupo3.mmorpg.services;

import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Configuración de seguridad Spring Security.
 * Define rutas públicas, protegidas, y el filtro JWT.
 * Sin ORM - solo JDBC puro.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable()) // API REST sin CSRF
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                ) // Sin
                // sesiones
                .authorizeHttpRequests(auth ->
                        auth
                                // Rutas públicas (no requieren token)
                                .requestMatchers(HttpMethod.OPTIONS, "/**")
                                .permitAll()
                                .requestMatchers("/api/auth/**")
                                .permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/raids")
                                .permitAll() // Listar raids público
                                .requestMatchers(HttpMethod.GET, "/api/raids/**")
                                .permitAll() // Ver raid individual público
                                .requestMatchers(HttpMethod.GET, "/api/clanes/**")
                                .permitAll()
                                .requestMatchers(HttpMethod.GET, "/api/personajes/**")
                                .authenticated()
                                .requestMatchers(HttpMethod.POST, "/api/personajes/**")
                                .authenticated()
                                .requestMatchers(HttpMethod.PUT, "/api/personajes/**")
                                .authenticated()
                                .requestMatchers(HttpMethod.DELETE, "/api/personajes/**")
                                .authenticated()
                                // Rutas solo para Admin (creación/modificación de datos)
                                .requestMatchers(HttpMethod.POST, "/api/items/**")
                                .hasRole("ADMIN")
                                .requestMatchers(HttpMethod.POST, "/api/raids")
                                .hasRole("ADMIN") // Crear raid
                                .requestMatchers(HttpMethod.PUT, "/api/raids/**")
                                .hasRole("ADMIN") // Editar raid
                                .requestMatchers(HttpMethod.DELETE, "/api/raids/**")
                                .hasRole("ADMIN") // Eliminar raid
                                .requestMatchers("/api/clanes/*/lider")
                                .hasRole("ADMIN")
                                // Distribucion de loot y operaciones masivas
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/raids/distribuir-loot"
                                )
                                .hasRole("ADMIN")
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/raids/con-inscripcion-masiva"
                                )
                                .hasRole("ADMIN")
                                // Gestion de clanes (crear cualquiera, editar/borrar solo Admin)
                                .requestMatchers(HttpMethod.PUT, "/api/clanes/**")
                                .hasRole("ADMIN")
                                .requestMatchers(HttpMethod.DELETE, "/api/clanes/**")
                                .hasRole("ADMIN")
                                // Refresco de ranking solo Admin
                                .requestMatchers(HttpMethod.POST, "/api/ranking/refresh")
                                .hasRole("ADMIN")
                                // Cualquier usuario autenticado puede inscribirse/desinscribirse de raids
                                .requestMatchers(HttpMethod.POST, "/api/raids/*/inscribir")
                                .authenticated()
                                .requestMatchers(
                                        HttpMethod.POST,
                                        "/api/raids/*/desinscribir"
                                )
                                .authenticated()
                                // Rutas para cualquier usuario autenticado
                                .anyRequest()
                                .authenticated()
                )
                .addFilterBefore(
                        jwtAuthFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("http://localhost:*"));
        config.setAllowedMethods(
                List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")
        );
        config.setAllowedHeaders(
                List.of("Authorization", "Content-Type", "Accept", "Origin")
        );
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
