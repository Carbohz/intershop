package ru.carbohz.shop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.carbohz.shop.model.RegisterUserForm;
import ru.carbohz.shop.model.User;
import ru.carbohz.shop.service.UserService;

import java.util.List;

@Controller
@RequestMapping("/register")
@RequiredArgsConstructor
public class RegisterController {
    private final UserService userService;
    private final ServerSecurityContextRepository securityContextRepository;

    @GetMapping
    public Mono<String> getForm() {
        return Mono.just("register");
    }


    @PostMapping
    public Mono<String> register(Model model, RegisterUserForm form, ServerWebExchange exchange) {
        return userService.registerUser(form)
                .flatMap(user -> autoLogin(user, exchange))
                .then(Mono.fromCallable(() -> {
                    model.addAttribute("success", "Registration successful! Please login.");
                    return "redirect:/main/items";
                }))
                .onErrorResume(throwable -> {
                    model.addAttribute("error", throwable.getMessage());
                    model.addAttribute("registrationRequest", form);
                    return Mono.just("register");
                });
    }

    private Mono<Void> autoLogin(User user, ServerWebExchange exchange) {
        return userService.findByUsername(user.getUsername())
                .map(savedUser -> createAuthentication(savedUser.getUsername(), savedUser.getPassword()))
                .flatMap(authentication -> {
                    // Создаем SecurityContext с аутентификацией
                    SecurityContext securityContext = new SecurityContextImpl(authentication);

                    // Сохраняем контекст безопасности в сессии
                    return securityContextRepository.save(exchange, securityContext)
                            .then(Mono.defer(() -> {
                                // Устанавливаем контекст в SecurityContextHolder
                                exchange.getAttributes().put(
                                        SecurityContext.class.getName(),
                                        securityContext
                                );
                                return Mono.empty();
                            }));
                });
    }

    private Authentication createAuthentication(String username, String password) {
        // Создаем объект аутентификации
        return new UsernamePasswordAuthenticationToken(
                username,
                password,
                List.of()
        );
    }
}
