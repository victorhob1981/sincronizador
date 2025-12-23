package com.sincronizador.application.usecase;

import com.sincronizador.application.dto.EstadoProdutoCatalogo;
import com.sincronizador.application.dto.ProdutoCatalogoStatusDTO;
import com.sincronizador.application.port.CatalogoReader;
import com.sincronizador.application.port.EstoqueReader;
import com.sincronizador.domain.model.*;
import com.sincronizador.domain.service.ComparadorDeTamanhos;

import java.util.*;

public class GerarStatusDoCatalogoUseCase {

    private final EstoqueReader estoqueReader;
    private final CatalogoReader catalogoReader;

    public GerarStatusDoCatalogoUseCase(
            EstoqueReader estoqueReader,
            CatalogoReader catalogoReader
    ) {
        this.estoqueReader = estoqueReader;
        this.catalogoReader = catalogoReader;
    }

    public List<ProdutoCatalogoStatusDTO> executar() {

        List<Disponibilidade> disponibilidadesERP = estoqueReader.obterDisponibilidades();
        List<ItemDeCatalogo> itensCatalogo = catalogoReader.obterItens();

        Map<SKU, Disponibilidade> erpPorSku = new HashMap<>();
        for (Disponibilidade d : disponibilidadesERP) {
            erpPorSku.put(d.getSku(), d);
        }

        Map<SKU, ItemDeCatalogo> catalogoPorSku = new HashMap<>();
        for (ItemDeCatalogo i : itensCatalogo) {
            catalogoPorSku.put(i.getSku(), i);
        }

        Set<SKU> todosOsSkus = new HashSet<>();
        todosOsSkus.addAll(erpPorSku.keySet());
        todosOsSkus.addAll(catalogoPorSku.keySet());

        ComparadorDeTamanhos comparadorDeTamanhos = new ComparadorDeTamanhos();

        List<ProdutoCatalogoStatusDTO> resultado = new ArrayList<>();

        for (SKU sku : todosOsSkus) {

            Disponibilidade erp = erpPorSku.get(sku);
            ItemDeCatalogo catalogo = catalogoPorSku.get(sku);

            EstadoProdutoCatalogo estado;

            if (erp != null && catalogo == null) {
                estado = EstadoProdutoCatalogo.SEM_IMAGEM;
            } else if (erp == null && catalogo != null) {
                estado = EstadoProdutoCatalogo.ORFAO;
            } else {
                ResultadoComparacaoTamanhos comparacao =
                        comparadorDeTamanhos.comparar(erp, catalogo);

                estado = (comparacao == ResultadoComparacaoTamanhos.IGUAIS)
                        ? EstadoProdutoCatalogo.OK
                        : EstadoProdutoCatalogo.DESATUALIZADO;
            }

            String nomeProduto = sku.toString();

            resultado.add(new ProdutoCatalogoStatusDTO(
                    sku,
                    nomeProduto,
                    estado
            ));
        }

        return resultado;
    }
}
