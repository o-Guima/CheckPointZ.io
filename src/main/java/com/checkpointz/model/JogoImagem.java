package com.checkpointz.model;

import jakarta.persistence.*;

@Entity
@Table(name = "jogo_imagens")
public class JogoImagem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "url_imagem")
    private String urlImagem;

   
    @ManyToOne
    @JoinColumn(name = "id_jogo") 
    private JogoApi jogo;

    public JogoImagem() {}

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getUrlImagem() { return urlImagem; }
    public void setUrlImagem(String urlImagem) { this.urlImagem = urlImagem; }

    public JogoApi getJogo() { return jogo; }
    public void setJogo(JogoApi jogo) { this.jogo = jogo; }
}