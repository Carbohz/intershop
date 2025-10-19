package ru.carbohz.shop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;

@Controller
@RequestMapping("/logout")
public class LogoutController {

    @GetMapping
    public Mono<String> getForm() {
        return Mono.just("logout");
    }

}
