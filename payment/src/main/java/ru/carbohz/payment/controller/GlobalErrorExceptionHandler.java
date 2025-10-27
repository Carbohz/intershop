package ru.carbohz.payment.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;
import ru.carbohz.payment.exception.NotEnoughBalanceException;
import ru.carbohz.payment.exception.UserNotFoundException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalErrorExceptionHandler {

    @ExceptionHandler(NotEnoughBalanceException.class)
    public Mono<ResponseEntity<Void>> notEnoughBalanceException(NotEnoughBalanceException ex) {
        log.warn("Not Enough Balance Exception: {}", ex.getMessage(), ex);
        return Mono.just(ResponseEntity.status(HttpStatus.CONFLICT).build());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public Mono<ResponseEntity<Void>> userNotFoundException(UserNotFoundException ex) {
        log.warn("User Not Found Exception: {}", ex.getMessage(), ex);
        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @ExceptionHandler(Throwable.class)
    public Mono<ResponseEntity<Map<String, String>>> handleIllegalArgument(Throwable exception) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", exception.getMessage());
        return Mono.just(ResponseEntity.internalServerError().body(errorResponse));
    }
}
