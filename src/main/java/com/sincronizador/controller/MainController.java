package com.sincronizador.controller;

import com.sincronizador.database.DatabaseConnection;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class MainController {

    // === UI Components (ligados ao FXML) ===
    @FXML private Label lblStatusPastaLocal;
    @FXML private Label lblStatusConexaoDB;
    @FXML private Label lblStatusTabela;
    @FXML private Button btnProsseguir;

    // Caminho local fixo definido nos requisitos
    private static final String LOCAL_IMAGES_PATH = "C:/Users/Vitinho/Desktop/Pronta Entrega";

    @FXML
    public void initialize() {
        validarSistema();
    }

    /**
     * Valida todos os requisitos iniciais estritamente conforme o fluxo do projeto.
     */
    private void validarSistema() {
        boolean pastaOk = validarPastaLocal();
        boolean conexaoOk = testarConexaoBanco();
        boolean tabelaOk = conexaoOk && validarTabelaProdutos();

        btnProsseguir.setDisable(!(pastaOk && conexaoOk && tabelaOk));
    }

    /**
     * 1️⃣ Validar se a pasta local existe e pode ser lida/escrita
     */
    private boolean validarPastaLocal() {
        File pasta = new File(LOCAL_IMAGES_PATH);

        if (pasta.exists() && pasta.isDirectory() && pasta.canWrite()) {
            lblStatusPastaLocal.setText("✔ Pasta local OK (" + LOCAL_IMAGES_PATH + ")");
            lblStatusPastaLocal.setStyle("-fx-text-fill: green;");
            return true;
        } else {
            lblStatusPastaLocal.setText("✖ Pasta não encontrada ou sem permissão (" + LOCAL_IMAGES_PATH + ")");
            lblStatusPastaLocal.setStyle("-fx-text-fill: red;");
            return false;
        }
    }

    /**
     * 2️⃣ Testar conexão com MySQL
     */
    private boolean testarConexaoBanco() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            if (conn != null && !conn.isClosed()) {
                lblStatusConexaoDB.setText("✔ Conexão com MySQL OK");
                lblStatusConexaoDB.setStyle("-fx-text-fill: green;");
                return true;
            }
        } catch (Exception ignored) {}

        lblStatusConexaoDB.setText("✖ Erro ao conectar MySQL");
        lblStatusConexaoDB.setStyle("-fx-text-fill: red;");
        return false;
    }

    /**
     * 3️⃣ Verificar se a tabela "produtos" existe e está acessível
     */
    private boolean validarTabelaProdutos() {
        String sql = "SELECT COUNT(*) FROM produtos LIMIT 1";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            lblStatusTabela.setText("✔ Tabela 'produtos' OK");
            lblStatusTabela.setStyle("-fx-text-fill: green;");
            return true;

        } catch (Exception e) {
            lblStatusTabela.setText("✖ Tabela 'produtos' não encontrada");
            lblStatusTabela.setStyle("-fx-text-fill: red;");
            return false;
        }
    }

    /**
     * 4️⃣ Ação do botão "Prosseguir" (apenas se tudo estiver validado)
     */
    @FXML
private void onProsseguir() {
    System.out.println("✔ Sistema validado — avançando para associação de imagens...");
    com.sincronizador.ScreenManager.changeScreen("AssociacaoView.fxml");
}

}
