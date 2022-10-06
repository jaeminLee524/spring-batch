package com.study.springbatch.grade;

import static javax.persistence.CascadeType.PERSIST;

import com.study.springbatch.config.Orders;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Table(name = "users")
@Entity
@NoArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    @Enumerated(EnumType.STRING)
    private Level level = Level.NORMAL;

//    private int totalAmount;

    @OneToMany(cascade = PERSIST)
    @JoinColumn(name = "user_id")
    private List<Orders> ordersList;

    private LocalDate updatedDate;

    @Builder
    public User(String username, List<Orders> ordersList) {
        this.username = username;
        this.ordersList = ordersList;
    }

    public boolean availableLevelUp() {
        return Level.availableLevelUp(this.getLevel(), this.getTotalAmount());

    }

    private int getTotalAmount() {
        return this.ordersList.stream()
            .mapToInt(Orders::getAmount)
            .sum();
    }

    public Level levelUp() {
        Level nextLevel = Level.getNextLevel(this.getTotalAmount());
        this.level = nextLevel;
        this.updatedDate = LocalDate.now();

        return nextLevel;
    }

    public enum Level {
        VIP(500_000, null),
        GOLD(500_000, VIP),
        SILVER(300_000, GOLD),
        NORMAL(200_000, SILVER);

        private final int nextAmount;
        private final Level nextLevel;

        Level(int nextAmount, Level nextLevel) {
            this.nextAmount = nextAmount;
            this.nextLevel = nextLevel;
        }

        private static boolean availableLevelUp(Level level, int totalAmount) {
            if (Objects.isNull(level)) {
                // 잘못된 파라미터를 보냈다는 에러
                return false;
            }

            if (Objects.isNull(level.nextLevel)) {
                // 최대 레벨에 도달했다는 에러
                return false;
            }

            return totalAmount >= level.nextAmount;
        }

        private static Level getNextLevel(int totalAmount) {
            if (totalAmount >= Level.VIP.nextAmount) {
                return VIP;
            }

            if (totalAmount >= Level.GOLD.nextAmount) {
                return GOLD;
            }

            if (totalAmount >= Level.SILVER.nextAmount) {
                return SILVER;
            }

            if (totalAmount >= Level.NORMAL.nextAmount) {
                return NORMAL;
            }

            return NORMAL;
        }
    }
}
