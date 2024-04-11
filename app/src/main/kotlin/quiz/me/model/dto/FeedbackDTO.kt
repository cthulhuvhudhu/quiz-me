package quiz.me.model.dto

data class FeedbackDTO(
    val success: Boolean,
    val feedback: String
)

val success = FeedbackDTO(true, "Congratulations, you're right!")
val failed = FeedbackDTO(false, "Wrong answer! Please, try again.")
