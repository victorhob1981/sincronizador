package com.sincronizador.service;

import com.sincronizador.dao.ProdutoDAO;
import com.sincronizador.model.Produto;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.Locale;

public class ImageService {

    private final String pastaLocalImagens;
    private final ProdutoDAO produtoDAO;

    public ImageService(String pastaLocalImagens) {
        this.pastaLocalImagens = pastaLocalImagens;
        this.produtoDAO = new ProdutoDAO();
    }

    /**
     * Verifica se a extensão do arquivo é suportada.
     */
    public boolean isExtensaoValida(File arquivo) {
        String nome = arquivo.getName().toLowerCase();
        return nome.endsWith(".jpg") || nome.endsWith(".jpeg") || nome.endsWith(".png");
    }

    /**
     * Gera o nome do arquivo conforme regra:
     * <3_LETRAS_CLUBE> - <MODELO>.ext
     */
    public String gerarNomePadraoArquivo(Produto produto, File arquivoOrigem) {
        String extensao = arquivoOrigem.getName()
                .substring(arquivoOrigem.getName().lastIndexOf(".")).toLowerCase();

        String clube3 = removerAcentos(produto.getClube())
                .substring(0, 3)
                .toUpperCase(Locale.ROOT);

        String modeloSanitizado = removerAcentos(produto.getModelo())
                .replaceAll("[^a-zA-Z0-9\\s]", "")
                .replace(" ", "_")
                .toUpperCase(Locale.ROOT);

        return clube3 + " - " + modeloSanitizado + extensao;
    }

    /**
     * Copia o arquivo para a pasta padrão do sistema, com nome seguro.
     */
    public boolean copiarParaPastaLocal(Produto produto, File arquivoOrigem) {
        if (!isExtensaoValida(arquivoOrigem)) {
            System.err.println("Extensão inválida: " + arquivoOrigem.getName());
            return false;
        }

        if (!new File(pastaLocalImagens).exists()) {
            new File(pastaLocalImagens).mkdirs();
        }

        String novoNome = gerarNomePadraoArquivo(produto, arquivoOrigem);
        File destino = new File(pastaLocalImagens + File.separator + novoNome);

        try {
            Files.copy(arquivoOrigem.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);

            // Atualiza banco
            produtoDAO.atualizarCaminhoImagem(produto.getProdutoID(), destino.getAbsolutePath());
            return true;

        } catch (IOException e) {
            System.err.println("Erro ao copiar imagem: " + e.getMessage());
            return false;
        }
    }

    /**
     * Remove acentos e caracteres especiais.
     */
    private String removerAcentos(String texto) {
        if (texto == null) return "";
        String normalizado = Normalizer.normalize(texto, Normalizer.Form.NFD);
        return normalizado.replaceAll("[^\\p{ASCII}]", "");
    }
}
