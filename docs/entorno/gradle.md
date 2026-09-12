# Manual de Gradle

**No es la herramienta del módulo**, pero te la vas a encontrar: en ofertas de empleo, en proyectos de código abierto y, si tocas Android, en absolutamente todo.

La buena noticia es que **los conceptos son los mismos que en Maven**. Cambia la sintaxis y poco más.

## 1. La tabla de equivalencias

Tenla delante mientras lees el resto:

| Concepto | Maven | Gradle |
|---|---|---|
| Fichero de construcción | `pom.xml` | `build.gradle.kts` |
| Identidad del proyecto | `groupId`, `artifactId`, `version` | `group`, `rootProject.name`, `version` |
| Nombre del proyecto | en el `pom.xml` | `settings.gradle.kts` |
| Compilar | `mvn compile` | `gradle compileJava` |
| Tests | `mvn test` | `gradle test` |
| Empaquetar | `mvn package` | `gradle build` |
| Limpiar | `mvn clean` | `gradle clean` |
| Instalar en local | `mvn install` | `gradle publishToMavenLocal` |
| Árbol de dependencias | `mvn dependency:tree` | `gradle dependencies` |
| Ejecutar Spring Boot | `mvn spring-boot:run` | `gradle bootRun` |
| *Wrapper* | `mvnw` | `gradlew` |
| Salida | `target/` | `build/` |
| Ámbito de compilación | `compile` | `implementation` |
| Ámbito de test | `scope test` | `testImplementation` |
| Ámbito de ejecución | `scope runtime` | `runtimeOnly` |

**La estructura de carpetas es idéntica** (`src/main/java`, `src/test/java`…). Eso no cambia.

## 2. Los ficheros de un proyecto Gradle

``` { .text .sinajuste }
mi-proyecto/
├── build.gradle.kts        ← el equivalente al pom.xml
├── settings.gradle.kts     ← el nombre del proyecto y los módulos
├── gradle.properties       ← propiedades (versiones, memoria)
├── gradlew  gradlew.bat    ← el wrapper
├── gradle/wrapper/
├── src/                    ← igual que en Maven
└── build/                  ← la salida (equivale a target/)
```

!!! tip "`.kts` o sin extensión"
    Verás dos variantes: `build.gradle` (Groovy, la clásica) y `build.gradle.kts` (Kotlin, la moderna).

    **Usa siempre `.kts`.** Kotlin tiene tipos, así que el IDE te autocompleta y detecta errores antes de ejecutar. Con Groovy escribes a ciegas.

## 3. Un `build.gradle.kts` explicado

``` { .kotlin .numerado title="build.gradle.kts" }
// ① Qué sabe hacer este proyecto
plugins {
    java
    id("org.springframework.boot") version "3.4.1"
    id("io.spring.dependency-management") version "1.1.7"
}

// ② Identidad
group = "es.iesx.daw"
version = "1.0.0-SNAPSHOT"

// ③ Versión de Java
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

// ④ De dónde se descargan las librerías
repositories {
    mavenCentral()
}

// ⑤ Qué librerías
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    runtimeOnly("com.h2database:h2")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

// ⑥ Ajustes de tareas
tasks.withType<Test> {
    useJUnitPlatform()
    testLogging { events("passed", "skipped", "failed") }
}
```

**① Los *plugins*** son la diferencia conceptual más grande con Maven. En Gradle, un proyecto **no sabe hacer nada** hasta que le añades un *plugin*. `java` le enseña a compilar Java; el de Spring Boot le enseña `bootRun` y a generar un `.jar` ejecutable.

**③ El *toolchain*** es una ventaja real sobre Maven: si no tienes el JDK 25, **Gradle lo descarga solo**. No hace falta configurar nada en el sistema.

**⑤ Las dependencias** se escriben en una línea, con las tres coordenadas separadas por dos puntos:

```
grupo : artefacto : versión
"com.fasterxml.jackson.core:jackson-databind:2.18.2"
```

Sin versión, igual que en Maven: la pone el *plugin* de gestión de dependencias de Spring.

## 4. Las configuraciones (los *scopes* de Gradle)

