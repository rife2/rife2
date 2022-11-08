plugins {
    idea
    java
    `java-library`
    antlr
    `maven-publish`
    signing
}

group = "com.uwyn.rife2"
version = "0.5.5"

base {
    archivesName.set("rife2")
}

java {
    withJavadocJar()
    withSourcesJar()
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.ow2.asm:asm:9.4")
    antlr("org.antlr:antlr4:4.11.1")
    implementation("org.antlr:antlr4-runtime:4.11.1")
    compileOnly("org.jsoup:jsoup:1.15.3")
    compileOnly("org.eclipse.jetty:jetty-server:11.0.12")
    compileOnly("org.eclipse.jetty:jetty-servlet:11.0.12")
    compileOnly("jakarta.servlet:jakarta.servlet-api:5.0.0")
    testImplementation("org.jsoup:jsoup:1.15.3")
    testImplementation("org.eclipse.jetty:jetty-server:11.0.12")
    testImplementation("org.eclipse.jetty:jetty-servlet:11.0.12")
    testImplementation("net.sourceforge.htmlunit:htmlunit:2.66.0")
    testImplementation("jakarta.servlet:jakarta.servlet-api:5.0.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("org.postgresql:postgresql:42.5.0")
    testImplementation("mysql:mysql-connector-java:8.0.31")
    testImplementation("org.hsqldb:hsqldb:2.7.1")
    testImplementation("com.h2database:h2:2.1.214")
    testImplementation("org.apache.derby:derby:10.16.1.1")
    testImplementation("org.apache.derby:derbytools:10.16.1.1")
    testImplementation("com.oracle.database.jdbc:ojdbc11:21.7.0.0")
}

configurations[JavaPlugin.API_CONFIGURATION_NAME].let { apiConfiguration ->
    apiConfiguration.setExtendsFrom(apiConfiguration.extendsFrom.filter { it.name != "antlr" })
}

sourceSets.main {
    java.srcDirs("${projectDir}/src/generated/java/")
    resources.exclude("templates/**")
}

tasks.register<JavaExec>("precompileHtmlTemplates") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("rife.template.TemplateDeployer")
    args = listOf(
        "-verbose",
        "-t", "html",
        "-d", "${projectDir}/build/classes/java/main",
        "-encoding", "UTF-8", "${projectDir}/src/main/resources/templates"
    )
}

tasks.register<JavaExec>("precompileXmlTemplates") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("rife.template.TemplateDeployer")
    args = listOf(
        "-verbose",
        "-t", "xml",
        "-d", "${projectDir}/build/classes/java/main",
        "-encoding", "UTF-8", "${projectDir}/src/main/resources/templates"
    )
}

tasks.register<JavaExec>("precompileSqlTemplates") {
    classpath = sourceSets["main"].runtimeClasspath
    mainClass.set("rife.template.TemplateDeployer")
    args = listOf(
        "-verbose",
        "-t", "sql",
        "-d", "${projectDir}/build/classes/java/main",
        "-encoding", "UTF-8", "${projectDir}/src/main/resources/templates"
    )
}

tasks.register("precompileTemplates") {
    dependsOn("precompileHtmlTemplates")
    dependsOn("precompileXmlTemplates")
    dependsOn("precompileSqlTemplates")
}

tasks.jar {
    dependsOn("precompileTemplates")
}
tasks.generateGrammarSource {
    arguments = arguments + listOf(
        "-visitor",
        "-long-messages"
    )
    outputDirectory = File("${projectDir}/src/generated/java/rife/template/antlr")
}

tasks.clean {
    delete("${projectDir}/src/generated")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    environment("project.dir", project.projectDir.toString())
}

idea {
    module {
        sourceDirs.add(File("${projectDir}/src/generated/java"))
        generatedSourceDirs.add(File("${projectDir}/src/generated/java"))
    }
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            artifactId = "rife2"
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

tasks.javadoc {
    title = "RIFE 2"
    if (JavaVersion.current().isJava9Compatible) {
        (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }
}
