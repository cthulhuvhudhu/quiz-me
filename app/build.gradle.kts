import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:${Version.kotlin}")
        classpath("org.springframework.boot:spring-boot-gradle-plugin:${Version.springBoot}")
        classpath("io.spring.gradle:dependency-management-plugin:${Version.springDepMan}")
        classpath("org.jetbrains.kotlin:kotlin-allopen:${Version.kotlin}")
        classpath("org.jetbrains.kotlin:kotlin-noarg:${Version.kotlin}")
    }
}

plugins {
    id("org.springframework.boot") version Version.springBoot
    id("io.spring.dependency-management") version Version.springDepMan
    kotlin("jvm") version Version.kotlin
    kotlin("plugin.spring") version Version.kotlin
    kotlin("plugin.jpa") version Version.kotlin
    kotlin("plugin.serialization") version Version.kotlin
    application
}

group = Config.groupId
version = Config.versionName

java {
    sourceCompatibility = Config.javaVersion
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-security:3.2.4")
    implementation("jakarta.validation:jakarta.validation-api:3.0.2")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

    runtimeOnly("com.h2database:h2:2.2.224")

    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5:1.7.10")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.assertj:assertj-core:3.25.1")
    testImplementation("org.testcontainers:testcontainers:1.19.7")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:junit-jupiter")
}

application {
    mainClass.set("quiz.me.QuizMeKt")
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs += "-Xjsr305=strict"
        jvmTarget = Config.jvmTarget
    }
}

tasks.named<Test>("test") {
    jvmArgs("-XX:+EnableDynamicAgentLoading", "-Djdk.instrument.traceUsage")
    useJUnitPlatform()
}
