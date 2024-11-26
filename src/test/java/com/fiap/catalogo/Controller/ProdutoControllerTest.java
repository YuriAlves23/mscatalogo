package com.fiap.catalogo.Controller;

import com.fiap.catalogo.dto.ProdutoRequestDTO;
import com.fiap.catalogo.entity.Produto;
import com.fiap.catalogo.repository.ProdutoRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
public class ProdutoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProdutoRepository produtoRepository;

    @Test
    void getAll_deveRetornarListaDeProdutos() throws Exception {
        produtoRepository.save(new Produto(null, "Produto A", "Descrição A", 10.0, 5));

        mockMvc.perform(get("/produtos")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void save_deveCriarProdutoERetornarDTO() throws Exception {
        ProdutoRequestDTO request = new ProdutoRequestDTO("Produto A", "Descrição A", 10.0, 5);

        mockMvc.perform(post("/produtos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "nome": "Produto A",
                                    "descricao": "Descrição A",
                                    "preco": 10.0,
                                    "quantidadeEstoque": 5
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Produto A"));
    }

    @Test
    void update_comProdutoInexistente_deveRetornarNotFound() throws Exception {
        mockMvc.perform(put("/produtos/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                    "nome": "Produto Atualizado",
                                    "descricao": "Descrição Atualizada",
                                    "preco": 20.0,
                                    "quantidadeEstoque": 10
                                }
                                """))
                .andExpect(status().isNotFound());
    }
}
