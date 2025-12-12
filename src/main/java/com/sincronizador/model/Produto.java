package com.sincronizador.model;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.File;
import java.text.Normalizer;
import java.util.Locale;

/**
 * Modelo Produto usado pela UI (JavaFX).
 *
 * Observações:
 * - Contém propriedades JavaFX para ligação direta com TableView.
 * - Possui utilitários para gerar nome de arquivo padronizado:
 *     <3 letras do clube em caps> - <modelo sanitized>.<ext>
 * - A flag 'possuiImagem' é mantida via caminhoImagem (se preenchido).
 * - Não faz acesso ao banco — é apenas o modelo em memória.
 */
public class Produto {

    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty modelo = new SimpleStringProperty();
    private final StringProperty clube = new SimpleStringProperty();
    private final StringProperty tipo = new SimpleStringProperty();
    private final StringProperty tamanho = new SimpleStringProperty();
    private final StringProperty caminhoImagem = new SimpleStringProperty(); // caminho local absoluto ou null

    public Produto() {}

    public Produto(int id, String modelo, String clube, String tipo, String tamanho, String caminhoImagem) {
        this.id.set(id);
        this.modelo.set(modelo);
        this.clube.set(clube);
        this.tipo.set(tipo);
        this.tamanho.set(tamanho);
        this.caminhoImagem.set(caminhoImagem);
    }

    // --- Properties (para TableView binding) ---
    public IntegerProperty idProperty() { return id; }
    public StringProperty modeloProperty() { return modelo; }
    public StringProperty clubeProperty() { return clube; }
    public StringProperty tipoProperty() { return tipo; }
    public StringProperty tamanhoProperty() { return tamanho; }
    public StringProperty caminhoImagemProperty() { return caminhoImagem; }

    // --- Getters / Setters ---
    public int getId() { return id.get(); }
    public void setId(int value) { id.set(value); }

    public String getModelo() { return modelo.get(); }
    public void setModelo(String value) { modelo.set(value); }

    public String getClube() { return clube.get(); }
    public void setClube(String value) { clube.set(value); }

    public String getTipo() { return tipo.get(); }
    public void setTipo(String value) { tipo.set(value); }

    public String getTamanho() { return tamanho.get(); }
    public void setTamanho(String value) { tamanho.set(value); }

    public String getCaminhoImagem() { return caminhoImagem.get(); }
    public void setCaminhoImagem(String value) { caminhoImagem.set(value); }

    /**
     * Retorna true se a propriedade caminhoImagem estiver preenchida
     * e o arquivo correspondente existir no disco.
     * @return boolean
     */
    public boolean possuiImagemLocal() {
        String caminho = getCaminhoImagem();
        if (caminho == null || caminho.isBlank()) return false;
        File f = new File(caminho);
        return f.exists() && f.isFile();
    }

    /**
     * Gera o nome do arquivo local esperado baseado nas regras:
     *   <TRÊS LETRAS DO CLUBE EM MAIÚSCULO> - <MODELO SANITIZADO>.<ext>
     *
     * Exemplo: "FLA - Camisa2025Premium.jpg"
     *
     * NOTA: A extensão não é definida aqui — este método retorna o nome base (sem extensão)
     * Use getStandardFileNameWithExt(...).
     *
     * @return nome base do arquivo (sem extensão)
     */
    public String getStandardFileNameBase() {
        String club = (getClube() == null) ? "" : getClube().trim().toUpperCase(Locale.ROOT);
        String club3 = club.length() >= 3 ? club.substring(0,3) : club;
        String mdl = (getModelo() == null) ? "produto" : getModelo().trim();
        String sanitized = sanitizeFileName(mdl);
        return club3 + " - " + sanitized;
    }

    /**
     * Retorna nome padrão com extensão detectada a partir do arquivo original.
     * Se originalExt for null/empty, usa ".jpg" por padrão.
     *
     * @param originalExt ex: ".png" ou ".jpg"
     * @return nome final do arquivo (com extensão)
     */
    public String getStandardFileNameWithExt(String originalExt) {
        String ext = (originalExt == null || originalExt.isBlank()) ? ".jpg" : (originalExt.startsWith(".") ? originalExt : "." + originalExt);
        return getStandardFileNameBase() + ext;
    }

    /**
     * Sanitiza um texto para ser um nome de arquivo: remove acentos, caracteres inválidos
     * e troca espaços por nada (juntando as palavras) ou por underscore se preferir.
     */
    private String sanitizeFileName(String input) {
        if (input == null) return "produto";
        // remover acentos
        String noAccent = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
        // remover caracteres que não são letras, números, espaços ou - _
        String cleaned = noAccent.replaceAll("[^\\p{Alnum} \\-_.()]", "");
        // remover múltiplos espaços
        cleaned = cleaned.trim().replaceAll(" +", " ");
        // opcional: remover espaços (concatenar) ou substituir por underscore
        cleaned = cleaned.replace(" ", "");
        if (cleaned.isEmpty()) return "produto";
        return cleaned;
    }

    @Override
    public String toString() {
        return String.format("Produto{id=%d, clube='%s', modelo='%s', tamanho='%s', imagem=%s}",
                getId(), getClube(), getModelo(), getTamanho(), getCaminhoImagem());
    }
}
