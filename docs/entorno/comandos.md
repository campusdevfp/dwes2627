# Chuleta de comandos: Maven y Gradle con Spring Boot

Todo lo que se ejecuta en este módulo, en un sitio. Cada bloque trae la orden en las dos herramientas: **elige una pestaña y toda la página cambia**.

!!! tip "Usa el *wrapper*"
    Si el proyecto trae `mvnw` o `gradlew`, úsalos: fijan la versión de la herramienta, así que a ti y al profesor os construye igual.

    ```bash
    ./mvnw clean test        # Linux y macOS
    mvnw.cmd clean test      # Windows (cmd)
    .\mvnw.cmd clean test    # Windows (PowerShell)
    ```

    En el resto de la página se escribe `mvn` y `gradle` para no repetir, pero **léelo siempre como `./mvnw` y `./gradlew`**.

---

## 1. Lo del día a día

=== "Maven"

    ``` { .bash .numerado }
    mvn clean                 # borrar target/
    mvn compile               # compilar src/main/java
    mvn test                  # compilar y ejecutar los tests
    mvn package               # generar el .jar (ejecuta los tests antes)
    mvn verify                # package + tests de integración + comprobaciones
    mvn install               # verify + copiar el .jar a ~/.m2 para otros proyectos

    mvn clean test            # lo más usado mientras programas
    mvn clean package         # lo que se ejecuta antes de entregar
    ```

=== "Gradle"

    ``` { .bash .numerado }
    gradle clean              # borrar build/
    gradle compileJava        # compilar src/main/java
    gradle test               # ejecutar los tests
    gradle assemble           # generar el .jar SIN ejecutar tests
    gradle build              # assemble + check (compila, prueba y empaqueta)
    gradle publishToMavenLocal   # el equivalente a «mvn install»

    gradle clean test         # lo más usado mientras programas
    gradle clean build        # lo que se ejecuta antes de entregar
    ```

!!! warning "`package` y `build` no son lo mismo"
    En Maven, **`package` ejecuta los tests** por el camino: son fases del mismo ciclo.

    En Gradle, `assemble` **solo empaqueta** y `build` es el que además prueba. Si quieres el equivalente a `mvn package`, es `gradle build`.

---

## 2. Ejecutar la aplicación

=== "Maven"

    ``` { .bash .numerado }
    mvn spring-boot:run                     # arrancar
    mvn spring-boot:run -Dspring-boot.run.profiles=dev
    mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=9090"

    # varias propiedades a la vez
    mvn spring-boot:run \
        -Dspring-boot.run.arguments="--server.port=9090 --spring.jpa.show-sql=true"

    # con opciones de la JVM
    mvn spring-boot:run -Dspring-boot.run.jvmArguments="-Xmx512m"

    # o directamente, si ya has empaquetado
    java -jar target/comercios-1.0.0.jar
    java -jar target/comercios-1.0.0.jar --spring.profiles.active=dev
    ```

=== "Gradle"

    ``` { .bash .numerado }
    gradle bootRun                          # arrancar
    gradle bootRun --args='--spring.profiles.active=dev'
    gradle bootRun --args='--server.port=9090'

    # varias propiedades a la vez
    gradle bootRun --args='--server.port=9090 --spring.jpa.show-sql=true'

    # con opciones de la JVM (en build.gradle.kts)
    # tasks.bootRun { jvmArgs = listOf("-Xmx512m") }

    # o directamente, si ya has empaquetado
    java -jar build/libs/comercios-1.0.0.jar
    java -jar build/libs/comercios-1.0.0.jar --spring.profiles.active=dev
    ```

!!! tip "El perfil, de tres maneras"
    Las tres valen y se pisan en este orden (gana la última):

    ```bash
    # 1. en application.yml
    spring.profiles.active: dev

    # 2. variable de entorno — la de Docker
    SPRING_PROFILES_ACTIVE=prod java -jar app.jar

    # 3. argumento — la que manda
    java -jar app.jar --spring.profiles.active=prod
    ```

---

## 3. Tests

=== "Maven"

    ``` { .bash .numerado }
    mvn test                                    # todos

    mvn test -Dtest=ComercioServiceTest         # una clase
    mvn test -Dtest=ComercioServiceTest#ingiere # un método
    mvn test -Dtest='*ServiceTest'              # por patrón
    mvn test -Dtest='ComercioTest,PedidoTest'   # varias

    mvn test -Dgroups=lento                     # por @Tag de JUnit 5
    mvn test -DexcludedGroups=lento

    mvn test -DfailIfNoTests=false              # no fallar si el filtro no casa
    mvn test -Dmaven.test.failure.ignore=true   # seguir aunque haya rojos

    mvn package -DskipTests                     # compila los tests, no los ejecuta
    mvn package -Dmaven.test.skip=true          # ni siquiera los compila

    mvn verify                                  # incluye los *IT.java (failsafe)
    ```

