package com.example.apigateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http,
                                                            @Value("${app.security.permit-paths:/auth/**,/actuator/health,/actuator/info}") String permitPaths) {
        http.csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .authorizeExchange(reg -> {
                    for (String p : permitPaths.split(",")) {
                        String path = p.trim();
                        if (!path.isEmpty()) reg.pathMatchers(path).permitAll();
                    }
                    reg.anyExchange().authenticated();
                });
        return http.build();
    }
}
