package com.sincronizador.controller;

import com.sincronizador.dao.ProdutoDAO;
import com.sincronizador.model.Produto;
import com.sincronizador.util.FileManager;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;

public class AssociarImagemController {

    // Componentes do FXML
    @FXML private TextField txtIdProduto;
    @FXML private TextField txtModelo;
    @FXML private Label lblArquivoSelecionado;
    @FXML private Label lblResultado;
    @FXML private Button btnSelecionarImagem;
    @FXML private Button btnConfirmar;

    // Arquivo selecionado temporariamente
    private File imagemSelecionada;

    // Mesmos critérios usados anteriormente
    private static final String LOCAL_IMAGES_PATH = "C:/Users/Vitinho/Desktop/Pronta Entrega";

    // Extensões aceitas
    private static final String[] EXTENSOES_VALIDAS = {"jpg", "jpeg", "png"};


    /**
     * BOTÃO - Selecionar imagem
     */
    @FXML
    private void selecionarImagem() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Selecione uma imagem válida");

        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Imagens", "*.jpg", "*.jpeg", "*.png")
        );

        File arquivo = fileChooser.showOpenDialog(null);

        if (arquivo != null) {
            imagemSelecionada = arquivo;
            lblArquivoSelecionado.setText("Selecionado: " + arquivo.getName());
            lblArquivoSelecionado.setStyle("-fx-text-fill: green;");
        }
    }


    /**
     * BOTÃO - Confirmar associação da imagem
     */
    @FXML
    private void confirmar() {
        lblResultado.setText("");

        // Validações obrigatórias
        if (!validarCampos()) return;
        if (!validarFormatoNomeArquivo(imagemSelecionada.getName())) return;

        // Obtém dados digitados
        int idProduto = Integer.parseInt(txtIdProduto.getText());
        String modelo = txtModelo.getText().trim();
        String novoNomeImagem = gerarNomeImagem(modelo, imagemSelecionada);

        // Copiar imagem para pasta local
        boolean copiado = FileManager.copiarArquivo(
                imagemSelecionada.getAbsolutePath(),
                LOCAL_IMAGES_PATH + "/" + novoNomeImagem,
                true
        );

        if (!copiado) {
            exibirErro("Falha ao copiar a imagem para a pasta local!");
            return;
        }

        // Atualizar banco de dados
        boolean atualizado = ProdutoDAO.atualizarImagem(idProduto, novoNomeImagem);

        if (atualizado) {
            exibirSucesso("Imagem associada com sucesso e registrada no banco.");
        } else {
            exibirErro("Falha ao atualizar o registro no banco de dados.");
        }
    }


    /**
     * Valida se os campos obrigatórios foram preenchidos
     */
    private boolean validarCampos() {
        if (txtIdProduto.getText().isEmpty() || !txtIdProduto.getText().matches("\\d+")) {
            exibirErro("ID do produto inválido!");
            return false;
        }

        if (txtModelo.getText().trim().isEmpty()) {
            exibirErro("Modelo não pode estar vazio!");
            return false;
        }

        if (imagemSelecionada == null) {
            exibirErro("Nenhuma imagem selecionada!");
            return false;
        }

        return true;
    }


    /**
     * Valida nome do arquivo conforme regras
     */
    private boolean validarFormatoNomeArquivo(String nome) {
        String nomeSemExtensao = nome.substring(0, nome.lastIndexOf("."));
        String extensao = nome.substring(nome.lastIndexOf(".") + 1).toLowerCase();

        if (!nomeSemExtensao.matches("^[A-Za-z]{3} - .+$")) {
            exibirErro("Nome inválido. Use: AAA - MODELO");
            return false;
        }

        boolean extensaoOk = false;
        for (String ext : EXTENSOES_VALIDAS) {
            if (ext.equals(extensao)) extensaoOk = true;
        }

        if (!extensaoOk) {
            exibirErro("Extensão inválida! Permitidas: jpg, jpeg, png");
            return false;
        }

        return true;
    }


    /**
     * Gera o nome final do arquivo garantido conforme padrão
     */
    private String gerarNomeImagem(String modelo, File arquivo) {
        String prefixo = arquivo.getName().substring(0, 3).toUpperCase();
        String extensao = arquivo.getName().substring(arquivo.getName().lastIndexOf("."));
        return prefixo + " - " + modelo.toUpperCase() + extensao.toLowerCase();
    }


    // Mensagens de UI
    private void exibirErro(String msg) {
        lblResultado.setText("✘ " + msg);
        lblResultado.setStyle("-fx-text-fill: red;");
    }

    private void exibirSucesso(String msg) {
        lblResultado.setText("✔ " + msg);
        lblResultado.setStyle("-fx-text-fill: green;");
    }
}
