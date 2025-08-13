package ru.carbohz.payment.controller;

import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.reactive.server.WebTestClient;
import ru.carbohz.payment.api.model.BalanceGet200Response;
import ru.carbohz.payment.api.model.BalancePostRequest;

import java.math.BigDecimal;

@WebFluxTest(controllers = PaymentController.class)
class PaymentControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @Test
    public void getBalanceTest() {
        var expectedBalance = new BalanceGet200Response();
        expectedBalance.setBalance(new BigDecimal(1000));

        webTestClient.get().uri("/balance").exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody(BalanceGet200Response.class)
                .isEqualTo(expectedBalance);
    }

    @Test
    public void reduceBalanceTest() {
        var postBody = new BalancePostRequest();
        postBody.setSum(new BigDecimal(100));

        webTestClient.post()
                .uri("/balance")
                .bodyValue(postBody)
                .exchange()
                .expectStatus().isOk()
                .expectBody().isEmpty();

        var expectedBalance = new BalanceGet200Response();
        expectedBalance.setBalance(new BigDecimal(900));

        webTestClient.get().uri("/balance").exchange()
                .expectStatus().isOk()
                .expectHeader().contentType("application/json")
                .expectBody(BalanceGet200Response.class)
                .isEqualTo(expectedBalance);
    }

    @Test
    public void overReduceBalanceTest() {
        var postBody = new BalancePostRequest();
        postBody.setSum(new BigDecimal(1100));

        webTestClient.post()
                .uri("/balance")
                .bodyValue(postBody)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody().isEmpty();
    }
}