import com.vanniktech.maven.publish.JavadocJar
import com.vanniktech.maven.publish.KotlinJvm
import com.vanniktech.maven.publish.MavenPublishBaseExtension

plugins {
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.kover) apply false
    alias(libs.plugins.shadow) apply false
    alias(libs.plugins.dokka.javadoc) apply false
    alias(libs.plugins.maven.publish) apply false
}

allprojects {
    group = property("GROUP").toString()
    version = property("VERSION_NAME").toString()
}

// Library modules keep an explicit public API; all of them ship inside the :distribution artifact.
val libraryModules = setOf(
    ":core:domain",
    ":core:api",
    ":core:engine",
    ":infrastructure:psi",
    ":infrastructure:analysis",
    ":infrastructure:config",
    ":infrastructure:reporting",
    ":rules:builtin-psi",
    ":rules:semantic",
    ":testing:testkit",
)

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlinx.kover")

    configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
        jvmToolchain(17)
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    dependencies {
        add("testImplementation", rootProject.libs.junit.jupiter)
        add("testRuntimeOnly", "org.junit.platform:junit-platform-launcher")
    }

    if (path in libraryModules) {
        configure<org.jetbrains.kotlin.gradle.dsl.KotlinJvmProjectExtension> {
            explicitApi()
        }
    }

    tasks.withType<Jar>().configureEach {
        manifest {
            attributes("Implementation-Version" to project.version)
        }
    }

    // Only :distribution is published: one artifact with every module plus the CLI fat jar.
    if (path != ":distribution") return@subprojects
    val artifactId = property("POM_ARTIFACT_ID").toString()

    apply(plugin = "org.jetbrains.dokka-javadoc")
    apply(plugin = "com.vanniktech.maven.publish")

    configure<MavenPublishBaseExtension> {
        configure(
            KotlinJvm(
                javadocJar = JavadocJar.Dokka("dokkaGeneratePublicationJavadoc"),
                sourcesJar = true,
            ),
        )
        publishToMavenCentral(automaticRelease = false)
        // Signing is required by Maven Central; mavenLocal and GitHub Packages builds run without keys.
        // CI passes unset secrets as empty strings, so check for a non-blank key rather than presence.
        val signingKey = project.findProperty("signingInMemoryKey")?.toString()
        if (!signingKey.isNullOrBlank() || project.hasProperty("signing.keyId")) {
            signAllPublications()
        }

        pom {
            name.set(artifactId)
            description.set(
                project.findProperty("POM_DESCRIPTION")?.toString()
                    ?: "Kotlin Code Analyzer module $artifactId",
            )
            inceptionYear.set("2026")
            url.set("https://github.com/eilinwis/kotlin-code-analyzer")
            licenses {
                license {
                    name.set("The Apache License, Version 2.0")
                    url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                    distribution.set("repo")
                }
            }
            developers {
                developer {
                    id.set("eilinwis")
                    name.set("Roman Kudriavtsev")
                    url.set("https://github.com/eilinwis")
                }
            }
            scm {
                url.set("https://github.com/eilinwis/kotlin-code-analyzer")
                connection.set("scm:git:git://github.com/eilinwis/kotlin-code-analyzer.git")
                developerConnection.set("scm:git:ssh://git@github.com/eilinwis/kotlin-code-analyzer.git")
            }
        }
    }

    configure<PublishingExtension> {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/eilinwis/kotlin-code-analyzer")
                credentials(PasswordCredentials::class)
            }
        }
    }
}
