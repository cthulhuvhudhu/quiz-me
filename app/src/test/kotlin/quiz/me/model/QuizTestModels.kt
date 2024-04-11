package quiz.me.model

import quiz.me.model.dao.QuizEntity
import quiz.me.model.dao.UserEntity
import quiz.me.model.dao.UserQuizEntity
import quiz.me.model.dao.UserQuizKey
import quiz.me.model.dto.CreateQuizDTO
import quiz.me.model.dto.QuizDTO
import quiz.me.model.dto.ViewCompletedQuizDTO
import java.time.LocalDateTime

object QuizTestModels {
    val quizzes = listOf(
        QuizSet(1, "Quiz 1 (ans 1 usr 1)", "Test quiz one answer", listOf(1), UserTestModels.users[0].entityOut),
        QuizSet(2, "Quiz 2 (ans 2,3 usr 2)", "Test quiz two answer", listOf(2, 3), UserTestModels.users[1].entityOut),
        QuizSet(
            3,
            "Quiz 3 (ans [] usr 1)",
            "Test quiz no answer unsolved",
            emptyList(),
            UserTestModels.users[0].entityOut
        ),
    )

    val userQuizzes = listOf(
        UserQuizSet(user = UserTestModels.users[0].entityOut, quiz = quizzes[0].entityOut),
        UserQuizSet(user = UserTestModels.users[0].entityOut, quiz = quizzes[0].entityOut),
        UserQuizSet(user = UserTestModels.users[1].entityOut, quiz = quizzes[0].entityOut),
        UserQuizSet(user = UserTestModels.users[1].entityOut, quiz = quizzes[1].entityOut),
    )

    class UserQuizSet(
        val user: UserEntity,
        val quiz: QuizEntity
    ) {
        private val completedAt = LocalDateTime.now()
        private val key = UserQuizKey(user.id!!, quiz.id!!, completedAt)
        val userQuizEntity = UserQuizEntity(id = key, user = user, quiz = quiz)
        val userQuizDTO = ViewCompletedQuizDTO(quiz.id!!, completedAt)
    }

    class QuizSet(
        val id: Long,
        title: String,
        text: String,
        answer: List<Int>,
        author: UserEntity
    ) {
        private val options = listOf("1", "2", "3", "4")
        val entityIn: QuizEntity = QuizEntity(null, title, text, options, answer, author)
        val entityOut: QuizEntity = QuizEntity(id, title, text, options, answer, author)
        val dto: QuizDTO = QuizDTO(id, title, text, options)
        val createDto: CreateQuizDTO = CreateQuizDTO(title, text, options, answer)
    }
}