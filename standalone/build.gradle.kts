plugins {
    application
}

sourceSets {
    main {
        runtimeClasspath = files(file("../app/src/main/resources"), runtimeClasspath);
    }
}

application {
    mainClass.set("rife.HelloWorld")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":app"))
    runtimeOnly("org.slf4j:slf4j-simple:2.0.3")
}

tasks.test {
    enabled = false
}