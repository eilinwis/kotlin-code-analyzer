plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":core:api"))
    api(project(":core:domain"))
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.compiler) { isTransitive = false }
}
