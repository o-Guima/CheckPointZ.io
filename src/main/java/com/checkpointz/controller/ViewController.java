package com.checkpointz.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ViewController {

   
    @GetMapping("/cadastro")
    public String exibirTelaCadastro() {
        
        return "cadastro"; 
    }

    
    @GetMapping("/index")
    public String exibirTelaLogin() {
      
        return "index"; 
    }
     @GetMapping("/jogo")
    public String exibirTelaJogo() {
        
        return "jogo"; 
    }
      
    
    
    @GetMapping("/")
    public String telaInicial() {
        return "redirect:/index"; 
    }
}