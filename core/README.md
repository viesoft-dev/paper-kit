# PaperKit Core

[![](https://jitpack.io/v/viesoft-dev/paper-kit.svg)](https://jitpack.io/#viesoft-dev/paper-kit)

This is the core module of PaperKit, which contains only the Kotlin implementations of general Paper components.

### Components

- [x] [KotlinPlugin](./src/main/kotlin/dev/viesoft/paperkit/core/plugin/KotlinPlugin.kt) — alternative to JavaPlugin
  with suspend functions.
- [x] [KotlinListener](./src/main/kotlin/dev/viesoft/paperkit/core/listener/KotlinListener.kt) — alternative to Listener
  with suspend listeners support.
- [x] [KotlinCommand](./src/main/kotlin/dev/viesoft/paperkit/core/command/KotlinCommand.kt) — alternative to Command
  with suspend execute & tabComplete functions.

## Get Started

First of all add the dependency to your project:

```kotlin
dependencies {
    implementation("dev.viesoft.paperkit", "core", "VERSION")
}
```

Then you can use the KotlinPlugin class as a base for your plugin:

```kotlin
class MyPlugin : KotlinPlugin() {

    override suspend fun loadConfig() {
        // Your plugin config logic here
    }

    override suspend fun onEnabled() {
        // Your plugin start logic here
    }

    override suspend fun onDisabled() {
        // Your plugin stop logic here
    }
}
```

