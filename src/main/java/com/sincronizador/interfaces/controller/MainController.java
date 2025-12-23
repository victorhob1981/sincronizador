package com.sincronizador.interfaces.controller;

import com.sincronizador.application.dto.ProdutoCatalogoStatusDTO;
import com.sincronizador.application.dto.ResultadoSincronizacaoDTO;
import com.sincronizador.application.usecase.AssociarImagemAoCatalogoUseCase;
import com.sincronizador.application.usecase.GerarStatusDoCatalogoUseCase;
import com.sincronizador.application.usecase.SincronizarCatalogoUseCase;
import com.sincronizador.domain.model.SKU;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;

import java.io.File;

public class MainController {

    @FXML
    private TableView<ProdutoCatalogoStatusDTO> tabelaProdutos;

    @FXML
    private TableColumn<ProdutoCatalogoStatusDTO, String> colProduto;

    @FXML
    private TableColumn<ProdutoCatalogoStatusDTO, String> colStatus;

    private GerarStatusDoCatalogoUseCase gerarStatusUseCase;
    private SincronizarCatalogoUseCase sincronizarCatalogoUseCase;
    private AssociarImagemAoCatalogoUseCase associarImagemUseCase;

    @FXML
    public void initialize() {

        colProduto.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(
                        cell.getValue().getNomeProduto()
                )
        );

        colStatus.setCellValueFactory(cell ->
                new javafx.beans.property.SimpleStringProperty(
                        cell.getValue().getEstado().name()
                )
        );

        colStatus.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                    return;
                }

                switch (item) {
                    case "OK" -> {
                        setText("🟢 OK");
                        setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                    }
                    case "SEM_IMAGEM" -> {
                        setText("🟠 Sem imagem");
                        setStyle("-fx-text-fill: orange; -fx-font-weight: bold;");
                    }
                    case "DESATUALIZADO" -> {
                        setText("🟡 Desatualizado");
                        setStyle("-fx-text-fill: #b58900; -fx-font-weight: bold;");
                    }
                    case "ORFAO" -> {
                        setText("🔴 Órfão");
                        setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    }
                    default -> {
                        setText(item);
                        setStyle("");
                    }
                }
            }
        });
    }

    public void setGerarStatusUseCase(GerarStatusDoCatalogoUseCase useCase) {
        this.gerarStatusUseCase = useCase;
        carregarTabela();
    }

    public void setSincronizarCatalogoUseCase(SincronizarCatalogoUseCase useCase) {
        this.sincronizarCatalogoUseCase = useCase;
    }

    public void setAssociarImagemUseCase(AssociarImagemAoCatalogoUseCase useCase) {
        this.associarImagemUseCase = useCase;
    }

    private void carregarTabela() {
        if (gerarStatusUseCase == null) return;

        ObservableList<ProdutoCatalogoStatusDTO> dados =
                FXCollections.observableArrayList(gerarStatusUseCase.executar());

        tabelaProdutos.setItems(dados);
    }

    @FXML
    private void onSincronizar() {
        if (sincronizarCatalogoUseCase == null) {
            alertErro("Sincronizar Catálogo", "UseCase de sincronização não foi injetado.");
            return;
        }

        try {
            ResultadoSincronizacaoDTO r = sincronizarCatalogoUseCase.executar();

            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Sincronizador de Catálogo");
            alert.setHeaderText("Sincronização concluída.");
            alert.setContentText(
                    "Atualizados: " + r.getAtualizados() + "\n" +
                    "Removidos: " + r.getRemovidos() + "\n" +
                    "Pendentes de criação (sem imagem): " + r.getPendentesCriacaoSemImagem()
            );
            alert.showAndWait();

            carregarTabela();

        } catch (Exception e) {
            alertErro("Erro ao sincronizar catálogo", e.getMessage());
        }
    }

    @FXML
    private void onAssociarImagem() {
        if (associarImagemUseCase == null) {
            alertErro("Associar imagem", "UseCase de associação de imagem não foi injetado.");
            return;
        }

        ProdutoCatalogoStatusDTO selecionado = tabelaProdutos.getSelectionModel().getSelectedItem();
        if (selecionado == null) {
            alertErro("Associar imagem", "Selecione um produto na tabela.");
            return;
        }

        SKU sku = selecionado.getSku();

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Selecione a imagem do produto");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imagens", "*.png", "*.jpg", "*.jpeg", "*.webp")
        );

        File imagem = chooser.showOpenDialog(tabelaProdutos.getScene().getWindow());
        if (imagem == null) return;

        try {
            associarImagemUseCase.executar(sku, imagem);

            Alert ok = new Alert(Alert.AlertType.INFORMATION);
            ok.setTitle("Associar imagem");
            ok.setHeaderText("Imagem associada com sucesso.");
            ok.setContentText(selecionado.getNomeProduto());
            ok.showAndWait();

            carregarTabela();

        } catch (Exception e) {
            alertErro("Erro ao associar imagem", e.getMessage());
        }
    }

    private void alertErro(String titulo, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText(null);
        alert.setContentText(msg == null ? "Erro desconhecido." : msg);
        alert.showAndWait();
    }
}
