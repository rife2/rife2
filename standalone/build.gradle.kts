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
    runtimeOnly("org.slf4j:slf4j-simple:2.0.5")
    runtimeOnly("com.h2database:h2:2.1.214")
}

val rifeAgentJar: String by rootProject.extra

tasks {
    register<JavaExec>("run") {
        dependsOn("runHelloAll")
    }

    register<JavaExec>("runHelloAll") {
        dependsOn(":lib:agentJar")
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("rife.HelloAll")
        jvmArgs = listOf("-javaagent:${project(":lib").buildDir}/libs/$rifeAgentJar")
        project.logger.lifecycle("")
        project.logger.lifecycle("================================================================================")
        project.logger.lifecycle(" All the examples are included in this site. These are their URLs:")
        project.logger.lifecycle("")
        project.logger.lifecycle(" HelloAuthentication:")
        project.logger.lifecycle("   Open your browser at http://localhost:8080/authentication")
        project.logger.lifecycle("   The authentication credentials are: testUser / testPassword")
        project.logger.lifecycle("")
        project.logger.lifecycle(" HelloContentManagement:")
        project.logger.lifecycle("   Install the database at http://localhost:8080/cmf/install")
        project.logger.lifecycle("   Then add a news item by going to http://localhost:8080/cmf/add")
        project.logger.lifecycle("   Delete the database through http://localhost:8080/cmf/remove")
        project.logger.lifecycle("")
        project.logger.lifecycle(" HelloContinuations:")
        project.logger.lifecycle("   Open your browser at http://localhost:8080/guess")
        project.logger.lifecycle("")
        project.logger.lifecycle(" HelloCounterContinuations:")
        project.logger.lifecycle("   Open your browser at http://localhost:8080/count")
        project.logger.lifecycle("")
        project.logger.lifecycle(" HelloDatabase:")
        project.logger.lifecycle("   Install the database at http://localhost:8080/install")
        project.logger.lifecycle("   Then add names by going to http://localhost:8080/add")
        project.logger.lifecycle("   When you're done, delete the database through http://localhost:8080/remove")
        project.logger.lifecycle("")
        project.logger.lifecycle(" HelloErrors:")
        project.logger.lifecycle("   Trigger and exception at http://localhost:8080/error")
        project.logger.lifecycle("   Try any other URL that doesn't exit, ie. http://localhost:8080/treasure")
        project.logger.lifecycle("")
        project.logger.lifecycle(" HelloForm:")
        project.logger.lifecycle("   Open your browser at http://localhost:8080/form")
        project.logger.lifecycle("")
        project.logger.lifecycle(" runHelloFormGeneration:")
        project.logger.lifecycle("   Open your browser at http://localhost:8080/generation/form")
        project.logger.lifecycle("")
        project.logger.lifecycle(" HelloFormContinuations:")
        project.logger.lifecycle("   Open your browser at http://localhost:8080/continuation/form")
        project.logger.lifecycle("")
        project.logger.lifecycle(" HelloGenericQueryManager:")
        project.logger.lifecycle("   Install the database at http://localhost:8080/generic/install")
        project.logger.lifecycle("   Then add names by going to http://localhost:8080/generic/add")
        project.logger.lifecycle("   Delete the database through http://localhost:8080/generic/remove")
        project.logger.lifecycle("")
        project.logger.lifecycle(" HelloGroup:")
        project.logger.lifecycle("   Open your browser at http://localhost:8080/welcome")
        project.logger.lifecycle("   Also at http://localhost:8080/group/hello")
        project.logger.lifecycle("   And at http://localhost:8080/group/bonjour")
        project.logger.lifecycle("")
        project.logger.lifecycle(" HelloLink:")
        project.logger.lifecycle("   Open your browser at http://localhost:8080/link")
        project.logger.lifecycle("")
        project.logger.lifecycle(" HelloPathInfoMapping:")
        project.logger.lifecycle("   Open your browser at http://localhost:8080/mapping/John")
        project.logger.lifecycle("   Or http://localhost:8080/mapping/John/Smith")
        project.logger.lifecycle("   Or another other matching path info")
        project.logger.lifecycle("")
        project.logger.lifecycle(" HelloScheduler:")
        project.logger.lifecycle("   Install database at http://localhost:8080/scheduler/install")
        project.logger.lifecycle("   Add tasks by going to http://localhost:8080/scheduler/add")
        project.logger.lifecycle("   Start scheduler by going to http://localhost:8080/scheduler/start")
        project.logger.lifecycle("   View status by going to http://localhost:8080/scheduler/status")
        project.logger.lifecycle("   Stop scheduler by going to http://localhost:8080/scheduler/stop")
        project.logger.lifecycle("   Delete the database through http://localhost:8080/scheduler/remove")
        project.logger.lifecycle("")
        project.logger.lifecycle(" HelloSvg:")
        project.logger.lifecycle("   Open your browser at http://localhost:8080/clock")
        project.logger.lifecycle("")
        project.logger.lifecycle(" HelloTemplate:")
        project.logger.lifecycle("   Open your browser at http://localhost:8080/template")
        project.logger.lifecycle("")
        project.logger.lifecycle(" HelloValidation:")
        project.logger.lifecycle("   Install the database at http://localhost:8080/validation/install")
        project.logger.lifecycle("   Then add names by going to http://localhost:8080/validation/add")
        project.logger.lifecycle("   Delete the database through http://localhost:8080/validation/remove")
        project.logger.lifecycle("")
        project.logger.lifecycle(" HelloWorld:")
        project.logger.lifecycle("   Open your browser at http://localhost:8080/hello")
        project.logger.lifecycle("================================================================================")
        project.logger.lifecycle("")
    }

    register<JavaExec>("runHelloAuthentication") {
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("rife.HelloAuthentication")
        project.logger.lifecycle("")
        project.logger.lifecycle("================================================================================")
        project.logger.lifecycle(" To try this example, open your browser at http://localhost:8080/authentication")
        project.logger.lifecycle(" The authentication credentials are: testUser / testPassword")
        project.logger.lifecycle("================================================================================")
        project.logger.lifecycle("")
    }

    register<JavaExec>("runHelloContentManagement") {
        dependsOn(":lib:agentJar")
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("rife.HelloContentManagement")
        jvmArgs = listOf("-javaagent:${project(":lib").buildDir}/libs/$rifeAgentJar")
        project.logger.lifecycle("")
        project.logger.lifecycle("================================================================================")
        project.logger.lifecycle("   Install the database at http://localhost:8080/install")
        project.logger.lifecycle("   Then add a news item by going to http://localhost:8080/add")
        project.logger.lifecycle("   Delete the database through http://localhost:8080/remove")
        project.logger.lifecycle("================================================================================")
        project.logger.lifecycle("")
    }

    register<JavaExec>("runHelloContinuations") {
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

    register<JavaExec>("runHelloCounterContinuations") {
        dependsOn(":lib:agentJar")
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("rife.HelloCounterContinuations")
        jvmArgs = listOf("-javaagent:${project(":lib").buildDir}/libs/$rifeAgentJar")
        project.logger.lifecycle("")
        project.logger.lifecycle("================================================================================")
        project.logger.lifecycle(" To try this example, open your browser at http://localhost:8080/count")
        project.logger.lifecycle("================================================================================")
        project.logger.lifecycle("")
    }

    register<JavaExec>("runHelloDatabase") {
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

    register<JavaExec>("runHelloErrors") {
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("rife.HelloErrors")
        project.logger.lifecycle("")
        project.logger.lifecycle("================================================================================")
        project.logger.lifecycle(" To try this example, open your browser at http://localhost:8080/error")
        project.logger.lifecycle(" and also any other URL, for instance http://localhost:8080/treasure")
        project.logger.lifecycle("================================================================================")
        project.logger.lifecycle("")
    }

    register<JavaExec>("runHelloForm") {
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("rife.HelloForm")
        project.logger.lifecycle("")
        project.logger.lifecycle("================================================================================")
        project.logger.lifecycle(" To try this example, open your browser at http://localhost:8080/form")
        project.logger.lifecycle("================================================================================")
        project.logger.lifecycle("")
    }

    register<JavaExec>("runHelloFormContinuations") {
        dependsOn(":lib:agentJar")
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("rife.HelloFormContinuations")
        jvmArgs = listOf("-javaagent:${project(":lib").buildDir}/libs/$rifeAgentJar")
        project.logger.lifecycle("")
        project.logger.lifecycle("================================================================================")
        project.logger.lifecycle(" To try this example, open your browser at http://localhost:8080/form")
        project.logger.lifecycle("================================================================================")
        project.logger.lifecycle("")
    }

    register<JavaExec>("runHelloFormGeneration") {
        dependsOn(":lib:agentJar")
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("rife.HelloFormGeneration")
        jvmArgs = listOf("-javaagent:${project(":lib").buildDir}/libs/$rifeAgentJar")
        project.logger.lifecycle("")
        project.logger.lifecycle("================================================================================")
        project.logger.lifecycle(" To try this example, open your browser at http://localhost:8080/form")
        project.logger.lifecycle("================================================================================")
        project.logger.lifecycle("")
    }

    register<JavaExec>("runHelloGenericQueryManager") {
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("rife.HelloGenericQueryManager")
        project.logger.lifecycle("")
        project.logger.lifecycle("================================================================================")
        project.logger.lifecycle(" To try this example, open your browser at http://localhost:8080/install")
        project.logger.lifecycle(" Then add names by going to http://localhost:8080/add")
        project.logger.lifecycle(" When you're done, delete the database through http://localhost:8080/remove")
        project.logger.lifecycle("================================================================================")
        project.logger.lifecycle("")
    }

    register<JavaExec>("runHelloGroup") {
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("rife.HelloGroup")
        project.logger.lifecycle("")
        project.logger.lifecycle("================================================================================")
        project.logger.lifecycle(" To try this example, open your browser at http://localhost:8080/welcome")
        project.logger.lifecycle(" or http://localhost:8080/group/hello, or http://localhost:8080/group/bonjour")
        project.logger.lifecycle("================================================================================")
        project.logger.lifecycle("")
    }

    register<JavaExec>("runHelloLink") {
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("rife.HelloLink")
        project.logger.lifecycle("")
        project.logger.lifecycle("================================================================================")
        project.logger.lifecycle(" To try this example, open your browser at http://localhost:8080/link")
        project.logger.lifecycle("================================================================================")
        project.logger.lifecycle("")
    }

    register<JavaExec>("runHelloPathInfoMapping") {
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("rife.HelloPathInfoMapping")
        project.logger.lifecycle("")
        project.logger.lifecycle("================================================================================")
        project.logger.lifecycle(" To try this example, open your browser at http://localhost:8080/mapping/John")
        project.logger.lifecycle(" or http://localhost:8080/mapping/John/Smith, or another other matching path info")
        project.logger.lifecycle("================================================================================")
        project.logger.lifecycle("")
    }

    register<JavaExec>("runHelloScheduler") {
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("rife.HelloScheduler")
        project.logger.lifecycle("")
        project.logger.lifecycle("================================================================================")
        project.logger.lifecycle(" To try this example, open your browser at http://localhost:8080/install")
        project.logger.lifecycle(" Then add tasks by going to http://localhost:8080/add")
        project.logger.lifecycle(" Start the scheduler by going to http://localhost:8080/start")
        project.logger.lifecycle(" View the status by going to http://localhost:8080/status")
        project.logger.lifecycle(" Stop the scheduler by going to http://localhost:8080/stop")
        project.logger.lifecycle(" When you're done, delete the database through http://localhost:8080/remove")
        project.logger.lifecycle("================================================================================")
        project.logger.lifecycle("")
    }

    register<JavaExec>("runHelloSvg") {
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("rife.HelloSvg")
        project.logger.lifecycle("")
        project.logger.lifecycle("================================================================================")
        project.logger.lifecycle(" To try this example, open your browser at http://localhost:8080/clock")
        project.logger.lifecycle("================================================================================")
        project.logger.lifecycle("")
    }

    register<JavaExec>("runHelloTemplate") {
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("rife.HelloTemplate")
        project.logger.lifecycle("")
        project.logger.lifecycle("================================================================================")
        project.logger.lifecycle(" To try this example, open your browser at http://localhost:8080/template")
        project.logger.lifecycle("================================================================================")
        project.logger.lifecycle("")
    }

    register<JavaExec>("runHelloValidation") {
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("rife.HelloValidation")
        project.logger.lifecycle("")
        project.logger.lifecycle("================================================================================")
        project.logger.lifecycle(" To try this example, open your browser at http://localhost:8080/install")
        project.logger.lifecycle(" Then add names by going to http://localhost:8080/add")
        project.logger.lifecycle(" When you're done, delete the database through http://localhost:8080/remove")
        project.logger.lifecycle("================================================================================")
        project.logger.lifecycle("")
    }

    register<JavaExec>("runHelloWorld") {
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("rife.HelloWorld")
        project.logger.lifecycle("")
        project.logger.lifecycle("================================================================================")
        project.logger.lifecycle(" To try this example, open your browser at http://localhost:8080/hello")
        project.logger.lifecycle("================================================================================")
        project.logger.lifecycle("")
    }

    test {
        enabled = false
    }
}