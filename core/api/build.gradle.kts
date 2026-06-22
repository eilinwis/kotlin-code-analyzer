plugins {
    kotlin("jvm")
}

dependencies {
    api(project(":core:domain"))
    implementation(libs.kotlin.stdlib)
}

kotlin {
    explicitApi()
}
