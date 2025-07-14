import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer

group = "no.nav.syfo"
version = "1.0.0"

val coroutinesVersion = "1.10.2"
val jacksonVersion = "2.19.1"
val kluentVersion = "1.73"
val ktorVersion = "3.2.1"
val logbackVersion = "1.5.18"
val logstashEncoderVersion = "8.1"
val prometheusVersion = "0.16.0"
val mockkVersion = "1.14.4"
val testContainerKafkaVersion = "1.21.3"
val altinnCorrespondenceAgencyExternalVersion = "1.2020.01.20-15.44-063ae9f84815"
val saxonVersion = "12.8"
val cxfVersion = "3.6.4"
val jaxsWsApiVersion = "2.3.1"
val jaxwsToolsVersion = "2.3.1"
val javaxActivationVersion = "1.1.1"
val postgresVersion = "42.7.7"
val flywayVersion = "11.10.1"
val hikariVersion = "6.3.0"
val postgresContainerVersion = "1.21.3"
val kotlinVersion = "2.2.0"
val googleCloudStorageVersion = "2.53.2"
val xmlschemaCoreVersion = "2.2.5"
val jaxbApiVersion = "2.4.0-b180830.0359"
val jaxbRuntimeVersion = "2.4.0-b180830.0438"
val syfoXmlCodeGen = "2.0.1"
val jsoupVersion = "1.21.1"
val ktfmtVersion = "0.44"
val bcprovJdk15onVersion = "1.70"
val junitJupiterVersion="5.13.3"
val kafkaVersion = "3.9.0"

///Due to vulnerabilities
val commonsCollectionsVersion = "3.2.2"
val commonsCompressVersion = "1.27.1"

plugins {
    id("application")
    id("com.diffplug.spotless") version "7.0.4"
    kotlin("jvm") version "2.2.0"
    id("com.gradleup.shadow") version "8.3.8"
}

application {
    mainClass.set("no.nav.syfo.ApplicationKt")
}

repositories {
    mavenCentral()
    maven(url = "https://packages.confluent.io/maven/")
    maven {
        url = uri("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    }
}


buildscript {
    dependencies {
        classpath("javax.xml.bind:jaxb-api:2.4.0-b180830.0359")
        classpath("org.glassfish.jaxb:jaxb-runtime:2.4.0-b180830.0438")
        classpath("com.sun.activation:javax.activation:1.2.0")
        classpath("com.sun.xml.ws:jaxws-tools:2.3.1") {
            exclude(group = "com.sun.xml.ws", module = "policy")
        }
    }
}


dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:$coroutinesVersion")
    implementation("io.prometheus:simpleclient_hotspot:$prometheusVersion")
    implementation("io.prometheus:simpleclient_common:$prometheusVersion")

    implementation("io.ktor:ktor-server-auth:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt:$ktorVersion")
    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")

    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-server-call-id:$ktorVersion")
    implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-apache:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")

    implementation("no.nav.helse.xml:sykmelding-arbeidsgiver:$syfoXmlCodeGen")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashEncoderVersion")

    implementation("com.fasterxml.jackson.module:jackson-module-jaxb-annotations:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")

    implementation("org.apache.kafka:kafka_2.12:$kafkaVersion")
    implementation("org.apache.kafka:kafka-streams:$kafkaVersion")

    implementation("org.postgresql:postgresql:$postgresVersion")
    implementation("com.zaxxer:HikariCP:$hikariVersion")
    compileOnly("org.flywaydb:flyway-core:$flywayVersion")
    implementation("org.flywaydb:flyway-database-postgresql:$flywayVersion")

    implementation("com.google.cloud:google-cloud-storage:$googleCloudStorageVersion")

    implementation("net.sf.saxon:Saxon-HE:$saxonVersion")

    implementation("javax.xml.bind:jaxb-api:$jaxbApiVersion")
    implementation("org.glassfish.jaxb:jaxb-runtime:$jaxbRuntimeVersion")
    implementation("no.nav.tjenestespesifikasjoner:altinn-correspondence-agency-external-basic:$altinnCorrespondenceAgencyExternalVersion")
    implementation("org.apache.cxf:cxf-rt-frontend-jaxws:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-features-logging:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-transports-http:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-ws-security:$cxfVersion"){
        exclude(group = "org.apache.velocity", module = "velocity")
    }
    constraints {
        implementation("org.bouncycastle:bcprov-jdk15on:$bcprovJdk15onVersion"){
            because("override transient from org.apache.cxf:cxf-rt-ws-security")
        }
    }
    constraints {
        implementation("commons-collections:commons-collections:$commonsCollectionsVersion") {
            because("override transient from org.apache.cxf:cxf-rt-ws-security")
        }
    }

    implementation("org.apache.ws.xmlschema:xmlschema-core:$xmlschemaCoreVersion")
    implementation("javax.activation:activation:$javaxActivationVersion")
    implementation("javax.xml.ws:jaxws-api:$jaxsWsApiVersion")
    implementation("com.sun.xml.ws:jaxws-tools:$jaxwsToolsVersion") {
        exclude(group = "com.sun.xml.ws", module = "policy")
    }

    testImplementation("org.jsoup:jsoup:$jsoupVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    testImplementation("org.testcontainers:postgresql:$postgresContainerVersion")
    constraints {
        implementation("org.apache.commons:commons-compress:$commonsCompressVersion") {
            because("Due to vulnerabilities, see CVE-2024-26308")
        }
    }
    testImplementation("org.amshove.kluent:kluent:$kluentVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.testcontainers:kafka:$testContainerKafkaVersion") {
        exclude(group = "junit", module = "junit")
    }
    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion") {
        exclude(group = "org.eclipse.jetty")
    }
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitJupiterVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}


tasks {

    shadowJar {
        transform(ServiceFileTransformer::class.java) {
            setPath("META-INF/services/org.flywaydb.core.extensibility.Plugin")
            setPath("META-INF/cxf")
            include("bus-extensions.txt")
        }
        archiveBaseName.set("app")
        archiveClassifier.set("")
        isZip64 = true
        manifest {
            attributes(
                mapOf(
                    "Main-Class" to "no.nav.syfo.ApplicationKt",
                ),
            )
        }
    }

    test {
        useJUnitPlatform {
        }
        testLogging {
            events("skipped", "failed")
            showStackTraces = true
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }

    spotless {
        kotlin { ktfmt(ktfmtVersion).kotlinlangStyle() }
        check {
            dependsOn("spotlessApply")
        }
    }
}
