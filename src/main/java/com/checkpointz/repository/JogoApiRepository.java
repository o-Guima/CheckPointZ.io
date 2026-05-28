package com.checkpointz.repository;

import com.checkpointz.model.JogoApi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JogoApiRepository extends JpaRepository<JogoApi, Integer> {

    // 1. OFERTAS IMPERDÍVEIS: Nome corrigido para precosPromocoes
    @Query(value = "SELECT * FROM api_jogos_precos WHERE precosPromocoes IS NOT NULL ORDER BY precosPromocoes ASC LIMIT 5", nativeQuery = true)
    List<JogoApi> encontrarMelhoresOfertas();

    // 2. JOGOS EM ALTA: Nome corrigido para fotoJogo
    @Query(value = "SELECT * FROM api_jogos_precos WHERE fotoJogo IS NOT NULL ORDER BY RAND() LIMIT 5", nativeQuery = true)
    List<JogoApi> encontrarJogosAleatorios();

    // 3. PESQUISA: Este não precisa de Query manual, o Spring resolve sozinho
    List<JogoApi> findByNomeJogoContainingIgnoreCase(String nomeJogo);

    JogoApi findFirstByNomeJogoIgnoreCase(String nomeJogo);

    // Verifica se um jogo já existe pelo nome
    boolean existsByNomeJogo(String nomeJogo);
}