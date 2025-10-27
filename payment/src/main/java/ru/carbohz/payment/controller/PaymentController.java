package ru.carbohz.payment.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.carbohz.payment.api.PaymentApi;
import ru.carbohz.payment.api.model.BalancePostRequest;
import ru.carbohz.payment.api.model.BalanceUserIdGet200Response;
import ru.carbohz.payment.service.UserService;

@RestController
@RequiredArgsConstructor
public class PaymentController implements PaymentApi {
    private final UserService userService;

    @Override
    public Mono<ResponseEntity<BalanceUserIdGet200Response>> balanceUserIdGet(
            @Parameter(name = "userId", description = "Идентификатор пользователя", required = true, in = ParameterIn.PATH) @PathVariable("userId") Long userId,
            @Parameter(hidden = true) final ServerWebExchange exchange
    ) {
        return userService.balanceGet(userId)
                .flatMap(balance -> {
                    BalanceUserIdGet200Response response = new BalanceUserIdGet200Response();
                    response.setBalance(balance);
                    return Mono.just(ResponseEntity.ok(response));
                });
    }

    @Override
    public Mono<ResponseEntity<Void>> balancePost(Mono<BalancePostRequest> balancePostRequest, ServerWebExchange exchange) {
        return userService.balancePost(balancePostRequest)
                .then(Mono.just(ResponseEntity.ok().build()));
    }
}
