package com.fiap.catalogo.config;

import com.fiap.catalogo.entity.Produto;
import jakarta.persistence.EntityManagerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class BatchConfiguration {

    @Bean
    public Job importarProduto(JobRepository jobRepository, Step step) {
        return new JobBuilder("importarProduto", jobRepository)
                .incrementer(new RunIdIncrementer())
                .flow(step)
                .end()
                .build();
    }

    @Bean
    public Step step(JobRepository jobRepository,
                     PlatformTransactionManager transactionManager,
                     ItemReader<Produto> reader,
                     ItemWriter<Produto> writer) {
        return new StepBuilder("step", jobRepository)
                .<Produto, Produto>chunk(10, transactionManager)
                .reader(reader)
                .writer(writer)
                .build();
    }

    @Bean
    @StepScope
    public FlatFileItemReader<Produto> reader(@Value("#{jobParameters['arquivo.path']}") String filePath) {
        System.out.println("Initializing FlatFileItemReader with file path: " + filePath);

        FileSystemResource resource = new FileSystemResource(filePath);
        if (!resource.exists() || !resource.isReadable()) {
            throw new IllegalArgumentException("O arquivo fornecido não existe ou não pode ser lido: " + filePath);
        }

        FlatFileItemReader<Produto> reader = new FlatFileItemReader<>();
        reader.setResource(resource);
        reader.setLinesToSkip(1); // Ignorar a primeira linha (cabeçalho)

        reader.setLineMapper(new DefaultLineMapper<Produto>() {{
            setLineTokenizer(new DelimitedLineTokenizer() {{
                setNames("nome", "descricao", "preco", "quantidadeEstoque");
            }});
            setFieldSetMapper(new BeanWrapperFieldSetMapper<>() {{
                setTargetType(Produto.class);
            }});
        }});

        System.out.println("FlatFileItemReader configured successfully.");
        return reader;
    }


    @Bean
    public JpaItemWriter<Produto> writer(EntityManagerFactory entityManagerFactory) {
        return new JpaItemWriterBuilder<Produto>()
                .entityManagerFactory(entityManagerFactory)
                .build();
    }
}
