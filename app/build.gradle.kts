plugins {
    `java-library`
}

base {
    archivesName.set("rife2-site")
    version = 1.0
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    implementation(project(":lib"))
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
