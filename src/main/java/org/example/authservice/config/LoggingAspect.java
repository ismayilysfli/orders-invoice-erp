package org.example.authservice.config;

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
import java.security.Principal;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

@Aspect
@Component
public class LoggingAspect {

    private static final Logger log = LoggerFactory.getLogger(LoggingAspect.class);

    @Pointcut("within(org.example.authservice.controller..*)")
    public void controllerLayer() {}

    @Pointcut("within(org.example.authservice.service..*)")
    public void serviceLayer() {}

    @Around("controllerLayer() || serviceLayer()")
    public Object logAround(ProceedingJoinPoint pjp) throws Throwable {
        long start = System.currentTimeMillis();
        String method = pjp.getSignature().toShortString();
        String args = maskArgs(pjp.getArgs());

        // populate MDC with correlation id (from incoming header or generated) and principal if available
        HttpServletRequest req = currentRequest();
        String correlationId = null;
        if (req != null) {
            correlationId = req.getHeader("X-Correlation-Id");
            Principal principal = req.getUserPrincipal();
            if (principal != null) MDC.put("principal", principal.getName());
        }
        if (correlationId == null || correlationId.isEmpty()) {
            correlationId = UUID.randomUUID().toString();
        }
        MDC.put("correlationId", correlationId);

        log.info("START {} args=[{}] correlationId={}", method, args, correlationId);
        try {
            Object result = pjp.proceed();
            long took = System.currentTimeMillis() - start;
            log.info("END   {} took={}ms result={} correlationId={}", method, took, summarizeResult(result), correlationId);
            return result;
        } catch (Throwable ex) {
            long took = System.currentTimeMillis() - start;
            log.warn("EX    {} took={}ms ex={} msg={} correlationId={}", method, took, ex.getClass().getSimpleName(), ex.getMessage(), correlationId);
            throw ex;
        } finally {
            // ensure MDC is cleared to avoid leakage between threads/requests
            MDC.remove("correlationId");
            MDC.remove("principal");
        }
    }

    private HttpServletRequest currentRequest(){
        RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
        if(attrs instanceof ServletRequestAttributes){
            return ((ServletRequestAttributes) attrs).getRequest();
        }
        return null;
    }

    private String maskArgs(Object[] args){
        if(args == null || args.length == 0) return "";
        return Arrays.stream(args)
                .map(this::maskObject)
                .collect(Collectors.joining(", "));
    }

    private String maskObject(Object o){
        if(o == null) return "null";
        // Simple masking for DTOs containing password field when toString() reveals it
        String s = o.toString();
        if(s.toLowerCase().contains("password")){
            return s.replaceAll("(?i)password=([^,)}]+)", "password=***");
        }
        return s;
    }

    private String summarizeResult(Object result){
        if(result == null) return "null";
        String s = result.toString();
        if(s.length() > 200) {
            return s.substring(0,197) + "...";
        }
        // avoid leaking password if present in result DTO (should not normally happen)
        if(s.toLowerCase().contains("password")){
            s = s.replaceAll("(?i)password=([^,)}]+)", "password=***");
        }
        return s;
    }
}
