package code81.Library_Management_System_Challenge.infrastructure.configuration;
import code81.Library_Management_System_Challenge.infrastructure.security.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints
                        .requestMatchers("/api/auth/**").permitAll()

                        // Admin only endpoints
                        .requestMatchers(HttpMethod.POST, "/api/users/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/users/**").hasAuthority("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasAuthority("ADMIN")

                        // Librarian and Admin can manage books, members, transactions
                        .requestMatchers(HttpMethod.POST, "/api/books/**").hasAnyAuthority("ADMIN", "LIBRARIAN")
                        .requestMatchers(HttpMethod.PUT, "/api/books/**").hasAnyAuthority("ADMIN", "LIBRARIAN")
                        .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasAnyAuthority("ADMIN", "LIBRARIAN")
                        // Librarian and Admin can view activity logs
                        .requestMatchers(HttpMethod.GET, "/api/users/activity").hasAnyAuthority("ADMIN", "LIBRARIAN")

                        .requestMatchers(HttpMethod.POST, "/api/members/**").hasAnyAuthority("ADMIN", "LIBRARIAN")
                        .requestMatchers(HttpMethod.PUT, "/api/members/**").hasAnyAuthority("ADMIN", "LIBRARIAN")
                        .requestMatchers(HttpMethod.DELETE, "/api/members/**").hasAnyAuthority("ADMIN", "LIBRARIAN")

                        .requestMatchers(HttpMethod.POST, "/api/transactions/**").hasAnyAuthority("ADMIN", "LIBRARIAN", "STAFF")
                        .requestMatchers(HttpMethod.PUT, "/api/transactions/**").hasAnyAuthority("ADMIN", "LIBRARIAN", "STAFF")



                        // All authenticated users can read
                        .requestMatchers(HttpMethod.GET, "/api/**").authenticated()

                        .anyRequest().authenticated()
                )
                .httpBasic(httpBasic -> {});

        return http.build();
    }
}
