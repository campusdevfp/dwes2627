# La consola: Java, Maven y Gradle sin IDE

**Este es el manual que más se usa del curso.** Todo lo que hagas tiene que funcionar desde la terminal, no solo desde IntelliJ.

No es una manía. Es que **así se corrige, así se despliega y así funciona un servidor**. El día que entregues algo que solo compila con el botón verde, no está terminado.

!!! danger "La regla que decide entregas"
    ```bash
    mvn clean test
    ```

    Si eso pasa en una carpeta recién descargada, tu proyecto está bien. Si no, da igual lo bonito que se vea en tu pantalla.

---

## 1. Moverse

Lo mínimo para no perderse:

=== "Linux y macOS"

    ```bash
    pwd                    # dónde estoy
    ls -la                 # qué hay aquí, incluidos los ocultos
    cd carpeta             # entrar
    cd ..                  # subir
    cd ~                   # a mi carpeta personal
    mkdir -p a/b/c         # crear carpetas anidadas
    rm -rf carpeta         # borrar (OJO sin papelera)
    cp origen destino      # copiar
    mv origen destino      # mover o renombrar
    cat fichero            # ver entero
    head -20 fichero       # las 20 primeras líneas
    tail -f fichero        # seguir un log en vivo
    grep -rn "texto" .     # buscar texto en todo el árbol
    find . -name "*.java"  # buscar ficheros
    which java             # dónde está el ejecutable
    ```

=== "Windows (PowerShell)"

    ```powershell
    pwd                    # dónde estoy
    ls                     # qué hay aquí
    cd carpeta             # entrar
    cd ..                  # subir
    mkdir a\b\c            # crear carpetas
    rm -r -fo carpeta      # borrar
    cp origen destino      # copiar
    mv origen destino      # mover
    cat fichero            # ver entero
    Get-Content f -Tail 20 -Wait    # seguir un log
    Select-String -Path *.java -Pattern "texto"
    where.exe java         # dónde está el ejecutable
    ```

!!! tip "Instala Git Bash en Windows"
    Viene con Git y te da las órdenes de Linux en Windows. Todos los ejemplos del curso están escritos así, y así te ahorras traducir cada vez.

**Atajos que ahorran horas:** `Tab` completa nombres · `↑` recupera órdenes anteriores · `Ctrl+R` busca en el historial · `Ctrl+C` corta lo que esté corriendo · `Ctrl+L` limpia la pantalla.

---

## 2. Java desde la consola

### Comprobar la instalación

```bash
java -version          # el que ejecuta
javac -version         # el que compila
echo $JAVA_HOME        # dónde está el JDK   (Windows: echo %JAVA_HOME%)
```

!!! danger "El fallo de la primera semana"
    `java -version` dice 25 y `javac -version` dice 17. Tienes varios JDK y el `PATH` los mezcla.

    === "Linux y macOS"
        ```bash
        export JAVA_HOME=$(/usr/libexec/java_home -v 25)   # macOS
        export PATH="$JAVA_HOME/bin:$PATH"
        ```
        Ponlo en `~/.zshrc` o `~/.bashrc` para que sobreviva al reinicio.

    === "Windows"
        Panel de control → *Variables de entorno* → `JAVA_HOME` a la carpeta del JDK (sin `\bin`), y `%JAVA_HOME%\bin` **la primera** del `Path`.

### Compilar y ejecutar a mano

```bash
javac Hola.java                  # genera Hola.class
java Hola                        # OJO SIN la extensión .class
```

Con paquetes, la estructura de carpetas tiene que coincidir:

```bash
javac -d target/classes src/main/java/es/iesx/Hola.java
java -cp target/classes es.iesx.Hola
```

### Un solo fichero, sin compilar

Desde Java 11, y es lo que usarás en la UT2 y la UT3:

```bash
java Hola.java                   # compila en memoria y ejecuta
```

Con Java 25 ni siquiera hace falta la ceremonia:

```java
void main() {
    IO.println("Hola");
}
```

### El `classpath`

Es **dónde busca Java las clases**. La causa del 90 % de los `ClassNotFoundException`.

```bash
java -cp target/classes es.iesx.Main
java -cp "target/classes:libs/*" es.iesx.Main        # varios, con : en Linux/macOS
java -cp "target/classes;libs/*" es.iesx.Main        # con ; en Windows
```

### Empaquetar y ejecutar un `.jar`

