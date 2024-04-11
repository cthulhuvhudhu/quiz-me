package quiz.me.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import quiz.me.model.QuizTestModels
import quiz.me.model.UserTestModels
import quiz.me.repository.UserQuizRepository

@SpringBootTest
class UserQuizServiceTest {

    @MockBean
    private lateinit var userQuizRepository: UserQuizRepository
    @Autowired
    private lateinit var userQuizService: UserQuizService
    private val pr = PageRequest.of(0, 2, Sort.by("completedAt").descending())

    @Test
    fun `test find all completed quizzes by user`() {
        UserTestModels.users.map{ it.entityOut }.forEach { user ->
            val userQuizzes = QuizTestModels.userQuizzes.filter { it.user == user }
            val repoResponse = PageImpl(userQuizzes.map { it.userQuizEntity })
            `when`(userQuizRepository.findAllByUser(user, pr)).thenReturn(repoResponse)
            val actual = userQuizService.findAllByUser(user, pr)
            assertThat(actual)
                .containsOnly(*userQuizzes.map { it.userQuizDTO }.toTypedArray())
                .withFailMessage("Check for user '%s' completed quizzes failed", user.email)
        }
    }

    @Test
    fun `test find all completed quizzes by user does not exist`() {
        `when`(userQuizRepository.findAllByUser(UserTestModels.dneUser, pr)).thenReturn(PageImpl(emptyList()))
        val actual = userQuizService.findAllByUser(UserTestModels.dneUser, pr)
        assertThat(actual).isEmpty()
    }
}