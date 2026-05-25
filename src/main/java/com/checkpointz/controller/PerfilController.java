package com.checkpointz.controller;

import com.checkpointz.model.Usuario;
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
import java.util.Base64;
import java.util.List;

@Controller
public class PerfilController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @GetMapping("/perfil")
    public String exibirPerfil(HttpSession session, Model model) {
        Usuario usuarioSessao = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioSessao == null) return "redirect:/index.html";

        Usuario usuarioAtualizado = usuarioRepository.findById(usuarioSessao.getUserId()).orElse(null);
        model.addAttribute("usuario", usuarioAtualizado);
        
        return "perfil";
    }
    @GetMapping("/perfil/{username}")
    public String verPerfilPublico(@PathVariable String username, HttpSession session, Model model) {
        
        Usuario usuarioSessao = (Usuario) session.getAttribute("usuarioLogado");
        if (usuarioSessao == null) return "redirect:/index.html";

        // Busca o usuário pesquisado no banco de dados
        Usuario usuarioPerfil = usuarioRepository.findByUsername(username);
        
        // Se digitar um nome que não existe, volta para a pesquisa
        if (usuarioPerfil == null) {
            return "redirect:/pesquisar"; 
        }

        // Manda o usuário pesquisado para a tela
        model.addAttribute("usuario", usuarioPerfil);
        
        return "perfil"; // Reutilizamos a mesma tela HTML bonita que já criamos!
    }

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
        if (usuarioSessao == null) return "redirect:/index.html";

        Usuario usuarioBanco = usuarioRepository.findById(usuarioSessao.getUserId()).orElse(null);

        if (usuarioBanco != null) {
            usuarioBanco.setUsername(username);
            
            if (senha != null && !senha.trim().isEmpty()) {
                usuarioBanco.setSenha(senha);
            }

            // [LÓGICA DA FOTO MANTIDA IGUAL AO PASSO ANTERIOR AQUI...]
            if (fotoPerfilArquivo != null && !fotoPerfilArquivo.isEmpty()) {
                try {
                    String pastaUploads = "src/main/resources/static/uploads/";
                    java.nio.file.Path caminhoPasta = java.nio.file.Paths.get(pastaUploads);
                    if (!java.nio.file.Files.exists(caminhoPasta)) {
                        java.nio.file.Files.createDirectories(caminhoPasta);
                    }
                    String nomeArquivoUnico = java.util.UUID.randomUUID().toString() + "_" + fotoPerfilArquivo.getOriginalFilename();
                    java.nio.file.Path caminhoArquivo = caminhoPasta.resolve(nomeArquivoUnico);
                    fotoPerfilArquivo.transferTo(caminhoArquivo.toFile());
                    usuarioBanco.setFotoPerfil("/uploads/" + nomeArquivoUnico);
                } catch (IOException e) {
                    System.out.println("Erro: " + e.getMessage());
                }
            }

            usuarioBanco.setDescricao(descricao);
            usuarioBanco.setConquistaDestaque(conquistaDestaque);

            // --- NOVA LÓGICA DE MÚLTIPLAS PLATAFORMAS ---
            StringBuilder plataformasConstruidas = new StringBuilder();
            if (nomesPlataformas != null && linksPlataformas != null) {
                for (int i = 0; i < nomesPlataformas.size(); i++) {
                    String nome = nomesPlataformas.get(i).trim();
                    String link = linksPlataformas.get(i).trim();
                    if (!nome.isEmpty() && !link.isEmpty()) {
                        // Salva no formato: Nome|||Link###Nome|||Link
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
}