```bash
jar cf app.jar -C target/classes .          # crear
jar tf app.jar                              # ver contenido
java -jar app.jar                           # ejecutar (necesita Main-Class)
java -jar app.jar --server.port=9090        # con argumentos
```

### `jshell`: probar sin crear proyecto

Es la consola interactiva de Java, y es utilísima para salir de dudas en dos segundos.

```bash
jshell
```
```java
jshell> var lista = List.of(3, 1, 2);
jshell> lista.stream().sorted().toList()
$3 ==> [1, 2, 3]
jshell> LocalDate.now().plusDays(10)
jshell> /exit
```

Cuando en el examen dudes de qué imprime algo, esto lo resuelve antes que buscarlo.

### Diagnóstico

```bash
jps                              # procesos Java corriendo, con su PID
jinfo <pid>                      # configuración de esa JVM
java -XX:+PrintFlagsFinal -version | grep MaxHeapSize
java -Xmx512m -jar app.jar       # limitar memoria
```

---

## 3. Maven desde la consola

### Lo del día a día

```bash
mvn clean                    # borra target/
mvn compile                  # compila src/main/java
mvn test                     # compila y ejecuta los tests
mvn package                  # compila, testea y genera el .jar
mvn clean package            # lo anterior, desde cero
mvn install                  # además lo instala en ~/.m2
mvn verify                   # package + comprobaciones extra
```

Recuerda que las fases están **ordenadas**: `mvn package` ejecuta antes `validate`, `compile` y `test`.

### Ejecutar tests concretos

```bash
mvn test -Dtest=ProductoServicioTest
mvn test -Dtest='UT3*'                              # todos los que empiecen así
mvn test -Dtest=ProductoServicioTest#crearFalla     # un único método
mvn test -Dtest='UT3P3Test+UT3P6Test'               # varios
mvn test -DfailIfNoTests=false                      # no fallar si el filtro no casa
```

### Spring Boot

```bash
mvn spring-boot:run
mvn spring-boot:run -Dspring-boot.run.profiles=dev
mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=9090
mvn spring-boot:build-image                          # imagen Docker sin Dockerfile
```

### Informarse

```bash
mvn dependency:tree                       # el árbol completo, con las transitivas
mvn dependency:tree -Dincludes=jackson*   # filtrado
mvn dependency:analyze                    # declaradas y no usadas, y al revés
mvn help:effective-pom                    # el pom real, con todo lo heredado
mvn help:active-profiles
mvn versions:display-dependency-updates   # qué se puede actualizar
mvn versions:display-plugin-updates
```

### Cuando algo falla

```bash
mvn -X clean test            # salida de depuración completa
mvn -e clean test            # solo las trazas de error
mvn -o test                  # modo sin conexión
mvn -U clean install         # fuerza a revalidar las dependencias
mvn -q test                  # silencioso: solo errores
mvn -T 4 clean package       # compilación en paralelo con 4 hilos
```

!!! tip "Leer un error de Maven"
    Maven escupe cincuenta líneas. La útil es **la primera que empieza por `[ERROR]`**, no la última. Y si aparece un `Caused by:`, la causa real está ahí.

### El *wrapper*

```bash
./mvnw clean test            # Linux y macOS
mvnw.cmd clean test          # Windows
chmod +x mvnw                # si da "permission denied" tras clonar
```

Usa siempre el *wrapper* si el proyecto lo trae: garantiza que tú, tus compañeros y el profesor ejecutáis **la misma versión de Maven**.

---

## 4. Gradle desde la consola

### Lo del día a día

```bash
./gradlew clean
./gradlew compileJava
./gradlew test
./gradlew build              # equivale a mvn package
./gradlew clean build
```

### Tests concretos

```bash
./gradlew test --tests ProductoServicioTest
./gradlew test --tests "UT3*"
./gradlew test --tests "ProductoServicioTest.crearFalla"
./gradlew test --info                    # ver qué pasa test a test
```

### Spring Boot

```bash
./gradlew bootRun
./gradlew bootRun --args='--spring.profiles.active=dev --server.port=9090'
./gradlew bootJar
```

### Informarse

```bash
./gradlew tasks                                   # qué puedes ejecutar
./gradlew tasks --all
./gradlew dependencies
./gradlew dependencyInsight --dependency jackson-databind
./gradlew properties
```

!!! tip "`dependencyInsight` no tiene equivalente cómodo en Maven"
    Responde a *«¿por qué tengo esta versión y no otra?»* con el camino completo y el motivo del conflicto. Cuando pelees con versiones, es el mejor comando de Gradle.

