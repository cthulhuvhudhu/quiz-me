package quiz.me.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mockito.*

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import quiz.me.OwnershipPermissionDeniedException
import quiz.me.QuizNotFoundException
import quiz.me.model.QuizTestModels
import quiz.me.model.UserTestModels
import quiz.me.model.dao.UserQuizEntity
import quiz.me.model.dto.failed
import quiz.me.model.dto.success
import quiz.me.repository.QuizRepository
import quiz.me.repository.UserQuizRepository
import quiz.me.repository.UserRepository
import java.time.LocalDateTime
import java.util.Optional

@SpringBootTest
class QuizServiceTest {

    @MockBean
    private lateinit var quizRepository: QuizRepository
    @MockBean
    private lateinit var userRepository: UserRepository
    @MockBean
    private lateinit var userQuizRepository: UserQuizRepository
    @Autowired
    private lateinit var quizService: QuizService

    @Captor
    private lateinit var userQuizCaptor: ArgumentCaptor<UserQuizEntity>

    private val pr = PageRequest.of(0, 2)
    private val testUserEmail = "a@a.com"

    @Test
    fun `test get all quizzes`() {
        `when`(quizRepository.findAll(pr))
            .thenReturn(PageImpl(QuizTestModels.quizzes.map { it.entityOut }))
        val actual = quizService.getQuizzes(pr)
        assertThat(actual.content.size).isEqualTo(QuizTestModels.quizzes.size)
        assertThat(actual.content).containsOnly(*QuizTestModels.quizzes.map{ it.dto }.toTypedArray())
    }

    @Test
    fun `test get all quizzes empty`() {
        `when`(quizRepository.findAll(pr)).thenReturn(PageImpl(emptyList()))
        val actual = quizService.getQuizzes(pr)
        assertThat(actual.content).isEmpty()
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
            `when`(userRepository.findUserByEmail(it.entityOut.author.email))
                .thenReturn(it.entityOut.author)
            val actual = quizService.addQuiz(it.createDto, it.entityOut.author.email)
            assertThat(actual).isEqualTo(it.dto)
        }
    }

    @Test
    fun `test delete quiz exists`() {
        val testQuiz = QuizTestModels.quizzes.first().entityOut
        `when`(quizRepository.findById(testQuiz.id!!)).thenReturn(Optional.of(testQuiz))
        quizService.deleteQuiz(testQuiz.id!!, testUserEmail)
        verify(quizRepository, times(1)).deleteById(testQuiz.id!!)
    }

    @Test
    fun `test delete quiz does not exist`() {
        `when`(quizRepository.findById(-1)).thenReturn(Optional.empty())
        assertThrows<QuizNotFoundException> { quizService.deleteQuiz(-1, testUserEmail) }
    }

    @Test
    fun `test delete quiz deleter not creator`() {
        `when`(quizRepository.findById(1)).thenReturn(Optional.of(QuizTestModels.quizzes.first().entityOut))
        assertThrows<OwnershipPermissionDeniedException> { quizService.deleteQuiz(1, "deleterEmail") }
    }

    @Test
    fun `test grade quiz correct is one answer`() {
        val possibleAnswers = QuizTestModels.quizzes.map { it.entityOut.answers }
        val testUser = UserTestModels.users.first().entityOut
        val testQuizSet = QuizTestModels.quizzes.first()
//        assertThat(testQuizSet.entityOut.completedQuizzes).isEmpty()
        `when`(quizRepository.findById(testQuizSet.id))
            .thenReturn(Optional.of(testQuizSet.entityOut))
        `when`(userRepository.findUserByEmail(testUser.email)).thenReturn(testUser)
        possibleAnswers.forEach { guess ->
            val actual = quizService.gradeQuiz(testQuizSet.id, guess, testUser.email)
            if (guess == testQuizSet.entityOut.answers) {
                assertThat(actual).isEqualTo(success)
                verify(userQuizRepository).save(userQuizCaptor.capture())
                assertThat(userQuizCaptor.value.user).isEqualTo(testUser)
                assertThat(userQuizCaptor.value.quiz).isEqualTo(testQuizSet.entityOut)
                assertThat(userQuizCaptor.value.completedAt).isBefore(LocalDateTime.now())
            } else {
                assertThat(actual).isEqualTo(failed)
            }
        }
        verify(quizRepository, times(3)).findById(testQuizSet.id)
    }

    @Test
    fun `test grade quiz correct is multiple answers`() {
        val possibleAnswers = QuizTestModels.quizzes.map { it.entityOut.answers }
        val testUser = UserTestModels.users[0].entityOut
        val testQuizSet = QuizTestModels.quizzes[1]

        `when`(quizRepository.findById(testQuizSet.id))
            .thenReturn(Optional.of(testQuizSet.entityOut))
        `when`(userRepository.findUserByEmail(testUser.email)).thenReturn(testUser)
        possibleAnswers.forEach { guess ->
            val actual = quizService.gradeQuiz(testQuizSet.id, guess, testUser.email)
            if (guess == testQuizSet.entityOut.answers) {
                assertThat(actual).isEqualTo(success)
                verify(userQuizRepository).save(userQuizCaptor.capture())
                assertThat(userQuizCaptor.value.user).isEqualTo(testUser)
                assertThat(userQuizCaptor.value.quiz).isEqualTo(testQuizSet.entityOut)
                assertThat(userQuizCaptor.value.completedAt).isBefore(LocalDateTime.now())
            } else {
                assertThat(actual).isEqualTo(failed)
            }
        }
        verify(quizRepository, times(3)).findById(testQuizSet.id)
    }

    @Test
    fun `test grade quiz correct is no answers`() {
        val possibleAnswers = QuizTestModels.quizzes.map { it.entityOut.answers }
        val testUser = UserTestModels.users[0].entityOut
        val testQuizSet = QuizTestModels.quizzes[2]

        `when`(quizRepository.findById(testQuizSet.id))
            .thenReturn(Optional.of(testQuizSet.entityOut))
        `when`(userRepository.findUserByEmail(testUser.email)).thenReturn(testUser)
        possibleAnswers.forEach { guess ->
            val actual = quizService.gradeQuiz(testQuizSet.id, guess, testUser.email)
            if (guess == testQuizSet.entityOut.answers) {
                assertThat(actual).isEqualTo(success)
                verify(userQuizRepository).save(userQuizCaptor.capture())
                assertThat(userQuizCaptor.value.user).isEqualTo(testUser)
                assertThat(userQuizCaptor.value.quiz).isEqualTo(testQuizSet.entityOut)
                assertThat(userQuizCaptor.value.completedAt).isBefore(LocalDateTime.now())
            } else {
                assertThat(actual).isEqualTo(failed)
            }
        }
        verify(quizRepository, times(3)).findById(testQuizSet.id)
    }

    @Test
    fun `test grade quiz does not exist`() {
        `when`(quizRepository.findById(-1)).thenReturn(Optional.empty())
        assertThrows<QuizNotFoundException> { quizService.gradeQuiz(-1, listOf(), "a@a.com") }
    }
}
