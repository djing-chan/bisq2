plugins {
    id("bisq.java-library")
    id("bisq.protobuf")
}

repositories {
    mavenCentral()
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    implementation(project(":i18n"))
    implementation(project(":persistence"))
    implementation(project(":security"))
    implementation(project(":identity"))
    implementation(project(":bonded_roles"))

    implementation("network:network")
    implementation("network:network-identity")

    implementation(libs.google.gson)
    implementation(libs.google.guava)
    implementation(libs.typesafe.config)
}
