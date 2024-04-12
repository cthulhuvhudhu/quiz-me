package quiz.me

import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.authentication.dao.DaoAuthenticationProvider
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import quiz.me.model.UserTestModels
import quiz.me.repository.UserRepository
import quiz.me.service.UserAdapter
import kotlin.test.assertNotNull

@SpringBootTest
class ApplicationConfigTest {
    @MockBean
    private lateinit var userRepository: UserRepository
    @Autowired
    private lateinit var appConfig: ApplicationConfig

    @Test
    fun `test password encoder bean`() {
        val actual = appConfig.passwordEncoder()
        assertThat(actual is BCryptPasswordEncoder).isTrue()
        assertThat(actual.matches("hprskll", "\$2a\$12\$QHQ0KOjmoCm.Q1/IVYlz9ekth2pqzMeklkanicrD5XSVt90KDnT6S")).isTrue()
    }

    @Test
    fun `test user details service bean`() {
        val testUser = UserTestModels.users.first()
        `when`(userRepository.findUserByEmail(testUser.email)).thenReturn(testUser.entityOut)
        val actual = appConfig.userDetailsService()!!.loadUserByUsername(testUser.email)
        assertThat(actual).isEqualTo(testUser.entityOut)
    }

    @Test
    fun `test user details service bean does not exist`() {
        assertThrows<UsernameNotFoundException> { appConfig.userDetailsService()!!.loadUserByUsername(UserTestModels.dneUser.email) }
    }

    @Test
    fun `test authentication provider bean`() {
        val actual = appConfig.authenticationProvider()
        assertNotNull(actual)
        assertThat(actual).isInstanceOf(DaoAuthenticationProvider::class.java)
    }

    @Test
    fun `test authentication manager bean`() {
        val actual = appConfig.authenticationProvider()
        assertNotNull(actual)
        assertThat(actual).isInstanceOf(DaoAuthenticationProvider::class.java)
    }
}
