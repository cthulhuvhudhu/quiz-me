package quiz.me.service

import quiz.me.model.dao.UserEntity
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

class UserAdapter(private val user: UserEntity) : UserDetails {
    override fun getAuthorities(): Collection<GrantedAuthority>  = user.authorities.toList()

    override fun getPassword(): String = requireNotNull(user.password)

    override fun getUsername(): String = requireNotNull(user.email)

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserAdapter

        return user == other.user
    }

    override fun hashCode(): Int {
        return user.hashCode()
    }
}
