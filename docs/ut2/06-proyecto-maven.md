# 6. Un proyecto Maven, paso a paso

Hasta aquí todo se ha ejecutado con `java Fichero.java` y `jshell`. Eso está bien para aprender y **no vale para construir nada**. En esta página montas tu primer proyecto de verdad, y dentro va todo lo de la unidad: `record`, `enum`, interfaz, colecciones, *streams*, excepciones y `Optional`.

!!! info "Aquí no hay tests"
    Los tests se ven en la **UT4**, cuando ya haya algo que merezca la pena probar. Meterlos ahora sería aprender dos cosas nuevas a la vez y ninguna bien.

---

## 1. El problema que resuelve Maven

Con dos ficheros, `java Fichero.java` basta. Ahora imagina veinte clases en paquetes, más tres librerías externas:

```bash
javac -d target/classes -cp "libs/jackson-core-2.18.jar:libs/jackson-databind-2.18.jar:libs/jackson-annotations-2.18.jar" \
      src/main/java/es/iesx/tienda/**/*.java

java -cp "target/classes:libs/jackson-core-2.18.jar:libs/jackson-databind-2.18.jar:..." es.iesx.tienda.Main
```

Y esto **hay que escribirlo igual en todos los ordenadores** del equipo, con las mismas versiones, en el mismo orden. Además hay que bajar los `.jar` a mano, y los `.jar` de los que dependen esos `.jar`.

Maven convierte todo eso en:

```bash
mvn compile
mvn exec:java
```

| Lo que hace | Cómo |
|---|---|
| Descargar librerías **y las suyas** | Las declaras en un fichero; él las baja |
| Compilar en el orden correcto | Estructura de carpetas estándar |
| Empaquetar en un `.jar` | `mvn package` |
| Que funcione igual en cualquier máquina | Todo está en el `pom.xml` |

---

## 2. Crear el proyecto

Se puede crear desde IntelliJ (*New Project → Maven*), pero conviene hacerlo **una vez a mano** para entender qué hay dentro.

```bash
mkdir -p catalogo/src/main/java/es/iesx/catalogo
mkdir -p catalogo/src/main/resources
cd catalogo
```

La estructura estándar, que **no es opcional**: Maven busca exactamente en estos sitios.

```
catalogo/
├── pom.xml                        ← la definición del proyecto
├── src/
│   ├── main/
│   │   ├── java/                  ← tu código
│   │   │   └── es/iesx/catalogo/
│   │   └── resources/             ← ficheros que acompañan (datos, config)
│   └── test/
│       └── java/                  ← los tests (vacío por ahora)
└── target/                        ← lo que genera Maven. NO se sube a Git
```

!!! warning "`target/` no se sube nunca"
    Es todo generado: se rehace con `mvn package`. Súbelo y el repositorio pesa cien veces más para nada. En el `.gitignore` va `target/` la primera.

---

## 3. El `pom.xml`, línea a línea

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             https://maven.apache.org/xsd/maven-4.0.0.xsd">

  <modelVersion>4.0.0</modelVersion>

  <groupId>es.iesx</groupId>          <!-- (1) -->
  <artifactId>catalogo</artifactId>   <!-- (2) -->
  <version>1.0.0</version>            <!-- (3) -->

  <properties>
    <maven.compiler.release>25</maven.compiler.release>   <!-- (4) -->
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>  <!-- (5) -->
    <exec.mainClass>es.iesx.catalogo.Main</exec.mainClass>              <!-- (6) -->
  </properties>

  <dependencies>
    <!-- de momento, ninguna -->
  </dependencies>

