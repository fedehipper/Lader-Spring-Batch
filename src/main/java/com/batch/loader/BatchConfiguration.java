package com.batch.loader;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.database.BeanPropertyItemSqlParameterSourceProvider;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.LineMapper;
import org.springframework.batch.item.file.mapping.BeanWrapperFieldSetMapper;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Configuration
@EnableBatchProcessing
public class BatchConfiguration {

    @Autowired
    public JobBuilderFactory jobBuilderFactory;

    @Autowired
    public StepBuilderFactory stepBuilderFactory;

    @Bean
    public FlatFileItemReader<Persona> reader() {
        FlatFileItemReader<Persona> flatFileItemReader = new FlatFileItemReader<>();
        flatFileItemReader.setResource(new ClassPathResource("personas.txt"));

        LineMapper<Persona> studentLineMapper = crearLineMapper();
        flatFileItemReader.setLineMapper(studentLineMapper);

        return flatFileItemReader;
    }

    private LineMapper<Persona> crearLineMapper() {
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        tokenizer.setDelimiter("|");
        tokenizer.setNames(new String[]{"nombre", "apellido"});

        BeanWrapperFieldSetMapper<Persona> mapper = new BeanWrapperFieldSetMapper<>();
        mapper.setTargetType(Persona.class);

        DefaultLineMapper<Persona> lineMapper = new DefaultLineMapper<>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(mapper);
        return lineMapper;
    }

    @Bean
    public PersonaItemProcessor processor() {
        return new PersonaItemProcessor();
    }

    @Bean
    public JdbcBatchItemWriter<Persona> writer(DataSource dataSource) {
        return new JdbcBatchItemWriterBuilder<Persona>()
                .itemSqlParameterSourceProvider(new BeanPropertyItemSqlParameterSourceProvider<>())
                .sql("INSERT INTO persona (nombre, apellido) VALUES (:nombre, :apellido)")
                .dataSource(dataSource)
                .build();
    }

    @Bean
    public Job importUserJob(Step step1) {
        return jobBuilderFactory.get("importUserJob")
                .incrementer(new RunIdIncrementer())
                .flow(step1)
                .end()
                .build();
    }

    @Bean
    public Step step1(JdbcBatchItemWriter<Persona> writer) {
        return stepBuilderFactory.get("step1")
                .<Persona, Persona>chunk(10)
                .reader(reader())
                .processor(processor())
                .writer(writer)
                .build();
    }
}
