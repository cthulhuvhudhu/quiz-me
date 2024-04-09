package quiz.me.repository

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
import org.springframework.test.context.jdbc.Sql
import quiz.me.model.dao.QuizEntity
import quiz.me.model.dao.UserEntity
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
        val testUser = users[0]
        val (id, email, password, authority) = userRepository.findUserByEmail(testUser.email)!!
        assertThat(id).isEqualTo(testUser.id)
        assertThat(email).isEqualTo(testUser.email)
        assertThat(password).isEqualTo(testUser.password)
        assertThat(authority).isEqualTo(testUser.authority)
    }
    @Test
    fun `test find missing user by email`() {
        val user = userRepository.findUserByEmail(dneUser.email)
        assertThat(user).isNull()
    }

    @Test
    fun `test find a completed quiz by user`() {
        val testUser = users[0]
        val testQuiz = quizzes[0]
        val actual = userQuizRepository.findAllByUser(testUser, pr)
        assertThat(actual.content.size).isEqualTo(1)
        assertThat(actual.content.first().user).isEqualTo(testUser)
        assertThat(actual.content.first().quiz.id).isEqualTo(testQuiz.id)
        assertThat(actual.content.first().completedAt).isBefore(LocalDateTime.now())
    }

    @Test
    fun `test find all completed quizzes by missing user`() {
        val fail = userQuizRepository.findAllByUser(dneUser, pr)
        assertThat(fail.content.size).isEqualTo(0)
    }

    @Test
    fun `test find all completed quizzes by user`() {
        val fail = userQuizRepository.findAllByUser(users[1], pr)
        assertThat(fail.content.size).isEqualTo(2)
        assertThat(fail.content.map { it.quiz.id }).containsOnly(*quizzes.map { it.id }.toTypedArray())
        assertThat(fail.content.map { it.user }).containsOnly(users[1])
    }

    private val dneUser = UserEntity(
        id = "be859744-0000-4c4e-87c8-3d6bcd611111",
        email = "DNE@a.com",
        password = "fakePassword",
        authority = "ROLE_ADMIN"
    )

    private val users = listOf(
        UserEntity(
            id = "be859744-ee6c-4c4e-87c8-3d6bcd600000",
            email = "a@a.com",
            password = "password",
            authority = "ROLE_USER"
        ),
        UserEntity(
            id = "be859744-ee6c-4c4e-87c8-3d6bcd600001",
            email = "b@a.com",
            password = "password",
            authority = "ROLE_USER"
        )
    )

    private val quizzes = listOf(
        QuizEntity(
            id = 1,
            title = "Select option 1",
            text = "Quiz 1",
            options = listOf("1", "2", "3", "4"),
            answers = listOf(0),
            author = users[0]
        ),
        QuizEntity(
            id = 2,
            title = "Select option 2",
            text = "Quiz 2",
            options = listOf("1", "2", "3", "4"),
            answers = listOf(0, 1),
            author = users[1]
        ),
    )
}