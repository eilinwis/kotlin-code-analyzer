plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":core:domain"))
    implementation(project(":infrastructure:psi"))
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlin.compiler) { isTransitive = false }
    implementation(libs.kotlin.analysis.api) { isTransitive = false }
    implementation(libs.kotlin.analysis.api.k2) { isTransitive = false }
    implementation(libs.kotlin.analysis.api.standalone) { isTransitive = false }
    implementation(libs.kotlin.analysis.api.impl.base) { isTransitive = false }
    implementation(libs.kotlin.analysis.api.platform) { isTransitive = false }
    implementation(libs.kotlin.low.level.api.fir) { isTransitive = false }
    implementation(libs.kotlin.symbol.light.classes) { isTransitive = false }
    implementation(libs.kotlinx.coroutines.core.intellij) { isTransitive = false }
    implementation(libs.kotlinx.serialization.json) { isTransitive = false }
    runtimeOnly(libs.caffeine)
    runtimeOnly(libs.trove4j)
}
