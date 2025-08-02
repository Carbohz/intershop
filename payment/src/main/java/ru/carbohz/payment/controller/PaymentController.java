package ru.carbohz.payment.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.carbohz.payment.api.PaymentApi;
import ru.carbohz.payment.api.model.BalanceGet200Response;
import ru.carbohz.payment.api.model.BalancePostRequest;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@RestController
public class PaymentController implements PaymentApi {
    private final AtomicReference<BigDecimal> balanceRef = new AtomicReference<>(new BigDecimal(1000));

    @Override
    public Mono<ResponseEntity<BalanceGet200Response>> balanceGet(ServerWebExchange exchange) {
        var body = new BalanceGet200Response();
        body.setBalance(balanceRef.get());
        return Mono.just(ResponseEntity.ok(body));
    }

    @Override
    public Mono<ResponseEntity<Void>> balancePost(Mono<BalancePostRequest> balancePostRequest, ServerWebExchange exchange) {
        return balancePostRequest.map(sumObj -> {
            var oldValue = balanceRef.getAndUpdate(balance -> {
                if (balance.compareTo(sumObj.getSum()) >= 0) {
                    return balance.subtract(sumObj.getSum());
                } else {
                    return balance;
                }
            });
            if (oldValue.compareTo(sumObj.getSum()) >= 0) {
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT).build();
            }
        });
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public Mono<ResponseEntity<Map<String, String>>> handleIllegalArgument(IllegalArgumentException exception) {
        Map<String, String> errorResponse = new HashMap<>();
        errorResponse.put("error", exception.getMessage());
        return Mono.just(ResponseEntity.badRequest().body(errorResponse));
    }
}
