package com.sincronizador.dao;

import com.sincronizador.db.Conexao;
import com.sincronizador.model.Produto;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProdutoDAO {

    private static final String URL = "jdbc:mysql://localhost:3306/gemini_erp?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC";
    private static final String USUARIO = "root";
    private static final String SENHA = "Senhalp3";

    /**
     * Retorna todos os produtos do banco, ordenando primeiro os que não têm imagem.
     * O valor de caminhoImagem pode estar nulo no BD (caso ainda não tenha sido associada).
     */
    public List<Produto> listarProdutos() {
        List<Produto> lista = new ArrayList<>();

        String sql = """
                SELECT ProdutoID, Modelo, Clube, Tipo, Tamanho, CaminhoImagem
                FROM produtos
                ORDER BY (CaminhoImagem IS NOT NULL AND CaminhoImagem <> '') DESC,
                         Clube, Modelo, Tamanho
                """;

        try (Connection conn = DriverManager.getConnection(URL, USUARIO, SENHA);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                Produto p = new Produto(
                        rs.getInt("ProdutoID"),
                        rs.getString("Modelo"),
                        rs.getString("Clube"),
                        rs.getString("Tipo"),
                        rs.getString("Tamanho"),
                        rs.getString("CaminhoImagem")
                );
                lista.add(p);
            }

        } catch (SQLException e) {
            System.err.println("Erro ao listar produtos: " + e.getMessage());
        }

        return lista;
    }

    /**
     * Atualiza o caminho da imagem no banco para um produto específico.
     */
    public boolean atualizarCaminhoImagem(int produtoId, String caminhoImagem) {
        String sql = "UPDATE produtos SET CaminhoImagem = ? WHERE ProdutoID = ?";

        try (Connection conn = DriverManager.getConnection(URL, USUARIO, SENHA);
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, caminhoImagem);
            stmt.setInt(2, produtoId);

            return stmt.executeUpdate() > 0; // true se atualizou

        } catch (SQLException e) {
            System.err.println("Erro ao atualizar caminho da imagem: " + e.getMessage());
            return false;
        }
    }

    public static boolean atualizarImagem(int idProduto, String nomeImagem) {
    String sql = "UPDATE produtos SET imagem = ? WHERE id = ?";

    try (Connection conn = Conexao.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {

        stmt.setString(1, nomeImagem);
        stmt.setInt(2, idProduto);

        int linhasAfetadas = stmt.executeUpdate();
        return linhasAfetadas == 1;

    } catch (Exception e) {
        System.err.println("Erro ao atualizar imagem do produto: " + e.getMessage());
        return false;
    }
}

}
