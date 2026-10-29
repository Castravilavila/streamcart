package com.streamcart.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.Optional;

@Configuration
public class RateLimiterConfig {

    /**
     * Decides *who* a rate-limit bucket belongs to. Here: one bucket per client IP.
     * Referenced from application.yml as "#{@ipKeyResolver}".
     * A real deployment behind a load balancer would read X-Forwarded-For instead.
     */
    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> Mono.just(
                Optional.ofNullable(exchange.getRequest().getRemoteAddress())
                        .map(InetSocketAddress::getAddress)
                        .map(addr -> addr.getHostAddress())
                        .orElse("unknown")
        );
    }
}
