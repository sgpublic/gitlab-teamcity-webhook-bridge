import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.bmuschko.gradle.docker.tasks.image.DockerPushImage
import com.bmuschko.gradle.docker.tasks.image.Dockerfile
import com.codingfeline.buildkonfig.compiler.FieldSpec
import org.gradle.internal.os.OperatingSystem

plugins {
    application
    alias(gtwb.plugins.kotlin.multiplatform)
    alias(gtwb.plugins.docker.remote.api)
    alias(gtwb.plugins.buildkonfig)
    alias(gtwb.plugins.kotlin.plugin.serialization)
    alias(gtwb.plugins.ksp)
    alias(gtwb.plugins.ktorfit)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvm {
        testRuns.named("test") {
            executionTask.configure {
                useJUnitPlatform()
            }
        }
        mainRun {
            mainClass = "io.github.sgpublic.gtwb.ApplicationKt"
        }
    }
    listOf(
        macosArm64(),
        macosX64(),
        linuxArm64(),
        linuxX64(),
        // TODO: ktor-server
//        mingwX64(),
    ).forEach {
        it.binaries.executable {
            baseName = "gtwb-$GTWB_APP"
            entryPoint = "io.github.sgpublic.gtwb.main"
        }
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        // common
        val commonMain by getting {
            dependencies {
                implementation(gtwb.kotlin.stdlib)
                implementation(gtwb.kotlin.test)

                implementation(gtwb.kotlinx.coroutines.core)
                implementation(gtwb.ktorfit.lib.light)

                implementation(gtwb.ktor.server.core)
                implementation(gtwb.ktor.server.cio)
                implementation(gtwb.ktor.client.core)
                implementation(gtwb.ktor.client.cio)
                implementation(gtwb.ktor.client.auth)
                implementation(gtwb.ktor.plugin.logging)

                implementation(gtwb.xmlutil.serialization)
                implementation(gtwb.kotlinx.serialization.core)
                implementation(gtwb.kotlinx.serialization.json)
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(gtwb.ktor.server.tests)
            }
        }

        val jvmMain by getting {
            dependencies {
                implementation(gtwb.logback.classic)
            }
        }

        // windows
//        val mingwMain by getting {
//            dependencies {
//
//            }
//        }
    }
}

dependencies {
    with(gtwb.ktorfit.ksp) {
        add("kspCommonMainMetadata", this)
        add("kspJvm", this)
        add("kspLinuxArm64", this)
        add("kspLinuxX64", this)
        add("kspMacosArm64", this)
        add("kspMacosX64", this)
//        add("kspMingwX64", this)
    }
}

