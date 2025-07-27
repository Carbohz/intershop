package ru.carbohz.intershop.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table(name = "items")
@Getter
@Setter
@ToString
public class Item {
    @Id
    private Long id;

    @Column
    private String title;

    @Column
    private String description;

    @Column
    private String imagePath;

    @Column
    private Long price;
}
