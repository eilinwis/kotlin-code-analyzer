plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":core:api"))
    implementation(project(":infrastructure:analysis"))
    implementation(project(":infrastructure:psi"))
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.compiler) { isTransitive = false }
    implementation(libs.kotlin.analysis.api) { isTransitive = false }
    implementation(libs.kotlin.analysis.api.standalone) { isTransitive = false }
}
