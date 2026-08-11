plugins {
    alias(libs.plugins.fabric.loom)
    alias(libs.plugins.spotless)
}

base {
    archivesName = properties["archives_base_name"] as String
    group = properties["maven_group"] as String
    version = libs.versions.mod.version.get() as String
}

repositories {
    maven {
        name = "meteor-maven"
        url = uri("https://maven.meteordev.org/releases")
    }
    maven {
        name = "meteor-maven-snapshots"
        url = uri("https://maven.meteordev.org/snapshots")
    }
}

dependencies {
    // Fabric
    // MC 26.1.x ships de-obfuscated with Mojang official names, so Loom refuses an
    // explicit officialMojangMappings() declaration: "Cannot use Mojang mappings in a
    // non-obfuscated environment." The default (no mappings line) IS Mojang official —
    // matching the mojmap-converted sources and the blackout.accesswidener (v2 official).
    // Do NOT re-add a mappings(...) line; it cannot compile in this environment.
    minecraft(libs.minecraft)
    implementation(libs.fabric.loader)

    // Meteor
    implementation(libs.meteor.client)
    compileOnly(libs.baritone)
}

loom {
    accessWidenerPath = file("src/main/resources/blackout.accesswidener")
}

fun toMinecraftCompat(version: String): String {
    val match = Regex("""^(\d{2})\.([1-9]\d*)(?:\.([1-9]\d*))?$""")
        .matchEntire(version)
        ?: error("Invalid Minecraft version format: $version. Expected YY.D or YY.D.H")

    val (year, drop, _) = match.destructured
    return "~$year.$drop"
}

tasks {
    processResources {
        val propertyMap = mapOf(
            "version" to project.version,
            "minecraft_version" to toMinecraftCompat(libs.versions.minecraft.get()),
            "jdk_version" to libs.versions.jdk.get(),
            )

        inputs.properties(propertyMap)

        filesMatching("fabric.mod.json") {
            expand(propertyMap)
        }
    }

    withType<JavaCompile> {
        options.release = 25
        options.encoding = "UTF-8"
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_25
        targetCompatibility = JavaVersion.VERSION_25
    }
}

spotless {
    java {
        target("src/main/java/**/*.java")
        googleJavaFormat()

        trimTrailingWhitespace()
        endWithNewline()
    }
}
