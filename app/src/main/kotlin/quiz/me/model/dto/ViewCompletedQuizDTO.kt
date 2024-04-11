package quiz.me.model.dto

import com.fasterxml.jackson.annotation.JsonProperty
import java.time.LocalDateTime

data class ViewCompletedQuizDTO (
    @field:JsonProperty("id")
    val quizId: Long?,
    val completedAt: LocalDateTime
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ViewCompletedQuizDTO

        return quizId == other.quizId
    }

    override fun hashCode(): Int {
        return quizId?.hashCode() ?: 0
    }
}