=== "Gradle"

    ``` { .bash .numerado }
    gradle test                                     # todos

    gradle test --tests ComercioServiceTest         # una clase
    gradle test --tests '*ComercioServiceTest.ingiere'   # un método
    gradle test --tests '*ServiceTest'              # por patrón

    # por @Tag, en build.gradle.kts:
    # tasks.test { useJUnitPlatform { includeTags("lento") } }

    gradle test --continue                          # seguir aunque haya rojos
    gradle test --rerun-tasks                       # ignorar la caché y repetir

    gradle build -x test                            # saltarse los tests

    gradle check                                    # test + análisis estático
    ```

!!! danger "`-DskipTests` y `-Dmaven.test.skip` no son lo mismo"
    - **`-DskipTests`**: compila los tests pero no los ejecuta. Si un test no compila, **te enteras**.
    - **`-Dmaven.test.skip=true`**: ni los compila. Es más rápido y **esconde tests rotos**.

    Usa el primero. El segundo, solo cuando sepas exactamente por qué.

    Y ninguno de los dos en una entrega: si hay que saltarse los tests para empaquetar, el proyecto no está terminado.

---

## 4. Ver el informe de los tests

=== "Maven"

    ``` { .bash .numerado }
    # los informes quedan aquí
    target/surefire-reports/          # tests unitarios
    target/failsafe-reports/          # tests de integración (*IT.java)

    # el resumen legible
    cat target/surefire-reports/*.txt

    # el XML es el que leen las herramientas de corrección
    ls target/surefire-reports/TEST-*.xml
    ```

=== "Gradle"

    ``` { .bash .numerado }
    # informe HTML, que se abre en el navegador
    build/reports/tests/test/index.html

    # el XML, mismo formato que surefire
    ls build/test-results/test/TEST-*.xml
    ```

---

## 5. Dependencias: qué tienes de verdad

=== "Maven"

    ``` { .bash .numerado }
    mvn dependency:tree                   # el árbol completo
    mvn dependency:tree -Dincludes=com.fasterxml.jackson.core:*
    mvn dependency:tree -Dverbose         # incluye las descartadas por conflicto

    mvn dependency:analyze                # declaradas y no usadas / usadas sin declarar
    mvn dependency:list                   # lista plana
    mvn dependency:resolve                # descargar todo sin compilar
    mvn dependency:purge-local-repository # borrar las de este proyecto de ~/.m2

    mvn help:effective-pom                # el pom REAL, con lo heredado del parent
    mvn versions:display-dependency-updates   # qué se puede actualizar
    mvn versions:display-plugin-updates
    ```

=== "Gradle"

    ``` { .bash .numerado }
    gradle dependencies                                  # todas las configuraciones
    gradle dependencies --configuration runtimeClasspath # solo la de ejecución
    gradle dependencyInsight --dependency jackson-databind   # quién la mete y por qué

    gradle :app:dependencies                             # en proyectos con módulos

    # con el plugin com.github.ben-manes.versions
    gradle dependencyUpdates

    gradle properties                                    # propiedades del proyecto
    ```

!!! tip "La orden que más sorprende"
    `mvn dependency:tree` en un proyecto Spring Boot normal saca **más de cien líneas**. Casi ninguna la has escrito tú: vienen debajo de los *starters*.

    Merece la pena ejecutarla una vez y buscar `jackson-databind`, `logback` o `snakeyaml`. Ahí se entiende por qué una vulnerabilidad en una librería que no conocías te afecta.

---

## 6. Empaquetar y desplegar

=== "Maven"

    ``` { .bash .numerado }
    mvn clean package                     # genera target/*.jar

    # imagen de Docker SIN escribir Dockerfile (Cloud Native Buildpacks)
    mvn spring-boot:build-image
    mvn spring-boot:build-image -Dspring-boot.build-image.imageName=comercios:1.0

    # ver qué hay dentro del jar
    jar tf target/comercios-1.0.0.jar | head -30

    # las capas, para un Dockerfile eficiente
    java -Djarmode=tools -jar target/comercios-1.0.0.jar list-layers
    ```

