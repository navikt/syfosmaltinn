import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "no.nav.syfo"
version = "1.0.0"

val coroutinesVersion = "1.6.4"
val jacksonVersion = "2.15.0"
val kluentVersion = "1.72"
val ktorVersion = "2.3.0"
val logbackVersion = "1.4.7"
val logstashEncoderVersion = "7.3"
val prometheusVersion = "0.16.0"
val kotestVersion = "5.6.1"
val smCommonVersion = "1.9df1108"
val mockkVersion = "1.13.5"
val testContainerKafkaVersion = "1.18.0"
val sykmeldingArbeidsgiverVersion = "1.5d21db9"
val altinnCorrespondenceAgencyExternalVersion = "1.2020.01.20-15.44-063ae9f84815"
val saxonVersion = "10.6"
val cxfVersion = "3.5.5"
val jaxsWsApiVersion = "2.3.1"
val jaxwsRiVersion = "2.3.2"
val jaxwsToolsVersion = "2.3.1"
val javaxActivationVersion = "1.1.1"
val postgresVersion = "42.6.0"
val flywayVersion = "9.17.0"
val hikariVersion = "5.0.1"
val postgresContainerVersion = "1.18.0"
val kotlinVersion = "1.8.21"
val googleCloudStorageVersion = "2.22.0"
val xmlschemaCoreVersion = "2.2.5"
val jaxbApiVersion = "2.4.0-b180830.0359"
val jaxbRuntimeVersion = "2.4.0-b180830.0438"

tasks.withType<Jar> {
    manifest.attributes["Main-Class"] = "no.nav.syfo.BootstrapKt"
}

plugins {
    id("org.jmailen.kotlinter") version "3.14.0"
    kotlin("jvm") version "1.8.21"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

val githubUser: String by project
val githubPassword: String by project

repositories {
    mavenCentral()
    maven(url = "https://packages.confluent.io/maven/")
    maven {
        url = uri("https://maven.pkg.github.com/navikt/syfosm-common")
        credentials {
            username = githubUser
            password = githubPassword
        }
    }
    maven {
        url = uri("https://maven.pkg.github.com/navikt/maven-release")
        credentials {
            username = githubUser
            password = githubPassword
        }
    }
    maven {
        url = uri("https://maven.pkg.github.com/navikt/syfo-xml-codegen")
        credentials {
            username = githubUser
            password = githubPassword
        }
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

    implementation("io.ktor:ktor-server-core:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages:$ktorVersion")
    implementation("io.ktor:ktor-server-call-id:$ktorVersion")
    implementation("io.ktor:ktor-serialization-jackson:$ktorVersion")
    implementation("io.ktor:ktor-client-core:$ktorVersion")
    implementation("io.ktor:ktor-client-apache:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation:$ktorVersion")

    implementation("no.nav.helse.xml:sykmeldingArbeidsgiver:$sykmeldingArbeidsgiverVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashEncoderVersion")

    implementation("com.fasterxml.jackson.module:jackson-module-jaxb-annotations:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jacksonVersion")
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("no.nav.helse:syfosm-common-kafka:$smCommonVersion")
    implementation("no.nav.helse:syfosm-common-models:$smCommonVersion")
    implementation("no.nav.helse:syfosm-common-networking:$smCommonVersion")

    implementation("org.postgresql:postgresql:$postgresVersion")
    implementation("com.zaxxer:HikariCP:$hikariVersion")
    implementation("org.flywaydb:flyway-core:$flywayVersion")

    implementation("com.google.cloud:google-cloud-storage:$googleCloudStorageVersion")

    implementation("net.sf.saxon:Saxon-HE:$saxonVersion")

    implementation("javax.xml.bind:jaxb-api:$jaxbApiVersion")
    implementation("org.glassfish.jaxb:jaxb-runtime:$jaxbRuntimeVersion")
    implementation("no.nav.tjenestespesifikasjoner:altinn-correspondence-agency-external-basic:$altinnCorrespondenceAgencyExternalVersion")
    implementation("org.apache.cxf:cxf-rt-frontend-jaxws:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-features-logging:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-transports-http:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-ws-security:$cxfVersion") {
        exclude(group = "org.apache.velocity", module = "velocity")
    }

    implementation("org.apache.ws.xmlschema:xmlschema-core:$xmlschemaCoreVersion")
    implementation("javax.activation:activation:$javaxActivationVersion")
    implementation("javax.xml.ws:jaxws-api:$jaxsWsApiVersion")
    implementation("com.sun.xml.ws:jaxws-tools:$jaxwsToolsVersion") {
        exclude(group = "com.sun.xml.ws", module = "policy")
    }

    testImplementation("org.jetbrains.kotlin:kotlin-test:$kotlinVersion")
    testImplementation("org.testcontainers:postgresql:$postgresContainerVersion")
    testImplementation("org.amshove.kluent:kluent:$kluentVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.testcontainers:kafka:$testContainerKafkaVersion") {
        exclude(group = "junit", module = "junit")
    }
    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion") {
        exclude(group = "org.eclipse.jetty")
    }
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
}


tasks {

    create("printVersion") {
        println(project.version)
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    withType<ShadowJar> {
        isZip64 = true
        transform(ServiceFileTransformer::class.java) {
            setPath("META-INF/cxf")
            include("bus-extensions.txt")
        }
    }

    withType<Test> {
        useJUnitPlatform {
        }
        testLogging {
            events("skipped", "failed")
            showStackTraces = true
            exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
        }
    }

    "check" {
        dependsOn("formatKotlin")
    }
}
