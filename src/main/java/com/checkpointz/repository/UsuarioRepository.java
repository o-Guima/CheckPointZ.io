package com.checkpointz.repository;

import com.checkpointz.model.Usuario;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
    
        List<Usuario> findByUsernameContainingIgnoreCase(String username);

    Usuario findByUsername(String username);

}