=== "Gradle"

    ``` { .bash .numerado }
    gradle clean build                    # genera build/libs/*.jar

    # imagen de Docker SIN escribir Dockerfile
    gradle bootBuildImage
    gradle bootBuildImage --imageName=comercios:1.0

    # ver qué hay dentro del jar
    jar tf build/libs/comercios-1.0.0.jar | head -30

    gradle bootJar                        # solo el jar ejecutable
    gradle jar                             # el jar «normal», sin dependencias
    ```

!!! info "`build-image` no necesita Dockerfile"
    Spring Boot usa *Cloud Native Buildpacks*: detecta que es una aplicación Java, elige un JDK, monta las capas y produce una imagen optimizada.

    Es más lento la primera vez y produce una imagen mejor que un `Dockerfile` escrito a la ligera. Necesita que Docker esté arrancado.

---

## 7. Perfiles de construcción

=== "Maven"

    ``` { .bash .numerado }
    mvn test -Pintegracion                # activar un perfil del pom
    mvn test -P!lento                     # desactivarlo
    mvn help:active-profiles              # cuáles están activos y por qué
    ```

    ```xml title="pom.xml"
    <profiles>
      <profile>
        <id>integracion</id>
        <build><plugins>
          <plugin>
            <artifactId>maven-failsafe-plugin</artifactId>
            <executions><execution><goals>
              <goal>integration-test</goal><goal>verify</goal>
            </goals></execution></executions>
          </plugin>
        </plugins></build>
      </profile>
    </profiles>
    ```

=== "Gradle"

    ``` { .bash .numerado }
    gradle test -Pintegracion             # propiedad de proyecto
    gradle integrationTest                # o una tarea propia, que es lo habitual
    ```

    ```kotlin title="build.gradle.kts"
    val integrationTest by tasks.registering(Test::class) {
        useJUnitPlatform()
        include("**/*IT.class")
        shouldRunAfter(tasks.test)
    }
    ```

!!! warning "No confundas los perfiles"
    - **Perfil de Maven o Gradle**: afecta a **cómo se construye** el proyecto. Qué plugins corren, qué dependencias entran.
    - **Perfil de Spring** (`dev`, `prod`): afecta a **cómo se ejecuta**. Qué `application-*.yml` se lee, qué beans se crean.

    Son cosas distintas y se preguntan juntas en el examen.

---

## 8. Cuando algo falla

=== "Maven"

    ``` { .bash .numerado }
    mvn -X clean test                     # salida de depuración completa
    mvn -e clean test                     # con la traza de la excepción
    mvn -o clean test                     # sin conexión: solo lo que hay en ~/.m2
    mvn -U clean test                     # forzar la búsqueda de actualizaciones

    mvn dependency:purge-local-repository  # descargas corruptas en ~/.m2
    rm -rf ~/.m2/repository/org/springframework   # el martillo, si nada funciona

    mvn -version                          # versión de Maven Y del JDK que usa
    mvn help:system                       # todas las propiedades del sistema
    ```

=== "Gradle"

    ``` { .bash .numerado }
    gradle --debug clean test             # salida de depuración completa
    gradle --stacktrace clean test        # con la traza
    gradle --offline clean test           # sin conexión
    gradle --refresh-dependencies build   # ignorar la caché de dependencias

    gradle --stop                         # matar el demonio (soluciona rarezas)
    gradle clean build --no-daemon        # construir sin demonio

    gradle -version                       # versión de Gradle Y del JDK
    gradle tasks --all                    # todas las tareas disponibles
    ```

!!! tip "Los tres primeros diagnósticos"
    1. **`mvn -version` o `gradle -version`.** Comprueba **qué JDK está usando de verdad**. Que `java -version` diga 25 no significa que Maven use el 25: mira `JAVA_HOME`.
    2. **Modo sin conexión.** Si con `-o` funciona y sin él no, el problema es la red o un repositorio, no tu código.
    3. **La primera línea del error, no la última.** Las trazas de Maven son largas y el motivo real está arriba.

---

## 9. El *wrapper*

=== "Maven"

    ``` { .bash .numerado }
    # añadirlo a un proyecto que no lo tiene
    mvn wrapper:wrapper
    mvn wrapper:wrapper -Dmaven=3.9.9     # fijando la versión

    # usarlo
    ./mvnw clean test
    ```

    Se versionan: `mvnw`, `mvnw.cmd` y `.mvn/wrapper/maven-wrapper.properties`.

=== "Gradle"

    ``` { .bash .numerado }
    # añadirlo o actualizarlo
    gradle wrapper
    gradle wrapper --gradle-version 8.12

    # usarlo
    ./gradlew clean build
    ```

    Se versionan: `gradlew`, `gradlew.bat` y todo `gradle/wrapper/`.

