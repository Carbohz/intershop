package ru.carbohz.shop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.carbohz.shop.model.RegisterUserForm;
import ru.carbohz.shop.service.UserService;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository.DEFAULT_SPRING_SECURITY_CONTEXT_ATTR_NAME;

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

//    @PostMapping
//    public Mono<String> register(Model model, RegisterUserForm form, ServerWebExchange exchange) {
//        return ReactiveSecurityContextHolder.getContext()
//                .map(Optional::of)
//                .defaultIfEmpty(Optional.empty())
//                .map(securityContext -> {
//                    if (securityContext.isPresent() && securityContext.get().getAuthentication().isAuthenticated()) {
//                        throw new IllegalArgumentException("Вы уже зарегистрированы");
//                    }
//                    return form.getUsername();
//                })
//                .flatMap(v -> userService.findByName(form.getUsername()).map(u-> {
//                    throw new IllegalArgumentException("Пользователь с таким именем уже существует");
//                }).defaultIfEmpty(form.getUsername()))
//                .flatMap(name -> {
//                    UserDetails newUser = User
//                            .withUsername(form.getUsername())
//                            .password(form.getPassword())
//                            .passwordEncoder(passwordEncoder::encode)
//                            .build();
//                    return userService.insert(new ru.carbohz.shop.model.User(
//                            form.getUsername(),
//                            newUser.getPassword(),
//                            BigDecimal.valueOf(1000))); // FIXME хардкод
//                })
//                .flatMap(userUi -> exchange.getSession()
//                        .doOnNext(session -> {
//                            SecurityContextImpl securityContext = new SecurityContextImpl();
//                            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userUi.getName(), userUi.getPassword(), List.of());
//                            securityContext.setAuthentication(authentication);
//
//                            session.getAttributes().put(DEFAULT_SPRING_SECURITY_CONTEXT_ATTR_NAME, securityContext);
//                        })
//                        .flatMap(WebSession::changeSessionId)
//                        .thenReturn("redirect:/"))
//                .onErrorResume(err -> {
//                    if (err.getClass() == IllegalArgumentException.class) {
//                        model.addAttribute("registerError", err.getMessage());
//                    } else {
//                        model.addAttribute("registerError", "Ошибка регистрации, попробуйте позже");
//                    }
//                    return Mono.just("register");
//                });
//    }

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
