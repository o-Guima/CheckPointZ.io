package com.checkpointz.service;

import com.checkpointz.model.JogoApi;
import com.checkpointz.model.JogoImagem;
import com.checkpointz.repository.JogoApiRepository;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Service
public class JogoApiService {

    @Autowired
    private JogoApiRepository jogoApiRepository;

    @Value("${api.ggdeals.key}")
    private String GGDEALS_API_KEY;

    @Value("${api.rawg.key}")
    private String RAWG_API_KEY;

    @Value("${api.steam.key}")
    private String STEAM_API_KEY;

    private List<String> idsParaProcessar = new ArrayList<>();
    private int indiceAtual = 0;

    @PostConstruct
    public void carregarTodosIdsDaSteam() {
        try {
            System.out.println("A iniciar a transferência da lista da Steam...");
            RestTemplate restTemplate = new RestTemplate();
            
     
            String steamUrl = "https://api.steampowered.com/IStoreService/GetAppList/v1/?key=" + STEAM_API_KEY + "&max_results=50000";
            
            JsonNode root = restTemplate.getForObject(steamUrl, JsonNode.class);
            
            if (root != null) {
                JsonNode apps = root.path("response").path("apps");
                for (JsonNode app : apps) {
                    idsParaProcessar.add(String.valueOf(app.path("appid").asInt()));
                }
                Collections.reverse(idsParaProcessar);
                System.out.println("Sucesso! " + idsParaProcessar.size() + " IDs da Steam carregados.");
            }
        } catch (Exception e) {
            System.out.println("Erro ao contactar a Steam: " + e.getMessage());
        }
    }

    @Transactional 
    @Scheduled(fixedDelay = 60000)
    public void roboMestre() {
        if (idsParaProcessar.isEmpty() || indiceAtual >= idsParaProcessar.size()) return;

        List<JogoApi> jogosNoBanco = jogoApiRepository.findAll();
        for (JogoApi j : jogosNoBanco) {
            
            boolean precisaAtualizar = false;
            
            
            if (j.getPrecosPromocoes() != null && j.getPrecosOriginais() != null) {
                if (j.getPrecosPromocoes().compareTo(j.getPrecosOriginais()) >= 0) {
                    j.setPrecosPromocoes(null);
                    j.setLojaPromocao(null);
                    precisaAtualizar = true;
                }
            }

            
            if (j.getDistribuidora() == null || j.getDescricao() == null || j.getDescricao().contains("Link oficial GG.deals")) {
                precisaAtualizar = true;
            } else if (j.getLinkPlaystation() == null) {
                precisaAtualizar = true; 
            }

            if (precisaAtualizar) {
                System.out.println("🔧 A limpar erros antigos e atualizar jogo: " + j.getNomeJogo());
                
                if (j.getLinkXbox() == null) {
                    String nomeFormatado = j.getNomeJogo().replace(" ", "+");
                    j.setLinkXbox("https://www.xbox.com/pt-BR/Search?q=" + nomeFormatado);
                    j.setLinkPlaystation("https://store.playstation.com/pt-br/search/" + j.getNomeJogo().replace(" ", "%20"));
                    j.setLinkSteam("https://store.steampowered.com/search/?term=" + nomeFormatado);
                }

                RestTemplate restTemplate = new RestTemplate();
                if (j.getDistribuidora() == null || j.getDistribuidora().equals("Estúdio Desconhecido")) {
                    buscarDetalhesCompletosDaRawg(j, restTemplate);
                }
                
                jogoApiRepository.save(j);
                System.out.println("✅ Jogo '" + j.getNomeJogo() + "' corrigido com sucesso!");
                return; 
            }
        }

        processarNovoLote();
    }