!!! danger "En Linux y macOS, permisos de ejecución"
    Si al clonar sale `Permission denied`:

    ```bash
    chmod +x mvnw gradlew
    git update-index --chmod=+x mvnw       # para que se quede guardado en Git
    ```

---

## 10. Crear un proyecto desde cero

=== "Maven"

    ``` { .bash .numerado }
    # con Spring Initializr desde la consola
    curl https://start.spring.io/starter.zip \
      -d dependencies=web,data-jpa,postgresql,validation \
      -d type=maven-project \
      -d javaVersion=25 \
      -d bootVersion=3.5.16 \
      -d groupId=es.iesx.daw \
      -d artifactId=comercios \
      -d name=comercios \
      -d packageName=es.iesx.daw.comercios \
      -o comercios.zip

    unzip comercios.zip -d comercios && cd comercios
    ```

=== "Gradle"

    ``` { .bash .numerado }
    curl https://start.spring.io/starter.zip \
      -d dependencies=web,data-jpa,postgresql,validation \
      -d type=gradle-project-kotlin \
      -d javaVersion=25 \
      -d bootVersion=3.5.16 \
      -d groupId=es.iesx.daw \
      -d artifactId=comercios \
      -d name=comercios \
      -d packageName=es.iesx.daw.comercios \
      -o comercios.zip

    unzip comercios.zip -d comercios && cd comercios
    ```

!!! tip "Ver las opciones disponibles"
    ```bash
    curl https://start.spring.io           # la lista entera, en texto
    ```

---

## 11. Calidad y seguridad

=== "Maven"

    ``` { .bash .numerado }
    # vulnerabilidades conocidas en las dependencias
    mvn org.owasp:dependency-check-maven:check
    # informe en target/dependency-check-report.html

    # cobertura con JaCoCo
    mvn test jacoco:report
    # informe en target/site/jacoco/index.html

    # mutation testing: ¿tus tests detectan fallos de verdad?
    mvn org.pitest:pitest-maven:mutationCoverage
    # informe en target/pit-reports/
    ```

=== "Gradle"

    ``` { .bash .numerado }
    # con el plugin org.owasp.dependencycheck
    gradle dependencyCheckAnalyze
    # informe en build/reports/dependency-check-report.html

    # con el plugin jacoco
    gradle test jacocoTestReport
    # informe en build/reports/jacoco/test/html/index.html

    # con el plugin info.solidsoft.pitest
    gradle pitest
    ```

!!! info "PIT: la que mide si tus tests sirven"
    La cobertura dice **qué líneas se ejecutan**, no si se comprueban. Un test sin ni una aserción da 100 % de cobertura.

    PIT introduce fallos a propósito en tu código —cambia un `<` por un `<=`, borra una llamada— y mira si algún test se pone rojo. Si el fallo sobrevive, ahí tienes un agujero.

---

## Ejercicios (con solución)

### Ejercicio 1 — `package` y `build`

¿Por qué `mvn package` ejecuta los tests y `gradle assemble` no?

??? success "Solución"

    Porque los dos organizan el trabajo de manera distinta.

    **Maven tiene un ciclo de vida con fases en orden fijo**: `validate → compile → test → package → verify → install → deploy`. Pedir una fase ejecuta **todas las anteriores**. Como `test` va antes que `package`, no hay forma de empaquetar sin haber pasado por los tests, salvo saltándolos a mano.

    **Gradle tiene un grafo de tareas con dependencias declaradas.** `assemble` depende de lo necesario para construir el artefacto, y probar no lo es. La tarea que agrupa las dos cosas es `build`:

    ``` { .text .sinajuste }
    build ─┬─ assemble ── jar ── classes
           └─ check ──── test
    ```

    La equivalencia práctica:

    | Maven | Gradle |
    |---|---|
    | `mvn package` | `gradle build` |
    | `mvn package -DskipTests` | `gradle assemble` |
    | `mvn test` | `gradle test` |

### Ejercicio 2 — Un solo test

Ejecuta solo el método `ingiereSinDuplicar` de la clase `IngestaServiceTest`, en las dos herramientas.

