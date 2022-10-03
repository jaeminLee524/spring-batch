package com.study.springbatch.grade;

import static org.assertj.core.api.Assertions.assertThat;

import com.study.springbatch.config.TestConfiguration;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

@SpringBatchTest
@ContextConfiguration(classes = { UserConfiguration.class, TestConfiguration.class})
class UserConfigurationTest {

    @Autowired(required = false)
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired(required = false)
    private UserRepository userRepository;

    @Test
    void test() throws Exception {
        JobExecution jobExecution = jobLauncherTestUtils.launchJob();

        // 문제점이 있는 로직
        // 1. 데이터 검증 오류가 있을 수 있다(updatedDate를 통해 검증하기 때문에)
        // 2. updatedDate가 다른 곳에서도 수정될 수도 있기 때문에, updatedDate가 levelUp 메서드에서만 변경된다는 보장이없음
        int size = userRepository.findAllByUpdatedDate(LocalDate.now()).size();

        assertThat(jobExecution.getStepExecutions().stream()
                .filter(x -> x.getStepName().equals("userLevelUpStep"))
                .mapToInt(StepExecution::getWriteCount)
                .sum()
            ).isEqualTo(size)
            .isEqualTo(300);

        assertThat(userRepository.count())
            .isEqualTo(400);
    }

}