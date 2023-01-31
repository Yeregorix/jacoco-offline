# jacoco-offline

jacoco-offline is a fork of the official [JaCoCo plugin](https://github.com/gradle/gradle/tree/master/subprojects/jacoco)
for [Gradle](https://github.com/gradle/gradle).

This fork is based on [PR#23656](https://github.com/gradle/gradle/pull/23656) which adds Jacoco offline instrumentation support to the 
official plugin.

This fork is for people that cannot wait for the PR to get merged, or people who want to use this functionality in Gradle versions prior to the PR.

The only difference with the PR is that the classes has been relocated from `org.gradle` to `net.smoofyuniverse`
to avoid classpath conflicts with the official plugin.

## Usage

```groovy
plugins {
    id 'net.smoofyuniverse.jacoco-offline' version '1.0.0'
}

test {
    jacoco {
        offline.set(true)
    }
}
```

## Development

The development of the offline instrumentation occurs on the PR, not on this repository.
This repository has been generated from the full gradle repository using the following commands:
```
git filter-repo --subdirectory-filter subprojects/jacoco
git filter-repo --path src/main/java
```

This project requires Java 8 and Gradle 7 to build.

## Credits

- Contributors of the Gradle project.
- Contributors of the JaCoCo project.