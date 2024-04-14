package quiz.me

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationProvider
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.web.SecurityFilterChain

@Configuration
@EnableWebSecurity
class SecurityConfig(private val authenticationProvider: AuthenticationProvider) {

    @Bean
    @Throws(Exception::class)
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain =
        http
            .httpBasic(Customizer.withDefaults())
            .csrf { it.disable() }
            .authorizeHttpRequests { matcherRegistry ->
                matcherRegistry
                    .requestMatchers(HttpMethod.POST, "/api/register").permitAll()
                    .requestMatchers(HttpMethod.POST, "/actuator/shutdown").permitAll()
//                    .anyRequest().authenticated()
                    .anyRequest().permitAll()
            }
            .authenticationProvider(authenticationProvider)
            .build()
}
