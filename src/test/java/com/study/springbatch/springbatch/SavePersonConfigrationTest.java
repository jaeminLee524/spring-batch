/*
package com.study.springbatch.springbatch;

import static org.assertj.core.api.Assertions.assertThat;

import com.study.springbatch.config.PersonRepository;
import com.study.springbatch.config.SavePersonConfiguration;
import com.study.springbatch.config.TestConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBatchTest
@SpringBootTest
@ContextConfiguration(classes = {SavePersonConfiguration.class, TestConfiguration.class})
public class SavePersonConfigrationTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private PersonRepository personRepository;

    @AfterEach
    void tearDown() {
        personRepository.deleteAll();
    }

    // Step 테스트 코드, @JobScope가 동작하려면 Test클래스에 @SpringBatchTest 필요
    @Test
    void test_step() {
        JobExecution savePersonStep = jobLauncherTestUtils.launchStep("savePersonStep");

        assertThat(savePersonStep.getStepExecutions().stream()
            .mapToInt(StepExecution::getWriteCount)
            .sum())
            .isEqualTo(personRepository.count())
            .isEqualTo(3);
    }

    // Job 실행 테스트 코드
    @Test
    public void test_allow_duplicate() throws Exception {
        // given - 테스트에 필요한 데이터 생성
        JobParameters jobParameters = new JobParametersBuilder()
            .addString("allow_duplicate", "false")
            .toJobParameters();

        // when - 테스트 대상
        // launchJob 메소드를 통해 SavePersonConfiguration에 등록된 Job이 실행되고 결과로 jobExecution을 리턴 받는다
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertThat(jobExecution.getStepExecutions().stream()
            .mapToInt(StepExecution::getWriteCount)
            .sum())
            .isEqualTo(personRepository.count())
            .isEqualTo(3);
    }

    @Test
    public void test_not_allow_duplicate() throws Exception {
        // given - 테스트에 필요한 데이터 생성
        JobParameters jobParameters = new JobParametersBuilder()
            .addString("allow_duplicate", "true")
            .toJobParameters();

        // when - 테스트 대상
        // launchJob 메소드를 통해 SavePersonConfiguration에 등록된 Job이 실행되고 결과로 jobExecution을 리턴 받는다
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertThat(jobExecution.getStepExecutions().stream()
            .mapToInt(StepExecution::getWriteCount)
            .sum())
            .isEqualTo(personRepository.count())
            .isEqualTo(100);
    }
}
*/