| Gradle | Maven | Qué significa |
|---|---|---|
| `implementation` | `compile` | La necesitas, y **no la expones** a quien use tu librería |
| `api` | `compile` | La necesitas **y** la expones |
| `compileOnly` | `provided` | Solo al compilar |
| `runtimeOnly` | `runtime` | Solo al ejecutar (drivers de BD) |
| `testImplementation` | `test` | Solo en los tests |
| `testRuntimeOnly` | `test` + `runtime` | Solo al ejecutar los tests |
| `annotationProcessor` | — | Procesadores como Lombok o MapStruct |

!!! tip "`implementation` frente a `api`"
    Es la distinción que Maven no tiene y que hace a Gradle más rápido en proyectos grandes.

    Con `implementation`, si cambias esa librería, **solo se recompila tu módulo**. Con `api` se recompila también todo lo que dependa de ti.

    Regla práctica: usa `implementation` siempre, salvo que la librería aparezca en la firma pública de tus clases.

## 5. Las tareas

Donde Maven tiene un ciclo de vida fijo, Gradle tiene un **grafo de tareas** que se calcula al vuelo. Cada tarea declara de qué depende.

```bash
gradle tasks              # lista lo que puedes ejecutar
gradle tasks --all        # incluidas las internas
```

Las del día a día:

```bash
./gradlew clean                    # borra build/
./gradlew compileJava              # compila
./gradlew test                     # ejecuta los tests
./gradlew build                    # compila, testea y empaqueta
./gradlew clean build              # desde cero

# un test concreto
./gradlew test --tests ProductoServicioTest
./gradlew test --tests "UT3*"
./gradlew test --tests "ProductoServicioTest.crearFalla"

# Spring Boot
./gradlew bootRun
./gradlew bootRun --args='--spring.profiles.active=dev'
./gradlew bootJar                  # genera el .jar ejecutable

# información
./gradlew dependencies
./gradlew dependencyInsight --dependency jackson-databind
./gradlew properties

# depurar
./gradlew build --info
./gradlew build --stacktrace
./gradlew build --offline
```

!!! tip "El `dependencyInsight` no tiene equivalente cómodo en Maven"
    Responde a *«¿por qué tengo esta versión de esta librería y no otra?»* con el camino completo y el motivo del conflicto. Cuando pelees con versiones, es el mejor comando de Gradle.

## 6. Lo que Gradle hace mejor

**No repite trabajo.** Si no has tocado nada, no recompila:

```
> Task :compileJava UP-TO-DATE
> Task :test UP-TO-DATE
BUILD SUCCESSFUL in 412ms
```

**Compilación incremental.** Cambias una clase y recompila esa clase y lo que dependa de ella, no las 300.

**Demonio en segundo plano.** Se queda un proceso vivo entre ejecuciones, así que la segunda es mucho más rápida.

**Caché de construcción.** Puede reutilizar resultados de compilaciones anteriores, incluso de otro ordenador del equipo.

En un proyecto de clase la diferencia se nota poco. En uno de 500 clases, es la diferencia entre 8 segundos y 2 minutos.

## 7. Lo que Maven hace mejor

Por honestidad, porque no todo son ventajas:

**Es previsible.** El ciclo de vida es siempre el mismo. En Gradle, cualquiera puede escribir código en el fichero de construcción, y hay proyectos con `build.gradle.kts` de 400 líneas que no entiende nadie.

**El XML se lee sin saber programar.** Un `pom.xml` lo entiende cualquiera; un `build.gradle.kts` con lógica condicional, no.

**Hay más documentación y más respuestas.** Maven lleva veinte años; la mayoría de tutoriales de Spring usan Maven.

**Menos versiones incompatibles.** Actualizar Gradle a veces rompe *plugins*.

!!! note "Por eso el módulo usa Maven"
    En un contexto de aprendizaje, la previsibilidad vale más que la velocidad. Con Maven, todos los proyectos de la clase son iguales y la corrección automática funciona igual en todos.

    Pero saber Gradle es parte del oficio, y por eso está esta página.

## 8. El *wrapper*: `gradlew`

Igual que `mvnw`, y aquí se usa **todavía más**: en el mundo Gradle, ejecutar `gradle` directamente está mal visto porque cada proyecto necesita su versión concreta.

