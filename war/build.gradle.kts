plugins {
    war
}

base {
    archivesName.set("rife2-site")
    version = 1.0
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":app"))
}

tasks.war {
    webAppDirectory.set(file("../app/src/main/webapp"))
    webXml = file("src/web.xml") // copies a file to WEB-INF/web.xml
}

tasks.test {
    enabled = false
}