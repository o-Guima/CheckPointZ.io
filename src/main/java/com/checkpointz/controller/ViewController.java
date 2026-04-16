package com.checkpointz.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

    // Quando o usuário acessar localhost:8080/cadastro
    @GetMapping("/cadastro")
    public String exibirTelaCadastro() {
        // O Spring vai procurar e abrir o arquivo cadastro.html da pasta templates
        return "cadastro"; 
    }

    // Quando o usuário acessar localhost:8080/login
    @GetMapping("/index")
    public String exibirTelaLogin() {
        // O Spring vai procurar e abrir o arquivo login.html da pasta templates
        return "index"; 
    }
     @GetMapping("/jogo")
    public String exibirTelaJogo() {
        // O Spring vai procurar e abrir o arquivo login.html da pasta templates
        return "jogo"; 
    }
      
    
    // Opcional: Redirecionar a raiz (localhost:8080) direto pro login
    @GetMapping("/")
    public String telaInicial() {
        return "redirect:/index"; 
    }
}