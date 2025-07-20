package ru.carbohz.intershop.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import reactor.core.publisher.Mono;

@Controller
public class HomeController {
//    @GetMapping
//    public String home() {
//        return "redirect:/main/items";
//    }

    @GetMapping
    public Mono<String> home() {
        return Mono.just("redirect:/main/items");
    }
}
