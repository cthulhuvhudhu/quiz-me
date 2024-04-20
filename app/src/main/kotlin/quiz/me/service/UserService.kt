package quiz.me.service

import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UserDetailsService
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import quiz.me.model.dao.UserEntity
import quiz.me.repository.UserRepository

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
) : UserDetailsService {

//    @Transactional
    fun registerUser(email: String, password: String) =
        UserEntity(email = email, password = passwordEncoder.encode(password))
            .run { userRepository.save(this) }

    override fun loadUserByUsername(username: String): UserDetails =
        userRepository.findUserByEmail(username)
            ?.let{ UserAdapter(it) } ?:
        throw UsernameNotFoundException("Username '$username' not found")
}
