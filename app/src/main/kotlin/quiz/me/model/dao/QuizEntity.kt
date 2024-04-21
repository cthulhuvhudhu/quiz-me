package quiz.me.model.dao

import jakarta.persistence.*
import jakarta.validation.constraints.Size

@Entity(name = "quiz")
data class QuizEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    val title: String,
    val text: String,
    @Size(min=2)
    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "option")
    val options: List<String>,
    @ElementCollection(fetch = FetchType.EAGER)
    @Column(name = "answer")
    val answers: List<Int> = emptyList(),
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_user_id", referencedColumnName = "id")
    var author: UserEntity,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as QuizEntity

        if (id != other.id) return false
        if (title != other.title) return false
        if (text != other.text) return false
        if (options != other.options) return false
        if (answers != other.answers) return false
        if (author != other.author) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + title.hashCode()
        result = 31 * result + text.hashCode()
        result = 31 * result + options.hashCode()
        result = 31 * result + answers.hashCode()
        result = 31 * result + author.hashCode()
        return result
    }

    override fun toString(): String {
        return "QuizModel(id=$id, title='$title', text='$text', options=$options, answer=$answers, author=$author)"
    }
}
