import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    var kotlinVersion: String by extra
    kotlinVersion = "1.2.21"

    repositories {
        mavenCentral()
    }
    dependencies {
        classpath(kotlinModule("gradle-plugin", kotlinVersion))
    }
}

group = "syncdata-kt"
version = "1.0-SNAPSHOT"

apply {
    plugin("kotlin")
}

val kotlinVersion: String by extra

repositories {
    mavenCentral()
}

dependencies {
    "compile"(kotlinModule("stdlib-jdk8", kotlinVersion))

    "compile"("com.google.code.gson:gson:2.8.2")
    "compile"("com.lmax:disruptor:3.3.7")
    "compile"("redis.clients:jedis:2.9.0")
    "compile"("org.apache.rocketmq:rocketmq-client:4.2.0")
    "compile"("org.apache.logging.log4j:log4j-slf4j-impl:2.10.0")
    "compile"("org.apache.logging.log4j:log4j-core:2.10.0")
    "compile"("org.bytedeco:javacv:1.4")

    "compile"(fileTree("libs") {
        include("*.jar")
    })
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

var outLibs = "outLibs"

tasks {
    "copyJar"(Copy::class) {
        delete(outLibs)
        from(configurations.runtime)
        into(outLibs)
    }
}
