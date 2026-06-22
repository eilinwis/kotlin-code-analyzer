plugins {
    kotlin("jvm")
}

dependencies {
    testImplementation(project(":rules:builtin-psi"))
    testImplementation(project(":testing:testkit"))
    testImplementation(project(":core:domain"))
    testImplementation(libs.junit.jupiter)
    testImplementation(libs.strikt.jvm)
}
