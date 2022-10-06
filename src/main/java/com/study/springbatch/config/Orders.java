package com.study.springbatch.config;

import java.time.LocalDate;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
public class Orders {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String itemName;

    private int amount;

    private LocalDate createDate;

    @Builder
    private Orders(String itemName, int amount, LocalDate createDate) {
        this.itemName = itemName;
        this.amount = amount;
        this.createDate = createDate;
    }
}
