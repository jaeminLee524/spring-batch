package com.study.springbatch.config;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemWriter;
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

    @Bean
    public Job itemReaderJob() throws Exception {
        return this.jobBuilderFactory.get("itemReaderJob")
            .incrementer(new RunIdIncrementer())
            .start(this.customItemReaderStep())
            .next(this.csvFileStep())
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

    /**
     * csv 파일을 읽어서 Person 객체에 매핑
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
        // csv 파일을 한줄씩 읽는 Mapper
        DefaultLineMapper<Person> lineMapper = new DefaultLineMapper<>();
        // Person 객체 매핑을 위해 Person 필드명을 정의하는 Tokenizer가 필요
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();
        // Person 객체 필드명 설정
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
            .linesToSkip(1) //csv 파일 1행은 스킵
            .lineMapper(lineMapper)
            .build();
        // itemReader에서 필요한 필수속성들을 검증
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