</project>
```

1. **`groupId`**: quién lo hace. Por convenio, el dominio al revés.
2. **`artifactId`**: cómo se llama. Será el nombre del `.jar`.
3. **`version`**: la versión. `1.0.0-SNAPSHOT` significa «en desarrollo».
4. **La versión de Java.** Sin esto, Maven compila con una versión antigua y `record` o el `switch` moderno fallan.
5. **UTF-8.** Sin esto, en Windows los acentos salen rotos. No es opcional en español.
6. La clase que se ejecuta con `mvn exec:java`.

Los tres primeros juntos —`es.iesx:catalogo:1.0.0`— son las **coordenadas** del proyecto, y son la forma en que Maven identifica cualquier librería del mundo.

---

## 4. Los comandos que se usan

```bash
mvn compile          # compila a target/classes
mvn package          # compila y crea target/catalogo-1.0.0.jar
mvn clean            # borra target/
mvn clean package    # las dos cosas: lo habitual antes de entregar
mvn exec:java        # ejecuta la clase de exec.mainClass
mvn dependency:tree  # enseña TODAS las librerías, incluidas las indirectas
mvn -o compile       # sin conexión, con lo que ya está descargado
```

!!! tip "La primera vez tarda, después no"
    Maven baja las librerías a `~/.m2/repository` y las **reutiliza en todos tus proyectos**. La primera compilación tarda un minuto; las siguientes, segundos.

=== "Maven"

    ```bash
    mvn clean package
    mvn exec:java
    ```

=== "Gradle"

    ```bash
    ./gradlew clean build
    ./gradlew run
    ```

    Gradle hace lo mismo con otra sintaxis. En el módulo se usa **Maven**, pero conviene reconocer los dos: [manual de Gradle](../entorno/gradle.md).

---

## 5. El proyecto, paso a paso

Un catálogo de productos: se cargan, se consultan, se filtran y se saca un informe. Seis pasos, y en cada uno se usa algo de la unidad.

### Paso 1 · El modelo

`src/main/java/es/iesx/catalogo/Producto.java`

```java
package es.iesx.catalogo;

public record Producto(String codigo, String nombre,
                       Categoria categoria, double precio, int stock) {

    public Producto {                                    // (1)
        if (codigo == null || codigo.isBlank())
            throw new IllegalArgumentException("Código obligatorio");
        if (precio < 0)
            throw new IllegalArgumentException("Precio negativo: " + precio);
        if (stock < 0)
            throw new IllegalArgumentException("Stock negativo: " + stock);
    }

    public double precioConIva() { return precio * 1.21; }

    public boolean agotado() { return stock == 0; }
}
```

1. Constructor compacto: **el objeto no puede existir mal formado**.

`Categoria.java`

```java
package es.iesx.catalogo;

public enum Categoria {
    MOVILIDAD("Movilidad personal"),
    SEGURIDAD("Equipación de seguridad"),
    REPUESTO("Repuestos y accesorios");

    private final String descripcion;

    Categoria(String descripcion) { this.descripcion = descripcion; }

    public String descripcion() { return descripcion; }
}
```

### Paso 2 · La interfaz

`CatalogoRepositorio.java`

```java
package es.iesx.catalogo;

import java.util.List;
import java.util.Optional;

public interface CatalogoRepositorio {
    List<Producto> listar();
    Optional<Producto> buscarPorCodigo(String codigo);   // (1)
}
```

1. **`Optional`, no `null`.** El repositorio informa de que no está; no decide si eso es grave.

### Paso 3 · Una implementación

`CatalogoEnMemoria.java`

```java
package es.iesx.catalogo;

import java.util.*;

public class CatalogoEnMemoria implements CatalogoRepositorio {

    private final Map<String, Producto> productos = new LinkedHashMap<>();   // (1)

    public CatalogoEnMemoria(List<Producto> iniciales) {
        iniciales.forEach(p -> productos.put(p.codigo(), p));
    }

    @Override
    public List<Producto> listar() {
        return List.copyOf(productos.values());          // (2)
    }

    @Override
    public Optional<Producto> buscarPorCodigo(String codigo) {
        return Optional.ofNullable(productos.get(codigo));   // (3)
    }
}
```

1. **`LinkedHashMap`** y no `HashMap`: así el listado sale en el orden en que se cargaron, que es lo que espera quien lo lee. Y un `Map` porque la búsqueda por código tiene que ser directa.
2. Copia inmutable: nadie puede vaciar el catálogo desde fuera.
3. `Optional.ofNullable` convierte el posible `null` del mapa en un `Optional`. Es el puente entre la API antigua y la moderna.

### Paso 4 · Las reglas

`CatalogoServicio.java`

```java
package es.iesx.catalogo;

import java.util.*;
import java.util.stream.Collectors;

public class CatalogoServicio {

    private final CatalogoRepositorio repositorio;

    public CatalogoServicio(CatalogoRepositorio repositorio) {   // (1)
        this.repositorio = repositorio;
    }

    public Producto obtener(String codigo) {
        return repositorio.buscarPorCodigo(codigo)
                .orElseThrow(() -> new ProductoNoEncontradoException(codigo));  // (2)
    }

    public List<Producto> disponiblesDe(Categoria categoria) {
        return repositorio.listar().stream()
                .filter(p -> p.categoria() == categoria)
                .filter(p -> !p.agotado())
                .sorted(Comparator.comparingDouble(Producto::precio))
                .toList();
    }

