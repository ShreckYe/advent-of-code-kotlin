plugins {
    val kotlinVersion = "2.1.0"
    kotlin("jvm") version kotlinVersion
    id("org.jetbrains.compose") version "1.7.1"
    id("org.jetbrains.kotlin.plugin.compose") version kotlinVersion
}

//group = "adventofcode"
//version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    //implementation(rootProject)
}

compose.desktop {
    application {
        mainClass = "MainKt"

        /*nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "compose-day14"
            packageVersion = "1.0.0"
        }*/
    }
}
