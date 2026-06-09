package br.com.ecommerce.model;

import java.math.BigDecimal;

public class Pedido {

    private Long id;
    private String nomeCliente;
    private Long idProduto;
    private int quantidade;
    private BigDecimal total;
    private StatusPedido status;


    public Pedido() {

    }

    public Pedido(Long id, String nomeCliente, Long idProduto, int quantidade, BigDecimal total, StatusPedido status) {
        this.id = id;
        this.nomeCliente = nomeCliente;
        this.idProduto = idProduto;
        this.quantidade = quantidade;
        this.total = total;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public Long getIdProduto() {
        return idProduto;
    }

    public void setIdProduto(Long idProduto) {
        this.idProduto = idProduto;
    }

    public int getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(int quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public StatusPedido getStatus() {
        return status;
    }

    public void setStatus(StatusPedido status) {
        this.status = status;
    }
}


