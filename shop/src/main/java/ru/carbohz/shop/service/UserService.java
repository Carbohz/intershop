package ru.carbohz.shop.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.carbohz.shop.model.RegisterUserForm;
import ru.carbohz.shop.model.User;
import ru.carbohz.shop.repository.UserRepository;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements ReactiveUserDetailsService {
    private final UserRepository userRepository;
    public final PasswordEncoder passwordEncoder;

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository.findByName(username)
                .cast(UserDetails.class);
    }

    public Mono<User> registerUser(RegisterUserForm request) {
        return validateRegistration(request)
                .then(userRepository.existsByName(request.getUsername()))
                .flatMap(usernameExists -> {
                    if (usernameExists) {
                        return Mono.error(new RuntimeException("Username already exists"));
                    }

                    User user = new User(
                            request.getUsername(),
                            passwordEncoder.encode(request.getPassword()),
                            BigDecimal.valueOf(100500) // TODO в переменные окружения
                    );

                    return userRepository.save(user);
                });
    }

    private Mono<Void> validateRegistration(RegisterUserForm request) {
        return Mono.empty(); // TODO можно добавить интересные проверки на валидность
    }
}
