package quiz.me

import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

@SpringBootTest
class ApplicationConfigTest {
    @Autowired
    private lateinit var appConfig: ApplicationConfig

    @Test
    fun `test password encoder bean`() {
        val actual = appConfig.passwordEncoder()
        assertThat(actual is BCryptPasswordEncoder).isTrue()
        assertThat(actual.matches("hprskll", "\$2a\$12\$QHQ0KOjmoCm.Q1/IVYlz9ekth2pqzMeklkanicrD5XSVt90KDnT6S")).isTrue()
    }
}
