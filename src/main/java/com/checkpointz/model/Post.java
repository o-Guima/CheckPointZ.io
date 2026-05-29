package com.checkpointz.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "postagemId")
    private Integer idPost;

    @Column(name = "postagemTexto", columnDefinition = "TEXT")
    private String texto;

   
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "post_imagens", joinColumns = @JoinColumn(name = "postagemId"))
    @Column(name = "imagem_url", columnDefinition = "LONGTEXT")
    private List<String> imagensUrls = new ArrayList<>();

    @Column(name = "data", nullable = false)
    private LocalDate data;

    @Column(name = "hora", nullable = false)
    private LocalTime hora;

    @Column(name = "dataCriacao")
    private LocalDateTime dataCriacao;

    @Column(name = "reacaoGostei", columnDefinition = "int default 0")
    private Integer reacaoGostei = 0;

    @Column(name = "reacaoDesgostei", columnDefinition = "int default 0")
    private Integer reacaoDesgostei = 0;

    @ManyToOne
    @JoinColumn(name = "userId", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "jogo_id", nullable = true)
    private JogoApi jogoVinculado;

    
    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
        data = LocalDate.now();
        hora = LocalTime.now();
    }

    public Post() {}

    
    public Integer getIdPost() { return idPost; }
    public void setIdPost(Integer idPost) { this.idPost = idPost; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    // Novos Getters e Setters para a Lista de Imagens
    public List<String> getImagensUrls() { return imagensUrls; }
    public void setImagensUrls(List<String> imagensUrls) { this.imagensUrls = imagensUrls; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public LocalTime getHora() { return hora; }
    public void setHora(LocalTime hora) { this.hora = hora; }

    public LocalDateTime getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(LocalDateTime dataCriacao) { this.dataCriacao = dataCriacao; }

    public Integer getReacaoGostei() { return reacaoGostei; }
    public void setReacaoGostei(Integer reacaoGostei) { this.reacaoGostei = reacaoGostei; }

    public Integer getReacaoDesgostei() { return reacaoDesgostei; }
    public void setReacaoDesgostei(Integer reacaoDesgostei) { this.reacaoDesgostei = reacaoDesgostei; }
    
    public Usuario getUsuario() { return usuario; }
    public void setUsuario(Usuario usuario) { this.usuario = usuario; }

    public JogoApi getJogoVinculado() { return jogoVinculado; }
    public void setJogoVinculado(JogoApi jogoVinculado) { this.jogoVinculado = jogoVinculado; }

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "reacaoGostei",
        joinColumns = @JoinColumn(name = "postagemId"),
        inverseJoinColumns = @JoinColumn(name = "userId")
    )
    private java.util.Set<Usuario> usuariosQueGostaram = new java.util.HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "reacaoDesgostei",
        joinColumns = @JoinColumn(name = "postagemId"),
        inverseJoinColumns = @JoinColumn(name = "userId")
    )
    private java.util.Set<Usuario> usuariosQueDesgostaram = new java.util.HashSet<>();

    public java.util.Set<Usuario> getUsuariosQueGostaram() { return usuariosQueGostaram; }
    public void setUsuariosQueGostaram(java.util.Set<Usuario> usuariosQueGostaram) { this.usuariosQueGostaram = usuariosQueGostaram; }

    public java.util.Set<Usuario> getUsuariosQueDesgostaram() { return usuariosQueDesgostaram; }
    public void setUsuariosQueDesgostaram(java.util.Set<Usuario> usuariosQueDesgostaram) { this.usuariosQueDesgostaram = usuariosQueDesgostaram; }
}