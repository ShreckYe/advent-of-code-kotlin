plugins {
    kotlin("jvm") version "2.1.0"
}

sourceSets {
    main {
        kotlin.srcDir("src")
    }
}

tasks {
    wrapper {
        gradleVersion = "8.11.1"
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.1") // for `Flow`
    implementation("io.arrow-kt:arrow-core:2.0.0") // for `Option`
}
