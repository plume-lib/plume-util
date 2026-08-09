import net.ltgt.gradle.errorprone.errorprone
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
  id("java-library")

  // Creates the fat jar build/libs/...-all.jar as part of "assemble".
  // To create only that jar, run: ./gradlew shadowJar
  alias(libs.plugins.com.gradleup.shadow)

  // Code formatting; defines targets "spotlessApply" and "spotlessCheck"
  // which are run by "check" (which is itself run by "build").
  alias(libs.plugins.com.diffplug.spotless)

  // Error Prone linter
  alias(libs.plugins.net.ltgt.errorprone)

  // PMD linter
  id("pmd")

  // Code coverage
  id("jacoco")

  // Checker Framework pluggable type-checking
  alias(libs.plugins.org.checkerframework)

  // Publishing to Maven Central
  alias(libs.plugins.com.vanniktech.maven.publish)
}

repositories {
  // mavenLocal() comes first so that a locally-installed snapshot of a dependency takes
  // precedence.  Restricting this to snapshots prevents a stale locally-installed non-snapshot
  // release from shadowing the one on Maven Central.  One consequence is that a locally-installed
  // *release* build of a dependency is ignored; to build against such a build, either temporarily
  // remove "snapshotsOnly()" or install the build as a snapshot.
  mavenLocal {
    mavenContent { snapshotsOnly() }
  }
  mavenCentral()
  maven {
    url = uri("https://central.sonatype.com/repository/maven-snapshots/")
    mavenContent { snapshotsOnly() }
  }
}

dependencies {
  implementation(libs.hashmap.util)
  implementation(libs.reflection.util)
  // Including error_prone_annotations leads to javac warnings (not errors, but
  // annoying nonetheless) when a client of this library doesn't itself depend on
  // error_prone_annotations.  (Even if the dependency is "compileOnly".)
  // implementation("com.google.errorprone:error_prone_annotations")

  testImplementation(libs.junit.jupiter)
  testRuntimeOnly(libs.junit.platform.launcher)
}

// RequireJavadoc calls javac internals, which the jdk.compiler module does not export.
val javacInternalPackages = listOf("file", "parser", "tree", "util")
val javacExportTargets = javacInternalPackages.map {
  "jdk.compiler/com.sun.tools.javac.$it=ALL-UNNAMED"
}
val addExportsArgs = javacExportTargets.map { "--add-exports=$it" }
val addOpensArgs = javacExportTargets.map { "--add-opens=$it" }

// Compilation

java {
  toolchain {
    // Always compile using Java 21.  The "test" task below overrides this for
    // test execution, so that tests run under various Java versions.
    languageVersion = JavaLanguageVersion.of(21)
  }
}

tasks.withType<JavaCompile>().configureEach {
  options.release = 21

  // Gradle compiles in a worker process whenever the toolchain above differs
  // from the JVM that runs Gradle.  That worker does not inherit the
  // `org.gradle.jvmargs` heap setting in gradle.properties, so set it here too.
  options.forkOptions.jvmArgs = options.forkOptions.jvmArgs.orEmpty() + "-Xmx6g"
  options.compilerArgs.add("-Werror")
  // "-processing" avoids javac warning "No processor claimed any of these annotations".
  options.compilerArgs.add("-Xlint:all,-processing,-this-escape")
}

// Testing

// Compilation always uses Java 21, but the tests run under various Java versions.  The
// `java.toolchain` setting above applies to Test tasks as well as to compilation, so without this
// the tests would always run under Java 21.  By default the tests run under the JVM that Gradle
// itself is running under.  The job matrix in .github/workflows/gradle.yml does not rely on that
// default:  it runs Gradle under Java 21 in every job and selects the test JVM by passing
// `-PtestJavaVersion`.
// Override with, for example:
//   ./gradlew test -PtestJavaVersion=21
val testJavaVersionProperty = project.findProperty("testJavaVersion")?.toString()

// An empty value is a mistake rather than a request for the default, so reject it.
if (testJavaVersionProperty != null && testJavaVersionProperty.isEmpty()) {
  throw GradleException(
    "-PtestJavaVersion needs a value, as in `-PtestJavaVersion=25`." +
      "  Omit the property entirely to test under the JVM that Gradle is running under."
  )
}

