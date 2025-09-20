plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            // No external deps yet
        }
        jvmMain.dependencies {
            implementation(libs.tarsosdsp)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmTest.dependencies {
            implementation(libs.kotlin.testJunit)
            implementation(libs.junit)
        }
    }
}

