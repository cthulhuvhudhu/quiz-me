package quiz.me.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class FeedbackDTO(
    val success: Boolean,
    val feedback: String
)

val success = FeedbackDTO(true, "Congratulations, you're right!")
val failed = FeedbackDTO(false, "Wrong answer! Please, try again.")
