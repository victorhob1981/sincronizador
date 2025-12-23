package com.sincronizador.application.dto;

import com.sincronizador.domain.model.SKU;

public class ProdutoCatalogoStatusDTO {

    private final SKU sku;
    private final String nomeProduto;
    private final EstadoProdutoCatalogo estado;

    public ProdutoCatalogoStatusDTO(
            SKU sku,
            String nomeProduto,
            EstadoProdutoCatalogo estado
    ) {
        this.sku = sku;
        this.nomeProduto = nomeProduto;
        this.estado = estado;
    }

    public SKU getSku() {
        return sku;
    }

    public String getNomeProduto() {
        return nomeProduto;
    }

    public EstadoProdutoCatalogo getEstado() {
        return estado;
    }
}