    public Map<Categoria, Long> conteoPorCategoria() {
        return repositorio.listar().stream()
                .collect(Collectors.groupingBy(Producto::categoria,
                         TreeMap::new,                            // (3)
                         Collectors.counting()));
    }

    public double valorDelInventario() {
        return repositorio.listar().stream()
                .mapToDouble(p -> p.precio() * p.stock())
                .sum();
    }

    public Optional<Producto> masCaro() {
        return repositorio.listar().stream()
                .max(Comparator.comparingDouble(Producto::precio));
    }
}
```

1. **Recibe la interfaz por constructor.** No hace `new` de ninguna implementación.
2. Aquí sí se decide: no encontrarlo **es un error**.
3. `TreeMap::new` para que el informe salga siempre en el mismo orden.

`ProductoNoEncontradoException.java`

```java
package es.iesx.catalogo;

public class ProductoNoEncontradoException extends RuntimeException {

    private final String codigo;

    public ProductoNoEncontradoException(String codigo) {
        super("No existe el producto con código " + codigo);
        this.codigo = codigo;
    }

    public String codigo() { return codigo; }
}
```

### Paso 5 · El `main`

`Main.java`

```java
package es.iesx.catalogo;

import java.util.List;

public class Main {

    public static void main(String[] args) {

        CatalogoRepositorio repositorio = new CatalogoEnMemoria(datosDePrueba());  // (1)
        var servicio = new CatalogoServicio(repositorio);

        System.out.println("=== Inventario ===");
        System.out.printf("Valor total: %.2f €%n", servicio.valorDelInventario());

        System.out.println();
        System.out.println("=== Productos por categoría ===");
        servicio.conteoPorCategoria()
                .forEach((c, n) -> System.out.printf("%-12s %d%n", c, n));

        System.out.println();
        System.out.println("=== Seguridad disponible, de más barato a más caro ===");
        servicio.disponiblesDe(Categoria.SEGURIDAD)
                .forEach(p -> System.out.printf("  %-10s %7.2f €  (stock %d)%n",
                        p.nombre(), p.precio(), p.stock()));

        System.out.println();
        System.out.println("Más caro: " +
                servicio.masCaro().map(Producto::nombre).orElse("—"));

        System.out.println();
        try {
            servicio.obtener("NO-EXISTE");
        } catch (ProductoNoEncontradoException e) {
            System.out.println("Capturada: " + e.getMessage());
        }
    }

    private static List<Producto> datosDePrueba() {
        return List.of(
                new Producto("MOV-01", "Patinete", Categoria.MOVILIDAD, 120.0, 4),
                new Producto("MOV-02", "Bici",     Categoria.MOVILIDAD, 450.0, 2),
                new Producto("SEG-01", "Casco",    Categoria.SEGURIDAD,  35.0, 12),
                new Producto("SEG-02", "Candado",  Categoria.SEGURIDAD,  25.0, 0),
                new Producto("SEG-03", "Luces",    Categoria.SEGURIDAD,  15.0, 30),
                new Producto("REP-01", "Cámara",   Categoria.REPUESTO,    8.5, 50));
    }
}
```

1. **Aquí, y solo aquí, se decide qué implementación se usa.** Es la única línea que habría que cambiar para pasar de memoria a fichero o a base de datos.

### Paso 6 · Ejecutarlo

```bash
mvn clean compile
mvn exec:java
```

```
=== Inventario ===
Valor total: 2312,00 €

=== Productos por categoría ===
MOVILIDAD    2
SEGURIDAD    3
REPUESTO     1

=== Seguridad disponible, de más barato a más caro ===
  Luces        15,00 €  (stock 30)
  Casco        35,00 €  (stock 12)

Más caro: Bici

Capturada: No existe el producto con código NO-EXISTE
```

!!! success "Qué acabas de usar"
    | De la unidad | Dónde |
    |---|---|
    | `record` con validación | `Producto` |
    | `enum` con datos | `Categoria` |
    | Interfaz y polimorfismo | `CatalogoRepositorio` |
    | `Map`, `List` y copias inmutables | `CatalogoEnMemoria` |
    | *Streams*, `groupingBy`, `sorted`, `max` | `CatalogoServicio` |
    | Excepción propia | `ProductoNoEncontradoException` |
    | `Optional` | `buscarPorCodigo` y `masCaro` |

    Y la estructura —modelo, interfaz, implementación, servicio, `main`— es **exactamente** la que tendrá tu aplicación Spring Boot en la UT4. Lo único que cambiará es quién escribe el `new`.

---

## 6. Añadir una dependencia

El `Candado` está agotado y queremos avisarlo con un texto centrado y bonito. En vez de escribirlo, se usa una librería.

Se busca en [central.sonatype.com](https://central.sonatype.com) y se copian las coordenadas al `pom.xml`:

```xml
<dependencies>
  <dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-lang3</artifactId>
    <version>3.17.0</version>
  </dependency>
