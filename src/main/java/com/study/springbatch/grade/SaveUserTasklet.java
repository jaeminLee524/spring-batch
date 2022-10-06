package com.study.springbatch.grade;

import com.study.springbatch.config.Orders;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@RequiredArgsConstructor
public class SaveUserTasklet implements Tasklet {
    private static final int SIZE = 100;

    private final UserRepository userRepository;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {

        List<User> users = createUsers();

        Collections.shuffle(users);

        userRepository.saveAll(users);

        return RepeatStatus.FINISHED;
    }

    private List<User> createUsers() {
        List<User> users = new ArrayList<>();
        for (int i = 0; i < SIZE; i++) {
            users.add(User.builder()
                .ordersList(Collections.singletonList(Orders.builder()
                    .amount(1_000)
                    .createDate(LocalDate.of(2020, 11, 1))
                    .build()))
//                .totalAmount(1_000)
                .username("test username " + i)
                .build());
        }

        for (int i = 0; i < SIZE; i++) {
            users.add(User.builder()
                .ordersList(Collections.singletonList(Orders.builder()
                    .amount(200_000)
                    .createDate(LocalDate.of(2020, 11, 2))
                    .build()))
//                .totalAmount(200_000)
                .username("test username " + i)
                .build());
        }

        for (int i = 0; i < SIZE; i++) {
            users.add(User.builder()
                .ordersList(Collections.singletonList(Orders.builder()
                    .amount(300_000)
                    .createDate(LocalDate.of(2020, 11, 3))
                    .build()))
//                .totalAmount(300_000)
                .username("test username " + i)
                .build());
        }

        for (int i = 0; i < SIZE; i++) {
            users.add(User.builder()
                .ordersList(Collections.singletonList(Orders.builder()
                    .amount(500_000)
                    .createDate(LocalDate.of(2020, 11, 4))
                    .build()))
//                .totalAmount(500_000)
                .username("test username " + i)
                .build());
        }

        return users;
    }
}
