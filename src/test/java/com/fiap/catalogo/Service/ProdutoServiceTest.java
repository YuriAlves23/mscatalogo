package com.fiap.catalogo.Service;

import com.fiap.catalogo.dto.ProdutoRequestDTO;
import com.fiap.catalogo.dto.ProdutoResponseDTO;
import com.fiap.catalogo.entity.Produto;
import com.fiap.catalogo.infra.ProdutoException;
import com.fiap.catalogo.repository.ProdutoRepository;
import com.fiap.catalogo.service.ProdutoService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private ProdutoService produtoService;

    @Test
    void salvarProduto_deveSalvarEDevolverProdutoDTO() {
        Produto produto = new Produto(null, "Produto A", "Descrição A", 10.0, 5);
        Produto produtoSalvo = new Produto(1L, "Produto A", "Descrição A", 10.0, 5);

        when(produtoRepository.save(any(Produto.class))).thenReturn(produtoSalvo);

        ProdutoRequestDTO request = new ProdutoRequestDTO("Produto A", "Descrição A", 10.0, 5);
        ProdutoResponseDTO response = produtoService.salvarProduto(request);

        assertEquals("Produto A", response.nome());
        assertEquals(1L, response.id());
        verify(produtoRepository, times(1)).save(any(Produto.class));
    }

    @Test
    void buscarProduto_comIdInexistente_deveLancarProdutoException() {
        when(produtoRepository.findById(1L)).thenReturn(Optional.empty());

        ProdutoException exception = assertThrows(ProdutoException.class, () -> {
            produtoService.buscarProduto(1L);
        });

        assertEquals("Produto não encontrado", exception.getMessage());
    }
}
