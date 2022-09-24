package com.study.springbatch.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;

@RequiredArgsConstructor
public class DuplicateValidationProcessor<T> implements ItemProcessor<T, T> {

    private final Map<String, Object> keyPool = new ConcurrentHashMap<>();
    private final Function<T, String> keyExtractor; // Function Interface는 T 타입을 인자로 받아서 R 객체를 반환하는 apply 메소드를 지원
    private final boolean allowDuplicate; // true: no filtering, false: allow filtering

    @Override
    public T process(T item) throws Exception {
        if (allowDuplicate) {
            return item;
        }

        // keyExtractor를 이용해 T 타입으로 들어온 item의 Key를 추출
        String key = keyExtractor.apply(item);

        // keyPool에 추출한 key가 존재한다면 null을 반호나
        if (keyPool.containsKey(key)) {
            return null;
        }

        // keyPool에 존재하지 않는 key였다면 다음 중복체크를 위해 keyPool에 put
        keyPool.put(key, key);

        return item;
    }
}
