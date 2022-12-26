plugins {
    java
}

sourceSets {
    main {
        runtimeClasspath = files(file("../app/src/main/resources"), runtimeClasspath);
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":app"))
    runtimeOnly("org.slf4j:slf4j-simple:2.0.3")
    runtimeOnly("com.h2database:h2:2.1.214")
}

tasks.register<JavaExec>("runHelloAuthentication") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("rife.HelloAuthentication")
    project.logger.lifecycle("")
    project.logger.lifecycle("================================================================================")
    project.logger.lifecycle(" To try this example, open your browser at http://localhost:8080/hello")
    project.logger.lifecycle(" The authentication credentials are: testUser / testPassword")
    project.logger.lifecycle("================================================================================")
    project.logger.lifecycle("")
}

val rifeAgentJar: String by rootProject.extra
tasks.register<JavaExec>("runHelloContinuations") {
    dependsOn(":lib:agentJar")
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("rife.HelloContinuations")
    jvmArgs = listOf("-javaagent:${project(":lib").buildDir}/libs/$rifeAgentJar")
    project.logger.lifecycle("")
    project.logger.lifecycle("================================================================================")
    project.logger.lifecycle(" To try this example, open your browser at http://localhost:8080/guess")
    project.logger.lifecycle("================================================================================")
    project.logger.lifecycle("")
}

tasks.register<JavaExec>("runHelloDatabase") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("rife.HelloDatabase")
    project.logger.lifecycle("")
    project.logger.lifecycle("================================================================================")
    project.logger.lifecycle(" To try this example, open your browser at http://localhost:8080/install")
    project.logger.lifecycle(" Then add names by going to http://localhost:8080/add")
    project.logger.lifecycle(" When you're done, delete the database through http://localhost:8080/remove")
    project.logger.lifecycle("================================================================================")
    project.logger.lifecycle("")
}

tasks.register<JavaExec>("runHelloErrors") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("rife.HelloErrors")
    project.logger.lifecycle("")
    project.logger.lifecycle("================================================================================")
    project.logger.lifecycle(" To try this example, open your browser at http://localhost:8080/error")
    project.logger.lifecycle(" and also any other URL, for instance http://localhost:8080/treasure")
    project.logger.lifecycle("================================================================================")
    project.logger.lifecycle("")
}

tasks.register<JavaExec>("runHelloForm") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("rife.HelloForm")
    project.logger.lifecycle("")
    project.logger.lifecycle("================================================================================")
    project.logger.lifecycle(" To try this example, open your browser at http://localhost:8080/hello")
    project.logger.lifecycle("================================================================================")
    project.logger.lifecycle("")
}

tasks.register<JavaExec>("runHelloGroup") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("rife.HelloGroup")
    project.logger.lifecycle("")
    project.logger.lifecycle("================================================================================")
    project.logger.lifecycle(" To try this example, open your browser at http://localhost:8080/hello")
    project.logger.lifecycle(" or http://localhost:8080/group/hello, or http://localhost:8080/group/bonjour")
    project.logger.lifecycle("================================================================================")
    project.logger.lifecycle("")
}

tasks.register<JavaExec>("runHelloLink") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("rife.HelloLink")
    project.logger.lifecycle("")
    project.logger.lifecycle("================================================================================")
    project.logger.lifecycle(" To try this example, open your browser at http://localhost:8080/link")
    project.logger.lifecycle("================================================================================")
    project.logger.lifecycle("")
}

tasks.register<JavaExec>("runHelloPathInfoMapping") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("rife.HelloPathInfoMapping")
    project.logger.lifecycle("")
    project.logger.lifecycle("================================================================================")
    project.logger.lifecycle(" To try this example, open your browser at http://localhost:8080/hello/John")
    project.logger.lifecycle(" or http://localhost:8080/hello/John/Smith, or another other matching path info")
    project.logger.lifecycle("================================================================================")
    project.logger.lifecycle("")
}

tasks.register<JavaExec>("runHelloTemplate") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("rife.HelloTemplate")
    project.logger.lifecycle("")
    project.logger.lifecycle("================================================================================")
    project.logger.lifecycle(" To try this example, open your browser at http://localhost:8080/link")
    project.logger.lifecycle("================================================================================")
    project.logger.lifecycle("")
}

tasks.register<JavaExec>("runHelloWorld") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("rife.HelloWorld")
    project.logger.lifecycle("")
    project.logger.lifecycle("================================================================================")
    project.logger.lifecycle(" To try this example, open your browser at http://localhost:8080/hello")
    project.logger.lifecycle("================================================================================")
    project.logger.lifecycle("")
}

tasks.test {
    enabled = false
}