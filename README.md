# syfosmaltinn
[![Deploy to dev and prod](https://github.com/navikt/syfosmaltinn/actions/workflows/deploy.yml/badge.svg)](https://github.com/navikt/syfosmaltinn/actions/workflows/deploy.yml)

This project contains the application code and infrastructure for syfosmaltinn

## Technologies used
* Kotlin
* Ktor
* Gradle
* Junit

### :scroll: Prerequisites
* JDK 21
  Make sure you have the Java JDK 21 installed
  You can check which version you have installed using this command:
``` shell
java -version
```

* Docker
  Make sure you have the Docker installed
  You can check which version you have installed using this command:
``` shell
docker -version
```

## Getting started
### Building the application
#### Compile and package application
To build locally and run the integration tests you can simply run
``` bash
./gradlew shadowJar
``` 
or  on windows 
`gradlew.bat shadowJar`

### Upgrading the gradle wrapper

Find the newest version of gradle here: https://gradle.org/releases/ Then run this command:

``` bash
./gradlew wrapper --gradle-version $gradleVersjon
```

### Contact

This project is maintained by navikt/teamsykmelding

Questions and/or feature requests? Please create an [issue](https://github.com/navikt/syfosmaltinn/issues)

If you work in [@navikt](https://github.com/navikt) you can reach us at the Slack
channel [#team-sykmelding](https://nav-it.slack.com/archives/CMA3XV997)