</dependencies>
```

```java
import org.apache.commons.lang3.StringUtils;

System.out.println(StringUtils.center(" AGOTADO ", 40, '*'));
// *************** AGOTADO ****************
```

```bash
mvn compile      # la baja sola la primera vez
```

Y para ver qué ha entrado de verdad:

```bash
mvn dependency:tree
```

```
es.iesx:catalogo:jar:1.0.0
\- org.apache.commons:commons-lang3:jar:3.17.0:compile
```

!!! warning "Una dependencia arrastra a otras"
    Aquí solo hay una. Cuando añadas Jackson en la UT3 verás que una línea en el `pom.xml` mete **tres** librerías. Por eso existe `dependency:tree`: es la forma de saber qué hay realmente dentro de tu programa.

    Y por eso no se añaden dependencias a la ligera: cada una es código ajeno que tienes que mantener actualizado.

---

## 7. Un `.jar` que se ejecuta solo

```bash
mvn clean package
java -jar target/catalogo-1.0.0.jar
```

```
no main manifest attribute, in target/catalogo-1.0.0.jar
```

El `.jar` tiene tu código pero **no dice cuál es la clase principal ni lleva dentro las librerías**. Se arregla con un complemento:

```xml
<build>
  <plugins>
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-shade-plugin</artifactId>
      <version>3.6.0</version>
      <executions>
        <execution>
          <phase>package</phase>
          <goals><goal>shade</goal></goals>
          <configuration>
            <transformers>
              <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                <mainClass>es.iesx.catalogo.Main</mainClass>
              </transformer>
            </transformers>
          </configuration>
        </execution>
      </executions>
    </plugin>
  </plugins>
</build>
```

```bash
mvn clean package
java -jar target/catalogo-1.0.0.jar     # ahora sí
```

Ese fichero es **un programa entero**: se copia a cualquier máquina con Java y funciona. Es, a grandes rasgos, lo que hará Spring Boot en la UT4 con `mvn spring-boot:run` — solo que su `.jar` lleva además un servidor web dentro.

---

## Pruébalo ahora (20 min)

1. Monta el proyecto entero y ejecútalo. Que la salida sea la de arriba.
2. **Rompe algo a propósito:** quita `<maven.compiler.release>25</maven.compiler.release>` y compila. Lee el error. Vuélvelo a poner.
3. Crea `CatalogoEnFichero` que implemente la misma interfaz leyendo de una `List` que tú escribas en el constructor, y **cambia solo la línea del `main`**. Comprueba que el servicio no se toca.
4. Añade un método `agotados()` al servicio que devuelva los productos sin stock, ordenados por categoría y nombre.
5. Empaqueta con *shade* y ejecuta el `.jar` desde otra carpeta.

??? success "Solución de las cinco"

    **1.** Con la estructura del punto 5 y `mvn clean compile && mvn exec:java`, la salida es la de arriba. Si sale `no main manifest attribute`, estás lanzando el `.jar` sin el complemento *shade*: eso es el punto 5.

    **2.** Al quitar la línea de la versión:

    ```
    [ERROR] /…/Producto.java:[3,8] records are not supported in -source 8
    ```

    Maven usa por defecto una versión antigua de Java. **Es el primer error que da todo proyecto creado a mano**, y por eso la línea no es opcional.

    **3.** La segunda implementación, y la única línea que cambia:

    ```java
    public class CatalogoEnFichero implements CatalogoRepositorio {

        private final List<Producto> productos;

        public CatalogoEnFichero(List<Producto> productos) {
            this.productos = List.copyOf(productos);
        }

        @Override public List<Producto> listar() { return productos; }

        @Override public Optional<Producto> buscarPorCodigo(String codigo) {
            return productos.stream().filter(p -> p.codigo().equals(codigo)).findFirst();
        }
    }
    ```

    ```java
    // Main.java · la ÚNICA línea que cambia
    CatalogoRepositorio repositorio = new CatalogoEnFichero(datosDePrueba());
    ```

    **Si has tenido que tocar `CatalogoServicio`, las capas no están separadas.** El servicio depende de la interfaz, así que no se entera de nada.

    **4.** Los agotados, ordenados por dos criterios:

    ```java
    public List<Producto> agotados() {
        return repositorio.listar().stream()
                .filter(Producto::agotado)
                .sorted(Comparator.comparing(Producto::categoria)
                                  .thenComparing(Producto::nombre))
                .toList();
    }
    ```

    Ojo al `thenComparing`: si pones `.reversed()` al final, **invierte los dos criterios**, no solo el primero.

    **5.** Con el complemento *shade* en el `pom.xml`:

    ```bash
    mvn clean package
    cd /tmp
    java -jar ~/catalogo/target/catalogo-1.0.0.jar     # funciona desde cualquier sitio
    ```

    Ese fichero **es el programa entero**: lleva tu código y las librerías dentro. Se copia a otra máquina con Java y funciona. Es, a grandes rasgos, lo que hará Spring Boot en la UT4 — solo que su `.jar` incluye además un servidor web.
---

## Ejercicios (con solución)

### E1 — ¿Por qué no compila?

Un proyecto con `record Producto(...)` y este `pom.xml` no compila. ¿Por qué?

```xml
<properties>
  <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>
