# Manual de IntelliJ IDEA

El entorno del módulo. Esta página es para volver a ella, no para memorizarla de una sentada.

## 1. Instalación

**Descarga:** [jetbrains.com/idea/download](https://www.jetbrains.com/idea/download/) → pestaña **Community Edition** (está más abajo en la página; la Ultimate es la que se anuncia arriba).

| Sistema | Cómo |
|---|---|
| Windows | Instalador `.exe`. Marca *«Add bin folder to PATH»* y *«Create Desktop Shortcut»* |
| macOS | `.dmg` → arrastrar a Aplicaciones. Elige `Apple Silicon` o `Intel` según tu Mac |
| Linux | `.tar.gz` y ejecutar `bin/idea.sh`, o desde el gestor de paquetes: `sudo snap install intellij-idea-community --classic` |

**Al primer arranque** te preguntará por el tema y por los complementos. Salta todo, se cambia después en `File → Settings`.

### La licencia de estudiante (recomendado)

Con el correo del centro puedes pedir gratis la **Ultimate**, que añade soporte de Spring, cliente HTTP integrado y autocompletado en Thymeleaf:

1. Ve a [jetbrains.com/community/education](https://www.jetbrains.com/community/education/) → *Apply now* → *University student*.
2. Rellena con tu correo del instituto.
3. Confirma el correo de verificación. Suele tardar entre un día y tres.
4. En IntelliJ: `Help → Register` → inicia sesión con tu cuenta.

No es obligatoria. **Con Community se puede hacer el módulo entero.**

## 2. Configurar el JDK

Es lo primero que hay que dejar bien, y donde más gente se atasca.

`File → Project Structure → Project` (o `Ctrl+Alt+Shift+S`):

- **SDK:** debe poner `25`. Si no aparece, despliega → `Add SDK → Download JDK` → *Version 25*, *Vendor: Eclipse Temurin* → `Download`.
- **Language level:** `25`.

!!! bug "`java -version` dice 25 pero `javac -version` dice 17"
    Tienes varios JDK y el `PATH` está mezclado. En el sistema:

    === "Windows"
        Panel de control → *Variables de entorno* → crea `JAVA_HOME` apuntando a la carpeta del JDK 25 (sin `\bin`), y en `Path` pon `%JAVA_HOME%\bin` **la primera**.

    === "macOS / Linux"
        En `~/.zshrc` o `~/.bashrc`:
        ```bash
        export JAVA_HOME=$(/usr/libexec/java_home -v 25)   # macOS
        export PATH="$JAVA_HOME/bin:$PATH"
        ```
        Después: `source ~/.zshrc`.

    Y en IntelliJ, el JDK del proyecto es **independiente** del sistema: aunque el `PATH` esté mal, el IDE puede compilar bien. Por eso hay que comprobar los dos.

## 3. Tu primer proyecto

### Proyecto simple (UT2 y UT3)

`File → New → Project`:

- **Name:** el que sea, sin espacios ni tildes.
- **Language:** Java · **Build system:** **Maven** · **JDK:** 25.
- Desmarca *«Add sample code»* si prefieres empezar limpio.

Estructura resultante:

``` { .text .sinajuste }
mi-proyecto/
├── pom.xml
└── src/
    ├── main/java/          ← tu código
    ├── main/resources/     ← configuración, ficheros de datos
    └── test/java/          ← los tests
```

### Proyecto Spring Boot (desde la UT4)

Con **Community** se usa el Initializr web:

1. [start.spring.io](https://start.spring.io)
2. Maven · Java · Spring Boot 3.x · **Java 25** · Jar
3. Dependencias: `Spring Web`, `Spring Boot DevTools`, `Validation`… (según la unidad)
4. `Generate` → descarga el `.zip` → descomprímelo
5. En IntelliJ: `File → Open` y selecciona **la carpeta**, no el `pom.xml`

Con **Ultimate**: `File → New → Project → Spring Boot` y todo lo anterior desde el IDE.

!!! warning "`File → Open`, no `Import`"
    Selecciona la carpeta del proyecto. IntelliJ detecta el `pom.xml` y lo importa solo. Si abres el `pom.xml` directamente te pregunta *«Open as project or as file?»*: elige **project**.

## 4. Los atajos que hay que memorizar

Empieza por los diez primeros. En dos semanas te salen solos y programarás bastante más rápido.

| Atajo (Win/Linux) | macOS | Qué hace |
|---|---|---|
| `Shift Shift` | `Shift Shift` | **Buscar cualquier cosa**: fichero, clase, acción, ajuste |
| `Ctrl+N` | `⌘+O` | Ir a una clase por nombre |
| `Ctrl+Shift+N` | `⌘+⇧+O` | Ir a un fichero por nombre |
| `Ctrl+B` | `⌘+B` | Ir a la definición (o `Ctrl`+clic) |
| `Alt+F7` | `⌥+F7` | Buscar dónde se usa esto |
| `Ctrl+Alt+L` | `⌘+⌥+L` | **Formatear el código** |
| `Ctrl+Alt+O` | `⌃+⌥+O` | Ordenar y limpiar los imports |
| `Shift+F10` | `⌃+R` | Ejecutar lo último |
| `Shift+F9` | `⌃+D` | Depurar lo último |
| `Ctrl+Shift+F10` | `⌃+⇧+R` | Ejecutar **esto** (el test o la clase del cursor) |
| `Alt+Intro` | `⌥+Intro` | **Arreglarlo**: importar, crear el método, sugerencias |
| `Ctrl+Alt+V` | `⌘+⌥+V` | Extraer a variable |
| `Ctrl+Alt+M` | `⌘+⌥+M` | Extraer a método |
| `Shift+F6` | `⇧+F6` | **Renombrar** en todo el proyecto |
| `Ctrl+/` | `⌘+/` | Comentar la línea |
| `Ctrl+D` | `⌘+D` | Duplicar la línea |
| `Alt+Ins` | `⌘+N` | Generar: constructor, *getters*, `toString`, tests |
| `Ctrl+E` | `⌘+E` | Ficheros recientes |
| `Ctrl+Shift+F` | `⌘+⇧+F` | Buscar en todo el proyecto |
| `F2` | `F2` | Ir al siguiente error |

!!! tip "Los dos que más se usan"
    **`Alt+Intro`** es el atajo estrella. Cuando algo esté subrayado en rojo o amarillo, ponte encima y púlsalo: casi siempre te ofrece la solución. Importar la clase que falta, crear el método que no existe, convertir un bucle en `stream`…

    **`Shift Shift`** (pulsar Shift dos veces rápido) busca cualquier cosa. ¿No recuerdas dónde estaba un ajuste? Escríbelo ahí.

### Plantillas de código

Escribe estas abreviaturas y pulsa `Tab`:

| Escribes | Sale |
|---|---|
| `psvm` | `public static void main(String[] args) {}` |
| `sout` | `System.out.println();` |
| `soutv` | `System.out.println("var = " + var);` |
| `fori` | Bucle `for` con índice |
| `iter` | `for (X x : coleccion)` |
| `psf` | `public static final` |
| `.var` | Después de una expresión: extrae a variable |
| `.for` | Después de una colección: la recorre |
| `.null` | Después de una expresión: comprueba si es nula |

Las tres últimas se usan **detrás** de lo que escribas: `productos.for` + `Tab` genera el bucle completo.

## 5. Ejecutar y depurar

### Ejecutar

- El **triángulo verde** del margen, junto al `main` o junto a un test.
- `Ctrl+Shift+F10` ejecuta lo que tengas bajo el cursor.
- `Shift+F10` repite la última ejecución.

### El depurador: la herramienta que más tiempo te va a ahorrar

Poner `System.out.println` por todas partes funciona, pero es lento y ensucia. El depurador hace lo mismo mejor.

1. **Punto de ruptura:** clic en el margen izquierdo, junto al número de línea. Aparece un círculo rojo.
2. **Ejecuta en modo depuración** (`Shift+F9`).
3. Cuando el programa llegue ahí, se detiene y puedes mirar todas las variables.

| Tecla | Qué hace |
|---|---|
| `F8` | Siguiente línea |
| `F7` | Entrar dentro del método |
| `Shift+F8` | Salir del método actual |
| `F9` | Continuar hasta el siguiente punto de ruptura |
| `Alt+F8` | **Evaluar una expresión** aquí y ahora |

!!! tip "Puntos de ruptura condicionales"
    Clic derecho sobre el punto rojo → `Condition` y escribe, por ejemplo, `producto.getId() == 42`.

    Se detendrá **solo** cuando se cumpla. Es la diferencia entre pulsar `F9` doscientas veces y llegar directamente al caso que falla. Cuando en la UT5 tengas que depurar un bucle sobre 500 registros, lo vas a agradecer.

`Alt+F8` merece mención aparte: te deja escribir cualquier expresión Java y evaluarla con el estado actual del programa. Puedes probar una consulta, llamar a un método o comprobar una condición sin recompilar nada.

## 6. Tests

En este módulo se corrige con tests, así que aquí vas a pasar mucho tiempo.

- **Ejecutar un test:** triángulo verde junto al método o la clase.
- **Ejecutar todos:** clic derecho sobre `src/test/java` → `Run 'All Tests'`.
- **Generar la clase de test:** con el cursor en la clase, `Ctrl+Shift+T` → `Create New Test` → JUnit 5.

El panel de resultados muestra el árbol de tests en verde y rojo. Al hacer clic en uno fallido te lleva **a la línea exacta** de la aserción y te enseña qué se esperaba y qué llegó.

Dos botones del panel que conviene conocer:

- **Rerun failed tests**: repite solo los que fallaron.
- **Toggle auto-test**: relanza los tests cada vez que guardas. Muy útil mientras arreglas algo.

## 7. Maven desde IntelliJ

Panel lateral derecho → **Maven** (o `Shift Shift` → «Maven»).

``` { .text .sinajuste }
mi-proyecto
├── Lifecycle          ← clean, compile, test, package, install
├── Plugins
└── Dependencies       ← el árbol completo, con las transitivas
```

Doble clic en `test` o en `package` ejecuta esa fase. El botón **** recarga el proyecto tras tocar el `pom.xml`.

!!! bug "Has añadido una dependencia y no la reconoce"
    Pulsa el botón de recargar de Maven, o el aviso flotante que sale arriba a la derecha. Si sigue sin verla: `File → Invalidate Caches → Invalidate and Restart`.

    Ese último comando resuelve el 90 % de los «IntelliJ se ha vuelto loco».

## 8. Git integrado

`Ctrl+K` (*commit*) y `Ctrl+Shift+K` (*push*) son los dos atajos del día a día.

En la pestaña **Commit** ves los ficheros cambiados; a la izquierda de cada uno, un diálogo de diferencias que compara con la última versión. Merece la pena mirarlo **antes** de cada *commit*: es donde se detecta el `System.out.println` olvidado.

`Ctrl+Alt+Z` deshace los cambios de un fichero al último *commit*. Cuidado, que no hay vuelta atrás.

Y el más útil de todos, **`Alt+9` → Log**: el historial visual del repositorio, con quién tocó qué y cuándo.

## 9. Ajustes que conviene cambiar el primer día

`File → Settings` (`⌘+,` en macOS):

| Ajuste | Dónde | Por qué |
|---|---|---|
| Codificación **UTF-8** | Editor → File Encodings (los tres desplegables) | O tendrás `Ã±` en los acentos |
| **Formatear al guardar** | Tools → Actions on Save → *Reformat code* | Código consistente sin pensarlo |
| **Optimizar imports al guardar** | Tools → Actions on Save → *Optimize imports* | Sin imports muertos |
| Mostrar **números de línea** | Editor → General → Appearance | Para seguir las explicaciones de clase |
| **Tamaño de fuente** | Editor → Font | En clase, sube a 16-18 pt para que se vea desde el fondo |
| Marca de **margen a 120** | Editor → Code Style → *Hard wrap at* | Líneas legibles |

!!! tip "Sincroniza tus ajustes"
    `File → Manage IDE Settings → Settings Sync`, con tu cuenta de JetBrains. Así el ordenador de casa y el del aula tienen la misma configuración y los mismos atajos.

    Vale la pena hacerlo la primera semana: llegar al examen con el entorno que conoces cuenta más de lo que parece.

## 10. Cuando algo va mal

| Síntoma | Solución |
|---|---|
| «Cannot resolve symbol» en todo | Recargar Maven, o `Invalidate Caches → Invalidate and Restart` |
| Los cambios no se aplican al ejecutar | `Build → Rebuild Project` |
| «Java: error: release version 25 not supported» | El JDK del proyecto no es 25: `Project Structure → Project` |
| El puerto 8080 está ocupado | Otra instancia corriendo. Ciérrala en el panel `Services`, o cambia `server.port` |
| El proyecto no aparece como Maven | Clic derecho en `pom.xml` → `Add as Maven Project` |
| Acentos rotos en la consola | Codificación a UTF-8 en los tres desplegables de File Encodings |
| Todo va lentísimo | Ayuda → *Change Memory Settings* → subir a 2048 MB |

## Comprueba que tienes el entorno listo

- [ ] `java -version` y `javac -version` dicen los dos **25**.
- [ ] `Project Structure → Project` muestra SDK 25 y *language level* 25.
- [ ] Creas un proyecto Maven, escribes `psvm` + `sout`, y se ejecuta.
- [ ] Pones un punto de ruptura y el depurador se detiene ahí.
- [ ] Generas una clase de test con `Ctrl+Shift+T` y la ejecutas.
- [ ] La codificación está en UTF-8 y un `System.out.println("ñáé")` se ve bien.
- [ ] `Ctrl+K` te abre el diálogo de *commit*.
