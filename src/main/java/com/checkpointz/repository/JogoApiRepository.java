package com.checkpointz.repository;

import com.checkpointz.model.JogoApi;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JogoApiRepository extends JpaRepository<JogoApi, Integer> {

        @Query(value = "SELECT * FROM api_jogos_precos WHERE precosPromocoes IS NOT NULL ORDER BY precosPromocoes ASC LIMIT 5", nativeQuery = true)
    List<JogoApi> encontrarMelhoresOfertas();

        @Query(value = "SELECT * FROM api_jogos_precos WHERE fotoJogo IS NOT NULL ORDER BY RAND() LIMIT 5", nativeQuery = true)
    List<JogoApi> encontrarJogosAleatorios();

        List<JogoApi> findByNomeJogoContainingIgnoreCase(String nomeJogo);

    JogoApi findFirstByNomeJogoIgnoreCase(String nomeJogo);

        boolean existsByNomeJogo(String nomeJogo);

        List<JogoApi> findAllByOrderByNomeJogoAsc();
}