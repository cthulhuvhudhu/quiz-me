package quiz.me.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import quiz.me.model.QuizTestModels
import quiz.me.model.UserTestModels
import quiz.me.repository.UserQuizRepository

@SpringBootTest
class UserQuizServiceTest {

    @MockBean
    private lateinit var userQuizRepository: UserQuizRepository
    @Autowired
    private lateinit var userQuizService: UserQuizService

    @Test
    fun `test find all completed quizzes by user`() {
        UserTestModels.users.map{ it.entityOut }.forEach { user ->
            val userQuizzes = QuizTestModels.userQuizzes.filter { it.user == user }
            val repoResponse = userQuizzes.map { it.userQuizEntity }
            `when`(userQuizRepository.findAllByUser(user)).thenReturn(repoResponse)
            val actual = userQuizService.findAllByUser(user)
            assertThat(actual)
                .containsOnly(*userQuizzes.map { it.userQuizDTO }.toTypedArray())
                .withFailMessage("Check for user '%s' completed quizzes failed", user.email)
        }
    }

    @Test
    fun `test find all completed quizzes by user does not exist`() {
        val actual = userQuizService.findAllByUser(UserTestModels.dneUser)
        assertThat(actual).isEmpty()
    }
}