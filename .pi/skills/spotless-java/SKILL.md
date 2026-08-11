---
name: spotless-java
description: Production-grade Java code formatting and quality enforcement using Spotless with Google Java Format. Use when (1) setting up code formatting in Java projects, (2) configuring Gradle or Maven build files for code style enforcement, (3) implementing Google Java Style Guide or AOSP style, (4) adding pre-commit formatting checks, (5) fixing "code not formatted" errors in CI/CD, or (6) questions about java formatting configuration with spotless, google-java-format, or code style automation.
---

# Spotless Java Code Quality Enforcement

Enforce consistent Java code formatting using Spotless with Google Java Format in Gradle or Maven projects.

## Quick Start

### Gradle (build.gradle)
```groovy
plugins {
    id 'com.diffplug.spotless' version '7.0.2'
}

spotless {
    java {
        googleJavaFormat()
    }
}
```

Run: `./gradlew spotlessApply`

### Maven (pom.xml)
```xml
<plugin>
    <groupId>com.diffplug.spotless</groupId>
    <artifactId>spotless-maven-plugin</artifactId>
    <version>2.44.3</version>
    <configuration>
        <java>
            <googleJavaFormat/>
        </java>
    </configuration>
</plugin>
```

Run: `mvn spotless:apply`

## Style Selection

| Style | Indent | Use For |
|-------|--------|---------|
| `GOOGLE` (default) | 2 spaces | Server-side Java projects |
| `AOSP` | 4 spaces | Android projects |

**Gradle:**
```groovy
googleJavaFormat().aosp()  // Use AOSP style
```

**Maven:**
```xml
<googleJavaFormat>
    <style>AOSP</style>
</googleJavaFormat>
```

## Common Configuration

### Complete Setup with All Features

**Gradle:**
```groovy
spotless {
    java {
        target 'src/**/*.java'
        googleJavaFormat('1.25.2').aosp()
        importOrder('java', 'javax', 'org', 'com', '')
        removeUnusedImports()
        formatAnnotations()
        licenseHeader '/* (C) $YEAR */'
    }
}
```

**Maven:** See references/maven.md for complete example.

## CI/CD Integration

Add to build pipeline to fail on formatting issues:

- **Gradle:** `./gradlew spotlessCheck`
- **Maven:** `mvn spotless:check`

## Gradual Enforcement (Ratchet)

Only format changed files, useful for legacy codebases:

```groovy
spotless {
    ratchetFrom 'origin/main'
    java { googleJavaFormat() }
}
```

## Toggle Formatting Off

Exclude sections from formatting:
```java
// spotless:off
public class LegacyCode { }
// spotless:on
```

## Reference Documentation

- **references/gradle.md** - Complete Gradle configuration, tasks, and options
- **references/maven.md** - Complete Maven configuration, goals, and options
- **references/google-java-format.md** - Google Java Format styles, IDE integration, formatting rules

## Tasks Quick Reference

| Build System | Apply Formatting | Check Formatting |
|--------------|------------------|------------------|
| Gradle | `spotlessApply` | `spotlessCheck` |
| Maven | `spotless:apply` | `spotless:check` |
