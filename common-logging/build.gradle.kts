plugins { `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    api("org.springframework.boot:spring-boot-starter-aop:3.5.11")
    api("org.aspectj:aspectjrt:1.9.25.1")
    compileOnly("org.slf4j:slf4j-api:2.0.17")
    compileOnly("com.fasterxml.jackson.core:jackson-databind:2.21.1")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
