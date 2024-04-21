package quiz.me.model.dto

import quiz.me.model.dao.QuizEntity
import quiz.me.model.dao.UserEntity
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size
import kotlinx.serialization.Serializable

@Serializable
data class CreateQuizDTO(
    @field:NotBlank(message = "Title is required for quiz")
    val title: String = "",
    @field:NotBlank(message = "Text is required for quiz")
    val text: String = "",
    @field:NotEmpty(message = "At least two answer options are required for quiz")
    @field:Size(min = 2, message = "At least two answer options are required for quiz")
    val options: List<String> = emptyList(),
    val answer: List<Int> = emptyList()
)

fun CreateQuizDTO.toEntity(author: UserEntity) =
    QuizEntity(title = title, text = text, options = options, answers = answer, author = author)
