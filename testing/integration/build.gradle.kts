plugins {
    kotlin("jvm") version "2.1.20"
    kotlin("plugin.serialization") version "2.1.20"
}

dependencies {
    testImplementation(project(":cli"))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.strikt.jvm)
}

tasks.test {
    dependsOn(":cli:shadowJar")
    systemProperty("project.root", rootProject.projectDir.absolutePath)
}
