package com.duoc.bancoxyz.notificaciones.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Las API usan tokens; no sesiones ni formularios del navegador.
        http.csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/health/**", "/error").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/**").hasAuthority("SCOPE_banco.read")
                .requestMatchers(HttpMethod.POST, "/api/**").hasAuthority("SCOPE_banco.write")
                .requestMatchers("/actuator/**").hasAuthority("SCOPE_banco.read")
                .anyRequest().denyAll())
            .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()));
        return http.build();
    }
}
