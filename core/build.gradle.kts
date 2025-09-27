plugins {
    alias(libs.plugins.kotlinMultiplatform)
}

kotlin {
    jvm()

    sourceSets {
        jvmMain.dependencies {
            implementation(libs.tarsosdsp)
            implementation(libs.kotlinx.coroutinesCore)
        }
        jvmTest.dependencies {
            implementation(libs.kotlin.testJunit)
            implementation(libs.junit)
        }
    }
}
