package com.study.springbatch.config;

import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

public class CustomItemReader<T> implements ItemReader<T> {
    private final List<T> items;

    public CustomItemReader(List<T> items) {
        this.items = new ArrayList<>(items);
    }


    @Override
    public T read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
        if (!items.isEmpty()) {
            return items.remove(0);
        }
        return null; // null이면 chunk 반복이 끝난
    }
}
