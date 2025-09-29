package org.example.reportservice.config;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

@Aspect
@Component
public class LoggingAspect {
    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("within(org.example.reportservice.controller..*)")
    public void controllerLayer() {}

    @Pointcut("within(org.example.reportservice.client..*)")
    public void clientLayer() {}

    @Around("controllerLayer() || clientLayer()")
    public Object logAround(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        String method = pjp.getSignature().toShortString();
        String args = formatArgs(pjp.getArgs());
        HttpServletRequest req = currentRequest();
        String correlationId = req == null ? null : req.getHeader("X-Correlation-Id");
        if (correlationId == null || correlationId.isBlank()) correlationId = UUID.randomUUID().toString();
        MDC.put("correlationId", correlationId);
        log.info("START {} args=[{}] correlationId={}", method, args, correlationId);
        try {
            Object result = pjp.proceed();
            long took = System.currentTimeMillis() - start;
            log.info("END   {} took={}ms correlationId={}", method, took, correlationId);
            return result;
        } catch (Throwable ex) {
            long took = System.currentTimeMillis() - start;
            log.warn("EX    {} took={}ms ex={} msg={} correlationId={}", method, took, ex.getClass().getSimpleName(), ex.getMessage(), correlationId);
            throw ex;
        } finally {
            MDC.remove("correlationId");
        }
    }

    private HttpServletRequest currentRequest() {
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if (attrs instanceof ServletRequestAttributes sra) return sra.getRequest();
        return null;
    }

    private String formatArgs(Object[] args) {
        if (args == null || args.length == 0) return "";
        return Arrays.stream(args).map(a -> a == null ? "null" : a.toString()).collect(Collectors.joining(", "));
    }
}

