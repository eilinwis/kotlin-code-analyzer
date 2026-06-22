plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":core:api"))
    implementation(project(":infrastructure:psi"))
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.compiler) { isTransitive = false }
}
