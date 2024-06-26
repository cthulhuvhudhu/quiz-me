package quiz.me.service

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import quiz.me.OwnershipPermissionDeniedException
import quiz.me.QuizNotFoundException
import quiz.me.model.dao.UserQuizEntity
import quiz.me.model.dto.CreateQuizDTO
import quiz.me.model.dto.FeedbackDTO
import quiz.me.model.dto.QuizDTO
import quiz.me.model.dto.failed
import quiz.me.model.dto.success
import quiz.me.model.dto.toDTO
import quiz.me.model.dto.toEntity
import quiz.me.repository.QuizRepository
import quiz.me.repository.UserQuizRepository
import quiz.me.repository.UserRepository

@Service
class QuizService(
    private val quizRepository: QuizRepository,
    private val userRepository: UserRepository,
    private val userQuizRepository: UserQuizRepository
) {

    fun getQuizzes(pr: PageRequest): Page<QuizDTO> =
        quizRepository.findAll(pr).map { it.toDTO() }

    fun getQuiz(id: Long): QuizDTO? = quizRepository.findByIdOrNull(id)?.toDTO()

    fun addQuiz(createQuizDTO: CreateQuizDTO, username: String): QuizDTO {
        val user = userRepository.findUserByEmail(username)!!
        return quizRepository.save(createQuizDTO.toEntity(user)).toDTO()
    }

    @Transactional
    fun deleteQuiz(id: Long, deleterEmail: String) {
        val quiz = quizRepository.findByIdOrNull(id) ?: throw QuizNotFoundException(id)
        if (deleterEmail != quiz.author.email) throw OwnershipPermissionDeniedException()
        quizRepository.deleteById(id)
    }

    fun gradeQuiz(id: Long, answer: List<Int>, username: String): FeedbackDTO =
        quizRepository.findByIdOrNull(id)?.run {
            val user = userRepository.findUserByEmail(username)!!
            if (answer.size == this.answers.size && this.answers.toIntArray() contentEquals answer.toIntArray()) {
                val record = UserQuizEntity(user = user, quiz = this)
                userQuizRepository.save(record)
                return success
            } else {
                return failed
            }
        } ?: throw QuizNotFoundException(id)
}
