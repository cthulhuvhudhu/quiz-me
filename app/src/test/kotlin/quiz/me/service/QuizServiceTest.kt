package quiz.me.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import quiz.me.model.QuizTestModels
import quiz.me.model.dto.failed
import quiz.me.model.dto.success
import quiz.me.repository.QuizRepository
import java.util.Optional
import kotlin.test.assertNull

@SpringBootTest
class QuizServiceTest {

    @MockBean
    private lateinit var quizRepository: QuizRepository
    @Autowired
    private lateinit var quizService: QuizService

    @Test
    fun `test get all quizzes`() {
        `when`(quizRepository.findAll())
            .thenReturn(QuizTestModels.quizzes.map { it.entityOut })
        val actual = quizService.getQuizzes()
        assertThat(actual.size).isEqualTo(QuizTestModels.quizzes.size)
        assertThat(actual).containsOnly(*QuizTestModels.quizzes.map{ it.dto }.toTypedArray())
    }

    @Test
    fun `test get all quizzes empty`() {
        `when`(quizRepository.findAll()).thenReturn(emptyList())
        val actual = quizService.getQuizzes()
        assertThat(actual).isEmpty()
    }

    @Test
    fun `test get quiz exists`() {
        QuizTestModels.quizzes.forEach {
            `when`(quizRepository.findById(it.id))
                .thenReturn(Optional.of(it.entityOut))
            val actual = quizService.getQuiz(it.id)
            assertThat(actual).isEqualTo(it.dto)
        }
    }

    @Test
    fun `test get quiz does not exist`() {
        val actual = quizService.getQuiz(-1)
        assertThat(actual).isNull()
    }

    @Test
    fun `test add quiz`() {
        QuizTestModels.quizzes.forEach {
            `when`(quizRepository.save(it.entityIn))
                .thenReturn(it.entityOut)
            val actual = quizService.addQuiz(it.createDto, it.entityOut.author)
            assertThat(actual).isEqualTo(it.dto)
        }
    }

    @Test
    fun `test delete quiz exists`() {
        QuizTestModels.quizzes.forEach {
            quizService.deleteQuiz(it.id)
            verify(quizRepository, times(1)).deleteById(it.id)
        }
    }

    @Test
    fun `test delete quiz does not exist`() {
        quizService.deleteQuiz(-1)
        verify(quizRepository, times(1)).deleteById(-1)
    }

    @Test
    fun `test grade quiz`() {
        // Empty, single, and multiple solution sets (to three quizzes)
        val possibleAnswers = QuizTestModels.quizzes.map { it.entityOut.answers }
        QuizTestModels.quizzes.forEach {
            `when`(quizRepository.findById(it.id))
                .thenReturn(Optional.of(it.entityOut))
            possibleAnswers.forEach { guess ->
                val actual = quizService.gradeQuiz(it.id, guess)
                if (guess == it.entityOut.answers) {
                    assertThat(actual).isEqualTo(success)
                } else {
                    assertThat(actual).isEqualTo(failed)
                }
            }
            verify(quizRepository, times(3)).findById(it.id)
        }
    }

    @Test
    fun `test grade quiz does not exist`() {
        assertNull(quizService.gradeQuiz(-1, listOf()))
    }
}
