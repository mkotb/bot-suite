plugins {
    // Apply the Kotlin JVM plugin to add support for Kotlin.
    id("org.jetbrains.kotlin.jvm") version "1.3.41"

    // Apply the application plugin to add support for building a CLI application.
    application
}

repositories {
    // Use jcenter for resolving dependencies.
    // You can declare any Maven/Ivy/file repository here.
    jcenter()

    maven {
        name = "GitHub Packages"
        url = uri("https://maven.pkg.github.com/jTelegram/jTelegramBotAPI")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
}

dependencies {
    // Align versions of all Kotlin components
    implementation(platform("org.jetbrains.kotlin:kotlin-bom"))

    // Use the Kotlin JDK 8 standard library.
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    // Use the Kotlin test library.
    testImplementation("org.jetbrains.kotlin:kotlin-test")

    // Use the Kotlin JUnit integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")

    // jTelegram
    implementation("com.jtelegram:jtelegrambotapi-core:4.0.3")
}

application {
    // Define the main class for the application.
    mainClassName = "com.mazenk.telegram.AppKt"
}

sourceSets.getByName("main") {
    java.srcDir("src/main/kotlin")
}

tasks.withType<Jar> {
    manifest {
        attributes["Main-Class"] = "com.mazenk.telegram.AppKt"
    }

    configurations["compileClasspath"].forEach { file: File ->
        from(zipTree(file.absoluteFile))
    }
}