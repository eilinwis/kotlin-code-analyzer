plugins {
    kotlin("jvm")
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