    private void processarNovoLote() {
        int limite = Math.min(indiceAtual + 15, idsParaProcessar.size());
        List<String> loteAtual = idsParaProcessar.subList(indiceAtual, limite);
        String idsFormatados = String.join(",", loteAtual);

        RestTemplate restTemplate = new RestTemplate();
        String ggDealsUrl = "https://api.gg.deals/v1/prices/by-steam-app-id/?ids=" + idsFormatados + "&key=" + GGDEALS_API_KEY + "&country=BR&currency=BRL";

        try {
            JsonNode ggRoot = restTemplate.getForObject(ggDealsUrl, JsonNode.class);

            if (ggRoot != null && ggRoot.path("success").asBoolean()) {
                JsonNode dataNode = ggRoot.path("data");
                Iterator<Map.Entry<String, JsonNode>> campos = dataNode.fields();

                while (campos.hasNext()) {
                    Map.Entry<String, JsonNode> campo = campos.next();
                    JsonNode jogoGg = campo.getValue();

                    if (jogoGg.isNull()) continue; 

                    String steamAppId = campo.getKey();
                    String nomeDoJogo = jogoGg.path("title").asText();
                    
                    
                    
                    
                    
                    if (jogoApiRepository.existsByNomeJogo(nomeDoJogo)) {
                        System.out.println("⚠️ O jogo '" + nomeDoJogo + "' já existe. Pulando...");
                        continue; 
                    }

                    BigDecimal precoOficialSteam = BigDecimal.valueOf(jogoGg.path("prices").path("currentRetail").asDouble(0.0));
                    BigDecimal precoMercadoCinza = BigDecimal.valueOf(jogoGg.path("prices").path("currentKeyshops").asDouble(0.0));
                    
                    BigDecimal precoBase = precoOficialSteam; 
                    BigDecimal precoVencedor = null; 
                    String lojaVencedora = null;

                    if (precoMercadoCinza.compareTo(BigDecimal.ZERO) > 0 && precoMercadoCinza.compareTo(precoOficialSteam) < 0) {
                        precoVencedor = precoMercadoCinza;
                        lojaVencedora = "Keyshops";
                    }

                    
                    JogoApi jogoParaSalvar = new JogoApi();
                    jogoParaSalvar.setNomeJogo(nomeDoJogo);
                    jogoParaSalvar.setPrecosOriginais(precoBase);
                    jogoParaSalvar.setPrecosPromocoes(precoVencedor);
                    jogoParaSalvar.setLojaPromocao(lojaVencedora);

                    jogoParaSalvar.setLinkSteam("https://store.steampowered.com/app/" + steamAppId);
                    jogoParaSalvar.setLinkXbox("https://www.xbox.com/pt-BR/Search?q=" + nomeDoJogo.replace(" ", "+"));
                    jogoParaSalvar.setLinkPlaystation("https://store.playstation.com/pt-br/search/" + nomeDoJogo.replace(" ", "%20"));

                    buscarDetalhesCompletosDaRawg(jogoParaSalvar, restTemplate);

                    if (jogoParaSalvar.getDistribuidora() != null) {
                        
                        
                        jogoApiRepository.saveAndFlush(jogoParaSalvar);
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Erro no processamento do lote: " + e.getMessage());
        }

        indiceAtual += 15;
    }

    private void buscarDetalhesCompletosDaRawg(JogoApi jogo, RestTemplate restTemplate) {
        String rawgSearchUrl = "https://api.rawg.io/api/games?key=" + RAWG_API_KEY + "&search={query}&page_size=1";
        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Checkpointz-App");
            HttpEntity<String> entity = new HttpEntity<>(headers);

            var response = restTemplate.exchange(rawgSearchUrl, HttpMethod.GET, entity, JsonNode.class, jogo.getNomeJogo());
            JsonNode rawgRoot = response.getBody();

            if (rawgRoot != null && rawgRoot.path("results").isArray() && !rawgRoot.path("results").isEmpty()) {
                JsonNode primeiroResultado = rawgRoot.path("results").get(0);
                
                int rawgId = primeiroResultado.path("id").asInt();
                jogo.setFotoJogo(primeiroResultado.path("background_image").asText(null));

                JsonNode genresNode = primeiroResultado.path("genres");
                List<String> listaGeneros = new ArrayList<>();
                if (genresNode.isArray()) {
                    for (JsonNode g : genresNode) {
                        listaGeneros.add(g.path("name").asText());
                    }
                }
                jogo.setGeneros(String.join(", ", listaGeneros));

                JsonNode screenshots = primeiroResultado.path("short_screenshots");
                List<JogoImagem> galeria = new ArrayList<>();
                if (screenshots.isArray()) {
                    int contador = 0;
                    for (JsonNode shot : screenshots) {
                        if (contador >= 3) break; 
                        String url = shot.path("image").asText(null);
                        if (url != null) {
                            JogoImagem img = new JogoImagem();
                            img.setUrlImagem(url);
                            img.setJogo(jogo); 
                            galeria.add(img);
                            contador++;
                        }
                    }
                }
                if (jogo.getImagens() != null) {
                    jogo.getImagens().clear();
                    jogo.getImagens().addAll(galeria);
                } else {
                    jogo.setImagens(galeria);
                }

                String rawgDetailsUrl = "https://api.rawg.io/api/games/" + rawgId + "?key=" + RAWG_API_KEY;
                var detailsResponse = restTemplate.exchange(rawgDetailsUrl, HttpMethod.GET, entity, JsonNode.class);
                JsonNode detailsRoot = detailsResponse.getBody();

                if (detailsRoot != null) {
                    String descricaoReal = detailsRoot.path("description_raw").asText(null);
                    if (descricaoReal == null || descricaoReal.isEmpty()) {
                        descricaoReal = "Descrição detalhada não fornecida pela distribuidora oficial.";
                    }
                    jogo.setDescricao(descricaoReal);

                    JsonNode publishersNode = detailsRoot.path("publishers");
                    List<String> listaPublishers = new ArrayList<>();
                    if (publishersNode.isArray()) {
                        for (JsonNode p : publishersNode) {
                            listaPublishers.add(p.path("name").asText());
                        }
                    }
                    
                    if (!listaPublishers.isEmpty()) {
                        jogo.setDistribuidora(String.join(", ", listaPublishers));
                    } else {
                        JsonNode devNode = detailsRoot.path("developers");
                        if (devNode.isArray() && !devNode.isEmpty()) {
                            jogo.setDistribuidora(devNode.get(0).path("name").asText());
                        } else {
                            jogo.setDistribuidora("Estúdio Independente");
                        }
                    }
                }
            } else {
                jogo.setDistribuidora("Estúdio Desconhecido");
                jogo.setDescricao("Jogo não catalogado na base de dados internacional.");
            }
        } catch (Exception e) {
            System.out.println("Falha ao buscar detalhes complexos na RAWG para: " + jogo.getNomeJogo());
            jogo.setDistribuidora("Estúdio Desconhecido");
            jogo.setDescricao("Erro de comunicação com os servidores globais.");
        }
    }
}