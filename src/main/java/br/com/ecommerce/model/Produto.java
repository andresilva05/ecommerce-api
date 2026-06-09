package br.com.ecommerce.model;

import java.math.BigDecimal;

public class Produto {
    private Long id;
    private String nome;
    private String categoria;
    private int estoque;
    private boolean ativo;
    private BigDecimal preco;

    public Produto() {
    }

    public Produto(Long id, String nome, String categoria, int estoque, boolean ativo, BigDecimal preco) {
        this.id = id;
        this.nome = nome;
        this.categoria = categoria;
        this.estoque = estoque;
        this.ativo = ativo;
        this.preco = preco;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCategoria() {
        return categoria;
    }

    public void setCategoria(String categoria) {
        this.categoria = categoria;
    }

    public int getEstoque() {
        return estoque;
    }

    public void setEstoque(int estoque) {
        this.estoque = estoque;
    }

    public boolean isAtivo() {
        return ativo;
    }

    public void setAtivo(boolean ativo) {
        this.ativo = ativo;
    }

    public BigDecimal getPreco() {
        return preco;
    }

    public void setPreco(BigDecimal preco) {
        this.preco = preco;
    }
}
