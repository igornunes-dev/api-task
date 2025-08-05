package com.example.apitask.mappers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;

public class PageUtils {
    public static <T, U> Page<U> mapPage(Page<T> page, java.util.function.Function<T, U> mapper) {
        List<U> content = page.getContent().stream().map(mapper).toList();
        return new PageImpl<>(content, page.getPageable(), page.getTotalElements());
    }
}
