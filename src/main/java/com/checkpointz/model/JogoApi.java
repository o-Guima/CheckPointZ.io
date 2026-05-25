package com.checkpointz.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "api_jogos_precos")
public class JogoApi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idJogo")
    private Integer idJogo;

    @Column(name = "nomeJogo")
    private String nomeJogo;

    @Column(name = "descricao", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "fotoJogo")
    private String fotoJogo;

    @Column(name = "precosOriginais")
    private BigDecimal precosOriginais;

    @Column(name = "precosPromocoes")
    private BigDecimal precosPromocoes;

    // --- A NOSSA NOVA COLUNA ---
    @Column(name = "lojaPromocao")
    private String lojaPromocao;

    @Column(name = "generos")
    private String generos;

    @Column(name = "distribuidora")
    private String distribuidora;

    @Column(name = "link_steam")
    private String linkSteam;

    @Column(name = "link_xbox")
    private String linkXbox;

    @Column(name = "link_playstation")
    private String linkPlaystation;

    @OneToMany(mappedBy = "jogo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<JogoImagem> imagens;

    public JogoApi() {}

    // Getters e Setters
    public Integer getIdJogo() { return idJogo; }
    public void setIdJogo(Integer idJogo) { this.idJogo = idJogo; }

    public String getNomeJogo() { return nomeJogo; }
    public void setNomeJogo(String nomeJogo) { this.nomeJogo = nomeJogo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public String getFotoJogo() { return fotoJogo; }
    public void setFotoJogo(String fotoJogo) { this.fotoJogo = fotoJogo; }

    public BigDecimal getPrecosOriginais() { return precosOriginais; }
    public void setPrecosOriginais(BigDecimal precosOriginais) { this.precosOriginais = precosOriginais; }

    public BigDecimal getPrecosPromocoes() { return precosPromocoes; }
    public void setPrecosPromocoes(BigDecimal precosPromocoes) { this.precosPromocoes = precosPromocoes; }

    public String getLojaPromocao() { return lojaPromocao; }
    public void setLojaPromocao(String lojaPromocao) { this.lojaPromocao = lojaPromocao; }

    public String getGeneros() { return generos; }
    public void setGeneros(String generos) { this.generos = generos; }

    public String getDistribuidora() { return distribuidora; }
    public void setDistribuidora(String distribuidora) { this.distribuidora = distribuidora; }

    public String getLinkSteam() { return linkSteam; }
    public void setLinkSteam(String linkSteam) { this.linkSteam = linkSteam; }

    public String getLinkXbox() { return linkXbox; }
    public void setLinkXbox(String linkXbox) { this.linkXbox = linkXbox; }

    public String getLinkPlaystation() { return linkPlaystation; }
    public void setLinkPlaystation(String linkPlaystation) { this.linkPlaystation = linkPlaystation; }

    public List<JogoImagem> getImagens() { return imagens; }
    public void setImagens(List<JogoImagem> imagens) { this.imagens = imagens; }
}