package ru.carbohz.payment.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;

@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User {
    @Id
    private Long id;

    @Column
    private Long userId;

    @Column
    private BigDecimal balance;
}
