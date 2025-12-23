package com.sincronizador.domain.model;

public class ItemDeCatalogo {

    private final SKU sku;
    private final Disponibilidade disponibilidade;
    private final String idExterno; 

    public ItemDeCatalogo(SKU sku, Disponibilidade disponibilidade, String idExterno) {
        this.sku = sku;
        this.disponibilidade = disponibilidade;
        this.idExterno = idExterno;
    }

    public SKU getSku() {
        return sku;
    }

    public Disponibilidade getDisponibilidade() {
        return disponibilidade;
    }

    public String getIdExterno() {
        return idExterno;
    }

    public boolean estaAtivo() {
        return disponibilidade.estaDisponivel();
    }
}
