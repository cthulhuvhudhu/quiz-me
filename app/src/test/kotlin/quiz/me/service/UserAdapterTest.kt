package quiz.me.service

import org.assertj.core.api.AssertionsForClassTypes.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import quiz.me.model.UserTestModels

@SpringBootTest
class UserAdapterTest {

    @Test
    fun `test adapt user entity`() {
        UserTestModels.users.map { it.entityOut }.forEach { user ->
            val actual = UserAdapter(user)
            assertThat(actual.password == user.password)
            assertThat(actual.username == user.email)
            assertThat(actual.isAccountNonExpired)
            assertThat(actual.isAccountNonLocked)
            assertThat(actual.isCredentialsNonExpired)
            assertThat(actual.isEnabled)
            assertThat(actual.authorities == user.authorities)
        }
    }
}