tasks {
    val currentSystem = OperatingSystem.current()
    if (currentSystem.isLinux) {
        val tag = "mhmzx/gitlab-teamcity-webhook-bridge"
        fun Dockerfile.runCommand(vararg commands: String) {
            runCommand(commands.joinToString(" &&\\\n "))
        }
        // build jvm image
        val assembleDist by getting
        val installDist by getting
        val dockerCreateJvmDockerfile by creating(Dockerfile::class) {
            group = "docker"
            destFile = project.file("./build/docker-jvm/Dockerfile")
            from("openjdk:17-slim-bullseye")
            workingDir("/app")
            copy {
                copyFile("./app", "/app")
            }
            runCommand(
                "apt-get update",
                "apt-get install findutils -y",
            )
            entryPoint("/app/bin/app")

            doFirst {
                delete(project.file("./build/docker-jvm"))
                mkdir(project.file("./build/docker-jvm"))
            }
        }

        val dockerBuildJvmLinuxX64Image by creating(DockerBuildImage::class) {
            group = "docker"
            dependsOn(dockerCreateJvmDockerfile, assembleDist, installDist)
            inputDir = project.file("./build/docker-jvm")
            platform = "linux/amd64"
            dockerFile = dockerCreateJvmDockerfile.destFile
            images.add("$tag:$GTWB_APP-jvm")
            images.add("$tag:latest-jvm")
            noCache = true

            doFirst {
                copy {
                    from(project.file("./build/install"))
                    into(project.file("./build/docker-jvm/"))
                }
            }
        }
//        val dockerBuildJvmLinuxArm64Image by creating(DockerBuildImage::class) {
//            group = "docker"
//            dependsOn(assembleDist, installDist, dockerCreateJvmDockerfile)
//            inputDir = project.file("./build")
//            platform = "linux/arm64/v8"
//            dockerFile = dockerCreateJvmDockerfile.destFile
//            images.add("$tag:$GTWB_APP-jvm")
//            images.add("$tag:latest-jvm")
//            noCache = true
//        }
        val dockerBuildJvmImage by creating {
            dependsOn(
                dockerBuildJvmLinuxX64Image,
//                dockerBuildJvmLinuxArm64Image,
            )
        }

        val dockerPushJvmImageOfficial by creating(DockerPushImage::class) {
            group = "docker"
            dependsOn(dockerBuildJvmImage)
            images.add("$tag:$GTWB_APP-jvm")
            images.add("$tag:latest-jvm")
        }

        // build native image
        val linuxX64MainBinaries by getting
        val dockerCreateNativeLinuxX64Dockerfile by creating(Dockerfile::class) {
            group = "docker"
            destFile = project.file("./build/docker-linuxX64/Dockerfile")
            from("debian:stable-slim")
            workingDir("/app")
            copy {
                copyFile("./*.kexe", "/app/gtwb.kexe")
            }
            runCommand(
                "apt-get update",
                "apt-get install findutils -y",
            )
            entryPoint("/app/gtwb.kexe")

            doFirst {
                delete(project.file("./build/docker-linuxX64"))
                mkdir(project.file("./build/docker-linuxX64"))
            }
        }
        val dockerBuildNativeLinuxX64Image by creating(DockerBuildImage::class) {
            group = "docker"
            dependsOn(dockerCreateNativeLinuxX64Dockerfile, linuxX64MainBinaries)
            inputDir = project.file("./build/docker-linuxX64")
            platform = "linux/amd64"
            dockerFile = dockerCreateNativeLinuxX64Dockerfile.destFile
            images.add("$tag:$GTWB_APP-native")
            images.add("$tag:latest-native")
            noCache = true

            doFirst {
                copy {
                    from(project.file("./build/bin/linuxX64/releaseExecutable"))
                    into(project.file("./build/docker-linuxX64/"))
                }
            }
        }

//        val linuxArm64MainBinaries by getting
//        val dockerCreateNativeLinuxArm64Dockerfile by creating(Dockerfile::class) {
//            group = "docker"
//            destFile = project.file("./build/docker/Dockerfile.linuxArm64")
//            from("debian:stable-slim")
//            workingDir("/app")
//            copy {
//                copyFile("./bin/linuxArm64/releaseExecutable/*.kexe", "/app/gtwb.kexe")
//            }
//            runCommand(
//                "apt-get update",
//                "apt-get install findutils -y",
//            )
//            entryPoint("/app/gtwb.kexe")
//        }
//        val dockerBuildNativeLinuxArm64Image by creating(DockerBuildImage::class) {
//            group = "docker"
//            dependsOn(linuxArm64MainBinaries, dockerCreateNativeLinuxArm64Dockerfile)
//            inputDir = project.file("./build")
//            platform = "linux/arm64/v8"
//            dockerFile = dockerCreateNativeLinuxArm64Dockerfile.destFile
//            images.add("$tag:$GTWB_APP-native")
//            images.add("$tag:latest-native")
//            noCache = true
//        }
        val dockerBuildNativeImage by creating {
            dependsOn(
                dockerBuildNativeLinuxX64Image,
//                dockerBuildNativeLinuxArm64Image,
            )
        }
        val dockerPushNativeImageOfficial by creating(DockerPushImage::class) {
            group = "docker"
            dependsOn(dockerBuildNativeImage)
            images.add("$tag:$GTWB_APP-native")
            images.add("$tag:latest-native")
        }

        // push all
        val dockerBuildImage by creating {
            group = "docker"
            dependsOn(dockerBuildJvmImage, dockerBuildNativeImage)
        }
        val dockerPushImageOfficial by creating {
            group = "docker"
            dependsOn(dockerPushJvmImageOfficial, dockerPushNativeImageOfficial)
        }
    }
}


buildkonfig {
    packageName = "io.github.sgpublic.gtwb"

    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "VERSION_NAME", GTWB_APP)
        buildConfigField(FieldSpec.Type.STRING, "COMMIT", GIT_HEAD)
    }
}
