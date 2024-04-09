package quiz.me.model.dao

import jakarta.persistence.*
import java.io.Serializable
import java.time.LocalDateTime

@Embeddable
data class UserQuizKey (
    @Column(name = "user_id")
    var userId: String,
    @Column(name = "quiz_id")
    var quizId: Long,
    @Column(name = "completed_at")
    var completedAt: LocalDateTime
) : Serializable
