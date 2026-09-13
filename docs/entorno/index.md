# El entorno de trabajo del módulo

Antes de escribir una línea de Java hay que dejar clara una cosa: **con qué vas a trabajar durante 160 horas**. Esta página lo fija, y las siguientes son los manuales.

Léela entera en la primera sesión. Diez minutos aquí evitan tres semanas de «a mí no me funciona».

---

## 1. La decisión, sin rodeos

| | Entorno | Estado |
|---|---|---|
| :material-account-group: | **IntelliJ IDEA** (Community o Ultimate) | **El del módulo.** El que se usa en clase, en las prácticas y en los exámenes |
| | **Visual Studio Code** + Extension Pack for Java | Permitido, pero **bajo tu responsabilidad** |
| | Eclipse, NetBeans, Notepad, un IDE en la nube | No soportados |

!!! danger "Lo que significa «bajo tu responsabilidad»"
    Si trabajas con VS Code y algo no te funciona el día del examen, **el tiempo se te va igual**. No hay minutos extra por problemas de entorno, y el profesor no puede depurar una configuración que no es la de clase.

    Puedes usarlo. Pero entonces tienes que ser tú quien lo domine, no el profesor.

**Los ordenadores del aula llevan IntelliJ IDEA Community.** El examen se hace ahí. Si en casa usas otra cosa, asegúrate de haber trabajado también en el aula antes de la prueba: no descubras el día del examen que no sabes dónde está el botón de ejecutar.

## 2. Por qué IntelliJ y no otro

No es una cuestión de gustos. En un módulo de servidor con Spring Boot hay cuatro cosas que se usan a diario, y en IntelliJ vienen de serie:

**Entiende Spring.** Sabe qué es un *bean*, te avisa si una inyección no se puede resolver, y en el margen te pone un icono para saltar del controlador al servicio que usa. En un editor genérico eso es un `Ctrl+F` a ciegas.

**El depurador es de otra liga.** Puedes cambiar el valor de una variable a mitad de ejecución, poner un punto de ruptura que solo salte cuando `id == 42`, o volver atrás en la pila de llamadas. Vas a necesitarlo desde la UT4.

**Refactoriza de verdad.** Renombrar una clase actualiza los imports, las plantillas de Thymeleaf y hasta el nombre del fichero de test. Extraer un método, mover una clase de paquete, cambiar la firma de un método: todo con atajo y sin romper nada.

**Ejecuta los tests con un clic.** Verde y rojo en un panel, con el fallo enlazado a la línea exacta. En este módulo se corrige con tests: vas a vivir en ese panel.

Y hay un argumento que no es técnico y pesa igual: **es lo que vas a encontrar en la empresa**. La mayoría de equipos Java trabajan con IntelliJ. Aprender sus atajos ahora es tiempo invertido.

