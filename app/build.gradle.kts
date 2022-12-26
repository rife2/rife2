plugins {
    `java-library`
}

base {
    archivesName.set("rife2-examples")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
}

sourceSets {
    main {
        runtimeClasspath = files(file("src/main/resources"), runtimeClasspath);
    }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("org.jsoup:jsoup:1.15.3")
    runtimeOnly("org.eclipse.jetty:jetty-server:11.0.12")
    runtimeOnly("org.eclipse.jetty:jetty-servlet:11.0.12")
    implementation(project(":lib"))
    runtimeOnly("com.h2database:h2:2.1.214")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}
