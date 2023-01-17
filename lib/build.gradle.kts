import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import java.net.*
import java.net.http.*

plugins {
    idea
    java
    `java-library`
    antlr
    `maven-publish`
    signing
}

val rifeVersion by rootProject.extra { "0.9.8" }
var rifeAgentName: String = "rife2-$rifeVersion-agent"
val rifeAgentJar by rootProject.extra { "$rifeAgentName.jar" }
group = "com.uwyn.rife2"
version = rifeVersion

java {
    withJavadocJar()
    withSourcesJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenLocal()
    mavenCentral()
}

dependencies {
    antlr("org.antlr:antlr4:4.11.1")
    compileOnly("org.jsoup:jsoup:1.15.3")
    compileOnly("org.eclipse.jetty:jetty-server:11.0.13")
    compileOnly("org.eclipse.jetty:jetty-servlet:11.0.13")
    compileOnly("jakarta.servlet:jakarta.servlet-api:5.0.0")
    compileOnly("net.imagej:ij:1.53v")
    testImplementation("org.jsoup:jsoup:1.15.3")
    testImplementation("org.eclipse.jetty:jetty-server:11.0.13")
    testImplementation("org.eclipse.jetty:jetty-servlet:11.0.13")
    testImplementation("net.sourceforge.htmlunit:htmlunit:2.68.0")
    testImplementation("jakarta.servlet:jakarta.servlet-api:5.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
    testImplementation("org.postgresql:postgresql:42.5.1")
    testImplementation("mysql:mysql-connector-java:8.0.31")
    testImplementation("org.hsqldb:hsqldb:2.7.1")
    testImplementation("com.h2database:h2:2.1.214")
    testImplementation("org.apache.derby:derby:10.16.1.1")
    testImplementation("org.apache.derby:derbytools:10.16.1.1")
    testImplementation("com.oracle.database.jdbc:ojdbc11:21.8.0.0")
    testImplementation("net.imagej:ij:1.53v")
}

configurations[JavaPlugin.API_CONFIGURATION_NAME].let { apiConfiguration ->
    apiConfiguration.setExtendsFrom(apiConfiguration.extendsFrom.filter { it.name != "antlr" })
}

sourceSets.main {
    java.srcDirs("${projectDir}/src/processed/java/")
    resources.exclude("templates/**")
}

idea {
    module {
        sourceDirs.add(File("${projectDir}/src/processed/java"))
        generatedSourceDirs.add(File("${projectDir}/src/processed/java"))
    }
}

tasks {
    register<JavaExec>("precompileHtmlTemplates") {
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("rife.template.TemplateDeployer")
        args = listOf(
            "-t", "html",
            "-d", "${projectDir}/build/classes/java/main",
            "-encoding", "UTF-8", "${projectDir}/src/main/resources/templates"
        )
    }

    register<JavaExec>("precompileXmlTemplates") {
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("rife.template.TemplateDeployer")
        args = listOf(
            "-t", "xml",
            "-d", "${projectDir}/build/classes/java/main",
            "-encoding", "UTF-8", "${projectDir}/src/main/resources/templates"
        )
    }

    register<JavaExec>("precompileSqlTemplates") {
        classpath = sourceSets["main"].runtimeClasspath
        mainClass.set("rife.template.TemplateDeployer")
        args = listOf(
            "-t", "sql",
            "-d", "${projectDir}/build/classes/java/main",
            "-encoding", "UTF-8", "${projectDir}/src/main/resources/templates"
        )
    }

    register("precompileTemplates") {
        dependsOn("precompileHtmlTemplates")
        dependsOn("precompileXmlTemplates")
        dependsOn("precompileSqlTemplates")
    }

    jar {
        dependsOn("precompileTemplates")

        archiveBaseName.set("rife2")
    }

    generateGrammarSource {
        arguments = arguments + listOf(
            "-visitor",
            "-long-messages"
        )
        outputDirectory = File("${projectDir}/src/generated/java/rife/template/antlr")
    }

    register<Copy>("processGeneratedParserCode") {
        dependsOn("generateGrammarSource")
        from("${projectDir}/src/generated/java/rife/template/antlr")
        into("${projectDir}/src/processed/java/rife/template/antlr")
        filter { line -> line.replace("org.antlr.v4.runtime", "rife.antlr.v4.runtime") }
    }

    compileJava {
        dependsOn("processGeneratedParserCode")
    }

    register<Jar>("agentJar") {
        dependsOn("jar")

        archiveFileName.set("$rifeAgentJar")
        from(sourceSets.main.get().output)
        include(
            "rife/asm/**",
            "rife/instrument/**",
            "rife/continuations/ContinuationConfigInstrument**",
            "rife/continuations/instrument/**",
            "rife/database/querymanagers/generic/instrument/**",
            "rife/engine/EngineContinuationConfigInstrument**",
            "rife/tools/ClassBytesLoader*",
            "rife/tools/FileUtils*",
            "rife/tools/InstrumentationUtils*",
            "rife/tools/RawFormatter*",
            "rife/tools/exceptions/FileUtils*",
            "rife/validation/instrument/**",
            "rife/validation/MetaDataMerged**",
            "rife/validation/MetaDataBeanAware**"
        )
        manifest {
            attributes["Premain-Class"] = "rife.instrument.RifeAgent"
        }
    }

    test {
        useJUnitPlatform()
        testLogging {
            exceptionFormat = TestExceptionFormat.FULL
            events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        }
        addTestListener(object : TestListener {
            override fun beforeTest(p0: TestDescriptor?) = Unit
            override fun beforeSuite(p0: TestDescriptor?) = Unit
            override fun afterTest(desc: TestDescriptor, result: TestResult) = Unit
            override fun afterSuite(desc: TestDescriptor, result: TestResult) {
                printResults(desc, result)
            }
        })
        environment("project.dir", project.projectDir.toString())
        dependsOn("precompileTemplates")
        dependsOn("agentJar")
        jvmArgs = listOf("-javaagent:${buildDir}/libs/$rifeAgentJar")
        if (System.getProperty("test.postgres") != null) systemProperty("test.postgres", System.getProperty("test.postgres"))
        if (System.getProperty("test.mysql") != null) systemProperty("test.mysql", System.getProperty("test.mysql"))
        if (System.getProperty("test.oracle") != null) systemProperty("test.oracle", System.getProperty("test.oracle"))
        if (System.getProperty("test.derby") != null) systemProperty("test.derby", System.getProperty("test.derby"))
        if (System.getProperty("test.hsqldb") != null) systemProperty("test.hsqldb", System.getProperty("test.hsqldb"))
        if (System.getProperty("test.h2") != null) systemProperty("test.h2", System.getProperty("test.h2"))
    }

    clean {
        delete("${projectDir}/src/generated")
        delete("${projectDir}/src/processed")
    }

    javadoc {
        title = "RIFE2 ${rifeVersion}"
        options {
            this as StandardJavadocDocletOptions
            addBooleanOption("html5", true)
            addBooleanOption("Xdoclint:-missing", true)
        }
        exclude("rife/antlr/**")
        exclude("rife/asm/**")
        exclude("rife/template/antlr/**")
    }
}

val agentFile = layout.buildDirectory.file("libs/$rifeAgentJar")
val agentArtifact = artifacts.add("archives", agentFile.get().asFile) {
    type = "jar"
    classifier = "agent"
    builtBy("agentJar")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "rife2"
            artifact(agentArtifact)
            from(components["java"])
            pom {
                name.set("RIFE2")
                description.set("Full-stack, no-declaration, framework to quickly and effortlessly create web applications with modern Java.")
                url.set("https://github.com/gbevin/rife2")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("gbevin")
                        name.set("Geert Bevin")
                        email.set("gbevin@uwyn.com")
                        url.set("https://github.com/gbevin")
                    }
                }
                scm {
                    connection.set("scm:git:https://github.com/gbevin/rife2.git")
                    developerConnection.set("scm:git:git@github.com:gbevin/rife2.git")
                    url.set("https://github.com/gbevin/rife2")
                }
            }
            repositories {
                maven {
                    credentials {
                        username = project.properties["ossrhUsername"].toString()
                        password = project.properties["ossrhPassword"].toString()
                    }
                    val releasesRepoUrl = uri("https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/")
                    val snapshotsRepoUrl = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
                    url = if (version.toString().endsWith("SNAPSHOT")) snapshotsRepoUrl else releasesRepoUrl
                }
            }
        }
    }
}

