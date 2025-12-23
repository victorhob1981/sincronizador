package com.sincronizador.domain.service;

import com.sincronizador.domain.model.Disponibilidade;
import com.sincronizador.domain.model.SKU;
import com.sincronizador.domain.valueobject.Tamanho;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.stream.Collectors;

public class GeradorDeLegenda {

    public static String gerarLegenda(Disponibilidade disponibilidade) {
        SKU sku = disponibilidade.getSku();

        String clube = normalizar(sku.getClube())
                .substring(0, 3)
                .toUpperCase();

        String tamanhos = disponibilidade.getTamanhosDisponiveis()
                .stream()
                .sorted(Comparator.comparing(Enum::name))
                .map(Tamanho::getValorExibicao)
                .collect(Collectors.joining(", "));

        return clube + " - " + tamanhos;
    }

    private static String normalizar(String texto) {
        String normalizado = Normalizer.normalize(texto, Normalizer.Form.NFD);
        return normalizado.replaceAll("[^\\p{ASCII}]", "");
    }
}
