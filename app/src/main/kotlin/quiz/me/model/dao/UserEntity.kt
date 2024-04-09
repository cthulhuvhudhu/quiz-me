package quiz.me.model.dao

import jakarta.persistence.*
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.userdetails.UserDetails

@Entity(name = "user")
data class UserEntity (
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: String? = null,
    @Column(unique = true, nullable = false)
    val email: String,
    @Column(nullable = false)
    internal val password: String,
    @Transient
    @ElementCollection(fetch = FetchType.EAGER)
    internal val authorities: MutableCollection<SimpleGrantedAuthority>? = mutableListOf(),
//    @OneToMany(mappedBy = "user")
//    val completedQuizzes: List<UserQuizEntity> = emptyList()
) : UserDetails {

    override fun getAuthorities(): MutableCollection<SimpleGrantedAuthority>? = authorities

    override fun getPassword(): String = password

    override fun getUsername(): String = email

    override fun isAccountNonExpired(): Boolean = true

    override fun isAccountNonLocked(): Boolean = true

    override fun isCredentialsNonExpired(): Boolean = true

    override fun isEnabled(): Boolean = true

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as UserEntity

        if (id != other.id) return false
        if (email != other.email) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id?.hashCode() ?: 0
        result = 31 * result + email.hashCode()
        result = 31 * result + password.hashCode()
        result = 31 * result + (authorities?.hashCode() ?: 0)
//        result = 31 * result + completedQuizzes.hashCode()
        return result
    }

    override fun toString(): String {
        return "UserEntity(id=$id, authorities=$authorities, completedQuizzes=)"//$completedQuizzes)"
    }
}
