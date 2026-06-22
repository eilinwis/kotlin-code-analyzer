plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":core:api"))
    api(project(":core:domain"))
    implementation(project(":infrastructure:psi"))
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.core)
    compileOnly(libs.kotlin.compiler) { isTransitive = false }
    testImplementation(libs.strikt.jvm)
}
