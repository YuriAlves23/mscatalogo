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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
public class ProdutoServiceTest {

    @Mock
    private ProdutoRepository produtoRepository;

    @InjectMocks
    private ProdutoService produtoService;

    @Test
    void deveListarProdutosComSucesso() {
        Pageable pageable =  PageRequest.of(0, 10);
        List<Produto> produtos = List.of(
                new Produto(1L, "Produto 1", "Descrição 1", 10.0, 100),
                new Produto(2L, "Produto 2", "Descrição 2", 20.0, 200)
        );
        Page<Produto> page = new PageImpl<>(produtos);

        when(produtoRepository.findAll(pageable)).thenReturn(page);

        Page<ProdutoResponseDTO> result = produtoService.listarProdutos(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("Produto 1", result.getContent().get(0).nome());
        verify(produtoRepository, times(1)).findAll(pageable);
    }

    @Test
    void deveBuscarProdutoComSucesso() {
        Produto produto = new Produto(1L, "Produto 1", "Descrição 1", 10.0, 100);

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));

        ProdutoResponseDTO result = produtoService.buscarProduto(1L);

        assertEquals("Produto 1", result.nome());
        verify(produtoRepository, times(1)).findById(1L);
    }

    @Test
    void deveLancarExcecaoQuandoProdutoNaoExistir() {
        when(produtoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProdutoException.class, () -> produtoService.buscarProduto(1L));
        verify(produtoRepository, times(1)).findById(1L);
    }

    @Test
    void deveSalvarProdutoComSucesso() {
        ProdutoRequestDTO produtoRequestDTO = new ProdutoRequestDTO("Produto 1", "Descrição 1", 10.0, 100);
        Produto produto = produtoRequestDTO.toEntity();

        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        ProdutoResponseDTO result = produtoService.salvarProduto(produtoRequestDTO);

        assertEquals("Produto 1", result.nome());
        verify(produtoRepository, times(1)).save(any(Produto.class));
    }

    @Test
    void deveAtualizarProdutoComSucesso() {
        Produto produto = new Produto(1L, "Produto 1", "Descrição 1", 10.0, 100);
        ProdutoRequestDTO produtoRequestDTO = new ProdutoRequestDTO("Produto Atualizado", "Descrição Atualizada", 20.0, 200);

        when(produtoRepository.findById(1L)).thenReturn(Optional.of(produto));
        when(produtoRepository.save(any(Produto.class))).thenReturn(produto);

        ProdutoResponseDTO result = produtoService.atualizarProduto(1L, produtoRequestDTO);

        assertEquals("Produto Atualizado", result.nome());
        verify(produtoRepository, times(1)).findById(1L);
        verify(produtoRepository, times(1)).save(any(Produto.class));
    }

    @Test
    void deveLancarExcecaoQuandoProdutoNaoExistirParaAtualizacao() {
        ProdutoRequestDTO produtoRequestDTO = new ProdutoRequestDTO("Produto Atualizado", "Descrição Atualizada", 20.0, 200);

        when(produtoRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ProdutoException.class, () -> produtoService.atualizarProduto(1L, produtoRequestDTO));
        verify(produtoRepository, times(1)).findById(1L);
    }

    @Test
    void deveDeletarProdutoComSucesso() {
        Produto produto = new Produto(1L, "Produto 1", "Descrição 1", 10.0, 100);

        when(produtoRepository.getReferenceById(1L)).thenReturn(produto);

        produtoService.deletarProduto(1L);

        verify(produtoRepository, times(1)).deleteById(1L);
    }
}
