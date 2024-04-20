package quiz.me.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import org.springframework.core.ParameterizedTypeReference
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest

class TestUtils {
    // Solves initialization issue in integration testing
    @JsonIgnoreProperties("pageable")
    class JacksonPage<T> private constructor(content: List<T>, number: Int, size: Int, totalElements: Long) :
        PageImpl<T>(content, PageRequest.of(number, size), totalElements)
}

inline fun <reified T> typeReference() = object : ParameterizedTypeReference<T>() {}