package com.sincronizador.domain.model;

import java.util.Objects;

public class ItemDeCatalogo {

    private final SKU sku;
    private final Disponibilidade disponibilidade;
    private final String idExterno;

    public ItemDeCatalogo(SKU sku, Disponibilidade disponibilidade, String idExterno) {
        this.sku = Objects.requireNonNull(sku, "sku não pode ser nulo");
        this.disponibilidade = Objects.requireNonNull(disponibilidade, "disponibilidade não pode ser nula");
        this.idExterno = Objects.requireNonNull(idExterno, "idExterno não pode ser nulo");
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
