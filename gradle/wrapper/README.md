# Gradle launcher disclosure

`gradle-wrapper.jar` in this educational ZIP is a **minimal, transparent Gradle-distribution bootstrap** compiled from `GradleWrapperMain.java` in the same folder. It is **not** the official Gradle Wrapper JAR. It downloads the official Gradle 8.9 ZIP from `services.gradle.org`, extracts it to the local `~/.gradle/wrapper/dists/focusflow-gradle-8.9` cache, and then invokes Gradle.

For maximum trust and compatibility, developers can install Gradle and run `gradle wrapper --gradle-version 8.9` to replace the bootstrap with the official wrapper artifacts. Review the source before use. The Android project itself does not require Internet access to perform model inference, though it needs Internet on the first Gradle dependency sync.
