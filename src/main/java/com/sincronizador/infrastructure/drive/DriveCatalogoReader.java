package com.sincronizador.infrastructure.drive;

import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.sincronizador.application.port.CatalogoReader;
import com.sincronizador.domain.model.*;
import com.sincronizador.domain.valueobject.Tamanho;
import com.sincronizador.domain.valueobject.Tipo;

import java.util.*;
import java.util.stream.Collectors;

public class DriveCatalogoReader implements CatalogoReader {

    private final Drive drive;
    private final String folderId;

    public DriveCatalogoReader(Drive drive, String folderId) {
        this.drive = drive;
        this.folderId = folderId;
    }

    @Override
    public List<ItemDeCatalogo> obterItens() {

        List<ItemDeCatalogo> itens = new ArrayList<>();

        try {
            FileList result = drive.files().list()
                    .setQ("'" + folderId + "' in parents and trashed = false")
                    .setFields("files(id,name,appProperties)")
                    .execute();

            for (File file : result.getFiles()) {

                Optional<SKU> skuOpt = skuViaMetadata(file);
                if (skuOpt.isEmpty()) {
                    // sem metadata = não entra no catálogo (segurança pra não deletar por engano)
                    continue;
                }

                SKU sku = skuOpt.get();

                Set<Tamanho> tamanhos = extrairTamanhosDaLegenda(file.getName());

                Map<Tamanho, Integer> quantidades = new EnumMap<>(Tamanho.class);
                for (Tamanho t : tamanhos) {
                    quantidades.put(t, 1);
                }

                Estoque estoque = new Estoque(sku, quantidades);
                Disponibilidade disponibilidade = Disponibilidade.aPartirDoEstoque(estoque);

                itens.add(new ItemDeCatalogo(
                        sku,
                        disponibilidade,
                        file.getId()
                ));
            }

        } catch (Exception e) {
            throw new RuntimeException("Erro ao ler catálogo do Drive: " + e.getMessage(), e);

        }

        return itens;
    }

    private Optional<SKU> skuViaMetadata(File file) {
        Map<String, String> p = file.getAppProperties();
        if (p == null) return Optional.empty();

    
        String clube = p.get(DriveMetadataKeys.SKU_CLUBE);
        if (clube == null || clube.isBlank()) clube = p.get("clube");

        String modelo = p.get(DriveMetadataKeys.SKU_MODELO);
        if (modelo == null || modelo.isBlank()) modelo = p.get("modelo");

        String tipoStr = p.get(DriveMetadataKeys.SKU_TIPO);
        if (tipoStr == null || tipoStr.isBlank()) tipoStr = p.get("tipo");

        if (clube == null || modelo == null || tipoStr == null) return Optional.empty();

        Tipo tipo;
        try {
            tipo = Tipo.valueOf(tipoStr.trim().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            return Optional.empty();
        }

        Produto produto = new Produto(clube, modelo, tipo);
        return Optional.of(new SKU(produto));
    }

    private Set<Tamanho> extrairTamanhosDaLegenda(String legenda) {
        if (legenda == null) return Collections.emptySet();
        if (!legenda.contains("-")) return Collections.emptySet();

        String[] partes = legenda.split("-");
        if (partes.length != 2) return Collections.emptySet();

        String tamanhosStr = partes[1].trim();
        if (tamanhosStr.isBlank()) return Collections.emptySet();

        return Arrays.stream(tamanhosStr.split(","))
                .map(String::trim)
                .map(this::mapearTamanho)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
    }

    private Tamanho mapearTamanho(String raw) {
        if (raw == null) return null;
        String v = raw.trim().toUpperCase(Locale.ROOT);

        // Ajuste simples pra suportar variações comuns
        if (v.equals("P")) return Tamanho.P;
        if (v.equals("M")) return Tamanho.M;
        if (v.equals("G")) return Tamanho.G;
        if (v.equals("GG")) return Tamanho.GG;
        if (v.equals("")) return Tamanho._2GG;
        if (v.equals("")) return Tamanho._3GG;
        if (v.equals("")) return Tamanho._4GG;




        // Se tiver outros tamanhos no seu enum (ex.: XG, XGG etc.), adiciona aqui.
        try {
            return Tamanho.valueOf(v);
        } catch (Exception e) {
            return null;
        }
    }
}
