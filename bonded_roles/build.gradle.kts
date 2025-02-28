plugins {
    id("bisq.java-library")
    id("bisq.protobuf")
}

repositories {
    maven {
        url = uri("https://jitpack.io")
    }
}

dependencies {
    implementation(project(":persistence"))
    implementation(project(":security"))
    implementation(project(":i18n"))
    implementation(project(":identity"))

    implementation("network:common")
    implementation("network:network-identity")
    implementation("network:network")

    implementation(libs.google.gson)
    implementation(libs.google.guava)
    implementation(libs.typesafe.config)
    implementation(libs.bundles.jackson)
}
