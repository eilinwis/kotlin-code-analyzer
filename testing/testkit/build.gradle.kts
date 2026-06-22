plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":core:api"))
    api(project(":core:engine"))
    api(project(":infrastructure:psi"))
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.compiler) { isTransitive = false }
    implementation(libs.junit.jupiter.api)
    implementation(libs.strikt.jvm)
}
