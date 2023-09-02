plugins {
    buildsrc.convention.`kotlin-jvm`
    buildsrc.convention.`publish-jvm`
}

val cordaApiVersion by System.getProperties()
val junitVersion by System.getProperties()

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-test-junit5")
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    implementation(platform("net.corda:corda-api:$cordaApiVersion"))
    implementation("net.corda:corda-base")
    implementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")

    implementation("io.github.openfeign:feign-core:12.5")
    implementation("io.github.openfeign:feign-jackson:12.5")
    implementation("io.github.openfeign:feign-okhttp:12.5")
    // https://mvnrepository.com/artifact/org.gradle/gradle-tooling-api
    //runtimeOnly("org.gradle:gradle-api:7.4.2")
    implementation("org.gradle:gradle-tooling-api:7.4.2")


}
