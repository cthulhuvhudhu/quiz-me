package quiz.me.service

//import engine.OwnershipPermissionDeniedException
//import engine.QuizNotFoundException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import quiz.me.model.dao.UserEntity
import quiz.me.model.dto.CreateQuizDTO
import quiz.me.model.dto.FeedbackDTO
import quiz.me.model.dto.QuizDTO
import quiz.me.model.dto.failed
import quiz.me.model.dto.success
import quiz.me.model.dto.toDTO
import quiz.me.model.dto.toEntity
import quiz.me.repository.QuizRepository

@Service
class QuizService(
    private val quizRepository: QuizRepository
) {

    fun getQuizzes(pr: PageRequest): Page<QuizDTO> =
        quizRepository.findAll(pr).map { it.toDTO() }

    fun getQuiz(id: Long): QuizDTO? = quizRepository.findByIdOrNull(id)?.toDTO()

    fun addQuiz(createQuizDTO: CreateQuizDTO, user: UserEntity) =
        quizRepository.save(createQuizDTO.toEntity(user)).toDTO()

    @Transactional // TODO add deleter privs (after auth and spring sec testing)
    fun deleteQuiz(id: Long) { //deleter: UserEntity
//        val quiz = quizRepository.findByIdOrNull(id) ?: throw QuizNotFoundException(id)
//        if (quiz.author != deleter) throw OwnershipPermissionDeniedException()
        quizRepository.deleteById(id)
    }

    fun gradeQuiz(id: Long, answer: List<Int>): FeedbackDTO? = //, user: UserEntity
        quizRepository.findByIdOrNull(id)?.run {
            if (answer.size == this.answers.size && this.answers.toIntArray() contentEquals answer.toIntArray()) {
//                UserQuizEntity(
//                    UserQuizKey(user.id!!, this.id!!, LocalDateTime.now()),
//                    quiz = this,
//                    user = user,
//                    completedAt = LocalDateTime.now()
//                ).let { this.completedQuizzes.add(it) }
//                quizRepository.save(this)
                return success
            } else {
                return failed
            }
        }
}
