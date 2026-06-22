plugins {
    kotlin("jvm")
    alias(libs.plugins.shadow)
}

dependencies {
    implementation(project(":core:engine"))
    implementation(project(":core:domain"))
    implementation(project(":infrastructure:psi"))
    implementation(project(":infrastructure:analysis"))
    implementation(project(":infrastructure:config"))
    implementation(project(":infrastructure:reporting"))
    implementation(project(":rules:builtin-psi"))
    implementation(project(":rules:semantic"))
    implementation(libs.kotlin.stdlib)
    implementation(libs.clikt)
    implementation(libs.slf4j.api)
    implementation(libs.logback.classic)
}

tasks.shadowJar {
    archiveClassifier.set("all")
    manifest {
        attributes["Main-Class"] = "dev.kotlin.analyzer.cli.AnalyzerCliKt"
    }
}

tasks.build {
    dependsOn(tasks.shadowJar)
}
