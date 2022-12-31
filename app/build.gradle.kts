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
        runtimeClasspath = files(file("${projectDir}/src/main/resources"), runtimeClasspath);
    }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
    testImplementation("org.jsoup:jsoup:1.15.3")
    runtimeOnly("org.eclipse.jetty:jetty-server:11.0.13")
    runtimeOnly("org.eclipse.jetty:jetty-servlet:11.0.13")
    implementation(project(":lib"))
    runtimeOnly("com.h2database:h2:2.1.214")
}

val rifeAgentJar: String by rootProject.extra
tasks.test {
    dependsOn(":lib:agentJar")

    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
    environment("project.dir", project.projectDir.toString())
    jvmArgs = listOf("-javaagent:${project(":lib").buildDir}/libs/$rifeAgentJar")
}
