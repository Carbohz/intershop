package ru.carbohz.intershop.service;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class PageableService {
    public boolean hasPrevious(PageRequest pageable) {
        return pageable.hasPrevious();
    }

    public boolean hasNext(PageRequest pageable, long total) {
        return pageable.getOffset() + pageable.getPageSize() < (int) total;
    }
}
