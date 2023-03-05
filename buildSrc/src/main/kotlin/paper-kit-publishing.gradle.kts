import ProjectHelper.isJitPack
import ProjectHelper.isRelease
import ProjectHelper.isSnapshot
import java.util.*

plugins {
    `maven-publish`
    signing
}

publishing {
    publications {
        create<MavenPublication>("paper-kit") {
            groupId = project.group.toString()
            artifactId = project.name
            version = ProjectHelper.version

            pom {
                name.set("PaperKit ${project.name.replaceFirstChar { it.uppercase() }}")
                description.set(project.description)
                url.set("https://github.com/viesoft-dev/paper-kit")

                organization {
                    name.set("Viesoft")
                    url.set("https://github.com/viesoft-dev")
                }

                developers {
                    developer { name.set("The Viesoft Team") }
                }

                scm {
                    connection.set("scm:git:git@github.com:viesoft-dev/paper-kit.git")
                    developerConnection.set("scm:git:ssh://github.com:viesoft-dev/paper-kit.git")
                    url.set("https://github.com/viesoft-dev/paper-kit")
                }

                issueManagement {
                    system.set("GitHub")
                    url.set("https://github.com/viesoft-dev/paper-kit/issues")
                }

                licenses {
                    license {
                        name.set("GPL-3.0")
                        url.set("https://opensource.org/license/gpl-3-0/")
                    }
                }

                if (!isJitPack) {
                    repositories {
                        maven {
                            url = uri(
                                if (isSnapshot) {
                                    SonatypeRepository.snapshotsUrl
                                } else {
                                    SonatypeRepository.releasesUrl
                                }
                            )
                            credentials {
                                username = System.getenv("SONATYPE_USERNAME")
                                password = System.getenv("SONATYPE_PASSWORD")
                            }
                        }
                    }
                }
            }
        }
    }
}

if (!isJitPack && isRelease) {
    signing {
        val signingKey = findProperty("signingKey")?.toString()
        val signingPassword = findProperty("signingPassword")?.toString()
        if (signingKey != null && signingPassword != null) {
            useInMemoryPgpKeys(String(Base64.getDecoder().decode(signingKey)), signingPassword)
        }
        sign(publishing.publications["paper-kit"])
    }
}
