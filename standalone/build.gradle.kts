plugins {
    application
}

application {
    mainClass.set("rife.TestSite")
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    implementation(project(":app"))
    runtimeOnly("org.eclipse.jetty:jetty-server:11.0.12")
    runtimeOnly("org.eclipse.jetty:jetty-servlet:11.0.12")
}