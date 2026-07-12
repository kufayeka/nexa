plugins {
    application
    id("com.gradleup.shadow") version "9.2.0"
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation(libs.jackson.databind)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

application {
    mainClass.set("nexa.framework.App")
    applicationDefaultJvmArgs = listOf("-Xms8m", "-Xmx128m")
}

tasks.jar {
    manifest {
        attributes(
            "Main-Class" to application.mainClass.get()
        )
    }
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    minHeapSize = "8m"
    maxHeapSize = "128m"
    systemProperties(System.getProperties().map { it.key.toString() to it.value }.toMap())
}
