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
import org.springframework.context.annotation.Primary;


@Configuration
@EnableWebSecurity
@Order(2) // Se ejecutará después de la configuración de trabajadores
public class SeguridadConfig {

    private final UserDetailsService usuarioServicio;
    private final PasswordEncoder passwordEncoder;
    private final ManejadorAutenticacionExitosa manejadorAutenticacionExitosa;
    private final CorsConfigurationSource corsConfigurationSource;

    public SeguridadConfig(@Qualifier("usuarioServicioImpl") UserDetailsService usuarioServicio, 
                          PasswordEncoder passwordEncoder,
                          ManejadorAutenticacionExitosa manejadorAutenticacionExitosa,
                          CorsConfigurationSource corsConfigurationSource) {
        this.usuarioServicio = usuarioServicio;
        this.passwordEncoder = passwordEncoder;
        this.manejadorAutenticacionExitosa = manejadorAutenticacionExitosa;
        this.corsConfigurationSource = corsConfigurationSource;
    }

    // El bean corsConfigurationSource ahora está definido en la clase CorsConfig

    @Bean
    @Order(2) // Se ejecuta después de la configuración de trabajadores
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(
                    "/trabajador/**"
                ).denyAll() // Denegar acceso a rutas de trabajador (manejadas por SeguridadTrabajadorConfig)
                .requestMatchers(
                    "/",
                    "/registro",
                    "/registro/**",
                    "/login",
                    "/login/**",
                    "/buscar",
                    "/test-cambios",
                    "/css/**",
                    "/js/**",
                    "/img/**",
                    "/webjars/**",
                    "/uploads/**",
                    "/api/tiendas",
                    "/api/tiendas/buscar",
                    "/recuperar",
                    "/recuperar/**",
                    "/servicios",
                    "/nosotros",
                    "/condiciones",
                    "/politica",
                    "/cookies"
                ).permitAll()
                .requestMatchers("/api/productos/**").authenticated()
                .requestMatchers("/api/cambios/**").authenticated()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/panel/**").authenticated()
                .requestMatchers("/cambios/**").authenticated()
                .anyRequest().authenticated()
            )
            .formLogin(form -> form
                .loginPage("/login")
                .loginProcessingUrl("/login")
                .successHandler(manejadorAutenticacionExitosa)
                .failureUrl("/login?error=true")
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID")
                .permitAll()
            )
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
            )
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .csrf(AbstractHttpConfigurer::disable)
            .authenticationProvider(authenticationProvider());

        return http.build();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        // Aunque estos métodos están marcados como deprecados, son necesarios para la versión actual de Spring Security
        // En futuras versiones, podrían ser reemplazados por una configuración más moderna
        @SuppressWarnings("deprecation")
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(usuarioServicio);
        authProvider.setPasswordEncoder(passwordEncoder);
        return authProvider;
    }

    @Bean
    @Primary
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
}
