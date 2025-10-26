package ru.carbohz.shop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import reactor.core.publisher.Mono;
import ru.carbohz.shop.model.RegisterUserForm;
import ru.carbohz.shop.service.UserService;

@Controller
@RequestMapping("/register")
@RequiredArgsConstructor
public class RegisterController {
    private final UserService userService;

    @GetMapping
    public Mono<String> getForm() {
        return Mono.just("register");
    }


    @PostMapping
    public Mono<String> register(Model model, RegisterUserForm form) {
        return userService.registerUser(form)
                .then(Mono.fromCallable(() -> "redirect:/login"))
                .onErrorResume(throwable -> {
                    model.addAttribute("error", throwable.getMessage());
                    model.addAttribute("registrationRequest", form);
                    return Mono.just("register");
                });
    }
}
