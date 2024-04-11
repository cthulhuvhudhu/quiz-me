package quiz.me.service;

import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito.*
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder;
import quiz.me.model.UserTestModels
import quiz.me.repository.UserRepository;
import kotlin.test.Test

@SpringBootTest
class UserServiceTest {

    @MockBean
    private lateinit var userRepository: UserRepository
    @MockBean
    private lateinit var passwordEncoder: PasswordEncoder
    @Autowired
    private lateinit var userService: UserService

    @Test
    fun `test register user`() {
        UserTestModels.users.forEach { user ->
            `when`(passwordEncoder.encode(user.dto.password)).thenReturn(user.entityIn.password)
            `when`(userRepository.save(user.entityIn)).thenAnswer { user.entityOut }
            val actual = userService.registerUser(user.email, user.dto.password)
            assertThat(actual).isEqualTo(user.entityOut)
            verify(userRepository, times(1)).save(user.entityIn)
        }
    }

    @Test
    fun `test load user by username (auth support)`() {
        UserTestModels.users.forEach { user ->
            `when`(userRepository.findUserByEmail(user.email)).thenAnswer { user.entityOut }
            val expected = UserAdapter(user.entityOut)
            val actual = userService.loadUserByUsername(user.email)
            assertThat(expected).isEqualTo(actual)
            verify(userRepository, times(1)).findUserByEmail(user.email)
        }
    }

    @Test
    fun `test load user by username (auth support) does not exist`() {
        val user = UserTestModels.dneUser
        `when`(userRepository.findUserByEmail(user.email)).thenAnswer { null }
        assertThrows<UsernameNotFoundException> { userService.loadUserByUsername(user.email) }
        verify(userRepository, times(1)).findUserByEmail(user.email)
    }
}
