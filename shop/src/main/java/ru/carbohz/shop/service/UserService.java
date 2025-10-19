package ru.carbohz.shop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.carbohz.shop.exception.UserAlreadyExistsException;
import ru.carbohz.shop.model.RegisterUserForm;
import ru.carbohz.shop.model.User;
import ru.carbohz.shop.repository.UserRepository;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    public final PasswordEncoder passwordEncoder;

    public Mono<User> register(RegisterUserForm form) {
        return validateUserDoesNotExist(form)
                .flatMap(user -> {
                    final String username = form.getUsername();
                    final String password = passwordEncoder.encode(form.getPassword());
                    final BigDecimal amount = BigDecimal.valueOf(100500); // FIXME
                    return insert(new User(username, password, amount));
                })
                .doOnSuccess(user -> {
                    log.info("User {} registered successfully", user.getName());
                })
                .doOnError(error -> {
                    log.error("User registration failed for {}", form.getUsername(), error);
                });
    }

    public Mono<User> findByName(String name) {
        return userRepository.findByName(name);
    }

    public Mono<User> insert(User user) {
        return userRepository.save(user);
    }

    public Mono<User> update(User user) {
        return userRepository.save(user);
    }

    public Mono<User> deleteById(Long id) {
        return userRepository.findById(id)
                .flatMap(u -> userRepository.deleteById(id).thenReturn(u));
    }

    private Mono<User> validateUserDoesNotExist(RegisterUserForm form) {
        final String username = form.getUsername();
        return userRepository.existsByName(username)
                .flatMap(usernameExists -> {
                    if (usernameExists) {
                        final String message = String.format("Username %s already exists", username);
                        log.info(message);
                        return Mono.error(new UserAlreadyExistsException(message));
                    } else {
                        return Mono.empty();
                    }
                });
    }
}
