package com.checkpointz.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "postagemId")
    private Integer idPost;

    @Column(name = "postagemTexto", columnDefinition = "TEXT")
    private String texto;

    @Column(name = "postagemImagens", columnDefinition = "LONGTEXT")
    private String imagemUrl;

    // NOVAS COLUNAS QUE ESTAVAM A FALTAR E BLOQUEAVAM O SALVAMENTO
    @Column(name = "data", nullable = false)
    private LocalDate data;

    @Column(name = "hora", nullable = false)
    private LocalTime hora;

    @Column(name = "dataCriacao")
    private LocalDateTime dataCriacao;

    // COLUNAS DE REAÇÕES (Já pegando os defaults)
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

    // PREENCHE A DATA E HORA AUTOMATICAMENTE AO SALVAR
    @PrePersist
    protected void onCreate() {
        dataCriacao = LocalDateTime.now();
        data = LocalDate.now();
        hora = LocalTime.now();
    }

    public Post() {}

    // --- GETTERS E SETTERS COMPLETOS ---
    public Integer getIdPost() { return idPost; }
    public void setIdPost(Integer idPost) { this.idPost = idPost; }

    public String getTexto() { return texto; }
    public void setTexto(String texto) { this.texto = texto; }

    public String getImagemUrl() { return imagemUrl; }
    public void setImagemUrl(String imagemUrl) { this.imagemUrl = imagemUrl; }

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

    // ==========================================
    // NOVAS TABELAS DE MEMÓRIA PARA REAÇÕES ÚNICAS
    // ==========================================
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

    // ... (o resto do código continua igual, mas adicione os Getters/Setters novos lá no final):
    public java.util.Set<Usuario> getUsuariosQueGostaram() { return usuariosQueGostaram; }
    public void setUsuariosQueGostaram(java.util.Set<Usuario> usuariosQueGostaram) { this.usuariosQueGostaram = usuariosQueGostaram; }

    public java.util.Set<Usuario> getUsuariosQueDesgostaram() { return usuariosQueDesgostaram; }
    public void setUsuariosQueDesgostaram(java.util.Set<Usuario> usuariosQueDesgostaram) { this.usuariosQueDesgostaram = usuariosQueDesgostaram; }
}