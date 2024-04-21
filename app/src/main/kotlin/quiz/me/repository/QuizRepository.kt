package quiz.me.repository

import quiz.me.model.dao.QuizEntity
import org.springframework.data.jpa.repository.JpaRepository

interface QuizRepository : JpaRepository<QuizEntity, Long>
