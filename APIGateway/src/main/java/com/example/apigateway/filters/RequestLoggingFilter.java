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

@Component
public class RequestLoggingFilter implements GlobalFilter, Ordered {
    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final String CID = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        final long start = System.currentTimeMillis();
        final ServerHttpRequest req = exchange.getRequest();
        final String method = req.getMethod().name();
        final String path = req.getURI().getRawPath();
        final String cid = req.getHeaders().getFirst(CID) == null ? "NA" : req.getHeaders().getFirst(CID);
        log.debug("req:start method={} path={} correlationId={}", method, path, cid);
        return chain.filter(exchange).doFinally(signal -> {
            long duration = System.currentTimeMillis() - start;
            String status = exchange.getResponse().getStatusCode() == null ? "NA" : String.valueOf(exchange.getResponse().getStatusCode().value());
            log.debug("req:end method={} path={} status={} durationMs={} correlationId={}", method, path, status, duration, cid);
        });
    }

    @Override
    public int getOrder() { return -800; }
}