```

??? success "Solución"


    Un proyecto con `record Producto(...)` y este `pom.xml`:

    ```xml
    <properties>
      <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
    ```

    Falta `<maven.compiler.release>25</maven.compiler.release>`. Sin él, Maven usa una versión antigua por defecto y sale:

    ```
    [ERROR] records are not supported in -source 8
    ```

    Es el primer error que da todo proyecto Maven recién creado a mano.

### E2 — Dónde va cada fichero

Coloca: (a) `Producto.java` · (b) `productos.csv` de datos · (c) `application.properties` · (d) el `.jar` generado

??? success "Solución"

    | | Dónde |
    |---|---|
    | (a) `Producto.java` | `src/main/java/es/iesx/catalogo/` |
    | (b) `productos.csv` | `src/main/resources/` |
    | (c) `application.properties` | `src/main/resources/` |
    | (d) El `.jar` | `target/` — **generado, no se sube a Git** |

    Todo lo que esté en `resources/` acaba **dentro del `.jar`**, así que viaja con el programa.

### E3 — Las coordenadas

¿Qué significa cada línea y para qué sirven las tres juntas?

```xml
<groupId>com.fasterxml.jackson.core</groupId>
<artifactId>jackson-databind</artifactId>
<version>2.18.2</version>
```

??? success "Solución"


    ```xml
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.18.2</version>
    ```

    ¿Qué significa cada uno y para qué sirven juntos?

    - **`groupId`**: la organización. Por convenio, su dominio al revés.
    - **`artifactId`**: el nombre de la librería concreta.
    - **`version`**: la versión exacta.

    Los tres juntos identifican **sin ambigüedad** una librería entre millones, y son lo que Maven usa para bajarla del repositorio central. Es el mismo formato que verás en cualquier documentación: `grupo:artefacto:versión`.

### E4 — `mvn package` sin conexión

Estás sin conexión y `mvn package` falla al intentar descargar algo. ¿Qué puedes hacer?

??? success "Solución"


    Estás sin internet y `mvn package` falla al intentar bajar algo.

    ```bash
    mvn -o package        # modo offline: usa solo lo que ya está en ~/.m2
    ```

    Funcionará **si esa dependencia ya se descargó alguna vez**. Por eso conviene hacer un `mvn clean package` en casa antes del día del examen: lo que está en `~/.m2` ya no hace falta bajarlo.

### E5 — El `.jar` que no arranca

Has empaquetado y al lanzarlo sale esto. ¿Qué pasa y cómo se arregla?

```
$ java -jar target/catalogo-1.0.0.jar
no main manifest attribute
```

??? success "Solución"


    ```
    $ java -jar target/catalogo-1.0.0.jar
    no main manifest attribute
    ```

    El `.jar` no declara cuál es su clase principal. Dos soluciones:

    ```bash
    # rápida, sin tocar el pom
    java -cp target/catalogo-1.0.0.jar es.iesx.catalogo.Main
    ```

    ```xml
    <!-- definitiva: shade o el maven-jar-plugin con el manifiesto -->
    ```

    Y hay un segundo problema que aparece en cuanto tengas dependencias: el `.jar` normal **no las incluye**. Por eso hace falta *shade*, que mete todo dentro. Spring Boot resuelve esto mismo con su propio complemento.