### Cuando algo falla

```bash
./gradlew build --stacktrace
./gradlew build --info
./gradlew build --debug
./gradlew build --offline
./gradlew clean build --refresh-dependencies
./gradlew --stop                        # parar el demonio si consume mucho
```

!!! bug "`BUILD SUCCESSFUL` con 0 tests"
    Falta `useJUnitPlatform()` en el bloque `tasks.withType<Test>`. Gradle intenta usar JUnit 4, no encuentra nada y **no da error**: dice que todo fue bien.

    Es el fallo silencioso de Gradle. Cuando veas `BUILD SUCCESSFUL` sospechosamente rápido, comprueba cuántos tests se ejecutaron.

---

## 5. Git para entregar

```bash
git clone https://github.com/curso/proyecto.git
cd proyecto

git status                       # qué he cambiado
git add .                        # preparar todo
git add src/main/java/X.java     # o solo un fichero
git commit -m "UT4: repositorio en memoria"
git push

git log --oneline -10            # historial resumido
git diff                         # qué he cambiado sin preparar
git diff --staged                # qué he preparado
git restore src/X.java           # deshacer cambios de un fichero
git restore --staged src/X.java  # quitar de preparado
```

!!! warning "Antes de cada `commit`, mira el `diff`"
    Es donde se detecta el `System.out.println` olvidado, la contraseña que se coló y la carpeta `target/` que no debería estar.

**Mensajes de commit útiles:** `UT5: entidad Curso con enum STRING` dice qué hiciste. `cambios` y `arreglos` no dicen nada y dentro de un mes no te acordarás.

---

## 6. Probar la API sin navegador

```bash
# GET y ver el JSON formateado
curl -s localhost:8080/api/v1/productos | jq

# solo el código de estado
curl -s -o /dev/null -w "%{http_code}\n" localhost:8080/api/v1/productos/999

# ver cabeceras
curl -i localhost:8080/api/v1/productos/1

# POST con cuerpo
curl -X POST localhost:8080/api/v1/productos \
     -H "Content-Type: application/json" \
     -d '{"nombre":"Teclado","precio":29.90}'

# con token
curl localhost:8080/api/v1/productos -H "Authorization: Bearer $TOKEN"

# guardar y reutilizar cookies
curl -c galletas.txt -b galletas.txt localhost:8080/login

# ver todo el diálogo
curl -v localhost:8080/api/v1/productos
```

`jq` merece instalarse: convierte un JSON ilegible en algo que se entiende, y permite extraer campos.

```bash
curl -s .../productos | jq '.content | length'
curl -s .../productos | jq -r '.content[].nombre'
```

---

## 7. Un flujo de trabajo completo

Así es una sesión de clase típica, de principio a fin:

```bash
# 1 · traerte el proyecto
git clone https://github.com/dwes-2daw/ut4-capas-tunombre.git
cd ut4-capas-tunombre

# 2 · ver qué tienes que hacer
./mvnw test                        # todo rojo: los tests son el enunciado
./mvnw test 2>&1 | grep -A2 FAILED

# 3 · programar en el IDE… y comprobar en la consola
./mvnw -q test -Dtest=UT4P6Test    # solo el que estás resolviendo

# 4 · cuando esté verde, arrancar y probar a mano
./mvnw spring-boot:run
curl -s localhost:8080/api/v1/productos | jq

# 5 · entregar
git add .
git commit -m "UT4: repositorio en memoria y servicio"
git push
```

El paso 2 es el que cambia la forma de trabajar: **los tests en rojo son el enunciado**. No hay que adivinar qué se pide, está escrito en las aserciones.

---

## Comprueba que te manejas

1. Averigua qué versión de Java usa Maven en tu equipo, sin abrir el IDE.
2. Compila y ejecuta una clase con paquete, a mano, con `javac` y `java`.
3. Ejecuta **un solo método** de un test con Maven, y después con Gradle.
4. Averigua de qué dependencia viene Jackson en tu proyecto.
5. Arranca Spring Boot en el puerto 9090 con el perfil `dev`, de dos maneras distintas.
6. Comprueba con `curl` que un recurso inexistente devuelve 404, mostrando solo el código.
7. Tus tests de Gradle dicen `BUILD SUCCESSFUL` pero no se ejecuta ninguno. ¿Qué miras?
8. Explica qué hace `mvn -U clean install` y cuándo lo usarías.
