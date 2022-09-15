import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "no.nav.syfo"
version = "1.0.0"

val coroutinesVersion = "1.6.4"
val jacksonVersion = "2.13.4"
val kluentVersion = "1.68"
val ktorVersion = "2.1.1"
val logbackVersion = "1.4.0"
val logstashEncoderVersion = "7.2"
val prometheusVersion = "0.16.0"
val kotestVersion = "5.4.2"
val smCommonVersion = "1.f132f2b"
val mockkVersion = "1.12.7"
val nimbusdsVersion = "9.24.3"
val testContainerKafkaVersion = "1.17.3"
val sykmeldingArbeidsgiverVersion = "1.9daf0fa"
val altinnCorrespondenceAgencyExternalVersion = "1.2020.01.20-15.44-063ae9f84815"
val flyingSaucerVersion = "9.1.22"
val baticVersion = "1.14"
val iTextVersion = "2.1.7"
val saxonVersion = "10.6"
val pdfBoxVersion = "2.0.24"
val cxfVersion = "3.4.5"
val jaxsWsApiVersion = "2.3.1"
val jaxwsRiVersion = "2.3.2"
val jaxwsToolsVersion = "2.3.1"
val javaxActivationVersion = "1.1.1"
val postgresVersion = "42.4.2"
val flywayVersion = "9.1.6"
val hikariVersion = "5.0.1"
val postgresContainerVersion = "1.17.3"
val kotlinVersion = "1.7.10"
val googleCloudStorageVersion = "2.11.3"
val commonsVollectionsVersion = "3.2.2"
val xmlschemaCoreVersion = "2.2.5"
val jaxbApiVersion = "2.4.0-b180830.0359"
val jaxbRuntimeVersion = "2.4.0-b180830.0438"

tasks.withType<Jar> {
    manifest.attributes["Main-Class"] = "no.nav.syfo.BootstrapKt"
}

plugins {
    id("org.jmailen.kotlinter") version "3.10.0"
    kotlin("jvm") version "1.7.10"
    id("com.diffplug.spotless") version "6.5.0"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    jacoco
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
    implementation("io.ktor:ktor-client-cio:$ktorVersion")
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

    implementation("org.postgresql:postgresql:$postgresVersion")
    implementation("com.zaxxer:HikariCP:$hikariVersion")
    implementation("org.flywaydb:flyway-core:$flywayVersion")

    implementation("com.google.cloud:google-cloud-storage:$googleCloudStorageVersion")

    implementation("org.xhtmlrenderer:flying-saucer-pdf:$flyingSaucerVersion") {
        exclude("bouncycastle", "bcmail-jdk14")
        exclude("bouncycastle", "bcprov-jdk14")
        exclude("bouncycastle", "bctsp-jdk14")
        exclude("org.bouncycastle", "bctsp-jdk14")
    }
    implementation("org.xhtmlrenderer:flying-saucer-core:$flyingSaucerVersion")
    implementation("org.apache.xmlgraphics:batik-transcoder:$baticVersion") {
        exclude("xml-apis", "xml-apis")
        exclude("commons-logging", "commons-logging")
        exclude("org.python", "jython")
        exclude("xalan", "xalan")
    }
    runtimeOnly("org.apache.xmlgraphics:batik-codec:$baticVersion")
    implementation("com.lowagie:itext:$iTextVersion") {
        exclude("bouncycastle", "bcmail-jdk14")
        exclude("bouncycastle", "bcprov-jdk14")
        exclude("bouncycastle", "bctsp-jdk14")
        exclude("org.bouncycastle", "bctsp-jdk14")
    }

    implementation("net.sf.saxon:Saxon-HE:$saxonVersion")
    implementation("org.apache.pdfbox:pdfbox:$pdfBoxVersion") {
        exclude("commons-logging", "commons-logging")
    }
    implementation("javax.xml.bind:jaxb-api:$jaxbApiVersion")
    implementation("org.glassfish.jaxb:jaxb-runtime:$jaxbRuntimeVersion")
    implementation("no.nav.tjenestespesifikasjoner:altinn-correspondence-agency-external-basic:$altinnCorrespondenceAgencyExternalVersion")
    implementation("org.apache.cxf:cxf-rt-frontend-jaxws:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-features-logging:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-transports-http:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-ws-security:$cxfVersion")
    // for å overstyre sårbar versjon fra cxf-rt-ws-security
    implementation("commons-collections:commons-collections:$commonsVollectionsVersion")

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
    testImplementation("com.nimbusds:nimbus-jose-jwt:$nimbusdsVersion")
    testImplementation("org.testcontainers:kafka:$testContainerKafkaVersion")
    testImplementation("io.ktor:ktor-client-mock:$ktorVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion") {
        exclude(group = "org.eclipse.jetty")
    }
    testImplementation("io.kotest:kotest-runner-junit5:$kotestVersion")
}

tasks.jacocoTestReport {
    reports {
        xml.required.set(true)
        html.required.set(true)
    }
}


tasks {

    create("printVersion") {
        println(project.version)
    }

    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "17"
    }

    withType<JacocoReport> {
        classDirectories.setFrom(
                sourceSets.main.get().output.asFileTree.matching {
                    exclude()
                }
        )

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
