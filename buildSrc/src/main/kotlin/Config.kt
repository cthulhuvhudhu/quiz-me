import org.gradle.api.JavaVersion

object Config {
    private const val VERSION_MAJOR = 0
    private const val VERSION_MINOR = 0
    private const val VERSION_PATCH = 1

    @JvmField val javaVersion = JavaVersion.VERSION_21
    val jvmTarget = javaVersion.majorVersion

    const val groupId = "org.cthulhuvhudhu"

    const val versionCode = VERSION_MAJOR * 10_000 + VERSION_MINOR * 100 + VERSION_PATCH
    const val versionName = "$VERSION_MAJOR.$VERSION_MINOR.$VERSION_PATCH"

}