package ru.carbohz.shop.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
//import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
//import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
//import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
//import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.logout.HttpStatusReturningServerLogoutSuccessHandler;
import org.springframework.security.web.server.authentication.logout.RedirectServerLogoutSuccessHandler;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.carbohz.shop.service.UserService;

import java.net.URI;
import java.util.List;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http) {
        var logoutSuccessHandler = new RedirectServerLogoutSuccessHandler();
        logoutSuccessHandler.setLogoutSuccessUrl(URI.create("/"));

        return http
                .authorizeExchange(exchanges -> exchanges
                        .pathMatchers(HttpMethod.GET, "/", "/main/items", "/items/*", "/images/*").permitAll()
                        .pathMatchers("/register").permitAll()
                        .pathMatchers("/login").permitAll()
                        .anyExchange().authenticated()
                )
//                .oauth2Client(Customizer.withDefaults())
                .formLogin(form -> form
                        .loginPage("/login")
                )
//                .httpBasic(Customizer.withDefaults())
                .logout(logout -> logout
                                .logoutUrl("/logout")
                                .logoutSuccessHandler(logoutSuccessHandler)
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public ReactiveUserDetailsService userDetailsService(UserService userService) {
        return username -> userService.findByName(username)
                .map(u -> (UserDetails) new User(u.getName(), u.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_USER"))))
                .switchIfEmpty(Mono.error(new UsernameNotFoundException(username)));
    }

//    @Bean
//    public ReactiveOAuth2AuthorizedClientManager auth2AuthorizedClientManager(
//            ReactiveClientRegistrationRepository clientRegistrationRepository,
//            ReactiveOAuth2AuthorizedClientService authorizedClientService
//    ) {
//        AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager manager =
//                new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(clientRegistrationRepository, authorizedClientService);
//
//        manager.setAuthorizedClientProvider(ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
//                .clientCredentials()
//                .refreshToken()
//                .build()
//        );
//
//        return manager;
//    }
}
