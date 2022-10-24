plugins {
    application
}

sourceSets {
    main {
        runtimeClasspath = files(file("../app/src/main/resources"), runtimeClasspath);
    }
}

application {
    mainClass.set("rife.TestSite")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":app"))
    runtimeOnly("org.slf4j:slf4j-simple:2.0.3")
    runtimeOnly("org.eclipse.jetty:jetty-server:11.0.12")
    runtimeOnly("org.eclipse.jetty:jetty-servlet:11.0.12")
}

tasks.test {
    enabled = false
}