package com.checkpointz.controller;

import com.checkpointz.model.Usuario;
import com.checkpointz.repository.UsuarioRepository;
import com.checkpointz.service.EmailService;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller; 
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; 
import jakarta.servlet.http.HttpSession;
import java.util.UUID;

@Controller 
@RequestMapping("/api")
public class UsuarioController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EmailService emailService;

    @PostMapping("/cadastro")
    public String cadastrarUsuario(
            @RequestParam("usuario") String username,
            @RequestParam("email") String email,
            @RequestParam("senha") String senha,
            @RequestParam("confirmar-senha") String confirmarSenha,
            RedirectAttributes attributes) { 

        if (!senha.equals(confirmarSenha)) {
            attributes.addFlashAttribute("erro", "As palavras-passe não coincidem!");
            return "redirect:/cadastro"; 
        }

        if (usuarioRepository.findByUsername(username) != null) {
            attributes.addFlashAttribute("erro", "Este nome de utilizador já está em uso!");
            return "redirect:/cadastro";
        }

        Usuario novoUsuario = new Usuario();
        novoUsuario.setUsername(username);
        novoUsuario.setEmail(email);
        novoUsuario.setSenha(BCrypt.hashpw(senha, BCrypt.gensalt())); 
        
        novoUsuario.setVerificado(false); 
        String tokenUnico = UUID.randomUUID().toString(); 
        novoUsuario.setTokenVerificacao(tokenUnico);

        usuarioRepository.save(novoUsuario);

        try {
            emailService.enviarEmailVerificacao(email, tokenUnico);
            
        } catch (Exception e) {
            usuarioRepository.delete(novoUsuario);
            System.out.println("❌ Erro do Gmail: " + e.getMessage());
            attributes.addFlashAttribute("erro", "Falha ao enviar o e-mail de verificação. Verifique se o e-mail é válido e tente novamente.");
            return "redirect:/cadastro"; 
        }

        attributes.addFlashAttribute("sucesso", "Cadastro realizado! Por favor, verifique a sua caixa de e-mail (e Spam) para ativar a conta.");
        return "redirect:/index"; 
    }

    @GetMapping("/verificar-email")
    public String verificarEmail(@RequestParam("token") String token, RedirectAttributes attributes) {
        Usuario usuario = usuarioRepository.findByTokenVerificacao(token);

        if (usuario != null) {
            usuario.setVerificado(true); 
            usuario.setTokenVerificacao(null); 
            usuarioRepository.save(usuario);
            
            attributes.addFlashAttribute("sucesso", "Conta ativada com sucesso! Já pode fazer login.");
            return "redirect:/index";
        } else {
            attributes.addFlashAttribute("erro", "Link de verificação inválido ou já utilizado.");
            return "redirect:/index";
        }
    }

    @PostMapping("/login")
    public String realizarLogin(
            @RequestParam("usuario") String username,
            @RequestParam("senha") String senha,
            HttpSession session,
            RedirectAttributes attributes) { 

        Usuario usuarioEncontrado = usuarioRepository.findByUsername(username);

        if (usuarioEncontrado != null && BCrypt.checkpw(senha, usuarioEncontrado.getSenha())) {
            
            if (!usuarioEncontrado.isVerificado()) {
                attributes.addFlashAttribute("erro", "A sua conta ainda não foi verificada. Verifique o seu e-mail!");
                return "redirect:/index";
            }
            
            session.setAttribute("usuarioLogado", usuarioEncontrado);
            return "redirect:/feed";
            
        } else {
            attributes.addFlashAttribute("erro", "Nome de utilizador ou palavra-passe incorretos.");
            return "redirect:/index";
        }
    }
}