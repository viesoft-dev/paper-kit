import org.gradle.api.publish.maven.MavenPublication
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.repositories

plugins {
    `maven-publish`
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

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
            }
        }
    }
}
