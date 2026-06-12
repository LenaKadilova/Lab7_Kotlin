plugins {
    kotlin("jvm")
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":common"))
    implementation(kotlin("stdlib"))
    implementation("ch.qos.logback:logback-classic:1.4.11")
    implementation("org.slf4j:slf4j-api:2.0.7")
}

application {
    mainClass.set("gateway.MainKt")
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "gateway.MainKt"
    }
    from(configurations.runtimeClasspath.get().map { if (it.isDirectory) it else zipTree(it) })
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
}