val testJavaVersion =
  JavaLanguageVersion.of(testJavaVersionProperty ?: JavaVersion.current().majorVersion)

tasks.withType<Test>().configureEach {
  javaLauncher = javaToolchains.launcherFor { languageVersion = testJavaVersion }

  useJUnitPlatform {
    includeEngines("junit-jupiter")
    excludeEngines("junit-vintage")
  }

  // Always re-run the tests, so that their output always appears.  Both lines
  // are needed:  "upToDateWhen" alone would still permit the build cache to
  // supply the outputs of a previous run.
  outputs.upToDateWhen { false }
  outputs.cacheIf { false }

  testLogging {
    // "passed" is what makes the forced re-run above worth its cost:  without it, a run in which
    // every test passes prints nothing at all.
    // `showStandardStreams = true` would be a redundant way to say the same
    // thing as including the "standardOut" and "standardError" events here.
    events("passed", "skipped", "failed", "standardOut", "standardError")
    exceptionFormat = TestExceptionFormat.FULL
  }

  // Generate the coverage report after the tests run.
  finalizedBy(tasks.named("jacocoTestReport"))
}

jacoco { toolVersion = libs.versions.jacoco.get() }

tasks.named<JacocoReport>("jacocoTestReport") {
  reports {
    xml.required = false
    csv.required = true // Output is written to build/reports/jacoco/test/jacocoTestReport.csv
    html.required = true // Output is written to build/reports/jacoco/test/html/index.html
  }
}

// Code formatting

spotless {
  java {
    googleJavaFormat(libs.versions.google.java.format.get())
    formatAnnotations()
  }
  kotlinGradle {
    // The default target is every "*.gradle.kts" file.
    ktfmt(libs.versions.ktfmt.get()).googleStyle()
  }
}

// Error Prone linter

dependencies { errorprone(libs.error.prone.core) }

tasks.withType<JavaCompile>().configureEach {
  options.errorprone {
    disable("AnnotateFormatMethod") // Error Prone doesn't know about CF @FormatMethod.
    disable("DoNotCallSuggester") // Suggests use of an Error Prone annotation.
    disable("EffectivelyPrivate") // Loses information about the abstraction.
    disable("ExtendsObject") // Incorrect when using the Checker Framework.
    disable("InlineMeSuggester") // `@InlineMe` requires a dependency on error_prone_annotations.
    disable("ReferenceEquality") // Use Interning Checker instead.
  }
}

// PMD linter

pmd {
  toolVersion = libs.versions.pmd.get()
  ruleSets = emptyList() // Prevent the default errorprone.xml from being applied.
  ruleSetFiles = files("$rootDir/.pmd-ruleset.xml")
  isConsoleOutput = true
}

// Checker Framework pluggable type-checking

checkerFramework {
  version = libs.versions.checker.framework.get()
  checkers =
    listOf(
      // No need to run CalledMethodsChecker, because ResourceLeakChecker does so.
      // "org.checkerframework.checker.calledmethods.CalledMethodsChecker",
      "org.checkerframework.checker.formatter.FormatterChecker",
      "org.checkerframework.checker.index.IndexChecker",
      "org.checkerframework.checker.interning.InterningChecker",
      "org.checkerframework.checker.lock.LockChecker",
      "org.checkerframework.checker.nullness.NullnessChecker",
      "org.checkerframework.checker.regex.RegexChecker",
      "org.checkerframework.checker.resourceleak.ResourceLeakChecker",
      "org.checkerframework.checker.signature.SignatureChecker",
      "org.checkerframework.checker.signedness.SignednessChecker",
      "org.checkerframework.common.initializedfields.InitializedFieldsChecker",
    )
  extraJavacArgs =
    listOf(
      "-Werror",
      // "-Aversion",
      // "-verbose",
      "-AcheckPurityAnnotations",
      "-ArequirePrefixInWarningSuppressions",
      "-AwarnRedundantAnnotations",
      "-AwarnUnneededSuppressions",
    )
}

// Javadoc

// Javadoc generates a CSS import of a font that is not distributed alongside
// the documentation, so remove the import.
//
// This is an object rather than a function of the build script, because a task action that calls a
// function of the build script captures the script object, which the configuration cache forbids.
object JavadocFonts {
  private val dejaVuImport = Regex("""@import url\('(?:resources/)?fonts/dejavu\.css'\);[ \t]*""")

