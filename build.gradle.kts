plugins {
    base
}

tasks.register<Copy>("buildPlugin") {
    group = "build"
    description = "Builds Spigot plugin and collects JAR into build/libs."
    dependsOn(
        gradle.includedBuild("plugin").task(":build")
    )
    from("plugin/build/libs")
    into("build/libs")
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.register<Copy>("buildAll") {
    group = "build"
    description = "Builds Mod, Spigot plugin, and collects all JARs into build/libs."
    dependsOn(
        gradle.includedBuild("mod").task(":build"),
        "buildPlugin"
    )
    from("mod/build/libs")
    into("build/libs")
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.named("build") {
    dependsOn("buildPlugin")
}

tasks.named("clean") {
    dependsOn(
        gradle.includedBuild("plugin").task(":clean")
    )
}
