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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class ItemReaderConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job itemReaderJob() {
        return this.jobBuilderFactory.get("itemReaderJob")
            .incrementer(new RunIdIncrementer())
            .start(this.customItemReaderStep())
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

    private ItemWriter<Person> itemWriter() {
        return items -> log.info(items.stream()
            .map(Person::getName)
            .collect(Collectors.joining(", ")));
    }

    private List<Person> getItems() {
        List<Person> items = new ArrayList<>();
        IntStream.range(0, 10)
            .forEach(idx -> items.add(new Person(idx + 1, "test name" + idx, "test age", "test address")));
        return items;
    }
}
