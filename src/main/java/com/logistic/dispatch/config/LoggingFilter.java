package com.logistic.dispatch.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.MDC;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.UUID;

public class LoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        try {
            HttpServletRequest httpRequest = (HttpServletRequest) request;

            MDC.put("method", httpRequest.getMethod());

            var auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null) {
                MDC.put("username", auth.getName());
            }
            MDC.put("traceId", UUID.randomUUID().toString());
            chain.doFilter(request, response);
        } finally {
            MDC.clear(); // VERY IMPORTANT
        }
    }
}
