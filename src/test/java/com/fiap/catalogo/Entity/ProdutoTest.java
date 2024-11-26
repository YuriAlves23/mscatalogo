package com.fiap.catalogo.Entity;

import com.fiap.catalogo.entity.Produto;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
public class ProdutoTest {

    @Test
    void deveCriarProduto() {
        Produto produto = new Produto(1L, "Produto A", "Descrição A", 10.0, 5);

        assertEquals(1L, produto.getId());
        assertEquals("Produto A", produto.getNome());
        assertEquals("Descrição A", produto.getDescricao());
        assertEquals(10.0, produto.getPreco());
        assertEquals(5, produto.getQuantidadeEstoque());
    }
}
