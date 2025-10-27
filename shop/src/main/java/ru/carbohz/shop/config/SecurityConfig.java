package ru.carbohz.shop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.DefaultServerRedirectStrategy;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerRedirectStrategy;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.authentication.ServerHttpBasicAuthenticationConverter;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;

import java.net.URI;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http,
                                                      ReactiveAuthenticationManager authenticationManager) {
        final AuthenticationWebFilter authFilter = new AuthenticationWebFilter(authenticationManager);
        authFilter.setServerAuthenticationConverter(new ServerHttpBasicAuthenticationConverter());

        ServerLogoutSuccessHandler logoutHandler = (exchange, authentication) -> {
            final ServerRedirectStrategy redirectStrategy = new DefaultServerRedirectStrategy();
            return exchange.getExchange().getSession()
                    .flatMap(WebSession::invalidate)
                    .then(Mono.fromRunnable(() -> {
                        exchange.getExchange().getResponse()
                                .setStatusCode(HttpStatus.OK); // отвечаем 200 OK
                    }))
                    .then(redirectStrategy.sendRedirect(exchange.getExchange(), URI.create("/")));
        };

        return http
                .addFilterAt(authFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .securityContextRepository(securityContextRepository())
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.GET, "/", "/main/items", "/main/items/*").permitAll()
                        .pathMatchers("/register", "/login", "/logout").permitAll()
                        .pathMatchers("/favicon.ico", "/.well-known/appspecific/com.chrome.devtools.json").permitAll()
                        .anyExchange().authenticated()
                )
                .oauth2Client(Customizer.withDefaults())
                .formLogin(Customizer.withDefaults())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessHandler(logoutHandler))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ServerSecurityContextRepository securityContextRepository() {
        // Хранилище сессий в WebSession
        return new WebSessionServerSecurityContextRepository();
    }

    @Bean
    public ReactiveAuthenticationManager authenticationManager(
            ReactiveUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        UserDetailsRepositoryReactiveAuthenticationManager authManager =
                new UserDetailsRepositoryReactiveAuthenticationManager(userDetailsService);
        authManager.setPasswordEncoder(passwordEncoder);
        return authManager;
    }
}
