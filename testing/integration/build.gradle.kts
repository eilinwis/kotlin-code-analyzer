plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    testImplementation(project(":cli"))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.strikt.jvm)
}

tasks.test {
    dependsOn(":cli:shadowJar")
    systemProperty("project.root", rootProject.projectDir.absolutePath)
    systemProperty(
        "cli.jar",
        project(":cli").tasks.named<Jar>("shadowJar").get().archiveFile.get().asFile.absolutePath,
    )
}
