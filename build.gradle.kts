plugins {
    kotlin("jvm") version "1.9.25"
    kotlin("plugin.spring") version "1.9.25"
    id("org.springframework.boot") version "3.4.3"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.jpa") version "1.9.25"
}

group = "com.magrathea"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

repositories {
    mavenCentral()
}

sourceSets {
    create("integrationTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
        resources.srcDir("src/integrationTest/resources")
    }
    create("end2endTest") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
        resources.srcDir("src/end2endTest/resources")
    }
}

val integrationTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.implementation.get())
}

val end2endTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.implementation.get())
}

configurations["integrationTestImplementation"].extendsFrom(configurations.runtimeOnly.get())
configurations["end2endTestImplementation"].extendsFrom(configurations.runtimeOnly.get())


dependencies {
    developmentOnly("org.springframework.boot:spring-boot-devtools")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.kafka:spring-kafka")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.flywaydb:flyway-core")
    implementation("org.flywaydb:flyway-database-postgresql")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.3.0")
    implementation("org.springdoc:springdoc-openapi-starter-common:2.3.0")

    runtimeOnly("org.postgresql:postgresql")

    implementation("io.github.openfeign:feign-okhttp:13.6")
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign:4.2.1")
    implementation("org.springframework.cloud:spring-cloud-starter-circuitbreaker-resilience4j:3.2.1")

    implementation("commons-codec:commons-codec:1.18.0")

    implementation("org.apache.logging.log4j:log4j-api:2.24.3")
    implementation("org.apache.logging.log4j:log4j-core:2.24.3")
    implementation("org.apache.logging.log4j:log4j-slf4j2-impl:2.24.3")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("io.mockk:mockk:1.13.17")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testRuntimeOnly("com.h2database:h2")

    integrationTestImplementation("org.springframework.boot:spring-boot-starter-test")
    integrationTestImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    integrationTestImplementation("org.springframework.boot:spring-boot-testcontainers")
    integrationTestImplementation("org.testcontainers:junit-jupiter")
    integrationTestImplementation("org.testcontainers:postgresql")
    integrationTestImplementation("org.testcontainers:kafka")
    integrationTestImplementation("org.springframework.kafka:spring-kafka-test")
    integrationTestImplementation("org.wiremock.integrations:wiremock-spring-boot:3.10.0")

    end2endTestImplementation("org.springframework.boot:spring-boot-starter-test")
    end2endTestImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    end2endTestImplementation("org.springframework.boot:spring-boot-testcontainers")
    end2endTestImplementation("org.testcontainers:junit-jupiter")
    end2endTestImplementation("org.testcontainers:postgresql")
    end2endTestImplementation("org.testcontainers:kafka")
    end2endTestImplementation("io.rest-assured:rest-assured:5.4.0")
    end2endTestImplementation("io.rest-assured:kotlin-extensions:5.4.0")
    end2endTestImplementation("org.wiremock.integrations:wiremock-spring-boot:3.10.0")
}

configurations.all {
    exclude(group = "org.springframework.boot", module = "spring-boot-starter-logging")
    exclude(group = "ch.qos.logback", module = "logback-classic")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

val integrationTest = tasks.register<Test>("integrationTest") {
    description = "Runs integration tests."
    group = "verification"

    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
    shouldRunAfter("test")
}

val end2endTest = tasks.register<Test>("end2endTest") {
    description = "Runs end-to-end tests."
    group = "verification"

    testClassesDirs = sourceSets["end2endTest"].output.classesDirs
    classpath = sourceSets["end2endTest"].runtimeClasspath
    shouldRunAfter("test")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()

    systemProperty("spring.profiles.active", "test")

    testLogging {
        events("passed")
    }
}

tasks.getByName<Test>("integrationTest") {
    useJUnitPlatform()

    systemProperty("spring.profiles.active", "int")

    testLogging {
        events("passed")
    }
}

tasks.getByName<Test>("end2endTest") {
    useJUnitPlatform()

    systemProperty("spring.profiles.active", "e2e")

    testLogging {
        events("passed")
    }
}

tasks.named<ProcessResources>("processEnd2endTestResources") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}

tasks.named<ProcessResources>("processIntegrationTestResources") {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.check {
    dependsOn(integrationTest)
    dependsOn(end2endTest)
}
