package com.sincronizador.application.usecase;

import com.sincronizador.application.port.CatalogoReader;
import com.sincronizador.application.port.CatalogoWriter;
import com.sincronizador.application.port.EstoqueReader;
import com.sincronizador.domain.model.Disponibilidade;
import com.sincronizador.domain.model.ItemDeCatalogo;
import com.sincronizador.domain.model.SKU;
import com.sincronizador.domain.service.GeradorDeLegenda;

import java.io.File;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class AssociarImagemAoCatalogoUseCase {

    private final EstoqueReader estoqueReader;
    private final CatalogoReader catalogoReader;
    private final CatalogoWriter catalogoWriter;

    public AssociarImagemAoCatalogoUseCase(
            EstoqueReader estoqueReader,
            CatalogoReader catalogoReader,
            CatalogoWriter catalogoWriter
    ) {
        this.estoqueReader = Objects.requireNonNull(estoqueReader);
        this.catalogoReader = Objects.requireNonNull(catalogoReader);
        this.catalogoWriter = Objects.requireNonNull(catalogoWriter);
    }

    public void executar(SKU sku, File imagemLocal) {
        Objects.requireNonNull(sku, "sku não pode ser nulo");
        Objects.requireNonNull(imagemLocal, "imagemLocal não pode ser nulo");

        // 1) garantir que existe no ERP (pra termos tamanhos/legenda)
        Disponibilidade disponibilidadeErp = estoqueReader.obterDisponibilidades()
                .stream()
                .filter(d -> d.getSku().equals(sku))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Esse produto não existe no ERP: " + sku));

        // 2) procurar se já existe no catálogo (Drive) por SKU
        List<ItemDeCatalogo> itens = catalogoReader.obterItens();
        Optional<ItemDeCatalogo> existente = itens.stream()
                .filter(i -> i.getSku().equals(sku))
                .findFirst();

        String legendaAtual = GeradorDeLegenda.gerarLegenda(disponibilidadeErp);

        if (existente.isPresent()) {
            // Troca imagem mantendo a associação fixa (fileId)
            String fileId = existente.get().getIdExterno();
            catalogoWriter.trocarImagem(fileId, imagemLocal);

            // reforça metadados (garante identidade) e atualiza legenda
            catalogoWriter.vincularSku(fileId, sku);
            catalogoWriter.atualizarLegenda(fileId, legendaAtual);

        } else {
            // Cria novo no Drive (já com metadados + legenda)
            catalogoWriter.criarComImagemLocal(sku, disponibilidadeErp, imagemLocal);
        }
    }
}
