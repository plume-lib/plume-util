// No explicit check of the Gradle JVM's version is needed here.  Gradle 9 requires a JVM between
// 17 and 26 to run, and reports a clear error on an unsupported JVM.

plugins {
  // Downloads a JDK if the Java toolchain that build.gradle.kts requests is not installed locally.
  //
  // Literal version number because a settings script cannot use the version catalog
  // "gradle/libs.versions.toml", which is not yet available while settings.gradle.kts is evaluated.
  // Dependency-update tools do not propose new versions of this plugin, so update it by hand,
  // from https://plugins.gradle.org/plugin/org.gradle.toolchains.foojay-resolver-convention.
  id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

// Project name is read-only in build scripts, and defaults to directory name.
rootProject.name = "plume-util"
