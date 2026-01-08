package com.cdcrane.ekkochatsrv.auth;

import com.cdcrane.ekkochatsrv.auth.exception.handlers.EkkoAccessDeniedHandler;
import com.cdcrane.ekkochatsrv.auth.exception.handlers.EkkoAuthEntryPoint;
import com.cdcrane.ekkochatsrv.auth.filter.AccessTokenValidatorFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.ExceptionTranslationFilter;

@Configuration
@RequiredArgsConstructor
class SecurityConfig {

    private final JwtUseCase jwtService;
    private final CorsConfig corsConfig;

    public static final String[] PUBLIC_URIS = {
            "/error",
            "/api/v1/auth/login",
            "/api/v1/auth/register"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) {

        http.authorizeHttpRequests(requests -> requests
                .requestMatchers(PUBLIC_URIS).permitAll()
                .anyRequest().authenticated()
        );

        http.formLogin(AbstractHttpConfigurer::disable);
        http.httpBasic(AbstractHttpConfigurer::disable);
        http.csrf(AbstractHttpConfigurer::disable);

        http.sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.cors(c -> c.configurationSource(corsConfig));

        http.addFilterAfter(new AccessTokenValidatorFilter(jwtService), ExceptionTranslationFilter.class);

        http.exceptionHandling(eh -> eh
                .authenticationEntryPoint(new EkkoAuthEntryPoint())
                .accessDeniedHandler(new EkkoAccessDeniedHandler())
        );

        return http.build();

    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) {

        // Returns a DAOAuthenticationManager.
        return authConfig.getAuthenticationManager();

    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
