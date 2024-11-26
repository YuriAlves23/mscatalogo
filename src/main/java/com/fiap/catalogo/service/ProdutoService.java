package com.fiap.catalogo.service;

import com.fiap.catalogo.dto.ProdutoRequestDTO;
import com.fiap.catalogo.dto.ProdutoResponseDTO;
import com.fiap.catalogo.entity.Produto;
import com.fiap.catalogo.infra.ProdutoException;
import com.fiap.catalogo.repository.ProdutoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProdutoService {

    @Autowired
    private ProdutoRepository produtoRepository;

    public Page<ProdutoResponseDTO> listarProdutos(Pageable pageable){
        Page<Produto> produtos = produtoRepository.findAll(pageable);

        return produtos.map(ProdutoResponseDTO::toDTO);
    }

    public ProdutoResponseDTO buscarProduto(Long id){
        Produto produto = produtoRepository.findById(id).orElseThrow(() -> new ProdutoException("Produto não encontrado"));

        return ProdutoResponseDTO.toDTO(produto);
    }

    public ProdutoResponseDTO salvarProduto(ProdutoRequestDTO produtoRequestDTO){
        Produto produto = produtoRequestDTO.toEntity();

        produto = produtoRepository.save(produto);

        return ProdutoResponseDTO.toDTO(produto);
    }

    public ProdutoResponseDTO atualizarProduto(Long id, ProdutoRequestDTO produtoRequestDTO){
        Produto produto = produtoRepository.findById(id)
                .orElseThrow(() -> new ProdutoException("Produto com o ID " + id + " não encontrado."));

        try {
            produto.setNome(produtoRequestDTO.nome());
            produto.setDescricao(produtoRequestDTO.descricao());
            produto.setQuantidadeEstoque(produtoRequestDTO.quantidadeEstoque());
            produto.setPreco(produtoRequestDTO.preco());

            produto = produtoRepository.save(produto);

        }catch (EntityNotFoundException e){
            throw new ProdutoException("Não foi possivel atualizar o produto");
        }

        return ProdutoResponseDTO.toDTO(produto);
    }

    public void deletarProduto(Long id){
        Produto produto = produtoRepository.getReferenceById(id);

        produtoRepository.deleteById(id);
    }

    public List<String> validarEstruturaArquivo(MultipartFile arquivo) {
        List<String> erros = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(arquivo.getInputStream()))) {
            String header = reader.readLine();
            if (header == null || !header.equals("nome,descricao,preco,quantidadeEstoque")) {
                erros.add("Cabeçalho inválido. Esperado: nome,descricao,preco,quantidadeEstoque");
            }

            String linha;
            int linhaNumero = 1;
            while ((linha = reader.readLine()) != null) {
                linhaNumero++;
                String[] campos = linha.split(",");

                if (campos.length != 4) {
                    erros.add("Linha " + linhaNumero + " deve ter 4 colunas");
                    continue;
                }

                if (campos[0].isBlank()) {
                    erros.add("Linha " + linhaNumero + ": nome é obrigatório");
                }

                try {
                    Double.parseDouble(campos[2]);
                } catch (NumberFormatException e) {
                    erros.add("Linha " + linhaNumero + ": preco deve ser um número válido");
                }

                try {
                    int quantidade = Integer.parseInt(campos[3]);
                    if (quantidade < 0) {
                        erros.add("Linha " + linhaNumero + ": quantidadeEstoque deve ser um valor positivo");
                    }
                } catch (NumberFormatException e) {
                    erros.add("Linha " + linhaNumero + ": quantidadeEstoque deve ser um número inteiro");
                }
            }
        } catch (Exception e) {
            erros.add("Erro ao ler o arquivo: " + e.getMessage());
        }
        return erros;
    }

}
