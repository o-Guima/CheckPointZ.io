package com.checkpointz.controller;

import com.checkpointz.model.JogoApi;
import com.checkpointz.model.Post;
import com.checkpointz.model.Usuario;
import com.checkpointz.repository.JogoApiRepository;
import com.checkpointz.repository.PostRepository;
import com.checkpointz.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class FeedController {

    @Autowired
    private JogoApiRepository jogoApiRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PostRepository postRepository;

    // --- 1. CARREGAR O FEED ---
    @GetMapping("/feed")
    public String exibirFeed(HttpSession session, Model model) {
        
        Usuario usuarioSessao = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioSessao == null) {
            return "redirect:/index.html"; 
        }
        
        // Recarrega o usuário do banco para garantir que a lista de amigos na barra lateral esteja sempre atualizada
        Usuario usuarioAtualizado = usuarioRepository.findById(usuarioSessao.getUserId()).orElse(null);
        model.addAttribute("usuarioLogado", usuarioAtualizado);

        List<JogoApi> todosOsJogos = jogoApiRepository.findAll();

        // LÓGICA DE OFERTAS: Filtra promoções reais e ordena pelos mais caros, pegando os 7 primeiros
        List<JogoApi> ofertasComCalculo = todosOsJogos.stream()
            .filter(j -> j.getPrecosOriginais() != null 
                      && j.getPrecosPromocoes() != null 
                      && j.getPrecosOriginais().compareTo(BigDecimal.ZERO) > 0
                      && j.getPrecosPromocoes().compareTo(j.getPrecosOriginais()) < 0)
            .sorted((j1, j2) -> j2.getPrecosOriginais().compareTo(j1.getPrecosOriginais()))
            .limit(7) 
            .collect(Collectors.toList());

        List<JogoApi> emAlta = jogoApiRepository.encontrarJogosAleatorios();

        // Busca as publicações (posts) globais reais do banco de dados para a linha do tempo
        List<Post> feedPosts = postRepository.findAllByOrderByDataCriacaoDesc();

        model.addAttribute("ofertas", ofertasComCalculo);
        model.addAttribute("emAlta", emAlta);
        model.addAttribute("feedPosts", feedPosts);
        
        return "feed"; 
    }

    // --- 2. A TELA DE PESQUISA AVANÇADA ---
    @GetMapping("/pesquisar")
    public String pesquisar(@RequestParam(value = "q", required = false) String query, 
                            @RequestParam(value = "tipo", defaultValue = "todos") String tipo,
                            Model model, HttpSession session) {
        
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) return "redirect:/index.html";

        List<JogoApi> resultadosJogos = null;
        List<Usuario> resultadosUsuarios = null;

        if (query != null && !query.isEmpty()) {
            if (tipo.equals("todos") || tipo.equals("jogos")) {
                resultadosJogos = jogoApiRepository.findByNomeJogoContainingIgnoreCase(query);
            }
            if (tipo.equals("todos") || tipo.equals("perfis")) {
                resultadosUsuarios = usuarioRepository.findByUsernameContainingIgnoreCase(query);
            }
        } else {
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
        model.addAttribute("tipoBusca", tipo); 
        
        return "pesquisa"; 
    }

    // --- 3. CARREGAR A PÁGINA ESPECÍFICA DO JOGO ---
    @GetMapping("/jogo/{nomeJogo}")
    public String verDetalhesDoJogo(@PathVariable String nomeJogo, HttpSession session, Model model) {
        
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) return "redirect:/index.html";
        model.addAttribute("usuarioLogado", usuario);

        JogoApi jogo = jogoApiRepository.findFirstByNomeJogoIgnoreCase(nomeJogo);
        
        if (jogo == null) {
            return "redirect:/feed";
        }

        // Busca os posts que mencionam especificamente este jogo para a secção de "O Que Estão Dizendo"
        List<Post> postsDoJogo = postRepository.findByJogoVinculado_IdJogoOrderByDataCriacaoDesc(jogo.getIdJogo());

        model.addAttribute("jogo", jogo);
        model.addAttribute("postsDoJogo", postsDoJogo);
        return "jogo"; 
    }

   // --- 4. REAÇÕES COM LIMITE DE 1 POR USUÁRIO (TIPO REDE SOCIAL) ---
    @PostMapping("/post/{id}/reagir")
    @ResponseBody
    public ResponseEntity<java.util.Map<String, Integer>> reagirPost(@PathVariable Integer id, @RequestParam("tipo") String tipo, HttpSession session) {
        
        Usuario usuarioSessao = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioSessao == null) return ResponseEntity.status(401).build();

        Post post = postRepository.findById(id).orElse(null);
        
        if (post != null) {
            Integer meuId = usuarioSessao.getUserId();
            
            // Verifica se o usuário já clicou antes
            boolean jaGostou = post.getUsuariosQueGostaram().stream().anyMatch(u -> u.getUserId().equals(meuId));
            boolean jaDesgostou = post.getUsuariosQueDesgostaram().stream().anyMatch(u -> u.getUserId().equals(meuId));

            if ("gostei".equals(tipo)) {
                if (jaGostou) {
                    // Se já tinha curtido, Clica de novo para REMOVER (Descurtir)
                    post.getUsuariosQueGostaram().removeIf(u -> u.getUserId().equals(meuId));
                } else {
                    // Curte o post e remove o dislike caso existisse
                    post.getUsuariosQueGostaram().add(usuarioSessao);
                    post.getUsuariosQueDesgostaram().removeIf(u -> u.getUserId().equals(meuId));
                }
            } else if ("desgostei".equals(tipo)) {
                if (jaDesgostou) {
                    // Se já tinha dado dislike, Clica de novo para REMOVER
                    post.getUsuariosQueDesgostaram().removeIf(u -> u.getUserId().equals(meuId));
                } else {
                    // Dá dislike e remove o like caso existisse
                    post.getUsuariosQueDesgostaram().add(usuarioSessao);
                    post.getUsuariosQueGostaram().removeIf(u -> u.getUserId().equals(meuId));
                }
            }
            
            // Atualiza os números matemáticos reais baseados em quantas pessoas estão na lista
            post.setReacaoGostei(post.getUsuariosQueGostaram().size());
            post.setReacaoDesgostei(post.getUsuariosQueDesgostaram().size());
            
            postRepository.save(post);
            
            // Devolve os números atualizados para o HTML brilhar
            java.util.Map<String, Integer> resposta = new java.util.HashMap<>();
            resposta.put("gostei", post.getReacaoGostei());
            resposta.put("desgostei", post.getReacaoDesgostei());
            
            return ResponseEntity.ok(resposta);
        }
        
        return ResponseEntity.notFound().build();
    }
}