??? success "Solución"

    === "Maven"

        ```bash
        mvn test -Dtest=IngestaServiceTest#ingiereSinDuplicar
        ```

    === "Gradle"

        ```bash
        gradle test --tests '*IngestaServiceTest.ingiereSinDuplicar'
        ```

    Dos detalles que hacen falta:

    - En Maven el separador de método es **`#`**; en Gradle, un **punto**.
    - En Gradle hay que poner la clase completa o usar `*` delante, porque espera el nombre con paquete.

    Y si el filtro no casa con nada:

    - Maven **falla** con «No tests were executed». Se desactiva con `-DfailIfNoTests=false`.
    - Gradle también falla, y ahí sí es útil: te avisa de que te has equivocado escribiendo el nombre.

### Ejercicio 3 — El perfil `dev`

Arranca la aplicación con el perfil `dev` y el puerto 9090, en las dos herramientas.

??? success "Solución"

    === "Maven"

        ```bash
        mvn spring-boot:run \
            -Dspring-boot.run.profiles=dev \
            -Dspring-boot.run.arguments="--server.port=9090"
        ```

    === "Gradle"

        ```bash
        gradle bootRun --args='--spring.profiles.active=dev --server.port=9090'
        ```

    Y si ya tienes el `.jar`, la forma que sirve para las dos y es la que usarás al desplegar:

    ```bash
    java -jar target/comercios-1.0.0.jar --spring.profiles.active=dev --server.port=9090
    ```

    **La sintaxis de Maven es más pesada** porque hay que decirle al plugin qué pasarle a la aplicación; Gradle lo pasa entero con `--args`.

### Ejercicio 4 — De dónde sale `snakeyaml`

Aparece `snakeyaml` en tu proyecto y tú no lo has puesto. Averigua quién lo mete.

??? success "Solución"

    === "Maven"

        ```bash
        mvn dependency:tree -Dincludes=org.yaml:snakeyaml
        ```

    === "Gradle"

        ```bash
        gradle dependencyInsight --dependency snakeyaml
        ```

    Salida típica en Maven:

    ``` { .text .sinajuste }
    [INFO] es.iesx.daw:comercios:jar:1.0.0
    [INFO] \- org.springframework.boot:spring-boot-starter:jar:3.5.16:compile
    [INFO]    \- org.yaml:snakeyaml:jar:2.3:compile
    ```

    Es decir: lo trae `spring-boot-starter`, porque es lo que sabe leer los `application.yml`.

    **Por qué importa saber hacerlo:** cuando salga una vulnerabilidad en una librería que no conoces, esta es la orden con la que averiguas si te afecta y quién la mete. Y `dependencyInsight` de Gradle además explica **por qué se eligió esa versión** cuando hay conflicto.

### Ejercicio 5 — Modo sin conexión

Estás en el aula sin internet y la construcción falla. ¿Qué pruebas?

??? success "Solución"

    === "Maven"

        ```bash
        mvn -o clean test
        ```

    === "Gradle"

        ```bash
        gradle --offline clean test
        ```

    Funciona **si ya te habías descargado todo antes**: Maven en `~/.m2/repository` y Gradle en `~/.gradle/caches`.

    Y sirve de diagnóstico: **si con `-o` construye y sin él no**, el problema es la red o un repositorio, no tu proyecto. Es información útil antes de ponerse a tocar el `pom.xml`.

    Para no depender de la suerte, el día antes de un examen:

    ```bash
    mvn dependency:go-offline        # descargar todo lo necesario
    gradle build --refresh-dependencies
    ```

### Ejercicio 6 — La construcción va lenta

La construcción tarda tres minutos y antes tardaba treinta segundos. ¿Qué miras?

??? success "Solución"

    **Primero, dónde se va el tiempo**, que casi siempre es en los tests:

    === "Maven"

        ```bash
        mvn clean test | grep -E "Tests run|Time elapsed"
        cat target/surefire-reports/*.txt | grep "Time elapsed" | sort -k3 -rn | head
        ```

    === "Gradle"

        ```bash
        gradle clean test --profile
        # informe en build/reports/profile/
        gradle clean build --scan          # análisis completo en la web
        ```

    Las tres causas habituales, por frecuencia:

    1. **Tests que levantan el contexto de Spring.** Cada `@SpringBootTest` con configuración distinta arranca un contexto nuevo, y eso son segundos. Se arregla usando `@WebMvcTest` o `@DataJpaTest`, que levantan solo una rodaja.
    2. **Tests que salen a la red.** Lo que se ve en la UT9: si un test llama a una API de verdad, hereda su latencia.
    3. **Gradle sin demonio, o con la caché desactivada.** `gradle --stop` y volver a construir a veces lo arregla solo.

    Y en Gradle, dos ajustes que se notan:

    ```properties title="gradle.properties"
    org.gradle.caching=true
    org.gradle.parallel=true
    ```
