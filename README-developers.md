# Developer documentation

## Making a release

### Requirements

Your ~/.gradle/gradle.properties file must contain:

```properties
signing.keyId=...
signing.password=...
signing.secretKeyRingFile=...
mavenCentralUsername=...
mavenCentralPassword=...
```

### Steps

Run these steps on any filesystem, except the `javadocWeb` step.

* Make and test a snapshot release, see below.
* git pull
* In ../build.gradle, ensure that "To use a snapshot version" is not enabled.
* Update the version number in README.md, build.gradle, and in this file (multiple times in each).
  Ensure the version number in this file does not contain "-SNAPSHOT".
* Update ../CHANGELOG.md .
* Save files and stage changes.
* Run in the top-level directory:  ./gradlew publishToMavenCentral
  (Previously: ./gradlew clean publish)
* Browse to https://central.sonatype.com/publishing/deployments, complete the Maven Central release.
* Add a git tag and commit:
  VER=1.12.0 && git commit -m "Version $VER" && git push && git tag -a v$VER -m "Version $VER" && git push && git push --tags
* Make a GitHub release. Go to the GitHub releases page, make a release, call it "plume-util 1.12.0", use the text from ../CHANGELOG.md as the description, attach the .jar and -all.jar files from ../build/libs/ .
* Finally, run on the CSE filesystem:  git pull && ./gradlew javadocWeb
* Update clients and test, so that if it's broken we can re-release.

## Making a snapshot release

* git pull
* Set version to end in "-SNAPSHOT".
* Make the snapshot release.
   * Approach 1:  to Maven Central
      * ./gradlew publishToMavenCentral
      * In the clients' build.gradle: set version number and use:
        ```gradle
          repositories {
            maven { url = uri("https://central.sonatype.com/repository/maven-snapshots/") }
          }
          configurations.all {
            resolutionStrategy.cacheChangingModulesFor 0, "seconds"
          }
        ```
   * Approach 2:  to Maven Local
      * ./gradlew PublishToMavenLocal
      * In the clients' build.gradle: set version number and use:
        ```gradle
          repositories {
            mavenLocal()
          }
        ```
* Test the test snapshot release on some clients:
  * For the Checker Framework (don't skip running the tests):
    ```sh
    # This ensures that the correct JDK is being used
    usecf THE-BRANCH-THAT-USES-THE-SNAPSHOT
    cd $cf
    checker/bin-devel/test-cftests-all.sh && checker/bin-devel/test-typecheck.sh && checker/bin-devel/test-plume-lib.sh
    ```
 * For Daikon: make compile junit test

