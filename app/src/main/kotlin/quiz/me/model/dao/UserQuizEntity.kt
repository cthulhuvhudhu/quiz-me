package quiz.me.model.dao

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity(name = "user_quiz")
@Embeddable
data class UserQuizEntity (
    @EmbeddedId
    // TODO composite key for all columns? Eval performance, if embedded is really needed
    var id: UserQuizKey,
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("userId")
    @JoinColumn(name = "user_id", nullable = false, insertable = false, updatable = false)
    val user: UserEntity,
    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("quizId")
    @JoinColumn(name = "quiz_id", nullable = false, insertable = false, updatable = false)
    val quiz: QuizEntity,
    @Column(name = "completed_at", nullable = false, insertable = false, updatable = false)
    val completedAt: LocalDateTime = LocalDateTime.now()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserQuizEntity

        if (id != other.id) return false
        if (user != other.user) return false
        if (quiz != other.quiz) return false
        if (completedAt != other.completedAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + user.hashCode()
        result = 31 * result + quiz.hashCode()
        result = 31 * result + completedAt.hashCode()
        return result
    }
}
