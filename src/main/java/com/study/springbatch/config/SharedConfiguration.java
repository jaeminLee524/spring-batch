package com.study.springbatch.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@RequiredArgsConstructor
@Configuration
public class SharedConfiguration {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;

    @Bean
    public Job sharJob() {
        return jobBuilderFactory.get("shareJob")
            .incrementer(new RunIdIncrementer())
            .start(this.shareStep())
            .next(this.shareStep2())
            .build();
    }

    /**
     * execution context에 임의의 값을 넣고, job과 step, run parameter의 로그를 출력
      */
    @Bean
    public Step shareStep() {
        return stepBuilderFactory.get("shareStep")
            .tasklet((contribution, chunkContext) -> {
                StepExecution stepExecution = contribution.getStepExecution();
                ExecutionContext stepExecutionContext = stepExecution.getExecutionContext();
                stepExecutionContext.put("stepKey", "step execution context");

                JobExecution jobExecution = stepExecution.getJobExecution();
                JobInstance jobInstance = jobExecution.getJobInstance();
                ExecutionContext jobExecutionContext = jobExecution.getExecutionContext();
                jobExecutionContext.put("jobKey", "job execution context");
                JobParameters jobParameters = jobExecution.getJobParameters();

                log.info("jobName : {}, stepName : {}, parameter : {}",
                    jobInstance.getJobName(),
                    stepExecution.getStepName(),
                    jobParameters.getLong("run.id")
                );
                return RepeatStatus.FINISHED;
            })
            .build();
    }

    /**
     * executionContext는 job, step이 존재하는데, 각각 하나의 job or step내에서 데이터를 공유한다
     * 대신 jobExecutionContext를 step끼리 공유하는 것이다.
     */
    @Bean
    public Step shareStep2() {
        return stepBuilderFactory.get("sharStep2")
            .tasklet((contribution, chunkContext) -> {
                StepExecution stepExecution = contribution.getStepExecution();
                ExecutionContext stepExecutionContext = stepExecution.getExecutionContext();

                JobExecution jobExecution = stepExecution.getJobExecution();
                ExecutionContext jobExecutionContext = jobExecution.getExecutionContext();

                log.info("jobKey: {}, stepKey: {}",
                    jobExecutionContext.getString("jobKey", "emptyJobKey"),
                    stepExecutionContext.getString("stepKey", "emptyStepKey")
                );

                return RepeatStatus.FINISHED;
            })
            .build();
    }
}