```bash
./gradlew build          # Linux / macOS
gradlew.bat build        # Windows
```

Si un proyecto trae `gradlew`, **no instales Gradle**. No hace falta.

```bash
./gradlew wrapper --gradle-version 8.12    # cambiar la versión del wrapper
chmod +x gradlew                            # si da "permission denied" tras clonar
```

## 9. Crear un proyecto Gradle

**Con Spring Boot:** en [start.spring.io](https://start.spring.io), elige **Gradle - Kotlin** en vez de Maven. Todo lo demás igual.

**Desde cero:**

```bash
mkdir mi-proyecto && cd mi-proyecto
gradle init --type java-application --dsl kotlin
```

**En IntelliJ:** `File → New → Project` → *Build system:* **Gradle** → *Gradle DSL:* **Kotlin**.

## 10. Traducir un proyecto de Maven a Gradle

Un ejemplo completo, para ver que es casi mecánico:

=== "pom.xml"

    ```xml
    <groupId>es.iesx.daw</groupId>
    <artifactId>tienda</artifactId>
    <version>1.0.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>25</maven.compiler.source>
    </properties>

    <dependencies>
        <dependency>
            <groupId>com.fasterxml.jackson.core</groupId>
            <artifactId>jackson-databind</artifactId>
            <version>2.18.2</version>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <version>5.11.4</version>
            <scope>test</scope>
        </dependency>
    </dependencies>
    ```

=== "build.gradle.kts"

    ```kotlin
    plugins { java }

    group = "es.iesx.daw"
    version = "1.0.0-SNAPSHOT"

    java {
        toolchain { languageVersion = JavaLanguageVersion.of(25) }
    }

    repositories { mavenCentral() }

    dependencies {
        implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
        testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    }

    tasks.withType<Test> { useJUnitPlatform() }
    ```

De 25 líneas de XML a 15 de Kotlin, con el mismo resultado.

!!! bug "`useJUnitPlatform()` no se olvida"
    Sin esa línea, Gradle intenta usar JUnit 4 y **tus tests de JUnit 5 no se ejecutan**. Lo peor es que no da error: dice `BUILD SUCCESSFUL` y "0 tests".

    Es el equivalente gradleño a los fallos silenciosos de Maven, y el primero que hay que mirar si «los tests no se lanzan».

## 11. Problemas típicos

| Error | Causa | Solución |
|---|---|---|
| `BUILD SUCCESSFUL` con 0 tests | Falta `useJUnitPlatform()` | Añadirlo en `tasks.withType<Test>` |
| `Unsupported class file major version` | Gradle usa un JDK distinto al del *toolchain* | `./gradlew -version` y revisar `JAVA_HOME` |
| `Could not resolve` | Falta `mavenCentral()` en `repositories` | Añadirlo |
| `Permission denied: ./gradlew` | Perdió el permiso al clonar | `chmod +x gradlew` |
| Comportamiento raro tras cambiar de rama | Caché sucia | `./gradlew clean build --refresh-dependencies` |
| El demonio consume mucha memoria | Normal en proyectos grandes | `./gradlew --stop` para pararlo |

## 12. El `.gitignore`

```gitignore title=".gitignore"
build/
.gradle/
!gradle/wrapper/gradle-wrapper.jar
*.iml
.idea/
.env
```

Mismo principio que con Maven: **la carpeta de salida nunca se sube ni se entrega**.

## Comprueba que lo tienes

1. Traduce a Gradle: `mvn clean test`, `mvn package`, `mvn spring-boot:run`.
2. ¿Cuál es el equivalente de `<scope>test</scope>`?
3. ¿Qué diferencia hay entre `implementation` y `api`, y por qué importa?
4. ¿Qué hace el bloque `plugins {}` y por qué es distinto de Maven?
5. ¿Qué ventaja tiene el `toolchain` frente a configurar el JDK a mano?
6. Tus tests no se ejecutan y la construcción dice `BUILD SUCCESSFUL`. ¿Qué falta?
7. ¿Por qué se usa `./gradlew` en vez de `gradle`?
8. Da dos razones por las que este módulo usa Maven y no Gradle.
