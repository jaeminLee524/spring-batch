package com.study.springbatch.config;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class ChunkProcessingConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job chunkProcessingJob() {
        return jobBuilderFactory.get("chunkProcessingJob")
            .incrementer(new RunIdIncrementer())
            .start(this.taskBaseStep())
            .next(this.chunkBaseStep())
            .build();
    }

    @Bean
    public Step chunkBaseStep() {
        return stepBuilderFactory.get("chunckBaseStep")
            .<String, String>chunk(10)
            .reader(itemReader())
            .processor(itemProcessor())
            .writer(itemWriter())
            .build();
    }

    private ItemWriter<String> itemWriter() {
        return items -> log.info("chunk item size : {}", items.size());
    }

    // processor는 reader에서 읽은 데이터를 가공, writer로 데이터를 전달할지 말지 결정
    private ItemProcessor<String, String> itemProcessor() {
        return item -> item + ", spring batch";
    }

    private ItemReader<String> itemReader() {
        return new ListItemReader<>(getItems());
    }

    // Reader 가 null 을 리턴할 때 까지 반복
    @Bean
    public Step taskBaseStep() {
        return stepBuilderFactory.get("taskBaseStep")
            .tasklet(this.tasklet())
            .build();
    }

    @Bean
    public Tasklet tasklet() {
        return (contribution, chunkContext) -> {
            List<String> items = getItems();
            log.info("item.size: {}", items.size());

            return RepeatStatus.FINISHED;
        };
    }

    private List<String> getItems() {
        List<String> items = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            items.add(i + " items");
        }
        return items;
    }

}
