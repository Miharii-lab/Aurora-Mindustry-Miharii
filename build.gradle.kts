import ent.EntityAnnoExtension
import java.io.File

buildscript{
    val mindustryVersion = providers.gradleProperty("mindustryVersion").get()
    dependencies{
        classpath("Anuken:Mindustry:$mindustryVersion")
    }
    repositories{
        ivy{
            url = uri("https://github.com")
            patternLayout{ artifact("Anuken/Mindustry/releases/download/[revision]/dependencies.jar") }
            metadataSources{ artifact() }
        }
    }
}
plugins{
    java
    id("com.github.GglLfr.EntityAnno") apply false
}

val mindustryVersion = providers.gradleProperty("mindustryVersion").get()
val entVersion = providers.gradleProperty("entVersion").get()
val modName = providers.gradleProperty("modName").get()
val modArtifact = providers.gradleProperty("modArtifact").get()

fun mindustry() = "Anuken:Mindustry:$mindustryVersion"
fun entity(module: String) = "com.github.GglLfr.EntityAnno$module:$entVersion"

allprojects{
    apply(plugin = "java")
    sourceSets["main"].java.setSrcDirs(listOf(layout.projectDirectory.dir("src")))
    repositories{
        ivy{
            url = uri("https://github.com")
            patternLayout{ artifact("Anuken/Mindustry/releases/download/[revision]/dependencies.jar") }
            metadataSources{ artifact() }
        }
        mavenLocal()
        mavenCentral()
        maven("https://raw.githubusercontent.com/GglLfr/EntityAnnoMaven/main")
    }
    tasks.withType<JavaCompile>().configureEach{
        options.encoding = "UTF-8"
        options.compilerArgs.add("-Xlint:-options")
        // Fork the javac process and pass module exports/open flags to the compiler JVM so
        // annotation processors that use internal javac APIs (EntityAnno) can run under JDK17+
        options.isFork = true
        options.forkOptions.jvmArgs.addAll(listOf("--add-exports=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED", "--add-opens=jdk.compiler/com.sun.tools.javac.code=ALL-UNNAMED"))
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
}

project(":"){
    apply(plugin = "com.github.GglLfr.EntityAnno")
    configure<EntityAnnoExtension>{
        modName = providers.gradleProperty("modName").get()
        mindustryVersion = providers.gradleProperty("mindustryVersion").get()
        revisionDir = layout.projectDirectory.dir("revisions").asFile
        fetchPackage = providers.gradleProperty("modFetch").get()
        genSrcPackage = providers.gradleProperty("modGenSrc").get()
        genPackage = providers.gradleProperty("modGen").get()
    }
    dependencies{
        compileOnly(entity(":entity"))
        annotationProcessor(entity(":entity"))
        compileOnly(mindustry())
    }
    tasks.named<Jar>("jar"){
        archiveFileName = "${modArtifact}Desktop.jar"
        val meta = layout.projectDirectory.file("mod.hjson")
        from(sourceSets["main"].output)
        from(layout.projectDirectory.dir("src/assets"))
        from(meta)
        from(layout.projectDirectory.file("LICENSE"))
    }
}


tasks.register<Zip>("sourceZip") {
    archiveFileName = "${modArtifact}-source-v${providers.gradleProperty("projectVersion").getOrElse("2.0.4.6")}.zip"
    destinationDirectory.set(layout.buildDirectory.dir("release"))
    from(layout.projectDirectory) {
        exclude("build/**")
        exclude(".gradle/**")
        exclude("*.jar")
        exclude("*.zip")
    }
}
