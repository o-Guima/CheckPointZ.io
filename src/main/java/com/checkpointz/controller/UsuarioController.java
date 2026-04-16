package com.checkpointz.controller;

import com.checkpointz.model.Usuario;
import com.checkpointz.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/cadastro")
        public RedirectView cadastrarUsuario(
        @RequestParam("usuario") String username,
        @RequestParam("email") String email,
        @RequestParam("senha") String senha,
        @RequestParam("confirmar-senha") String confirmarSenha,
        HttpSession session) { // <-- Adicionamos a sessão aqui

    if (!senha.equals(confirmarSenha)) {
        return new RedirectView("/cadastro.html?erro=senhas-nao-conferem");
    }

    Usuario novoUsuario = new Usuario();
    novoUsuario.setUsername(username);
    novoUsuario.setEmail(email);
    novoUsuario.setSenha(senha); 

    // Salva no MySQL
    usuarioRepository.save(novoUsuario);

    // GUARDA O USUÁRIO REAL NA SESSÃO
    session.setAttribute("usuarioLogado", novoUsuario);

    // Muda o redirecionamento para a nova rota dinâmica do feed (veremos no Passo 2)
    return new RedirectView("/feed"); 
}

@PostMapping("/login")
    public RedirectView realizarLogin(
            @RequestParam("usuario") String username,
            @RequestParam("senha") String senha,
            HttpSession session) {

        // 1. Busca no banco de dados se existe alguém com esse nome de usuário
        Usuario usuarioEncontrado = usuarioRepository.findByUsername(username);

        // 2. Verifica se achou alguém e se a senha digitada bate com a salva no banco
        if (usuarioEncontrado != null && usuarioEncontrado.getSenha().equals(senha)) {
            
            // Sucesso! Guarda o usuário na sessão para o Thymeleaf poder usar no feed
            session.setAttribute("usuarioLogado", usuarioEncontrado);
            
            // Redireciona para a página do feed
            return new RedirectView("/feed");
            
        } else {
            // Falha! Redireciona de volta para a tela de login (podemos passar um erro na URL)
            return new RedirectView("/index.html?erro=dados-incorretos");
        }
    }
}