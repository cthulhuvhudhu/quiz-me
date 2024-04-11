package quiz.me.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.jdbc.Sql
import quiz.me.model.UserTestModels
import quiz.me.model.completedQuizzes
import java.time.LocalDateTime

@SpringBootTest
@Sql(scripts = ["classpath:schema.sql", "classpath:data.sql"])
class TestingRepositoriesApplicationTests {

    @Autowired
    private lateinit var userRepository: UserRepository
    @Autowired
    private lateinit var userQuizRepository: UserQuizRepository

    private val pr = PageRequest.of(0, 15)

    @Test
    fun `test find user by email`() {
        val testUser = UserTestModels.users[0]
        val (id, email, password, authority) = userRepository.findUserByEmail(testUser.email)!!
        assertThat(id).isEqualTo(testUser.id)
        assertThat(email).isEqualTo(testUser.email)
        assertThat(password).isEqualTo(testUser.password)
        assertThat(authority).isEqualTo(testUser.authority)
    }
    @Test
    fun `test find missing user by email`() {
        val user = userRepository.findUserByEmail(UserTestModels.dneUser.email)
        assertThat(user).isNull()
    }

    @Test
    fun `test find a completed quiz by user`() {
        val usersWithQuizzes = completedQuizzes.map { it.first }.distinct()
        UserTestModels.users
            .filter { usersWithQuizzes.contains(it) }
            .forEach { testUser ->
                val expectedQuizzes = completedQuizzes.filter { it.first == testUser }.map { it.second }

                val actual = userQuizRepository.findAllByUser(testUser, pr)
                assertThat(actual.content.size).isEqualTo(expectedQuizzes.size).isGreaterThan(0)
                assertThat(actual.content.map { it.user }.distinct().size).isEqualTo(1)
                assertThat(actual.content.map { it.user }.first() == testUser)
                assertThat(actual.content.map { it.quiz }.distinct().size).isEqualTo(expectedQuizzes.size)
                assertThat(actual.content.map { it.quiz } == listOf(expectedQuizzes))
                assertThat(actual.content.map { it.completedAt }).allSatisfy { it.isBefore(LocalDateTime.now()) }
        }
    }

    @Test
    fun `test find all completed quizzes by missing user`() {
        val actual = userQuizRepository.findAllByUser(UserTestModels.dneUser, pr)
        assertThat(actual.content.size).isEqualTo(0)
    }

    @Test
    fun `test find all completed quizzes by user with no quizzes`() {
        val usersWithQuizzes = completedQuizzes.map { it.first }.distinct()
        val testUser = UserTestModels.users.filterNot { usersWithQuizzes.contains(it) }.first()

        val actual = userQuizRepository.findAllByUser(testUser, pr)
        assertThat(actual.content.size).isEqualTo(0)
    }
}