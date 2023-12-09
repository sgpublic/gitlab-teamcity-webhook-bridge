pluginManagement {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        google()
        mavenLocal()
    }
}

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        google()
        mavenLocal()
    }
    versionCatalogs {
        val gtwb by creating {
            from(files(File(rootDir, "./gradle/gtwb.versions.toml")))
        }
    }
}

rootProject.name = "gitlab-teamcity-webhook-bridge"

include(":app")
