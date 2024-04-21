package quiz.me.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class GuessDTO(val answer: List<Int>)