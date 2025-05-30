package com.example.springcloud.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    /**
     * Disable all security: allow any request without authentication.
     */
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                // 1. Disable CSRF entirely:
                .csrf(csrf -> csrf.disable())

                // 2. Disable HTTP Basic and FormLogin (so there’s no default login requirement):
                .httpBasic(basic -> basic.disable())
                .formLogin(form -> form.disable())

                // 3. Permit every single exchange (no authentication, no authorization checks):
                .authorizeExchange(exchanges -> exchanges
                        .anyExchange().permitAll()
                );

        return http.build();
    }
}
