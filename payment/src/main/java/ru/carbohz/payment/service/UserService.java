package ru.carbohz.payment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.carbohz.payment.api.model.BalancePostRequest;
import ru.carbohz.payment.api.model.BalanceUserIdGet200Response;
import ru.carbohz.payment.exception.NotEnoughBalanceException;
import ru.carbohz.payment.exception.UserNotFoundException;
import ru.carbohz.payment.model.User;
import ru.carbohz.payment.repository.UserRepository;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final BigDecimal defaultBalance;

    public Mono<BigDecimal> balanceGet(Long userId) {
        return userRepository.findByUserId(userId)
                .switchIfEmpty(Mono.error(() -> {
                    String message = "Пользователь с id %s не найден".formatted(userId);
                    log.warn(message);
                    return new UserNotFoundException(message);
                }))
                .flatMap(user -> {
                    return Mono.just(user.getBalance());
                });
    }

    public Mono<Void> balancePost(Mono<BalancePostRequest> request) {
        return request.flatMap(req -> {
            Long userId = req.getUserId();
            return userRepository.findByUserId(userId)
                    .switchIfEmpty(addNewUser(userId))
                    .flatMap(user -> updateBalance(user, req.getSum()));
        });
    }

    private Mono<User> addNewUser(Long userId) {
        User user = new User();
        user.setUserId(userId);
        user.setBalance(defaultBalance);

        return userRepository.save(user);
    }

    private Mono<Void> updateBalance(User user, BigDecimal withdraw) {
        BigDecimal balance = user.getBalance();
        if (balance.compareTo(withdraw) < 0) {
            String message = "У пользователя %s недостаточно средств на балансе".formatted(user.getUserId());
            log.warn(message);
            return Mono.error(new NotEnoughBalanceException(message));
        }
        user.setBalance(balance.subtract(withdraw));
        return userRepository.save(user).then();
    }
}
