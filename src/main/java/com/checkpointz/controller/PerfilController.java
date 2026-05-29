package com.checkpointz.controller;

import com.checkpointz.model.JogoApi;
import com.checkpointz.model.Post;
import com.checkpointz.model.Usuario;
import com.checkpointz.repository.JogoApiRepository;
import com.checkpointz.repository.PostRepository;
import com.checkpointz.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

@Controller
public class PerfilController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private JogoApiRepository jogoApiRepository;

    // --- 1. EXIBIR O PRÓPRIO PERFIL ---
    @GetMapping("/perfil")
    public String exibirPerfil(HttpSession session, Model model) {
        Usuario usuarioSessao = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioSessao == null) return "redirect:/index";

        Usuario usuarioAtualizado = usuarioRepository.findById(usuarioSessao.getUserId()).orElse(null);
        model.addAttribute("usuario", usuarioAtualizado);

        if (usuarioAtualizado != null) {
            List<Post> meusPosts = postRepository.findByUsuario_UserIdOrderByDataCriacaoDesc(usuarioAtualizado.getUserId());
            model.addAttribute("postsUsuario", meusPosts);
        }

        // Dropdown ordenado alfabeticamente
        List<JogoApi> todosJogos = jogoApiRepository.findAllByOrderByNomeJogoAsc();
        model.addAttribute("todosJogos", todosJogos);
        
        return "perfil";
    }

    // --- 2. EXIBIR O PERFIL DE OUTRA PESSOA ---
    @GetMapping("/perfil/{username}")
    public String verPerfilPublico(@PathVariable String username, HttpSession session, Model model) {
        Usuario usuarioSessao = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioSessao == null) return "redirect:/index";

        Usuario usuarioPerfil = usuarioRepository.findByUsername(username);
        if (usuarioPerfil == null) return "redirect:/pesquisar"; 

        model.addAttribute("usuario", usuarioPerfil);

        boolean jaConectado = false;
        Usuario eu = usuarioRepository.findById(usuarioSessao.getUserId()).orElse(null);
        if (eu != null && eu.getAmigos() != null) {
            jaConectado = eu.getAmigos().stream().anyMatch(amigo -> amigo.getUserId().equals(usuarioPerfil.getUserId()));
        }
        model.addAttribute("jaConectado", jaConectado);

        List<Post> postsDoUsuario = postRepository.findByUsuario_UserIdOrderByDataCriacaoDesc(usuarioPerfil.getUserId());
        model.addAttribute("postsUsuario", postsDoUsuario);

        // Dropdown ordenado alfabeticamente
        List<JogoApi> todosJogos = jogoApiRepository.findAllByOrderByNomeJogoAsc();
        model.addAttribute("todosJogos", todosJogos);
        
        return "perfil";
    }

    // --- 3. SALVAR EDIÇÃO DO PERFIL ---
    @PostMapping("/perfil/editar")
    public String salvarEdicaoPerfil(
            @RequestParam("username") String username,
            @RequestParam(value = "senha", required = false) String senha,
            @RequestParam(value = "fotoPerfilArquivo", required = false) MultipartFile fotoPerfilArquivo,
            @RequestParam("descricao") String descricao,
            @RequestParam("conquistaDestaque") String conquistaDestaque,
            @RequestParam(value = "nomePlataforma", required = false) List<String> nomesPlataformas,
            @RequestParam(value = "linkPlataforma", required = false) List<String> linksPlataformas,
            HttpSession session) {

        Usuario usuarioSessao = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioSessao == null) return "redirect:/index";

        Usuario usuarioBanco = usuarioRepository.findById(usuarioSessao.getUserId()).orElse(null);

        if (usuarioBanco != null) {
            usuarioBanco.setUsername(username);
            
            if (senha != null && !senha.trim().isEmpty()) {
                // Se a sua edição de perfil já tiver a senha BCrypt configurada, mantenha-a aqui!
                usuarioBanco.setSenha(senha);
            }

            if (fotoPerfilArquivo != null && !fotoPerfilArquivo.isEmpty()) {
                try {
                    byte[] bytesImagem = fotoPerfilArquivo.getBytes();
                    String base64Imagem = Base64.getEncoder().encodeToString(bytesImagem);
                    String tipoConteudo = fotoPerfilArquivo.getContentType();
                    String imagemFormatada = "data:" + tipoConteudo + ";base64," + base64Imagem;
                    usuarioBanco.setFotoPerfil(imagemFormatada);
                } catch (IOException e) {
                    System.out.println("Erro ao converter a imagem para Base64: " + e.getMessage());
                }
            }

            usuarioBanco.setDescricao(descricao);
            usuarioBanco.setConquistaDestaque(conquistaDestaque);

            StringBuilder plataformasConstruidas = new StringBuilder();
            if (nomesPlataformas != null && linksPlataformas != null) {
                for (int i = 0; i < nomesPlataformas.size(); i++) {
                    String nome = nomesPlataformas.get(i).trim();
                    String link = linksPlataformas.get(i).trim();
                    if (!nome.isEmpty() && !link.isEmpty()) {
                        plataformasConstruidas.append(nome).append("|||").append(link).append("###");
                    }
                }
            }
            usuarioBanco.setPlataformasConectaveis(plataformasConstruidas.toString());

            usuarioRepository.save(usuarioBanco);
            session.setAttribute("usuarioLogado", usuarioBanco);
        }
        return "redirect:/perfil";
    }

    // --- 4. CRIAR UMA NOVA PUBLICAÇÃO (MÚLTIPLAS IMAGENS VIA BASE64) ---
    @PostMapping("/post/novo")
    public String criarPost(@RequestParam("texto") String texto,
                            @RequestParam(value = "imagensArquivos", required = false) List<MultipartFile> imagensArquivos,
                            @RequestParam(value = "nomeJogoVinculado", required = false) String nomeJogoVinculado,
                            HttpSession session) {
        
        try {
            Usuario usuarioSessao = (Usuario) session.getAttribute("usuarioLogado");
            if (usuarioSessao == null) return "redirect:/index.html";

            Usuario eu = usuarioRepository.findById(usuarioSessao.getUserId()).orElse(null);
            if (eu == null) return "redirect:/index";

            Post novoPost = new Post();
            novoPost.setTexto(texto);
            novoPost.setUsuario(eu);

            // Vincula o jogo ignorando o "Nenhum Jogo"
            if (nomeJogoVinculado != null && !nomeJogoVinculado.trim().isEmpty() && !nomeJogoVinculado.equals("Nenhum Jogo (Opcional)")) {
                JogoApi jogo = jogoApiRepository.findFirstByNomeJogoIgnoreCase(nomeJogoVinculado.trim());
                if (jogo != null) {
                    novoPost.setJogoVinculado(jogo);
                }
            }

            // ==========================================
            // LÓGICA DE MÚLTIPLAS IMAGENS
            // ==========================================
            if (imagensArquivos != null && !imagensArquivos.isEmpty()) {
                for (MultipartFile img : imagensArquivos) {
                    if (!img.isEmpty()) {
                        byte[] bytesImagem = img.getBytes();
                        String base64Imagem = Base64.getEncoder().encodeToString(bytesImagem);
                        String tipoConteudo = img.getContentType();
                        // Adiciona a imagem formatada à lista de imagens do Post
                        novoPost.getImagensUrls().add("data:" + tipoConteudo + ";base64," + base64Imagem);
                    }
                }
            }

            postRepository.save(novoPost);

        } catch (Exception e) {
            System.out.println("ERRO AO SALVAR POST: " + e.getMessage());
        }

        return "redirect:/perfil"; 
    }

    // --- 5. SISTEMA DE CONEXÃO ---
    @PostMapping("/conectar/{amigoId}")
    public String conectarComAmigo(@PathVariable Integer amigoId, HttpSession session) {
        Usuario usuarioSessao = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioSessao == null) return "redirect:/index";

        Usuario eu = usuarioRepository.findById(usuarioSessao.getUserId()).orElse(null);
        Usuario amigo = usuarioRepository.findById(amigoId).orElse(null);

        if (eu != null && amigo != null) {
            if (eu.getAmigos() == null) {
                eu.setAmigos(new ArrayList<>());
            }
            boolean jaAmigo = eu.getAmigos().stream().anyMatch(a -> a.getUserId().equals(amigo.getUserId()));
            if (!jaAmigo) {
                eu.getAmigos().add(amigo);
                usuarioRepository.save(eu);
            }
        }
        return "redirect:/perfil/" + amigo.getUsername(); 
    }

    // --- 6. FAZER LOGOUT (SAIR) ---
    @GetMapping("/logout")
    public String fazerLogout(HttpSession session) {
        session.invalidate(); 
        return "redirect:/index"; 
    }
}