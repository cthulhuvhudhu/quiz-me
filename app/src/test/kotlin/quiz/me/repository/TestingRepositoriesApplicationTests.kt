package quiz.me.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.test.context.jdbc.Sql
import quiz.me.model.QuizTestModels
import quiz.me.model.UserTestModels
import java.time.LocalDateTime
import java.util.*

@SpringBootTest
@Sql(scripts = ["classpath:schema.sql", "classpath:data.sql"])
class TestingRepositoriesApplicationTests {

    @Autowired
    private lateinit var userRepository: UserRepository
    @Autowired
    private lateinit var userQuizRepository: UserQuizRepository
    @Autowired
    private lateinit var passwordEncoder: PasswordEncoder

    private val pr = PageRequest.of(0, 2, Sort.by("completedAt").descending())

    @Test
    fun `test find user by email`() {
        val testUser = UserTestModels.users[0]
        val (id, email, password, authorities) = userRepository.findUserByEmail(testUser.email)!!

        assertDoesNotThrow { UUID.fromString(id) }
        assertThat(email).isEqualTo(testUser.email)
        assertThat(passwordEncoder.matches(testUser.dto.password, password)).isTrue()
        assertThat(authorities).containsOnlyOnceElementsOf(testUser.entityOut.authorities)
    }
    @Test
    fun `test find missing user by email`() {
        val user = userRepository.findUserByEmail(UserTestModels.dneUser.email)
        assertThat(user).isNull()
    }

    @Test
    fun `test find a completed quiz by user email`() {
        val usersWithQuizzes = QuizTestModels.userQuizzes.map { it.user }.distinct()
        UserTestModels.users
            .map { it.entityOut }
            .filter { usersWithQuizzes.contains(it) }
            .forEach { testUser ->
                val expectedQuizzes = QuizTestModels.userQuizzes
                    .filter { it.user == testUser }
                    .map { it.userQuizEntity }

                val actual = userQuizRepository.findAllByUser_Email(testUser.email, pr)
                assertThat(actual.content.size).isEqualTo(expectedQuizzes.size).isGreaterThan(0)
                assertThat(actual.content.map { it.user }.distinct().size).isEqualTo(1)
                assertThat(actual.content.map { it.user }.first() == testUser)
                assertThat(actual.content.map { it.quiz }.size).isEqualTo(expectedQuizzes.size)
                assertThat(actual.content.map { it.quiz } == listOf(expectedQuizzes))
                assertThat(actual.content.map { it.completedAt }).allSatisfy { it.isBefore(LocalDateTime.now()) }
                assertThat(actual.totalPages).isEqualTo(1)
        }
    }

    @Test
    fun `test find all completed quizzes by missing user`() {
        val actual = userQuizRepository.findAllByUser_Email(UserTestModels.dneUser.email, pr)
        assertThat(actual.content.size).isEqualTo(0)
    }

    @Test
    fun `test find all completed quizzes by user with no quizzes`() {
        val usersWithQuizzes = QuizTestModels.userQuizzes.map { it.user }.distinct()
        val testUser = UserTestModels.users
            .map { it.entityOut }
            .filterNot { usersWithQuizzes.contains(it) }
            .first()

        val actual = userQuizRepository.findAllByUser_Email(testUser.email, pr)
        assertThat(actual.content.size).isEqualTo(0)
    }
}