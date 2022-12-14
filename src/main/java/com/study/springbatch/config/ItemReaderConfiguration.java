package com.study.springbatch.config;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.database.JpaCursorItemReader;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.database.builder.JpaCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class ItemReaderConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final DataSource datasource;
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public Job itemReaderJob() throws Exception {
        return this.jobBuilderFactory.get("itemReaderJob")
            .incrementer(new RunIdIncrementer())
            .start(this.customItemReaderStep())
            .next(this.csvFileStep())
            .next(this.jdbcStep())
            .next(this.jpaStep())
            .build();
    }

    @Bean
    public Step customItemReaderStep() {
        return this.stepBuilderFactory.get("customItemReaderStep")
            .<Person, Person>chunk(10)
            .reader(new CustomItemReader<>(getItems()))
//            .processor()
            .writer(itemWriter())
            .build();
    }

    @Bean
    public Step jdbcStep() throws Exception {
        return this.stepBuilderFactory.get("jdbcStep")
            .<Person, Person>chunk(10)
            .reader(jdbcCursorItemReader())
            .writer(itemWriter())
            .build();
    }

    @Bean
    public Step jpaStep() throws Exception {
        return stepBuilderFactory.get("jpaStep")
            .<Person, Person>chunk(10)
            .reader(jpaCursorItemReader())
            .writer(itemWriter())
            .build();
    }
    private JpaCursorItemReader<Person> jpaCursorItemReader() throws Exception {
        JpaCursorItemReader<Person> jpaCursorItemReader = new JpaCursorItemReaderBuilder<Person>()
            .name("jpaCursorItemReader")
            .entityManagerFactory(entityManagerFactory)
            .queryString("select p from Person p")
            .build();
        jpaCursorItemReader.afterPropertiesSet();

        return jpaCursorItemReader;
    }

    private JdbcCursorItemReader<Person> jdbcCursorItemReader() throws Exception {
        JdbcCursorItemReader<Person> itemReader = new JdbcCursorItemReaderBuilder<Person>()
            .name("jdbcCursorItemReader")
            .dataSource(datasource)
            .sql("select id, name, age, address from person")
            .rowMapper((rs, rowNum) -> new Person(rs.getInt(1), rs.getString(2), rs.getInt(3), rs.getString(4)))
            .build();

        itemReader.afterPropertiesSet();
        return itemReader;
    }

    /**
     * csv ????????? ????????? Person ????????? ??????
     */
    @Bean
    public Step csvFileStep() throws Exception {
        return stepBuilderFactory.get("csvFileStep")
            .<Person, Person>chunk(10)
            .reader(this.csvFileItemReader())
            .writer(itemWriter())
            .build();
    }

    private FlatFileItemReader<Person> csvFileItemReader() throws Exception {
        // csv ????????? ????????? ?????? Mapper
        DefaultLineMapper<Person> lineMapper = new DefaultLineMapper<>();
        // Person ?????? ????????? ?????? Person ???????????? ???????????? Tokenizer??? ??????
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        // Person ?????? ????????? ??????
        tokenizer.setNames("id", "name", "age", "address");
        lineMapper.setLineTokenizer(tokenizer);

        lineMapper.setFieldSetMapper(fieldSet -> {
            int id = fieldSet.readInt("id");
            String name = fieldSet.readString("name");
            int age = fieldSet.readInt("age");
            String address = fieldSet.readString("address");

            return new Person(id, name, age, address);
        });
        // end read csv file and mapping to person object

        FlatFileItemReader<Person> itemReader = new FlatFileItemReaderBuilder<Person>()
            .name("csvFileItemReader")
            .encoding("UTF-8")
            .resource(new ClassPathResource("test.csv"))
            .linesToSkip(1) //csv ?????? 1?????? ??????
            .lineMapper(lineMapper)
            .build();
        // itemReader?????? ????????? ?????????????????? ??????
        itemReader.afterPropertiesSet();

        return itemReader;
    }

    private ItemWriter<Person> itemWriter() {
        return items -> log.info(items.stream()
            .map(Person::getName)
            .collect(Collectors.joining(", ")));
    }

    private List<Person> getItems() {
        List<Person> items = new ArrayList<>();
        IntStream.range(0, 10)
            .forEach(idx -> items.add(new Person(idx + 1, "test name" + idx, idx + 1, "test address")));
        return items;
    }
}
