package quiz.me.model.dto

import quiz.me.model.dao.QuizEntity
import jakarta.validation.constraints.Size
import kotlinx.serialization.Serializable

@Serializable
data class QuizDTO (
    val id: Long?,
    val title: String,
    val text: String,
    @field:Size(min = 2)
    val options: List<String>
)

fun QuizEntity.toDTO(): QuizDTO = QuizDTO(this.id, this.title, this.text, this.options)