package quiz.me.model.dto

import quiz.me.model.dao.QuizEntity
import quiz.me.model.dao.UserEntity
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty

data class CreateQuizDTO(
    @NotBlank(message = "Title is required for quiz")
    val title: String,
    @NotBlank(message = "Text is required for quiz")
    val text: String,
    @NotEmpty(message = "Answer options are required for quiz")
    val options: List<String>,
    val answer: List<Int> = emptyList()
)

fun CreateQuizDTO.toEntity(author: UserEntity) =
    QuizEntity(title = title, text = text, options = options, answers = answer, author = author)
