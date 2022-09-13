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
            .build();
    }

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
