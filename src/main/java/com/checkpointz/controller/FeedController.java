package com.checkpointz.controller;

import com.checkpointz.model.Usuario;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FeedController {

    @GetMapping("/feed")
    public String exibirFeed(HttpSession session, Model model) {
        
        // 1. Pega o usuário que foi salvo na sessão lá no cadastro ou login
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        // 2. Verifica se a pessoa realmente está logada
        if (usuario == null) {
            // Se tentar acessar o feed sem logar/cadastrar, manda pro login
            return "redirect:/index.html"; 
        }

        // 3. Envia o objeto real do banco de dados para o Thymeleaf
        model.addAttribute("usuarioLogado", usuario);

        // 4. Retorna o nome do seu arquivo HTML que está na pasta 'templates'
        // Se o seu arquivo chama 'index.html', coloque apenas "index"
        return "feed.html"; 
    }
}