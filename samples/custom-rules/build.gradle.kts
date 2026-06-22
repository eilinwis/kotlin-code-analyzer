dependencies {
    compileOnly(project(":core:api"))
    compileOnly(project(":core:domain"))
    compileOnly(project(":infrastructure:psi"))
    compileOnly(libs.kotlin.compiler) { isTransitive = false }
}