  fun removeDejaVuFontImport(javadocDir: File) {
    if (!javadocDir.isDirectory) {
      return
    }
    javadocDir
      .walkTopDown()
      .filter { it.isFile && (it.name.endsWith(".css") || it.name.endsWith(".html")) }
      .forEach { file ->
        val contents = file.readText(Charsets.UTF_8)
        val newContents = dejaVuImport.replace(contents, "")
        if (newContents != contents) {
          file.writeText(newContents, Charsets.UTF_8)
        }
      }
  }
}

tasks.withType<Javadoc>().configureEach {
  (options as StandardJavadocDocletOptions).isNoTimestamp = true
  options.quiet()
  // `destinationDirectory` is lazy, so this reads whatever the task is configured with, even if
  // that is set after this block runs.
  val javadocDir = destinationDirectory
  doLast { JavadocFonts.removeDejaVuFontImport(javadocDir.get().asFile) }
}

// Turns Javadoc warnings into errors.  This is applied to individual tasks rather than to every
// Javadoc task, because a task that uses a custom doclet rejects the standard doclet's options.
fun strictJavadoc(javadocTask: Javadoc) {
  val options = javadocTask.options as CoreJavadocOptions
  options.addBooleanOption("Xdoclint:all", true)
  options.addBooleanOption("Xwerror", true)
}

tasks.named<Javadoc>("javadoc") { strictJavadoc(this) }

// The `javadoc` task documents only the public API.
// `javadocPrivate` applies the same doclint checks to private members.
val javadocPrivate =
  tasks.register<Javadoc>("javadocPrivate") {
    group = "documentation"
    description = "Generate Javadoc for all members, including private ones."
    source = sourceSets.main.get().allJava
    classpath = sourceSets.main.get().output + sourceSets.main.get().compileClasspath
    destinationDirectory = layout.buildDirectory.dir("docs/javadocPrivate")
    (options as CoreJavadocOptions).addBooleanOption("private", true)
    strictJavadoc(this)
  }

tasks.named("check") { dependsOn("javadoc", javadocPrivate) }

val javadocWebDir = "/cse/web/research/plumelib/${project.name}/api"
val javadocWebUpload =
  tasks.register<Javadoc>("javadocWebUpload") {
    description = "Write API documentation to the website directory."
    source = sourceSets.main.get().allJava
    classpath = sourceSets.main.get().output + sourceSets.main.get().compileClasspath
    destinationDirectory = file(javadocWebDir)
    strictJavadoc(this)
  }
// Set permissions
val javadocWebChgrp =
  tasks.register<Exec>("javadocWebChgrp") {
    description = "Set the Unix group of the website's API documentation."
    mustRunAfter(javadocWebUpload)
    commandLine("chgrp", "-R", "plse_www", javadocWebDir)
    // A file that another user owns cannot be chgrped, which is not worth failing the build over.
    isIgnoreExitValue = true
    val result = executionResult
    doLast {
      val exitValue = result.get().exitValue
      if (exitValue != 0) {
        logger.warn("chgrp of the uploaded API documentation exited with status $exitValue.")
      }
    }
  }
val javadocWebChmod =
  tasks.register<Exec>("javadocWebChmod") {
    description = "Set the Unix permissions of the website's API documentation."
    mustRunAfter(javadocWebUpload)
    commandLine("chmod", "-R", "g+w", javadocWebDir)
    // A file that another user owns cannot be chmoded, which is not worth failing the build over.
    isIgnoreExitValue = true
    val result = executionResult
    doLast {
      val exitValue = result.get().exitValue
      if (exitValue != 0) {
        logger.warn("chmod of the uploaded API documentation exited with status $exitValue.")
      }
    }
  }

// The three tasks above are steps of "javadocWeb", so they have no group and thus do not appear in
// the output of `./gradlew tasks`.
tasks.register("javadocWeb") {
  group = "documentation"
  description = "Upload API documentation to website."
  dependsOn(javadocWebUpload, javadocWebChgrp, javadocWebChmod)
}

// `resolvable` rather than the `configurations { requireJavadoc }` shorthand, which creates a
// configuration that is both resolvable and consumable and that Gradle reports as legacy.
configurations.resolvable("requireJavadoc")

