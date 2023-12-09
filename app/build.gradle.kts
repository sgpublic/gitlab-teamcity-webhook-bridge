import com.codingfeline.buildkonfig.compiler.FieldSpec

plugins {
    alias(gtwb.plugins.kotlin.multiplatform)
    alias(gtwb.plugins.ktor)
    alias(gtwb.plugins.buildkonfig)
}

kotlin {
    jvm {
        jvmToolchain(17)
        testRuns.named("test") {
            executionTask.configure {
                useJUnitPlatform()
            }
        }
        application {
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
            baseName = "gtwb"
            entryPoint = "io.github.sgpublic.gtwb"
        }
    }

    applyDefaultHierarchyTemplate()

    sourceSets {
        // common
        val commonMain by getting {
            dependencies {
                implementation(gtwb.kotlinx.coroutines.core)
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

// https://github.com/JetBrains/compose-multiplatform/issues/3123#issuecomment-1699296352
tasks.configureEach {
    if (name == "jvmRun" || name.contains("run(.*?)Executable".toRegex())) {
        (this as ProcessForkOptions).workingDir = project.file("bin")
    }
}

buildkonfig {
    packageName = "io.github.sgpublic.gtwb"

    defaultConfigs {
        buildConfigField(FieldSpec.Type.STRING, "VERSION_NAME", GTWB_APP)
        buildConfigField(FieldSpec.Type.STRING, "COMMIT", GIT_HEAD)
    }
}
