package com.skala.shop.aop;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class ApiLoggingAspect {

    private static final int MAX_LOG_LENGTH = 1_000;

    private final ObjectMapper objectMapper;

    @Around("execution(public * com.skala.shop.controller..*(..))")
    public Object logApiRequestAndResponse(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String controllerMethod = joinPoint.getSignature().toShortString();
        String httpMethod = requestMethod();
        String requestUri = requestUri();
        String parameters = safeParameters(joinPoint.getArgs());

        log.info(
                "[API REQUEST] {} {} | Method: {} | Params: {}",
                httpMethod,
                requestUri,
                controllerMethod,
                parameters);

        try {
            Object result = joinPoint.proceed();
            long duration = System.currentTimeMillis() - startTime;
            int status = result instanceof ResponseEntity<?> responseEntity
                    ? responseEntity.getStatusCode().value()
                    : 200;
            Object responseBody = result instanceof ResponseEntity<?> responseEntity
                    ? responseEntity.getBody()
                    : result;

            log.info(
                    "[API RESPONSE] {} {} | Method: {} | Status: {} | Result: {} | Duration: {}ms",
                    httpMethod,
                    requestUri,
                    controllerMethod,
                    status,
                    safeJson(responseBody),
                    duration);
            return result;
        } catch (Throwable exception) {
            long duration = System.currentTimeMillis() - startTime;
            log.warn(
                    "[API ERROR] {} {} | Method: {} | Error: {} | Duration: {}ms",
                    httpMethod,
                    requestUri,
                    controllerMethod,
                    exception.getMessage(),
                    duration);
            throw exception;
        }
    }

    private String requestMethod() {
        ServletRequestAttributes attributes = requestAttributes();
        return attributes == null ? "N/A" : attributes.getRequest().getMethod();
    }

    private String requestUri() {
        ServletRequestAttributes attributes = requestAttributes();
        return attributes == null ? "N/A" : attributes.getRequest().getRequestURI();
    }

    private ServletRequestAttributes requestAttributes() {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            return attributes;
        }
        return null;
    }

    private String safeParameters(Object[] arguments) {
        return Arrays.stream(arguments)
                .filter(argument -> !(argument instanceof ServletRequest))
                .filter(argument -> !(argument instanceof ServletResponse))
                .map(this::safeJson)
                .collect(Collectors.joining(", ", "[", "]"));
    }

    private String safeJson(Object value) {
        if (value == null) {
            return "null";
        }
        try {
            JsonNode node = objectMapper.valueToTree(value);
            maskSensitiveFields(node);
            return truncate(objectMapper.writeValueAsString(node));
        } catch (Exception exception) {
            return truncate(String.valueOf(value));
        }
    }

    private void maskSensitiveFields(JsonNode node) {
        if (node == null) {
            return;
        }
        if (node.isObject()) {
            ObjectNode objectNode = (ObjectNode) node;
            Iterator<Map.Entry<String, JsonNode>> fields = objectNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (field.getKey().toLowerCase().contains("password")) {
                    objectNode.put(field.getKey(), "***");
                } else {
                    maskSensitiveFields(field.getValue());
                }
            }
        } else if (node.isArray()) {
            node.forEach(this::maskSensitiveFields);
        }
    }

    private String truncate(String value) {
        if (value.length() <= MAX_LOG_LENGTH) {
            return value;
        }
        return value.substring(0, MAX_LOG_LENGTH) + "...(truncated)";
    }
}
