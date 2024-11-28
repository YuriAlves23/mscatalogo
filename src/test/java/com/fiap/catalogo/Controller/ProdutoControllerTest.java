package com.fiap.catalogo.Controller;

import com.fiap.catalogo.dto.ProdutoResponseDTO;
import com.fiap.catalogo.entity.Produto;
import com.fiap.catalogo.infra.ProdutoException;
import com.fiap.catalogo.repository.ProdutoRepository;
import com.fiap.catalogo.service.ProdutoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProdutoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Autowired
    private ProdutoService produtoService;

    @BeforeEach
    void setUp() {
        produtoRepository.deleteAll();
    }

    @Test
    void deveListarTodosOsProdutos() throws Exception {
        produtoRepository.save(new Produto(null, "Produto 1", "Descrição 1", 100.0, 10));
        produtoRepository.save(new Produto(null, "Produto 2", "Descrição 2", 200.0, 20));

        mockMvc.perform(get("/produtos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].nome", is("Produto 1")))
                .andExpect(jsonPath("$.content[0].descricao", is("Descrição 1")))
                .andExpect(jsonPath("$.content[1].nome", is("Produto 2")))
                .andExpect(jsonPath("$.content[1].descricao", is("Descrição 2")));
    }

    @Test
    void deveRetornarProdutoQuandoIdExistir() throws Exception {
        // Arrange
        Produto produto = produtoRepository.save(new Produto(null, "Produto Teste", "Descrição Teste", 100.0, 10));

        // Act & Assert
        mockMvc.perform(get("/produtos/{id}", produto.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(produto.getId()))
                .andExpect(jsonPath("$.nome").value("Produto Teste"))
                .andExpect(jsonPath("$.descricao").value("Descrição Teste"))
                .andExpect(jsonPath("$.preco").value(100.0))
                .andExpect(jsonPath("$.quantidadeEstoque").value(10));
    }

    @Test
    void deveRetornarErroQuandoProdutoNaoExistir() throws Exception {
        // Arrange
        Long idProdutoInvalido = 9999L;
        // Act & Assert

        mockMvc.perform(get("/produtos/{id}", idProdutoInvalido)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }


    @Test
    void deveSalvarProduto() throws Exception {
        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "nome": "Produto Teste",
                      "descricao": "Descrição Teste",
                      "preco": 300.0,
                      "quantidadeEstoque": 15
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Produto Teste")))
                .andExpect(jsonPath("$.descricao", is("Descrição Teste")))
                .andExpect(jsonPath("$.preco", is(300.0)))
                .andExpect(jsonPath("$.quantidadeEstoque", is(15)));
    }

    @Test
    void deveAtualizarProduto() throws Exception {
        Produto produto = produtoRepository.save(new Produto(null, "Produto Atual", "Descrição Atual", 150.0, 5));

        mockMvc.perform(put("/produtos/{id}", produto.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                    {
                      "nome": "Produto Atualizado",
                      "descricao": "Descrição Atualizada",
                      "preco": 180.0,
                      "quantidadeEstoque": 8
                    }
                    """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome", is("Produto Atualizado")))
                .andExpect(jsonPath("$.descricao", is("Descrição Atualizada")))
                .andExpect(jsonPath("$.preco", is(180.0)))
                .andExpect(jsonPath("$.quantidadeEstoque", is(8)));
    }

    @Test
    void deveDeletarProduto() throws Exception {
        Produto produto = produtoRepository.save(new Produto(null, "Produto Deletar", "Descrição Deletar", 100.0, 10));

        mockMvc.perform(delete("/produtos/{id}", produto.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/produtos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void deveProcessarArquivo() throws Exception {
        MockMultipartFile mockFile = new MockMultipartFile(
                "arquivo",
                "produtos.csv",
                "text/csv",
                "nome,descricao,preco,quantidadeEstoque\nProduto1,Descrição1,100.0,10\nProduto2,Descrição2,200.0,20".getBytes()
        );

        mockMvc.perform(multipart("/produtos/carga")
                        .file(mockFile))
                .andExpect(status().isOk())
                .andExpect(content().string("Processamento iniciado com sucesso!"));
    }
}