signing {
    sign(publishing.publications["mavenJava"])
}

fun printResults(desc: TestDescriptor, result: TestResult) {
    if (desc.parent != null) {
        val output = result.run {
            "Results: $resultType (" +
                    "$testCount tests, " +
                    "$successfulTestCount successes, " +
                    "$failedTestCount failures, " +
                    "$skippedTestCount skipped" +
                    ")"
        }
        val testResultLine = "|  $output  |"
        val repeatLength = testResultLine.length
        val separationLine = "-".repeat(repeatLength)
        println()
        println(separationLine)
        println(testResultLine)
        println(separationLine)
    }

    if (desc.parent == null) {
        val passed = result.successfulTestCount
        val failed = result.failedTestCount
        val skipped = result.skippedTestCount

        if (project.properties["testsBadgeApiKey"] != null) {
            val apiKey = project.properties["testsBadgeApiKey"]
            val response: HttpResponse<String> = HttpClient.newHttpClient()
                .send(
                    HttpRequest.newBuilder()
                        .uri(
                            URI(
                                "https://rife2.com/tests-badge/update/com.uwyn.rife2/rife2?" +
                                        "apiKey=$apiKey&" +
                                        "passed=$passed&" +
                                        "failed=$failed&" +
                                        "skipped=$skipped"
                            )
                        )
                        .POST(HttpRequest.BodyPublishers.noBody())
                        .build(), HttpResponse.BodyHandlers.ofString()
                )
            println("RESPONSE: " + response.statusCode())
            println(response.body())
        }
    }
}