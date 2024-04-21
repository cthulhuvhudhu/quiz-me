package quiz.me

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class QuizMe

fun main(args: Array<String>) {
    runApplication<QuizMe>(*args)
}
