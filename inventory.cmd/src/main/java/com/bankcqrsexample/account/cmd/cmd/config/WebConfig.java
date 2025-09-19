package com.bankcqrsexample.account.cmd.cmd.config;

import com.bankcqrsexample.account.cmd.cmd.infrastructure.IdempotencyInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {
    
    private final IdempotencyInterceptor idempotencyInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(idempotencyInterceptor)
                .addPathPatterns("/v1/products/**")
                .excludePathPatterns("/v1/health", "/v1/ready", "/v1/metrics");
    }
}
