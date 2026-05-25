package com.checkpointz.controller;

import com.checkpointz.model.JogoApi;
import com.checkpointz.model.Usuario;
import com.checkpointz.repository.JogoApiRepository;
import com.checkpointz.repository.UsuarioRepository; // IMPORTAÇÃO ADICIONADA
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class FeedController {

    @Autowired
    private JogoApiRepository jogoApiRepository;

    @Autowired
    private UsuarioRepository usuarioRepository; // REPOSITÓRIO DE USUÁRIOS ADICIONADO

    // --- 1. CARREGAR O FEED ---
    @GetMapping("/feed")
    public String exibirFeed(HttpSession session, Model model) {
        
        // A. Verifica se a pessoa realmente está logada
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            // Se tentar acessar o feed sem logar/cadastrar, manda pro login
            return "redirect:/index.html"; 
        }
        
        // B. Envia o usuário real do banco para o Thymeleaf (perfil lateral)
        model.addAttribute("usuarioLogado", usuario);

        // C. Vai buscar os jogos ao MySQL para os carrosséis
        List<JogoApi> ofertas = jogoApiRepository.encontrarMelhoresOfertas();
        List<JogoApi> emAlta = jogoApiRepository.encontrarJogosAleatorios();

        // D. Envia as listas de jogos para o HTML
        model.addAttribute("ofertas", ofertas);
        model.addAttribute("emAlta", emAlta);
        
        // Dica: Com Thymeleaf, usamos apenas o nome do ficheiro sem o ".html"
        return "feed"; 
    }

    // --- 2. A TELA DE PESQUISA AVANÇADA (ATUALIZADA) ---
    @GetMapping("/pesquisar")
    public String pesquisar(@RequestParam(value = "q", required = false) String query, 
                            @RequestParam(value = "tipo", defaultValue = "todos") String tipo,
                            Model model, HttpSession session) {
        
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) return "redirect:/index.html";

        List<JogoApi> resultadosJogos = null;
        List<Usuario> resultadosUsuarios = null;

        // Verifica o que o usuário quer pesquisar
        if (query != null && !query.isEmpty()) {
            if (tipo.equals("todos") || tipo.equals("jogos")) {
                resultadosJogos = jogoApiRepository.findByNomeJogoContainingIgnoreCase(query);
            }
            if (tipo.equals("todos") || tipo.equals("perfis")) {
                resultadosUsuarios = usuarioRepository.findByUsernameContainingIgnoreCase(query);
            }
        } else {
            // Se a busca for vazia, mostra tudo
            if (tipo.equals("todos") || tipo.equals("jogos")) {
                resultadosJogos = jogoApiRepository.findAll();
            }
            if (tipo.equals("todos") || tipo.equals("perfis")) {
                resultadosUsuarios = usuarioRepository.findAll();
            }
        }

        model.addAttribute("jogos", resultadosJogos);
        model.addAttribute("usuariosPesquisa", resultadosUsuarios);
        model.addAttribute("usuarioLogado", usuario);
        model.addAttribute("termoDePesquisa", query);
        model.addAttribute("tipoBusca", tipo); // Guarda qual filtro está ativo
        
        return "pesquisa"; 
    }

    // --- 3. CARREGAR A PÁGINA ESPECÍFICA DO JOGO ---
    @GetMapping("/jogo/{nomeJogo}")
    public String verDetalhesDoJogo(@PathVariable String nomeJogo, HttpSession session, Model model) {
        
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) return "redirect:/index.html";
        model.addAttribute("usuarioLogado", usuario);

        // Agora usamos o repositório para buscar pelo Nome em vez do ID
        JogoApi jogo = jogoApiRepository.findFirstByNomeJogoIgnoreCase(nomeJogo);
        
        // Se alguém digitar um jogo que não existe na barra, volta para o feed
        if (jogo == null) {
            return "redirect:/feed";
        }

        model.addAttribute("jogo", jogo);
        return "jogo"; 
    }
}