package com.streamcart.gateway.filter;

import com.streamcart.gateway.security.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

/**
 * Per-route filter. Spring Cloud Gateway derives the filter name from the class name
 * by stripping the "GatewayFilterFactory" suffix, so this is referenced in
 * application.yml simply as "JwtAuthentication".
 *
 * On success it forwards the identity to the downstream service as plain headers
 * (X-User-Email, X-User-Role) so services don't have to re-parse the token.
 */
@Component
public class JwtAuthenticationGatewayFilterFactory
        extends AbstractGatewayFilterFactory<JwtAuthenticationGatewayFilterFactory.Config> {

    public static final String USER_EMAIL_HEADER = "X-User-Email";
    public static final String USER_ROLE_HEADER = "X-User-Role";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;

    public JwtAuthenticationGatewayFilterFactory(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
                return unauthorized(exchange, "Missing or malformed Authorization header");
            }

            Claims claims;
            try {
                claims = jwtUtil.parseClaims(authHeader.substring(BEARER_PREFIX.length()));
            } catch (JwtException | IllegalArgumentException e) {
                return unauthorized(exchange, "Invalid or expired token");
            }

            // Strip any X-User-* headers the client may have sent, then set our own.
            // Downstream services can therefore trust these headers came from the gateway.
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .headers(headers -> {
                        headers.remove(USER_EMAIL_HEADER);
                        headers.remove(USER_ROLE_HEADER);
                    })
                    .header(USER_EMAIL_HEADER, claims.getSubject())
                    .header(USER_ROLE_HEADER, claims.get("role", String.class))
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        };
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        String body = "{\"timestamp\":\"" + Instant.now() + "\","
                + "\"status\":401,"
                + "\"error\":\"Unauthorized\","
                + "\"message\":\"" + message + "\"}";

        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    public static class Config {
    }
}
