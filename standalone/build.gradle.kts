plugins {
    application
}

sourceSets {
    main {
        runtimeClasspath = files(file("../app/src/main/resources"), runtimeClasspath);
    }
}

application {
    mainClass.set("rife.HelloDatabase")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":app"))
    runtimeOnly("org.slf4j:slf4j-simple:2.0.3")
    runtimeOnly("com.h2database:h2:2.1.214")
}

tasks.test {
    enabled = false
}