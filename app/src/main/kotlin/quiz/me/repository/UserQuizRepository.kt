package quiz.me.repository

import quiz.me.model.dao.UserQuizEntity
import quiz.me.model.dao.UserEntity
import org.springframework.data.jpa.repository.JpaRepository

interface UserQuizRepository : JpaRepository<UserQuizEntity, Long> {
    fun findAllByUser(user: UserEntity): List<UserQuizEntity>//, page: Pageable): Page<UserQuizEntity>
}