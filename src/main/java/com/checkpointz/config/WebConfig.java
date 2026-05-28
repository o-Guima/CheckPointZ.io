package com.checkpointz.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Autowired
    private SegurancaInterceptor segurancaInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Rotas que EXIGEM login para entrar
        registry.addInterceptor(segurancaInterceptor)
                .addPathPatterns("/feed", "/perfil/**", "/jogo/**", "/pesquisar", "/post/**", "/conectar/**")
                // Rotas públicas que não precisam de login
                .excludePathPatterns("/index", "/cadastro", "/login", "/css/**", "/images/**", "/uploads/**");
    }
}