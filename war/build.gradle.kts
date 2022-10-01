plugins {
    war
}

base {
    archivesName.set("rife-site")
    version = 1.0
}

dependencies {
    implementation(project(":app"))
}

tasks.war {
    webXml = file("src/web.xml") // copies a file to WEB-INF/web.xml
}
