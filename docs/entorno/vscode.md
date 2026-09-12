# Trabajar con VS Code

**Alternativa permitida, no recomendada.** Si eliges este camino, esta página es tu responsabilidad, no la del profesor.

!!! danger "Antes de decidirte, lee esto"
    El aula tiene **IntelliJ IDEA Community**, y el examen se hace en el aula. Puedes preparar todo el curso en VS Code, pero el día de la prueba estarás delante de IntelliJ.

    Si vas a usar VS Code en casa, **trabaja también en el aula durante el curso**. No descubras en el examen que no sabes ejecutar un test con el ratón.

    Y si algo de tu configuración falla ese día, el tiempo corre igual.

## 1. Cuándo tiene sentido

Hay razones legítimas para elegirlo:

- Tu ordenador va justo de memoria (IntelliJ pide bastante).
- Ya lo usas para otros módulos y no quieres cambiar de teclas.
- Trabajas mucho en remoto por SSH o en contenedores.

Y una razón que no lo es: *«IntelliJ es muy complicado»*. Lo es los tres primeros días. Después es al revés.

## 2. Qué instalar

**El paquete imprescindible:** [Extension Pack for Java](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-java-pack) (Microsoft). Instala de una vez:

| Extensión | Para qué |
|---|---|
| Language Support for Java | Autocompletado, errores, navegación |
| Debugger for Java | Depuración |
| Test Runner for Java | Ejecutar JUnit |
| Maven for Java | Panel de Maven |
| Project Manager for Java | Gestión de proyectos |

**Añade además:**

| Extensión | Desde qué unidad |
|---|---|
| [Spring Boot Extension Pack](https://marketplace.visualstudio.com/items?itemName=vmware.vscode-boot-dev-pack) | UT4 |
| Gradle for Java | Si usas Gradle |
| Thymeleaf | UT8 |
| REST Client (o Thunder Client) | UT6, para probar la API |

## 3. Configuración mínima

`Ctrl+Shift+P` → *Preferences: Open User Settings (JSON)*:

```json
{
  "java.configuration.runtimes": [
    { "name": "JavaSE-25", "path": "/ruta/a/tu/jdk-25", "default": true }
  ],
  "java.compile.nullAnalysis.mode": "automatic",
  "java.format.settings.url": "https://raw.githubusercontent.com/google/styleguide/gh-pages/eclipse-java-google-style.xml",
  "editor.formatOnSave": true,
  "files.encoding": "utf8",
  "files.autoSave": "afterDelay",
  "java.test.defaultConfig": "default"
}
```

La ruta del JDK la averiguas con:

```bash
/usr/libexec/java_home -v 25     # macOS
which java && readlink -f $(which java)   # Linux
where java                        # Windows
```

## 4. Cómo se hace lo del día a día

| Tarea | Cómo |
|---|---|
| Abrir un proyecto | `File → Open Folder` y elegir la carpeta con el `pom.xml` |
| Crear un proyecto | `Ctrl+Shift+P` → *Java: Create Java Project* → Maven |
| Ejecutar | Enlace **Run** sobre el `main`, o `F5` |
| Depurar | Punto de ruptura en el margen + `F5` |
| Ejecutar un test | Icono ▷ junto al método, o panel *Testing* (matraz) |
| Ver Maven | Panel *Explorer* → sección **MAVEN** abajo |
| Terminal | `Ctrl+ñ` |
| Buscar en el proyecto | `Ctrl+Shift+F` |
| Renombrar | `F2` |
| Arreglo rápido | `Ctrl+.` (el equivalente a `Alt+Intro`) |
| Formatear | `Shift+Alt+F` |

Proyectos Spring Boot: se crean desde [start.spring.io](https://start.spring.io), o con `Ctrl+Shift+P` → *Spring Initializr: Create a Maven Project* si tienes el paquete de Spring.

## 5. Lo que vas a echar de menos

Con honestidad, para que decidas con información:

**El soporte de Spring no llega al mismo nivel.** IntelliJ Ultimate te avisa de una inyección que no se puede resolver *antes* de arrancar. En VS Code te enteras en el `ApplicationContext` que no levanta.

**La navegación es más torpe.** «Ir a la implementación de esta interfaz» o «ver todos los sitios donde se inyecta este bean» funcionan a medias.

**El refactor es limitado.** Renombrar va bien. Extraer un método o cambiar la firma de uno, regular. Y no propaga los cambios a las plantillas de Thymeleaf.

**El depurador es más básico.** No tiene puntos de ruptura condicionales tan cómodos, ni la evaluación de expresiones al vuelo con autocompletado.

**El servidor de lenguaje se atasca.** De vez en cuando marca errores donde no los hay. Se arregla con `Ctrl+Shift+P` → *Java: Clean Java Language Server Workspace* → reiniciar. Vas a usar ese comando bastante.

## 6. Problemas típicos

| Síntoma | Solución |
|---|---|
| Errores rojos por todas partes sin motivo | *Java: Clean Java Language Server Workspace* y reiniciar |
| No detecta el proyecto Maven | Abrir la carpeta **padre** del `pom.xml`, no el fichero |
| «The compiler compliance specified is 17 but a JRE 25 is used» | Revisar `java.configuration.runtimes` |
| Los tests no aparecen en el panel | Recargar la ventana; comprobar que están en `src/test/java` y acaban en `Test` |
| El autocompletado tarda muchísimo | Es normal al abrir; espera a que termine de indexar (barra de estado abajo) |
| Acentos rotos | `"files.encoding": "utf8"` y `-Dfile.encoding=UTF-8` en la configuración de ejecución |

## 7. La regla de oro si eliges este camino

**Todo tiene que funcionar desde la terminal.**

```bash
mvn clean test
mvn spring-boot:run
```

Si eso pasa, tu entrega es correcta independientemente del editor. Si solo funciona con los botones de VS Code, no está terminada.

Esa regla vale igual para quien use IntelliJ, pero aquí es más importante: es tu red de seguridad y lo que garantiza que el profesor pueda corregir tu proyecto.

## Antes de dar por bueno tu entorno

- [ ] `mvn -version` usa el JDK 25.
- [ ] Creas un proyecto, escribes un `main` y se ejecuta con `F5`.
- [ ] Pones un punto de ruptura y se detiene.
- [ ] Un test JUnit 5 aparece en el panel *Testing* y se ejecuta.
- [ ] `mvn clean test` funciona **desde la terminal**, fuera del editor.
- [ ] Has abierto al menos una vez tu proyecto en IntelliJ, en el aula, y sabes ejecutar los tests allí.
