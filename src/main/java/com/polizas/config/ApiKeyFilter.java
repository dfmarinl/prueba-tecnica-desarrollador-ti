package com.polizas.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Filtro unico de seguridad: valida el header x-api-key en todas las rutas.
 */
@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    public static final String HEADER_API_KEY = "x-api-key";

    private final String apiKey;

    public ApiKeyFilter(AppConfig appConfig) {
        this.apiKey = appConfig.apiKey();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String apiKeySolicitada = request.getHeader(HEADER_API_KEY);
        if (apiKeySolicitada == null || !apiKeySolicitada.equals(apiKey)) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.setCharacterEncoding(StandardCharsets.UTF_8.name());
            response.getWriter().write("{\"status\":401,\"mensaje\":\"API key ausente o invalida\"}");
            return;
        }
        filterChain.doFilter(request, response);
    }
}
