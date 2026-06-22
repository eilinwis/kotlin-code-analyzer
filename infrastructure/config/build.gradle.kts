plugins {
    kotlin("jvm")
    alias(libs.plugins.kotlin.serialization)
}

dependencies {
    api(project(":core:domain"))
    implementation(libs.kotlin.stdlib)
    implementation(libs.kaml)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.strikt.jvm)
}
