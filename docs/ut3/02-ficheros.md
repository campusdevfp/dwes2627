# Ficheros: leer y escribir datos

> Hasta ahora tus datos morían al cerrar el programa. Aquí aprenden a **persistir**. Usamos la API moderna (**NIO.2**, `java.nio.file`), no la antigua `File` de los tutoriales viejos.

## 1. Rutas: `Path`

```java
import java.nio.file.*;

var ruta = Path.of("datos", "productos.txt");   // datos/productos.txt
IO.println(ruta.toAbsolutePath());              // ruta completa
IO.println(ruta.getFileName());                 // productos.txt
IO.println(ruta.getParent());                   // datos
IO.println(Files.exists(ruta));                 // ¿existe?
```

`Path.of("a", "b")` construye la ruta con el separador correcto de cada sistema (`/` o `\`). **Nunca concatenes rutas a mano con `"/"`**: rompe en Windows.

| Ruta | Qué es |
|---|---|
| `Path.of("datos.txt")` | **Relativa**: depende de desde dónde ejecutes el programa |
| `Path.of("/home/ivan/datos.txt")` | **Absoluta**: siempre la misma |

## 2. Leer y escribir texto

La forma corta, para ficheros que caben en memoria:

```java
// Escribir (crea o sobrescribe)
Files.writeString(Path.of("saludo.txt"), "Hola DAW2");

// Añadir al final
Files.writeString(Path.of("saludo.txt"), "\nOtra línea", StandardOpenOption.APPEND);

// Leer entero
var contenido = Files.readString(Path.of("saludo.txt"));

// Leer como lista de líneas
List<String> lineas = Files.readAllLines(Path.of("saludo.txt"));
```

Para ficheros **grandes**, con streams (no carga todo en memoria):

```java
try (var lineas = Files.lines(Path.of("log.txt"))) {
    var errores = lineas.filter(l -> l.contains("ERROR")).count();
    IO.println("Errores: " + errores);
}
```

Otras operaciones útiles:

```java
Files.createDirectories(Path.of("datos"));        // crea carpetas (y las intermedias)
Files.copy(origen, destino, StandardCopyOption.REPLACE_EXISTING);
Files.move(origen, destino);
Files.deleteIfExists(ruta);
Files.size(ruta);                                  // bytes
try (var s = Files.list(Path.of("datos"))) { ... } // listar carpeta
```

## 3. CSV: el formato de intercambio más común

Un CSV es texto plano con valores separados por comas (o `;`):

```csv
id,nombre,categoria,precio
1,Patinete,movilidad,120.0
2,Casco,seguridad,35.0
```

**Leerlo a objetos:**

```java
record Producto(int id, String nombre, String categoria, double precio) {}

List<Producto> leerCsv(Path ruta) throws IOException {
    try (var lineas = Files.lines(ruta)) {
        return lineas
            .skip(1)                                  // saltar la cabecera
            .filter(l -> !l.isBlank())                // ignorar líneas vacías
            .map(l -> {
                var c = l.split(",");
                return new Producto(
                    Integer.parseInt(c[0].trim()),
                    c[1].trim(),
                    c[2].trim(),
                    Double.parseDouble(c[3].trim()));
            })
            .toList();
    }
}
```

**Escribirlo:**

```bash
void escribirCsv(Path ruta, List<Producto> productos) throws IOException {
    var contenido = new StringBuilder("id,nombre,categoria,precio\n");
    for (var p : productos) {
        contenido.append("%d,%s,%s,%.2f%n".formatted(
            p.id(), p.nombre(), p.categoria(), p.precio()));
    }
    Files.writeString(ruta, contenido.toString());
}
```

!!! bug "Las tres trampas del CSV"
    1. La cabecera — si no haces `skip(1)`, intentarás convertir "id" en un número → `NumberFormatException`.
    2. Comas dentro de los datos: "Casco, talla M" rompe el `split(",")`. En proyectos reales se usa una librería (OpenCSV, Apache Commons CSV) que gestiona las comillas.
    3. Los decimales: `Double.parseDouble` espera punto (120.0), no coma. Un CSV exportado de Excel en español trae 120,0 y hay que convertirlo.

## 4. Errores de E/S y recursos

Todo lo de ficheros lanza **`IOException`** — una excepción *checked*: el compilador te obliga a gestionarla.

```java
try {
    var texto = Files.readString(Path.of("noexiste.txt"));
} catch (NoSuchFileException e) {
    IO.println("El fichero no existe: " + e.getFile());
} catch (IOException e) {
    IO.println("Error de lectura: " + e.getMessage());
}
```

**Siempre `try-with-resources`** cuando abras algo que haya que cerrar (`Files.lines`, lectores, escritores):

```
try (var lineas = Files.lines(ruta)) {
    ...
}   // se cierra solo, incluso si salta una excepción
```

Si no cierras, el fichero queda bloqueado y acabas agotando los descriptores del sistema. En un servidor que lee miles de ficheros, eso lo tumba.

> **Regla del curso:** captura la excepción **donde puedas hacer algo con ella**. Si tu método de lectura no sabe qué hacer si falta el fichero, deja que la excepción suba (`throws IOException`) y que decida quien llamó.

## 5. Recorrer árboles de directorios

```java
// Solo el primer nivel
try (var hijos = Files.list(Path.of("datos"))) {
    hijos.filter(Files::isRegularFile).forEach(IO::println);
}

// Recursivo, con profundidad máxima
try (var todos = Files.walk(Path.of("proyecto"), 5)) {
    todos.filter(p -> p.toString().endsWith(".java"))
         .forEach(IO::println);
}

// Buscar por criterio
try (var encontrados = Files.find(Path.of("."), 3,
        (ruta, attrs) -> attrs.isRegularFile() && attrs.size() > 1_000_000)) {
    encontrados.forEach(p -> IO.println(p + " → " + p.toFile().length() + " bytes"));
}
```

`Files.walk` también necesita `try-with-resources`: mantiene abiertos descriptores de directorio mientras recorre.

Y los metadatos:

```java
var attrs = Files.readAttributes(ruta, BasicFileAttributes.class);
IO.println(attrs.size());
IO.println(attrs.creationTime());
IO.println(attrs.lastModifiedTime());
IO.println(Files.isReadable(ruta) + " " + Files.isWritable(ruta));
```

## 6. Ficheros binarios

No todo es texto. Una imagen, un PDF o un ZIP son **bytes**:

```java
byte[] datos = Files.readAllBytes(Path.of("logo.png"));
Files.write(Path.of("copia.png"), datos);

// Comprobar la "firma" de un PNG: 89 50 4E 47
boolean esPng = datos.length > 4 && (datos[0] & 0xFF) == 0x89
             && datos[1] == 'P' && datos[2] == 'N' && datos[3] == 'G';
```

Para ficheros grandes, por bloques y sin cargarlo todo:

```java
try (var in = Files.newInputStream(origen);
     var out = Files.newOutputStream(destino)) {
    in.transferTo(out);            // copia en bloques, memoria constante
}
```

!!! warning "Texto y codificación"
    `Files.readString` y `writeString` usan **UTF-8** por defecto desde Java 18. Si lees un fichero generado por un programa antiguo de Windows en ISO-8859-1, verás `Ã¡` donde debía haber `á`. Solución explícita:

    ```java
    Files.readString(ruta, StandardCharsets.ISO_8859_1);
    ```

    Regla:

    escribe siempre en UTF-8

    y declara la codificación al leer si sabes que no lo es.

## 7. Escritura segura

Si el programa se cae a mitad de escritura, te quedas con un fichero **corrupto y sin copia**. El patrón profesional es escribir en un temporal y renombrar:

```java
void guardarSeguro(Path destino, String contenido) throws IOException {
    var temporal = destino.resolveSibling(destino.getFileName() + ".tmp");
    Files.writeString(temporal, contenido);
    Files.move(temporal, destino,
        StandardCopyOption.REPLACE_EXISTING,
        StandardCopyOption.ATOMIC_MOVE);       // el renombrado es atómico
}
```

Mientras se escribe el `.tmp`, el fichero bueno sigue intacto. El `move` atómico ocurre en un instante: o está el viejo o está el nuevo, nunca uno a medias. Así funcionan los editores y las bases de datos.

Opciones de apertura que conviene conocer:

```
StandardOpenOption.CREATE            // crear si no existe
StandardOpenOption.CREATE_NEW        // crear, y fallar si ya existe
StandardOpenOption.APPEND            // añadir al final
StandardOpenOption.TRUNCATE_EXISTING // vaciar antes de escribir (por defecto)
```

## 8. CSV bien parseado

El `split(",")` se rompe en cuanto un valor lleva una coma. Este parser mínimo respeta las comillas:

```bash
List<String> partirLineaCsv(String linea) {
    var campos = new ArrayList<String>();
    var actual = new StringBuilder();
    boolean entreComillas = false;

    for (int i = 0; i < linea.length(); i++) {
        char c = linea.charAt(i);
        if (c == '"') {
            if (entreComillas && i + 1 < linea.length() && linea.charAt(i + 1) == '"') {
                actual.append('"'); i++;          // comilla escapada: ""
            } else {
                entreComillas = !entreComillas;
            }
        } else if (c == ',' && !entreComillas) {
            campos.add(actual.toString().trim());
            actual.setLength(0);
        } else {
            actual.append(c);
        }
    }
    campos.add(actual.toString().trim());
    return campos;
}
```

Con esto, `1,"Casco, talla M",seguridad,35.0` se parte en cuatro campos correctos. Aun así, en un proyecto real se usa **Apache Commons CSV** u **OpenCSV**: gestionan saltos de línea dentro de campos, distintos separadores y codificaciones. Escribir este parser sirve para **entender el problema**, no para reinventar la rueda.

---

## Pruébalo ahora (15 min)

!!! reto "Trabaja tú ahora"
    Este bloque se hace **en clase, en tu equipo**. No se entrega ni puntúa: es la práctica que hace que el examen te salga.

``` { .java .numerado }
import java.io.IOException;
import java.nio.file.*;
import java.util.*;

record Producto(int id, String nombre, String categoria, double precio) {}

void main() throws IOException {
    var dir = Path.of("datos");
    Files.createDirectories(dir);
    var csv = dir.resolve("productos.csv");

    // 1. Escribir un CSV
    var productos = List.of(
        new Producto(1, "Patinete", "movilidad", 120.0),
        new Producto(2, "Casco", "seguridad", 35.0),
        new Producto(3, "Bici", "movilidad", 450.0));

    var sb = new StringBuilder("id,nombre,categoria,precio\n");
    productos.forEach(p -> sb.append("%d,%s,%s,%.2f%n"
        .formatted(p.id(), p.nombre(), p.categoria(), p.precio())));
    Files.writeString(csv, sb.toString());
    IO.println("Escrito en " + csv.toAbsolutePath());

    // 2. Volver a leerlo
    try (var lineas = Files.lines(csv)) {
        var leidos = lineas.skip(1).filter(l -> !l.isBlank())
            .map(l -> l.split(","))
            .map(c -> new Producto(Integer.parseInt(c[0]), c[1], c[2],
                                   Double.parseDouble(c[3])))
            .toList();
        leidos.forEach(IO::println);
    }

    // 3. Provoca el error: lee un fichero que no existe
    try {
        Files.readString(dir.resolve("fantasma.csv"));
    } catch (NoSuchFileException e) {
        IO.println("Capturada: no existe " + e.getFile());
    }
}
```

Abre el fichero generado con un editor de texto: **ese CSV lo puede leer Excel**. Acabas de exportar datos.

---

## Ejercicios (con solución)

### Ejercicio 1 — ¿Por qué falla?

```bash
var lineas = Files.readAllLines(Path.of("productos.csv"));
for (var l : lineas) {
    var precio = Double.parseDouble(l.split(",")[3]);
}
```

??? success "Solución"

    Falla en la primera línea: es la cabecera y "precio" no es un número → `NumberFormatException`. Falta saltarla (`.skip(1)` con streams, o empezar el bucle en el índice 1). Y conviene filtrar líneas en blanco al final del fichero.


### Ejercicio 2 — Contador de líneas de error

Escribe un método que reciba la ruta de un log y devuelva cuántas líneas contienen "ERROR", **sin cargar el fichero entero en memoria**.

??? success "Solución"

    ```java
    long contarErrores(Path ruta) throws IOException {
        try (var lineas = Files.lines(ruta)) {
            return lineas.filter(l -> l.contains("ERROR")).count();
        }
    }
    ```

    La clave es `Files.lines` (perezoso, va línea a línea) frente a `readAllLines` (carga todo). Y el `try-with-resources`, obligatorio porque `Files.lines` abre el fichero.


### Ejercicio 3 — Rutas

¿Qué problema tiene `Path.of("datos" + "/" + "fichero.txt")` y cómo se escribe bien?

??? success "Solución"

    Fija el separador /, que no es el de Windows. Correcto: `Path.of("datos", "fichero.txt")` o `Path.of("datos").resolve("fichero.txt")`: Java pone el separador de cada sistema.


### Ejercicio 4 — Filtrar y guardar

Lee el CSV del "Pruébalo ahora", quédate solo con los productos de más de 100 € y escríbelos en `caros.csv` con la misma cabecera.

??? success "Solución"

    ```java
    void main() throws IOException {
        var origen = Path.of("datos/productos.csv");
        var destino = Path.of("datos/caros.csv");

        List<String> resultado;
        try (var lineas = Files.lines(origen)) {
            resultado = lineas.skip(1).filter(l -> !l.isBlank())
                .filter(l -> Double.parseDouble(l.split(",")[3]) > 100)
                .toList();
        }

        var salida = new StringBuilder("id,nombre,categoria,precio\n");
        resultado.forEach(l -> salida.append(l).append("\n"));
        Files.writeString(destino, salida.toString());
        IO.println("Guardados " + resultado.size() + " productos caros");
    }
    ```

    Fíjate: el stream se **cierra antes** de escribir (la lista `resultado` ya está en memoria). Leer y escribir el mismo fichero a la vez es una fuente clásica de corrupción de datos.


### Ejercicio 5 — Inventario de una carpeta

Escribe un método que recorra una carpeta recursivamente y devuelva `Map<String, Long>` con el número de ficheros de cada extensión.

??? success "Solución"

    ```java
    Map<String, Long> porExtension(Path raiz) throws IOException {
        try (var rutas = Files.walk(raiz)) {
            return rutas.filter(Files::isRegularFile)
                .map(p -> p.getFileName().toString())
                .map(n -> {
                    int i = n.lastIndexOf('.');
                    return i > 0 ? n.substring(i + 1).toLowerCase() : "(sin extensión)";
                })
                .collect(Collectors.groupingBy(Function.identity(), TreeMap::new,
                         Collectors.counting()));
        }
    }
    ```

    El i > 0 (y no i >= 0) es intencionado: evita que.gitignore se cuente como extensión `gitignore`.


### Ejercicio 6 — Escritura segura

¿Qué puede salir mal aquí y cómo se arregla?

```java
Files.writeString(Path.of("inventario.json"), json);
```

??? success "Solución"

    `writeString` trunca el fichero primero y luego escribe. Si el proceso se corta a mitad (fallo de corriente, disco lleno, excepción), el inventario anterior ya se ha perdido y el nuevo está incompleto: datos destruidos. Solución: escribir en `inventario.json.tmp` y hacer un `Files.move` con `ATOMIC_MOVE` y `REPLACE_EXISTING`. El original permanece válido hasta el instante del renombrado.


### Ejercicio 7 — CSV con comas

Esta línea rompe tu parser: `4,"Casco, talla M",seguridad,35.0`. ¿Cuántos campos devuelve `split(",")` y cómo se soluciona?

??? success "Solución"

    Devuelve cinco: 4, "Casco, talla M", seguridad, 35.0. Al intentar `parseDouble` del campo 3 obtendrías `NumberFormatException`, o peor: datos silenciosamente mal cargados. Soluciones, de menos a más recomendable: (1) el parser con estado `entreComillas` de la sección 8; (2) una expresión regular que ignore comas entre comillas; (3) una librería como Apache Commons CSV, que es lo que se hace en producción.


### Ejercicio 8 — De CSV a informe

Lee `ventas.csv` (`fecha,vendedor,producto,unidades,precio`) y genera `informe.txt` con la facturación por vendedor ordenada de mayor a menor.

??? success "Solución"

    ```java
    void main() throws IOException {
        var origen = Path.of("datos/ventas.csv");
        var destino = Path.of("datos/informe.txt");

        Map<String, Double> porVendedor;
        try (var lineas = Files.lines(origen)) {
            porVendedor = lineas.skip(1).filter(l -> !l.isBlank())
                .map(l -> l.split(","))
                .collect(Collectors.groupingBy(
                    c -> c[1].trim(),
                    Collectors.summingDouble(c -> Integer.parseInt(c[3].trim())
                                                 * Double.parseDouble(c[4].trim()))));
        }

        var informe = porVendedor.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
            .map(e -> "%-15s %10.2f €".formatted(e.getKey(), e.getValue()))
            .collect(Collectors.joining("\n",
                     "INFORME DE VENTAS\n=================\n", "\n"));

        Files.writeString(destino, informe);
        IO.println(informe);
    }
    ```

    Tres cosas que se evalúan: el stream se cierra antes de escribir, el joining con prefijo y sufijo evita concatenaciones a mano, y %-15s %10.2f alinea la salida en columnas.