dependencies { addProvider("requireJavadoc", libs.require.javadoc) }

// RequireJavadoc produces no output of its own, so write a marker file.  Without a declared output,
// the task could never be up to date and the declared inputs would have no effect.
val requireJavadocMarker = layout.buildDirectory.file("requireJavadoc/requireJavadoc.txt")
val requireJavadoc =
  tasks.register<JavaExec>("requireJavadoc") {
    group = "documentation"
    description = "Ensures that Javadoc documentation exists."
    inputs.files(sourceSets.main.get().allJava)
    outputs.file(requireJavadocMarker)
    mainClass = "org.plumelib.javadoc.RequireJavadoc"
    classpath = configurations["requireJavadoc"]
    args(sourceSets.main.get().allJava.srcDirs.map { it.absolutePath })
    jvmArgs(addExportsArgs)
    jvmArgs(addOpensArgs)
    // A task action that read `requireJavadocMarker` directly would capture the build script
    // object, which the configuration cache cannot serialize.
    val markerFile = requireJavadocMarker
    // Runs only if the tool found no problems, so that a failure does not leave behind a marker
    // file that would mark this task up to date.
    doLast {
      val marker = markerFile.get().asFile
      marker.parentFile.mkdirs()
      marker.writeText("")
    }
  }

tasks.named("check") { dependsOn(requireJavadoc) }

// On javadocWebUpload rather than on javadocWeb, so that the check runs *before* the upload.  A
// dependency of javadocWeb would be unordered with respect to javadocWebUpload.
javadocWebUpload.configure { dependsOn(requireJavadoc) }

// Publishing, see README-developers.md

// The fat jar is attached to GitHub releases, not published to Maven Central,
// so keep the `shadowRuntimeElements` variant out of the Gradle module metadata.
shadow { addShadowVariantIntoJavaComponent = false }

// Make the published archives byte-for-byte reproducible.
tasks.withType<AbstractArchiveTask>().configureEach {
  isPreserveFileTimestamps = false
  isReproducibleFileOrder = true
}

mavenPublishing {
  coordinates("org.plumelib", project.name, project.version.toString())

  pom {
    name = "Plume Util"
    description =
      "Utility libraries for Java.  Complements Guava, Apache Commons, Eclipse Collections, etc."
    url = "https://github.com/plume-lib/${project.name}"

    scm {
      url = "https://github.com/plume-lib/${project.name}/"
      connection = "scm:git:git://github.com/plume-lib/${project.name}.git"
      developerConnection = "scm:git:ssh://git@github.com/plume-lib/${project.name}.git"
    }

    licenses {
      license {
        name = "MIT License"
        url = "https://opensource.org/licenses/MIT"
      }
    }

    developers {
      developer {
        id = "mernst"
        name = "Michael Ernst"
        email = "mernst@alum.mit.edu"
      }
    }
  }
}

// Emacs support

/* Make Emacs TAGS table */
tasks.register<Exec>("tags") {
  group = "IDE"
  description = "Run etags to create an Emacs TAGS table"
  val sourceFiles =
    fileTree("src") {
      include("**/*.java")
      include("**/*.sh")
    }
  // `projectPath` in a build script is Gradle's project path (such as ":"), not a file system
  // path, so compute the project directory explicitly.
  val projectDirPath = layout.projectDirectory.asFile.toPath()
  inputs.files(sourceFiles)
  outputs.file(layout.projectDirectory.file("TAGS"))
  executable("etags")
  // Compute the arguments when the task runs, not when it is configured.
  argumentProviders.add(
    CommandLineArgumentProvider {
      sourceFiles.files.sorted().map { projectDirPath.relativize(it.toPath()).toString() }
    }
  )
}

// Debugging support

tasks.register("printCompileClasspaths") {
  group = "help"
  description = "Print the compile-time classpaths"
  // Look up the classpaths when the task is configured, and resolve them (`asPath`) when it runs.
  // Reading `sourceSets` from a task action would capture the `Project` object, which the
  // configuration cache forbids.
  val mainClasspath = sourceSets.main.get().compileClasspath
  val testClasspath = sourceSets.test.get().compileClasspath
  doFirst {
    println("Compile classpath:")
    println(mainClasspath.asPath)
    println("Compile test classpath:")
    println(testClasspath.asPath)
  }
}
