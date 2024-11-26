package com.fiap.catalogo.controller;

import com.fiap.catalogo.dto.ProdutoRequestDTO;
import com.fiap.catalogo.dto.ProdutoResponseDTO;
import com.fiap.catalogo.service.ProdutoService;
import jakarta.validation.Valid;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;


@RestController
@RequestMapping("/produtos")
public class ProdutoController {

    @Autowired
    private ProdutoService produtoService;

   @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job importarProduto;

    @PostMapping("/carga")
    public ResponseEntity<String> carga(@RequestParam("arquivo") MultipartFile arquivo) {
        List<String> errosValidacao = produtoService.validarEstruturaArquivo(arquivo);

        if (!errosValidacao.isEmpty()) {
            return ResponseEntity.badRequest().body("Erros de validação no arquivo: " + String.join("; ", errosValidacao));
        }

        try {
            // Criação de um arquivo temporário para armazenar o MultipartFile
            Path tempFile = Files.createTempFile(null, ".csv");
            Files.copy(arquivo.getInputStream(), tempFile, StandardCopyOption.REPLACE_EXISTING);

            // Preparando os parâmetros do Job
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("arquivo.path", tempFile.toString())
                    .toJobParameters();

            // Executando o Job com os parâmetros
            jobLauncher.run(importarProduto, jobParameters);

            return ResponseEntity.ok("Processamento iniciado com sucesso!");
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Erro ao iniciar o processamento: " + e.getMessage());
        }
    }


    @Transactional
    @GetMapping
    public ResponseEntity<Page<ProdutoResponseDTO>> getAll(@PageableDefault(size = 10, page = 0) Pageable pageable) {
        Page<ProdutoResponseDTO> Produtos = produtoService.listarProdutos(pageable);

        return ResponseEntity.ok(Produtos);
    }


    @Transactional
    @PostMapping
    public ResponseEntity<ProdutoResponseDTO> save(@Valid @RequestBody ProdutoRequestDTO produtoRequestDTO) {
        var Produto = produtoService.salvarProduto(produtoRequestDTO);

        return ResponseEntity.ok(Produto);
    }


    @Transactional
    @PutMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> update(@PathVariable Long id, @Valid @RequestBody ProdutoRequestDTO produtoRequestDTO) {
        var Produto = produtoService.atualizarProduto(id, produtoRequestDTO);

        return ResponseEntity.ok(Produto);
    }


    @Transactional
    @DeleteMapping("/{id}")
    public ResponseEntity<ProdutoResponseDTO> delete(@PathVariable Long id) {
        produtoService.deletarProduto(id);

        return ResponseEntity.noContent().build();
    }
}

