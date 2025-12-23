package com.sincronizador.application.usecase;

import com.sincronizador.application.dto.ResultadoSincronizacaoDTO;
import com.sincronizador.application.port.CatalogoReader;
import com.sincronizador.application.port.CatalogoWriter;
import com.sincronizador.application.port.EstoqueReader;
import com.sincronizador.domain.model.Disponibilidade;
import com.sincronizador.domain.model.ItemDeCatalogo;
import com.sincronizador.domain.model.ResultadoComparacaoTamanhos;
import com.sincronizador.domain.model.SKU;
import com.sincronizador.domain.service.ComparadorDeTamanhos;
import com.sincronizador.domain.service.GeradorDeLegenda;

import java.util.*;

public class SincronizarCatalogoUseCase {

    private final EstoqueReader estoqueReader;
    private final CatalogoReader catalogoReader;
    private final CatalogoWriter catalogoWriter;

    public SincronizarCatalogoUseCase(
            EstoqueReader estoqueReader,
            CatalogoReader catalogoReader,
            CatalogoWriter catalogoWriter
    ) {
        this.estoqueReader = estoqueReader;
        this.catalogoReader = catalogoReader;
        this.catalogoWriter = catalogoWriter;
    }

    public ResultadoSincronizacaoDTO executar() {

        List<Disponibilidade> disponibilidadesERP = estoqueReader.obterDisponibilidades();
        List<ItemDeCatalogo> itensCatalogo = catalogoReader.obterItens();

        Map<SKU, Disponibilidade> erpPorSku = new HashMap<>();
        for (Disponibilidade d : disponibilidadesERP) {
            if (d != null && d.getSku() != null) {
                erpPorSku.put(d.getSku(), d);
            }
        }

        Map<SKU, ItemDeCatalogo> catalogoPorSku = new HashMap<>();
        for (ItemDeCatalogo i : itensCatalogo) {
            if (i != null && i.getSku() != null) {
                catalogoPorSku.put(i.getSku(), i);
            }
        }

        int atualizados = 0;
        int removidos = 0;
        int pendentesCriacaoSemImagem = 0;
        List<String> erros = new ArrayList<>();

        // Comparador (instância: compila tanto se o método for static quanto se for não-static)
        ComparadorDeTamanhos comparador = new ComparadorDeTamanhos();

        // 1) REMOVER: arquivo no Drive cujo SKU não existe mais no ERP
        for (ItemDeCatalogo item : itensCatalogo) {
            if (item == null || item.getSku() == null) continue;

            SKU sku = item.getSku();

            // Segurança: se estiver usando "CATALOGO" como placeholder, não remove automaticamente
            // (evita apagar itens antigos/manuais sem identidade ainda)
            if (ehPlaceholderCatalogo(sku)) {
                continue;
            }

            if (!erpPorSku.containsKey(sku)) {
                try {
                    catalogoWriter.remover(item.getIdExterno());
                    removidos++;
                } catch (Exception e) {
                    erros.add("REMOVER " + sku + " -> " + resumirErro(e));
                }
            }
        }

        // 2) ATUALIZAR: quando existe no ERP e no Drive, mas tamanhos diferem
        for (Map.Entry<SKU, Disponibilidade> entry : erpPorSku.entrySet()) {
            SKU sku = entry.getKey();
            Disponibilidade erp = entry.getValue();

            ItemDeCatalogo cat = catalogoPorSku.get(sku);

            // não existe arquivo no Drive pra esse SKU -> pendente (sem imagem)
            if (cat == null) {
                pendentesCriacaoSemImagem++;
                continue;
            }

            // compara tamanhos
            ResultadoComparacaoTamanhos comparacao = comparador.comparar(erp, cat);

            if (comparacao != ResultadoComparacaoTamanhos.IGUAIS) {
                String legendaCorreta = GeradorDeLegenda.gerarLegenda(erp);

                try {
                    catalogoWriter.atualizarLegenda(cat.getIdExterno(), legendaCorreta);

                    // mantém identidade via metadata (idempotente)
                    catalogoWriter.vincularSku(cat.getIdExterno(), sku);

                    atualizados++;
                } catch (Exception e) {
                    erros.add("ATUALIZAR " + sku + " -> " + resumirErro(e));
                }
            }
        }

        return new ResultadoSincronizacaoDTO(atualizados, removidos, pendentesCriacaoSemImagem, erros);
    }

    private boolean ehPlaceholderCatalogo(SKU sku) {
        try {
            String modelo = sku.getModelo();
            return modelo != null && modelo.trim().equalsIgnoreCase("CATALOGO");
        } catch (Exception ignored) {
            return false;
        }
    }

    private String resumirErro(Exception e) {
        String msg = e.getMessage();
        if (msg == null || msg.isBlank()) return e.getClass().getSimpleName();
        msg = msg.replace("\n", " ").replace("\r", " ").trim();
        return msg.length() > 180 ? msg.substring(0, 180) + "..." : msg;
    }
}
