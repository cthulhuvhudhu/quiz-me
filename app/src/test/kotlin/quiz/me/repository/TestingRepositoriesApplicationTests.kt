package quiz.me.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.jdbc.Sql

@SpringBootTest
@Sql(scripts = ["classpath:schema.sql", "classpath:data.sql"], executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
class TestingRepositoriesApplicationTests {

    @Autowired
    private lateinit var userRepository: UserRepository

    @Test
    fun testFindUserByEmail() {
        val (id, email, password, authorities) = userRepository.findUserByEmail("a@a.com")!!
        assertThat(id).isEqualTo("be859744-ee6c-4c4e-87c8-3d6bcd600000")
        assertThat(email).isEqualTo("a@a.com")
        assertThat(password).isNotEmpty()
        assertThat(authorities).isNotEmpty()
    }
}