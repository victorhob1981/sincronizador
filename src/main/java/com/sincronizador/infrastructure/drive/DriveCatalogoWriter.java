package com.sincronizador.infrastructure.drive;

import com.google.api.client.http.FileContent;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.sincronizador.application.port.CatalogoWriter;
import com.sincronizador.domain.model.Disponibilidade;
import com.sincronizador.domain.model.SKU;
import com.sincronizador.domain.service.GeradorDeLegenda;

import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class DriveCatalogoWriter implements CatalogoWriter {

    private final Drive drive;
    private final String folderId;

    public DriveCatalogoWriter(Drive drive, String folderId) {
        this.drive = Objects.requireNonNull(drive, "drive não pode ser nulo");
        this.folderId = Objects.requireNonNull(folderId, "folderId não pode ser nulo")
        .trim()
            .replace("\"", "")
            .replace(";", "");
    }

    @Override
    public String criarComImagemLocal(SKU sku, Disponibilidade disponibilidade, java.io.File imagemLocal) {
        Objects.requireNonNull(sku, "sku não pode ser nulo");
        Objects.requireNonNull(disponibilidade, "disponibilidade não pode ser nula");
        validarArquivoImagem(imagemLocal);

        String nome = GeradorDeLegenda.gerarLegenda(disponibilidade);

        File metadata = new File();
        metadata.setName(nome);
        metadata.setParents(Collections.singletonList(folderId));
        metadata.setAppProperties(appPropertiesFromSku(sku));

        FileContent media = new FileContent(detectarMimeType(imagemLocal), imagemLocal);

        try {
            File created = drive.files()
                    .create(metadata, media)
                    .setFields("id,name,appProperties")
                    .execute();

            return created.getId();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao criar arquivo no Drive para o SKU: " + sku, e);
        }
    }

    @Override
    public void atualizarLegenda(String fileId, String novoNome) {
        Objects.requireNonNull(fileId, "fileId não pode ser nulo");
        Objects.requireNonNull(novoNome, "novoNome não pode ser nulo");

        File patch = new File();
        patch.setName(novoNome);

        try {
            drive.files()
                    .update(fileId, patch)
                    .setFields("id,name")
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao renomear arquivo no Drive: " + fileId, e);
        }
    }

    @Override
    public void trocarImagem(String fileId, java.io.File novaImagemLocal) {
        Objects.requireNonNull(fileId, "fileId não pode ser nulo");
        validarArquivoImagem(novaImagemLocal);

        FileContent media = new FileContent(detectarMimeType(novaImagemLocal), novaImagemLocal);

        try {
            // Atualiza o conteúdo mantendo id, nome e appProperties (associação fixa)
            drive.files()
                    .update(fileId, null, media)
                    .setFields("id")
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao substituir conteúdo do arquivo no Drive: " + fileId, e);
        }
    }

    @Override
    public void remover(String fileId) {
        Objects.requireNonNull(fileId, "fileId não pode ser nulo");

        try {
            drive.files().delete(fileId).execute();
        } catch (IOException e) {
            throw new RuntimeException("Erro ao remover arquivo do Drive: " + fileId, e);
        }
    }

    private Map<String, String> appPropertiesFromSku(SKU sku) {
        Map<String, String> props = new HashMap<>();
        props.put(DriveMetadataKeys.SKU_CLUBE, sku.getClube());
        props.put(DriveMetadataKeys.SKU_MODELO, sku.getModelo());
        props.put(DriveMetadataKeys.SKU_TIPO, sku.getTipo().name());
        props.put(DriveMetadataKeys.SKU_KEY, (sku.getClube() + "|" + sku.getModelo() + "|" + sku.getTipo().name()).toUpperCase(Locale.ROOT));
        return props;
    }

    private void validarArquivoImagem(java.io.File arquivo) {
        Objects.requireNonNull(arquivo, "arquivo não pode ser nulo");
        if (!arquivo.exists() || !arquivo.isFile()) {
            throw new IllegalArgumentException("Arquivo inválido: " + arquivo.getAbsolutePath());
        }
        String n = arquivo.getName().toLowerCase(Locale.ROOT);
        if (!(n.endsWith(".jpg") || n.endsWith(".jpeg") || n.endsWith(".png"))) {
            throw new IllegalArgumentException("Imagem deve ser .jpg/.jpeg/.png: " + arquivo.getName());
        }
    }

    private String detectarMimeType(java.io.File arquivo) {
        try {
            String probed = Files.probeContentType(arquivo.toPath());
            if (probed != null && !probed.isBlank()) return probed;
        } catch (IOException ignored) {}

        String n = arquivo.getName().toLowerCase(Locale.ROOT);
        if (n.endsWith(".png")) return "image/png";
        return "image/jpeg";
    }

    @Override
public void vincularSku(String fileId, SKU sku) {
    Objects.requireNonNull(fileId, "fileId não pode ser nulo");
    Objects.requireNonNull(sku, "sku não pode ser nulo");

    File patch = new File();
    patch.setAppProperties(appPropertiesFromSku(sku));

    try {
        drive.files()
                .update(fileId, patch)
                .setFields("id,appProperties")
                .execute();
    } catch (IOException e) {
        throw new RuntimeException("Erro ao vincular metadados no arquivo do Drive: " + fileId, e);
    }
}

}
