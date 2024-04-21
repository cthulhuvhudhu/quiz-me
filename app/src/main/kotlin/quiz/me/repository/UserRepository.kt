package quiz.me.repository

import quiz.me.model.dao.UserEntity
import org.springframework.data.repository.CrudRepository

interface UserRepository : CrudRepository<UserEntity, String> {
    fun findUserByEmail(email: String): UserEntity?
}
