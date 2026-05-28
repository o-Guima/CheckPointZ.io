package com.checkpointz.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class SegurancaInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false); // Pega a sessão sem criar uma nova

        // Se a sessão for nula ou não tiver utilizador logado, bloqueia e manda pro login!
        if (session == null || session.getAttribute("usuarioLogado") == null) {
            response.sendRedirect("/index");
            return false;
        }

        // Isso impede que o navegador guarde a página no histórico (Evita voltar após o Logout)
        response.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
        response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
        response.setDateHeader("Expires", 0); // Proxies.

        return true; // Deixa passar
    }
}