!!! tip "Community es suficiente"
    La versión **Community es gratuita** y cubre todo el módulo: Java, Maven, Gradle, JUnit, Git y Spring Boot vía Initializr web.

    La **Ultimate** añade soporte nativo de Spring, base de datos integrada, cliente HTTP y Thymeleaf con autocompletado. Es de pago, pero **es gratis para estudiantes**: se solicita en [jetbrains.com/community/education](https://www.jetbrains.com/community/education/) con el correo del centro y tarda un par de días.

    Merece la pena pedirla en la primera semana. No es obligatoria.

## 3. Qué instalar, y en qué orden

El orden importa: si instalas el IDE antes que el JDK, tendrás que configurarlo a mano después.

| Orden | Herramienta | Versión | Comprobación |
|:-:|---|---|---|
| 1.º | **JDK** ([Temurin](https://adoptium.net)) | **25 LTS** | `java -version` |
| 2.º | **IntelliJ IDEA Community** | La última | Abre y crea un proyecto |
| 3.º | **Git** | La última | `git --version` |
| 4.º | Cuenta de **GitHub** | — | Con el correo del centro |
| 5.º | **Maven** (opcional) | 3.9+ | `mvn -version` |
| 6.º | **Docker Desktop** | La última | `docker --version` · desde la UT5 |
| 7.º | **Bruno** | La última | `bru --version` · desde la UT6 |

!!! tip "Cliente HTTP: Bruno, y por qué"
    Para probar APIs usamos **[Bruno](https://www.usebruno.com)**. Es gratuito, no pide cuenta y —lo importante— **guarda las peticiones como ficheros de texto** dentro del propio repositorio, así que se versionan con Git igual que el código.

    | | |
    |---|---|
    |  **Bruno** | El del módulo. Ficheros `.bru` versionables, sin nube, sin cuenta |
    | **Insomnia** | Alternativa válida y muy parecida |
    | **Postman** | Permitido, pero pide cuenta y guarda las colecciones en su nube |
    |  `curl` | **Obligatorio saberlo**: es lo que se usa para corregir |

    Que las peticiones vivan en el repositorio no es un detalle: significa que puedes entregar tu colección con el proyecto y que el profesor la ejecute tal cual.

!!! note "Maven es opcional… y aun así conviene"
    Los proyectos traen el *wrapper* (`mvnw`), que descarga la versión correcta solo. Con eso basta para trabajar.

    Pero tener Maven instalado te permite lanzar `mvn` desde cualquier carpeta, y sobre todo **entender qué está pasando** en vez de ejecutar un comando mágico. En el examen se pregunta por el ciclo de vida de Maven, no por el *wrapper*.

## 4. Comprobación de la primera sesión

Copia esto en una terminal y compara la salida. Si algo falla, arréglalo **hoy**, no la semana que viene.

```bash
java -version      # debe decir 25.x.x
javac -version     # debe decir 25.x.x  ← ojo, no siempre coincide con el anterior
git --version
```

!!! warning "El fallo más común de la primera semana"
    `java -version` dice 25 y `javac -version` dice 17. Significa que tienes varios JDK instalados y el `PATH` apunta a distintos sitios.

    Se arregla con `JAVA_HOME` bien puesto. En el [manual de IntelliJ](intellij.md) está explicado paso a paso, y en clase se resuelve en dos minutos: pregúntalo en la S1 en vez de arrastrarlo un mes.

## 5. Maven y Gradle: por qué los dos

Las dos son **herramientas de construcción**: descargan las librerías, compilan, ejecutan los tests y empaquetan el `.jar`. Hacen lo mismo por caminos distintos.

| | Maven | Gradle |
|---|---|---|
| Configuración | XML (`pom.xml`) | Kotlin o Groovy (`build.gradle.kts`) |
| Filosofía | Convención: todo igual en todas partes | Flexibilidad: puedes programar la construcción |
| Curva de aprendizaje | **Suave** | Más pronunciada |
| Velocidad | Correcta | **Mejor** en proyectos grandes (caché y compilación incremental) |
| En el mundo Spring | Mayoritario | Frecuente, y creciendo |
| En Android | Casi nunca | **El estándar** |

**En este módulo usamos Maven** en clase, en las prácticas y en los exámenes. Es más previsible, el `pom.xml` se lee sin saber programar y la estructura es idéntica en todos los proyectos, lo que simplifica la corrección.

**Y también aprendes Gradle**, porque te lo vas a encontrar. En muchas ofertas de empleo aparece, y en Android no hay alternativa. El [manual de Gradle](gradle.md) enseña lo mismo que el de Maven, traducido: si entiendes uno, el otro es cuestión de sintaxis.

!!! tip "El truco para no liarte"
    Los conceptos son los mismos y solo cambia el nombre:

    | Concepto | Maven | Gradle |
    |---|---|---|
    | Fichero de construcción | `pom.xml` | `build.gradle.kts` |
    | Identificador del proyecto | `groupId` + `artifactId` | `group` + `name` |
    | Compilar | `mvn compile` | `gradle compileJava` |
    | Ejecutar tests | `mvn test` | `gradle test` |
    | Empaquetar | `mvn package` | `gradle build` |
    | Limpiar | `mvn clean` | `gradle clean` |

### El mismo proyecto, en los dos

Exactamente el mismo proyecto —Java 25, Jackson, JUnit— escrito con cada herramienta. Cambia de pestaña y compara:

=== "Maven"

    ``` xml title="pom.xml"
    <project>
        <modelVersion>4.0.0</modelVersion>

        <groupId>es.iesx.daw</groupId>
        <artifactId>tienda</artifactId>
        <version>1.0.0-SNAPSHOT</version>

        <properties>
            <maven.compiler.release>25</maven.compiler.release>
            <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
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
    </project>
    ```

    ``` bash
    mvn clean         # borrar target/
    mvn compile       # compilar
    mvn test          # ejecutar los tests
    mvn package       # generar el .jar
    ```

=== "Gradle"

    ``` kotlin title="build.gradle.kts"
    plugins {
        java
    }

    group = "es.iesx.daw"
    version = "1.0.0-SNAPSHOT"

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(25)
        }
    }

    repositories {
        mavenCentral()
    }

    dependencies {
        implementation("com.fasterxml.jackson.core:jackson-databind:2.18.2")
        testImplementation("org.junit.jupiter:junit-jupiter:5.11.4")
    }

    tasks.test {
        useJUnitPlatform()
    }
    ```

    ``` bash
    gradle clean          # borrar build/
    gradle compileJava    # compilar
    gradle test           # ejecutar los tests
    gradle build          # generar el .jar
    ```

!!! info "Las pestañas están enlazadas"
    Si eliges «Gradle» aquí, **todas** las pestañas Maven/Gradle de la web se cambian a Gradle a la vez. Elige una vez y sigue leyendo.

## 6. En esta sección

<div class="grid cards" markdown>

- **[Manual de IntelliJ IDEA](intellij.md)**
  Instalación, primer proyecto, los atajos que hay que memorizar, el depurador, ejecutar tests y trabajar con Spring Boot.

- **[Trabajar con VS Code](vscode.md)**
  Si eliges la alternativa: extensiones necesarias, configuración mínima y las cinco cosas que vas a echar de menos.

- **[Manual de Maven](maven.md)**
  El `pom.xml` por dentro, el ciclo de vida, dependencias, *scopes*, perfiles, el *wrapper* y cómo salir de los problemas típicos.

- **[Manual de Gradle](gradle.md)**
  Lo mismo en Gradle, con la tabla de equivalencias siempre a la vista.

- **[Chuleta de comandos](comandos.md)**
  Todos los comandos de Maven y Gradle para Spring Boot, uno al lado del otro: arrancar con perfiles, ejecutar un solo test, mirar el árbol de dependencias, construir la imagen de Docker y qué escribir cuando algo falla.

</div>

## 7. Reglas del módulo sobre el entorno

1. **El proyecto se entrega con su fichero de construcción**, sea `pom.xml` o `build.gradle.kts`. Un `.zip` con solo los `.java` no se corrige.
2. **Nunca se entrega la carpeta `target/` ni `build/`.** Van en el `.gitignore`.
3. **El proyecto tiene que compilar desde la terminal**, no solo desde el IDE: `mvn clean test` o `./gradlew clean test`. Si funciona en tu IntelliJ pero no en la terminal, no está terminado.
4. **Nada de rutas absolutas** de tu ordenador en la configuración. `C:\Users\pepe\...` en un `application.yml` es un suspenso de portabilidad.
5. **El JDK del proyecto es el 25.** Un proyecto compilado con otro no arranca en el aula.
