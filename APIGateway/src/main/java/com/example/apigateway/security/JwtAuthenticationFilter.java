package com.example.apigateway.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {
    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    private final JwtValidator validator;
    private final Set<String> publicPrefixes;

    public JwtAuthenticationFilter(JwtValidator validator,
                                   @Value("${app.security.permit-paths:/auth/**,/actuator/health,/actuator/info}") String permitPaths) {
        this.validator = validator;
        this.publicPrefixes = new HashSet<>();
        if (permitPaths != null) {
            Arrays.stream(permitPaths.split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .forEach(publicPrefixes::add);
        }
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (isPublic(path)) {
            return chain.filter(exchange);
        }
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return unauthorized(exchange, "Missing or invalid Authorization header");
        }
        String token = authHeader.substring(7);
        JwtPrincipal principal;
        try {
            principal = validator.validate(token);
        } catch (Exception e) {
            log.debug("JWT validation failed: {}", e.getMessage());
            return unauthorized(exchange, "Invalid token");
        }
        exchange = exchange.mutate().request(r -> r.headers(h -> {
            if (principal.getUserId() != null) h.set("X-User-Id", principal.getUserId());
            if (principal.getSubject() != null) h.set("X-Username", principal.getSubject());
            if (!principal.getRoles().isEmpty()) h.set("X-Roles", String.join(",", principal.getRoles()));
        })).build();
        return chain.filter(exchange);
    }

    private boolean isPublic(String path) {
        if (path == null) return true;
        for (String p : publicPrefixes) {
            if (p.endsWith("/**")) {
                String prefix = p.substring(0, p.length() - 3);
                if (path.startsWith(prefix)) return true;
            } else if (p.equals(path)) {
                return true;
            }
        }
        return false;
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        byte[] body = ("{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"" + message + "\"}")
                .getBytes(StandardCharsets.UTF_8);
        return exchange.getResponse().writeWith(Mono.just(exchange.getResponse().bufferFactory().wrap(body)));
    }

    @Override
    public int getOrder() { return -900; }
}
