package ru.carbohz.shop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.carbohz.shop.model.RegisterUserForm;
import ru.carbohz.shop.service.UserService;

@Controller
@RequestMapping("/register")
@RequiredArgsConstructor
public class RegisterController {
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public Mono<String> getForm() {
        return Mono.just("register");
    }


    @PostMapping
    public Mono<String> register(Model model, RegisterUserForm form, ServerWebExchange exchange) {
//        return ReactiveSecurityContextHolder.getContext()
//                .map(SecurityContext::getAuthentication)
//                .map(a -> {
//                    var p = a.getPrincipal();
//                    return Mono.just(p);
//                })
//                .flatMap(authentication -> userService.register(form))
//                .flatMap(user -> Mono.just("main"));
        return userService.register(form)
                .thenReturn("redirect:/main/items");
    }
}
