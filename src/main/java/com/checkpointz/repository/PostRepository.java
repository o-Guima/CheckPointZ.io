package com.checkpointz.repository;

import com.checkpointz.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {
    
    List<Post> findAllByOrderByDataCriacaoDesc();
    
    List<Post> findByUsuario_UserIdOrderByDataCriacaoDesc(Integer userId);

    // NOVA LINHA: Busca posts onde o ID do jogo vinculado seja igual ao que estamos a ver
    List<Post> findByJogoVinculado_IdJogoOrderByDataCriacaoDesc(Integer idJogo);
}