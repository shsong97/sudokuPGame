plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.serialization)
    application
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":engine"))
    implementation(libs.kotlinx.serialization.json)
}

application {
    mainClass.set("com.sudokupgame.generator.MainKt")
}

tasks.named<JavaExec>("run") {
    workingDir = rootDir
}

dependencies {
    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)
}

tasks.test {
    useJUnitPlatform()
    systemProperty("puzzlesJson", rootProject.file("app/src/main/assets/puzzles.json").path)
    inputs.file(rootProject.file("app/src/main/assets/puzzles.json"))
}
