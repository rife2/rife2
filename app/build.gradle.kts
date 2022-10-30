plugins {
    `java-library`
}

base {
    archivesName.set("hello")
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
    runtimeOnly("org.eclipse.jetty:jetty-server:11.0.12")
    runtimeOnly("org.eclipse.jetty:jetty-servlet:11.0.12")
    implementation(project(":lib"))
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
