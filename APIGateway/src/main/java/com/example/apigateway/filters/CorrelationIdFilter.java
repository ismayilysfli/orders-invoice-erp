package com.example.apigateway.filters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Component
public class CorrelationIdFilter implements GlobalFilter, Ordered {
    private static final Logger log = LoggerFactory.getLogger(CorrelationIdFilter.class);
    private static final String HEADER = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long start = System.currentTimeMillis();
        ServerHttpRequest request = exchange.getRequest();
        String correlationId = request.getHeaders().getFirst(HEADER);
        ServerWebExchange effectiveExchange = exchange;
        if (correlationId == null || correlationId.isBlank()) {
            correlationId = UUID.randomUUID().toString();
            final String cidFinal = correlationId;
            effectiveExchange = exchange.mutate().request(r -> r.headers(h -> h.set(HEADER, cidFinal))).build();
        }
        final String cid = correlationId;
        effectiveExchange.getResponse().getHeaders().add(HEADER, cid);
        final String method = request.getMethod() == null ? "UNKNOWN" : request.getMethod().name();
        final String path = request.getURI().getRawPath();
        log.info("event=start method={} path={} correlationId={}", method, path, cid);
        final ServerWebExchange exFinal = effectiveExchange;
        return chain.filter(exFinal)
                .doOnSuccess(v -> log(exFinal, cid, method, path, start, null))
                .doOnError(err -> log(exFinal, cid, method, path, start, err));
    }

    private void log(ServerWebExchange exchange, String correlationId, String method, String path, long start, Throwable err) {
        long duration = System.currentTimeMillis() - start;
        String status = exchange.getResponse().getStatusCode() == null ? "NA" : String.valueOf(exchange.getResponse().getStatusCode().value());
        if (err == null) {
            log.info("event=completed method={} path={} status={} durationMs={} correlationId={}", method, path, status, duration, correlationId);
        } else {
            log.error("event=error method={} path={} status={} durationMs={} correlationId={} error={} message={}", method, path, status, duration, correlationId, err.getClass().getSimpleName(), err.getMessage());
        }
    }

    @Override
    public int getOrder() {
        return -1000;
    }
}
