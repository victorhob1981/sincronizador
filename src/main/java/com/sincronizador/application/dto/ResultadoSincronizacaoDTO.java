package com.sincronizador.application.dto;

import java.util.Collections;
import java.util.List;

public class ResultadoSincronizacaoDTO {

    private final int atualizados;
    private final int removidos;
    private final int pendentesCriacaoSemImagem;
    private final List<String> erros;

    public ResultadoSincronizacaoDTO(
            int atualizados,
            int removidos,
            int pendentesCriacaoSemImagem,
            List<String> erros
    ) {
        this.atualizados = atualizados;
        this.removidos = removidos;
        this.pendentesCriacaoSemImagem = pendentesCriacaoSemImagem;
        this.erros = (erros == null) ? List.of() : List.copyOf(erros);
    }

    public int getAtualizados() {
        return atualizados;
    }

    public int getRemovidos() {
        return removidos;
    }

    // getter "bonito"
    public int getPendentesCriacaoSemImagem() {
        return pendentesCriacaoSemImagem;
    }

    // getter EXATO que seu MainController está chamando (Semimagem)
    public int getPendentesCriacaoSemimagem() {
        return pendentesCriacaoSemImagem;
    }

    public List<String> getErros() {
        return Collections.unmodifiableList(erros);
    }

    public boolean temErros() {
        return !erros.isEmpty();
    }
}
