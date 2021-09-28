package com.zw.platform.commons.filter;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Objects;

public class CustomCorsFilter extends OncePerRequestFilter {
    private final String authPath;
    private final String redirectPath;

    public CustomCorsFilter(String authPath, String redirectPath) {
        this.authPath = authPath;
        this.redirectPath = redirectPath;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        final String requestURI = request.getRequestURI();
        if (Objects.equals(authPath, requestURI) || Objects.equals(redirectPath, requestURI)) {
            if (request.getHeader(HttpHeaders.ORIGIN) != null) {
                String origin = request.getHeader(HttpHeaders.ORIGIN);
                String allowHeaders = request.getHeader(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS);

                response.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
                response.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, "GET, POST");
                response.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
                response.addHeader(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, allowHeaders);
            }
            if (HttpMethod.OPTIONS.matches(request.getMethod())) {
                response.getWriter().print(HttpStatus.OK.getReasonPhrase());
                response.getWriter().flush();
                return;
            }
        }
        filterChain.doFilter(request, response);
    }
}
