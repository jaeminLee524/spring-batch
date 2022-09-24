package com.study.springbatch.config;

import com.study.springbatch.config.BatchListener.SavePersonAnnotationJobExecutionListener;
import com.study.springbatch.config.BatchListener.SavePersonJobExecutionListener;
import com.study.springbatch.config.BatchListener.SavePersonStepExecutionListener;
import javax.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaItemWriter;
import org.springframework.batch.item.database.builder.JpaItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.support.CompositeItemWriter;
import org.springframework.batch.item.support.builder.CompositeItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class SavePersonConfiguration {

    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final EntityManagerFactory entityManagerFactory;

    @Bean
    public Job savePersonJob() throws Exception {
        return this.jobBuilderFactory.get("savePersonJob")
            .incrementer(new RunIdIncrementer())
            .start(this.savePersonStep(null)) //null로 설정해도 spring이 자동으로 allowDuplicate를 파라미터로 설정해줌
            .listener(new SavePersonJobExecutionListener())
            .listener(new SavePersonAnnotationJobExecutionListener())
            .build();
    }

    @Bean
    @JobScope // SpringBatch Job Parameters로 Step을 사용하기 위해 JobScope 설정
    public Step savePersonStep(@Value("#{jobParameters[allow_duplicate]}") String allowDuplicate) throws Exception {
        return this.stepBuilderFactory.get("savePersonStep")
            .<Person, Person>chunk(10)
            .reader(itemReader())
            .processor(new DuplicateValidationProcessor<>(Person::getName, Boolean.parseBoolean(allowDuplicate)))
            .writer(itemWriter())
            .listener(new SavePersonStepExecutionListener())
            .build();
    }

    private ItemWriter<? super Person> itemWriter() throws Exception {
        JpaItemWriter<Person> jpaItemWriter = new JpaItemWriterBuilder<Person>()
            .entityManagerFactory(entityManagerFactory)
            .usePersist(true)
            .build();

        ItemWriter<Person> logItemWriter = items -> log.info("person.size: {} ", items.size());

        // jpaItemWriter, logItemWriter를 합쳐서 실행할 수 있는 CompositeItemWriter 사용
        // CompositeItemWriter를 사용 시 주의 사항: 의도치 않게 아이템을 저장하는 ItemWriter를 2개 지정하게되면 이슈가 생길 수 있음
        CompositeItemWriter<Person> itemWriter = new CompositeItemWriterBuilder<Person>()
            .delegates(jpaItemWriter, logItemWriter)
            .build();

        itemWriter.afterPropertiesSet();

        return itemWriter;
    }

    private ItemReader<? extends Person> itemReader() throws Exception {
        DefaultLineMapper<Person> lineMapper = new DefaultLineMapper<>();

        DelimitedLineTokenizer lineTokenizer = new DelimitedLineTokenizer();
        lineTokenizer.setNames("name", "age", "address");

        lineMapper.setLineTokenizer(lineTokenizer);
        lineMapper.setFieldSetMapper(fieldSet -> new Person(
            fieldSet.readString(0),
            fieldSet.readInt(1),
            fieldSet.readString(2)));

        FlatFileItemReader<Person> itemReader = new FlatFileItemReaderBuilder<Person>()
            .name("savePersonItemReader")
            .encoding("UTF-8")
            .linesToSkip(1) //header skip
            .resource(new ClassPathResource("person.csv"))
            .lineMapper(lineMapper)
            .build();

        itemReader.afterPropertiesSet(); //lineMapper 검증
        return itemReader;
    }

}
