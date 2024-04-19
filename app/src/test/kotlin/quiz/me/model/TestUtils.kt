package quiz.me.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.core.ParameterizedTypeReference
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import java.nio.charset.Charset

@Component
class TestUtils {
    @JsonIgnoreProperties("pageable") // Solves initialization issue in integration testing
    class JacksonPage<T> private constructor(content: List<T>, number: Int, size: Int, totalElements: Long) :
        PageImpl<T>(content, PageRequest.of(number, size), totalElements)

    fun createAuthHeader(username: String, password: String): HttpHeaders {
        return HttpHeaders().apply {
            this.accept = listOf(MediaType.APPLICATION_JSON)
            this.contentType = MediaType.APPLICATION_JSON
            this.setBasicAuth(username, password, Charset.defaultCharset())
        }
    }
}

inline fun <reified T> typeReference() = object : ParameterizedTypeReference<T>() {}