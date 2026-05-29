package com.checkpointz.model;

import jakarta.persistence.*;

@Entity
@Table(name = "usuarios")
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "userId")
    private Integer userId;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "senha", nullable = false)
    private String senha;

        @Column(name = "fotoPerfil", columnDefinition = "LONGTEXT")
    private String fotoPerfil;
    
    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "conquistaDestaque")
    private String conquistaDestaque;

    @Column(name = "plataformasConectaveis", columnDefinition = "LONGTEXT")
    private String plataformasConectaveis;

    public Usuario() {}

        public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public String getFotoPerfil() { return fotoPerfil; }
    public void setFotoPerfil(String fotoPerfil) { this.fotoPerfil = fotoPerfil; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getConquistaDestaque() { return conquistaDestaque; }
    public void setConquistaDestaque(String conquistaDestaque) { this.conquistaDestaque = conquistaDestaque; }

    public String getPlataformasConectaveis() { return plataformasConectaveis; }
    public void setPlataformasConectaveis(String plataformasConectaveis) { this.plataformasConectaveis = plataformasConectaveis; }

      @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "conexoes", 
        joinColumns = @JoinColumn(name = "seguidorId"),         inverseJoinColumns = @JoinColumn(name = "seguidoId")     )
    private java.util.List<Usuario> amigos;

        public java.util.List<Usuario> getAmigos() { return amigos; }
    public void setAmigos(java.util.List<Usuario> amigos) { this.amigos = amigos; }

    @Column(name = "verificado", columnDefinition = "boolean default false")
    private boolean verificado = false;

    @Column(name = "tokenVerificacao")
    private String tokenVerificacao;

        public boolean isVerificado() { return verificado; }
    public void setVerificado(boolean verificado) { this.verificado = verificado; }

    public String getTokenVerificacao() { return tokenVerificacao; }
    public void setTokenVerificacao(String tokenVerificacao) { this.tokenVerificacao = tokenVerificacao; }
}