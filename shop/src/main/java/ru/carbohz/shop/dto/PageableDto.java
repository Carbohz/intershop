package ru.carbohz.shop.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@ToString
public class PageableDto {
    private long pageNumber;
    private long pageSize;
    private boolean hasNext;
    private boolean hasPrevious;

    public long pageNumber() {
        return this.pageNumber;
    }

    public long pageSize() {
        return this.pageSize;
    }

    public boolean hasNext() {
        return this.hasNext;
    }

    public boolean hasPrevious() {
        return this.hasPrevious;
    }
}
