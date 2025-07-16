package com.GPUHUB.GPUHUB.configuracion;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.beans.factory.annotation.Qualifier;

@Configuration
@EnableWebSecurity
@Order(1) // Asegura que esta configuración se aplique antes que la configuración principal
public class SeguridadTrabajadorConfig {

    private final UserDetailsService trabajadorUserDetailsService;
    private final PasswordEncoder passwordEncoder;
    private final ManejadorAutenticacionExitosaTrabajador manejadorAutenticacionExitosaTrabajador;
    private final CorsConfigurationSource corsConfigurationSource;

    public SeguridadTrabajadorConfig(@Qualifier("trabajadorUserDetailsService") UserDetailsService trabajadorUserDetailsService,
                                   PasswordEncoder passwordEncoder,
                                   ManejadorAutenticacionExitosaTrabajador manejadorAutenticacionExitosaTrabajador,
                                   CorsConfigurationSource corsConfigurationSource) {
        this.trabajadorUserDetailsService = trabajadorUserDetailsService;
        this.passwordEncoder = passwordEncoder;
        this.manejadorAutenticacionExitosaTrabajador = manejadorAutenticacionExitosaTrabajador;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    @Bean
    @Order(1) // Asegurar que esta configuración se aplique primero
    public SecurityFilterChain trabajadorFilterChain(HttpSecurity http) throws Exception {
        http
            .securityMatcher(
                "/trabajador/login",
                "/trabajador/login/**",
                "/trabajador/logout",
                "/trabajador/panel/**",
                "/api/trabajador/**",
                "/trabajador/cambios-trabajador"
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/trabajador/login",
                    "/trabajador/login/**"
                ).permitAll()
                .requestMatchers(
                    "/trabajador/panel/**",
                    "/trabajador/logout",
                    "/api/trabajador/**",
                    "/trabajador/cambios-trabajador"
                ).hasRole("TRABAJADOR")
                .anyRequest().denyAll()
            )
            .formLogin(form -> form
                .loginPage("/trabajador/login")
                .loginProcessingUrl("/trabajador/login")
                .successHandler(manejadorAutenticacionExitosaTrabajador)
                .failureUrl("/trabajador/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/trabajador/logout")
                .logoutSuccessUrl("/trabajador/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(AbstractHttpConfigurer::disable)
            .authenticationProvider(trabajadorAuthenticationProvider());

        return http.build();
    }

    @Bean
    @SuppressWarnings("deprecation") // Se mantiene por compatibilidad con la versión actual de Spring Security
    public DaoAuthenticationProvider trabajadorAuthenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(trabajadorUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean(name = "trabajadorAuthenticationManager")
    public AuthenticationManager trabajadorAuthenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // El bean corsConfigurationSource ahora está definido en la clase CorsConfig
}
