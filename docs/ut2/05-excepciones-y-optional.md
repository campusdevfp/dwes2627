# 5. Excepciones y `Optional`

Qué hacer cuando algo sale mal, y qué hacer cuando algo simplemente no está. Son dos cosas distintas y se resuelven distinto.

!!! tip "Todo esto se copia y se pega en `jshell`"
    Los bloques están escritos para pegarlos tal cual; el comentario de la derecha dice lo que tiene que salir. Pega primero los `import`:

    ```java
    import java.util.*;
    import java.nio.file.*;
    ```

---

## 1. Qué es una excepción

Cuando Java no puede seguir, **lanza** un objeto que describe el problema y aborta lo que estaba haciendo.

```java
System.out.println(10 / 0);
// lanza java.lang.ArithmeticException: / by zero
String s = null; s.length();
// lanza java.lang.NullPointerException
Integer.parseInt("hola");
// lanza java.lang.NumberFormatException: For input string: "hola"
List.of(1,2,3).get(7);
// lanza java.lang.IndexOutOfBoundsException: Index 7 out of bounds for length 3
```

Fíjate en que **el mensaje dice exactamente qué pasó**. Antes de buscar en internet, léelo: el 80 % de las veces ya está ahí la respuesta.

Si nadie la captura, la excepción sube hasta arriba y el programa termina imprimiendo la **traza**: la lista de métodos por los que pasó, del más reciente al más antiguo.

```
Exception in thread "main" java.lang.NumberFormatException: For input string: "hola"
    at java.base/java.lang.Integer.parseInt(Integer.java:652)
    at Catalogo.leer(Catalogo.java:18)      ← aquí está tu código
    at Main.main(Main.java:7)
```

**La línea que importa es la primera que lleva tu nombre de clase.** Las de `java.base` son las tripas del lenguaje.

---

## 2. `try / catch`

```java
void convertir(String texto) {
    try {
        int n = Integer.parseInt(texto);
        System.out.println("Número: " + n);
    } catch (NumberFormatException e) {
        System.out.println("No es un número: " + texto);
    }
}
System.out.println(convertir("42"));
// Número: 42
System.out.println(convertir("hola"));
// No es un número: hola
```

Y `finally`, que **se ejecuta pase lo que pase**:

```java
void prueba(int n) {
    try {
        System.out.println(100 / n);
        return;
    } catch (ArithmeticException e) {
        System.out.println("División por cero");
    } finally {
        System.out.println("--- siempre paso por aquí ---");
    }
}
System.out.println(prueba(5));
// 20
// --- siempre paso por aquí ---
System.out.println(prueba(0));
// División por cero
// --- siempre paso por aquí ---
```

Se ejecuta incluso con un `return` de por medio. Sirve para cerrar cosas… aunque para eso hay algo mejor, que viene en el punto 5.

### Varios `catch`

```java
try {
    procesar(fichero);
} catch (NoSuchFileException e) {
    System.out.println("No existe: " + e.getFile());
} catch (IOException e) {                        // (1)
    System.out.println("Error de lectura: " + e.getMessage());
}
```

1. **Del más concreto al más general.** Si pones `IOException` primero, el `catch` de `NoSuchFileException` no se alcanza nunca y **no compila**.

!!! danger "Las tres formas de hacerlo mal"
    ```java
    catch (Exception e) { }                      // 1 · tragárselo
    catch (Exception e) { e.printStackTrace(); } // 2 · imprimir y seguir
    catch (Exception e) { throw new RuntimeException(); }  // 3 · perder la causa
    ```

    1. **El fallo desaparece** y el programa sigue con datos a medias. Es el peor error de esta unidad: el problema aparecerá diez minutos después en otro sitio y no habrá forma de relacionarlos.
    2. Ensucia la salida y **tampoco detiene nada**.
    3. Se pierde la traza original. La causa se pasa siempre: `new RuntimeException("mensaje", e)`.

    Pruébalo para verlo:

    ```java
    try { Integer.parseInt("x"); } catch (Exception e) { }
    System.out.println("sigo como si nada");
    // sigo como si nada
    ```

---

## 3. Comprobadas y no comprobadas

Java tiene dos familias, y la diferencia es **si el compilador te obliga a tratarlas**.

```java
Files.readString(Path.of("x.txt"));
// lanza unreported exception IOException; must be caught or declared to be thrown
Integer.parseInt("x");
// lanza java.lang.NumberFormatException      // compila, falla al ejecutar
```

| | Comprobadas (*checked*) | No comprobadas (*unchecked*) |
|---|---|---|
| Heredan de | `Exception` | `RuntimeException` |
| El compilador | **Obliga** a `catch` o a `throws` | No dice nada |
| Ejemplos | `IOException`, `SQLException` | `NullPointerException`, `IllegalArgumentException` |
| Significan | «Puede fallar por causas externas» | «Alguien programó mal» o «los datos no valen» |

Dos formas de tratar una comprobada:

```java
// 1 · la capturo aquí
String leer(Path p) {
    try {
        return Files.readString(p);
    } catch (IOException e) {
        return "";
    }
}

// 2 · la paso a quien me llamó
String leer(Path p) throws IOException {
    return Files.readString(p);
}
```

!!! info "La regla práctica"
    **Captura donde puedas hacer algo útil.** Si en ese método no sabes qué hacer con el fallo, no lo captures: propágalo.

    Un `catch` que solo escribe «error» y devuelve `null` es peor que no capturar.

---

## 4. Excepciones propias

Las excepciones del lenguaje hablan de Java. Las tuyas hablan de **tu problema**, y eso es mucho más útil.

```java
class ProductoNoEncontradoException extends RuntimeException {
    private final String codigo;
    ProductoNoEncontradoException(String codigo) {
        super("No existe el producto " + codigo);     // (1)
        this.codigo = codigo;
    }
    String codigo() { return codigo; }                // (2)
}
throw new ProductoNoEncontradoException("PC-042");
// lanza ProductoNoEncontradoException: No existe el producto PC-042
```

1. El mensaje se pasa al constructor de arriba.
2. **Guardar el dato** permite que quien la capture haga algo con él, en vez de tener que sacarlo del texto del mensaje.

### Envolver sin perder la causa

```java
class CatalogoNoDisponibleException extends RuntimeException {
    CatalogoNoDisponibleException(String mensaje, Throwable causa) {
        super(mensaje, causa);                  // ← la causa, SIEMPRE
    }
}

List<Producto> cargar(Path fichero) {
    try {
        return leer(fichero);
    } catch (IOException e) {
        throw new CatalogoNoDisponibleException(
                "No se pudo cargar el catálogo desde " + fichero, e);
    }
}
```

Quien llama a `cargar` no necesita saber que por debajo hay ficheros — puede que mañana haya una base de datos. Pero **la traza conserva el fallo original**, así que sigue siendo depurable:

```
CatalogoNoDisponibleException: No se pudo cargar el catálogo desde datos/x.csv
    at Catalogo.cargar(Catalogo.java:22)
Caused by: java.nio.file.NoSuchFileException: datos/x.csv       ← aquí está
    at ...
```

Sin la `e`, ese `Caused by` no aparece y la información se pierde para siempre.

!!! success "Esto reaparece en la UT4"
    `ProductoNoEncontradoException` se traducirá automáticamente en un **404** sin que el servicio sepa nada de HTTP. Por eso merece la pena crearla ahora.

---

## 5. `try-with-resources`

Todo lo que se abre hay que cerrarlo: ficheros, conexiones, streams. Con `finally` es incómodo y fácil de olvidar. Con `try-with-resources` se cierra solo:

```java
try (var lineas = Files.lines(Path.of("datos.csv"))) {
    lineas.forEach(System.out::println);
}   // ← aquí se cierra, pase lo que pase
```

Y con varios recursos:

```java
try (var entrada = Files.newBufferedReader(origen);
     var salida  = Files.newBufferedWriter(destino)) {

    String linea;
    while ((linea = entrada.readLine()) != null) {
        salida.write(linea.toUpperCase());
        salida.newLine();
    }
}
```

Dos cosas que se preguntan:

1. **Se cierran en orden inverso** al de apertura: primero `salida`, después `entrada`.
2. **Se cierran todos**, aunque uno falle al cerrarse.

!!! warning "`Files.lines` hay que cerrarlo"
    ```java
    var lineas = Files.lines(ruta);       // deja el fichero ABIERTO
    lineas.forEach(...);                  // y nadie lo cierra
    ```

    Con unos pocos ficheros no se nota. Con muchos, el sistema se queda sin descriptores y todo deja de funcionar. Siempre dentro de un `try`.

---

## 6. `Optional`: «puede que no haya»

Un método que devuelve `null` obliga a que **quien lo llama se acuerde** de comprobarlo. Si se le olvida, el programa revienta más adelante y muy lejos de la causa.

```java
Map<String,Integer> stock = new HashMap<>(Map.of("casco", 5));
System.out.println(stock.get("bici") + 1);
// lanza java.lang.NullPointerException
```

`Optional` convierte eso en algo que **el compilador te obliga a tratar**:

```java
Optional<Integer> vacio = Optional.empty();
Optional<Integer> lleno = Optional.of(5);
System.out.println(lleno.get());   // 5
System.out.println(vacio.isPresent());   // false
```

### Cómo se usa de verdad

**Lo que NO hay que hacer**, aunque funcione:

```java
if (opt.isPresent()) { return opt.get().toUpperCase(); }
else                 { return "SIN NOMBRE"; }
```

Eso es escribir el mismo `if (x != null)` de siempre con más letras. `Optional` está para **encadenar**:

```java
record Alumno(String nombre, int nota) {}
var alumnos = List.of(new Alumno("Ana", 8), new Alumno("Bruno", 4));
System.out.println(alumnos.stream()
        .filter(a -> a.nota() >= 5)
        .findFirst()
        .map(Alumno::nombre)
        .map(String::toUpperCase)
        System.out.println(.orElse("NINGUNO"));
// ANA
System.out.println(alumnos.stream()
        .filter(a -> a.nota() >= 9)
        .findFirst()
        .map(Alumno::nombre)
        System.out.println(.orElse("NINGUNO"));
// NINGUNO
```

**En ningún momento hay un `if`.** Si el `Optional` está vacío, las transformaciones simplemente no se ejecutan.

### Las cuatro formas de terminar

```java
Optional<String> n = Optional.empty();
// 1 · un valor fijo
System.out.println(n.orElse("por defecto"));   // por defecto
System.out.println(n.orElseGet(() -> consultarValorCaro()));   // 2 · solo si hace falta
n.orElseThrow(() -> new IllegalStateException("No hay"));   // 3 · lanzar
// lanza java.lang.IllegalStateException: No hay
n.ifPresent(v -> System.out.println(v));                  // 4 · hacer algo solo si hay
```

La diferencia entre `orElse` y `orElseGet` importa: **`orElse` evalúa siempre su argumento**, incluso cuando el `Optional` tiene valor. Si dentro hay una consulta cara, se hace para nada.

```java
Optional.of("hay").orElse(caro());   // ← caro() SE EJECUTA
Optional.of("hay").orElseGet(() -> caro());   // ← no se ejecuta
```

### `map` frente a `flatMap`

```java
Optional<String> texto = Optional.of("42");
System.out.println(texto.map(Integer::parseInt));   // Optional[42]
Optional<Optional<String>> anidado = Optional.of(Optional.of("x"));
System.out.println(anidado.flatMap(o -> o));   // Optional[x]
```

**Si la función que aplicas ya devuelve un `Optional`, usa `flatMap`.** Con `map` te quedas con un `Optional<Optional<...>>`, que no sirve para nada.

!!! danger "Dónde NO se usa `Optional`"
    - **Nunca como parámetro de un método.** Sobrecarga el método o pasa el valor.
    - **Nunca como campo de una clase.** No es serializable y complica el modelo.
    - **Nunca `Optional<List<T>>`.** Una lista vacía ya expresa «no hay nada».

    Su sitio es **el tipo de retorno** de un método que puede no encontrar lo que busca.

---

## 7. Cuándo excepción y cuándo `Optional`

Es la decisión de esta página, y se contesta con una sola pregunta:

> **¿Que no esté es normal, o es un error?**

| Situación | Qué se devuelve |
|---|---|
| Buscar un producto que puede no existir | `Optional<Producto>` |
| Buscar el producto que el usuario acaba de pedir por su id | **Excepción** |
| Comprobar si un correo ya está registrado | `Optional` o `boolean` |
| Leer un fichero de configuración obligatorio | **Excepción** |

Y lo normal es tener **los dos**, y que quien decide sea la capa de arriba:

```java
class CatalogoRepositorio {
    Optional<Producto> buscarPorCodigo(String codigo) { … }   // solo informa
}

class CatalogoServicio {
    Producto obtener(String codigo) {                          // decide
        return repositorio.buscarPorCodigo(codigo)
                .orElseThrow(() -> new ProductoNoEncontradoException(codigo));
    }
}
```

**El repositorio no sabe si no encontrarlo es grave; el servicio sí.** Ese reparto es exactamente el que se usará en la UT4 y en la UT5.

---

## Pruébalo ahora (10 min)

```java
record Libro(String isbn, String titulo, int anio) {}
var libros = List.of(
    new Libro("978-1", "Ensayo sobre la ceguera", 1995),
    new Libro("978-2", "La colmena", 1951),
    new Libro("978-3", "Nada", 1945));
```

1. Escribe `Optional<Libro> buscar(String isbn)` usando streams.
2. Escribe `Libro obtener(String isbn)` que lance una excepción propia si no está.
3. Devuelve el título en mayúsculas del libro `978-2`, o `"DESCONOCIDO"`, **sin usar ningún `if`**.
4. Provoca un `NumberFormatException` con `Integer.parseInt` y captúralo dando un mensaje útil.
5. Escribe un `catch` vacío y comprueba que el programa sigue como si nada. Después bórralo.

??? success "Solución de las cinco"

    ```java
    // 1 · buscar puede no encontrar: Optional
    Optional<Libro> buscar(String isbn) {
        return libros.stream().filter(l -> l.isbn().equals(isbn)).findFirst();
    }

    // 2 · aquí no encontrarlo SÍ es un error: excepción
    class LibroNoEncontradoException extends RuntimeException {
        LibroNoEncontradoException(String isbn) { super("No existe el ISBN " + isbn); }
    }

    Libro obtener(String isbn) {
        return buscar(isbn).orElseThrow(() -> new LibroNoEncontradoException(isbn));
    }

    // 3 · encadenado, sin un solo if
    buscar("978-2").map(Libro::titulo).map(String::toUpperCase).orElse("DESCONOCIDO");
    // LA COLMENA

    // 4 · capturar dando información útil
    try {
        Integer.parseInt("mil");
    } catch (NumberFormatException e) {
        System.out.println("«mil» no es un número. Detalle: " + e.getMessage());
    }
    // «mil» no es un número. Detalle: For input string: "mil"
    ```

    **5.** El que hay que ver con los propios ojos:

    ```java
    try {
        Integer.parseInt("mil");
    } catch (Exception e) { }                 // ← vacío

    System.out.println("sigo como si nada");
    // sigo como si nada
    ```

    El programa **continúa sin enterarse**. Si esa conversión formaba parte de cargar un fichero, ahora tienes una lista a medias y ningún aviso: el fallo aparecerá dentro de media hora, en otro sitio, y no habrá forma de relacionarlo con esta línea.

    Por eso un `catch` vacío es el peor error de esta página. Si de verdad no hay nada que hacer, como mínimo se deja constancia:

    ```java
    } catch (NumberFormatException e) {
        log.warn("Valor no numérico, se descarta la línea: {}", texto);
    }
    ```

    En los puntos 1 y 2 está lo importante de la página: **el mismo dato ausente puede ser normal o ser un error**, y quien lo decide no es el que busca, sino el que llama.
---

## Ejercicios (con solución)

### E1 — ¿Compila?

```java
String contenido = Files.readString(Path.of("datos.txt"));
```

??? success "Solución"

    **No.** `IOException` es comprobada: hay que capturarla o declararla con `throws`.

    ```java
    try {
        String contenido = Files.readString(Path.of("datos.txt"));
    } catch (IOException e) {
        throw new UncheckedIOException(e);      // envolver, conservando la causa
    }
    ```

    `UncheckedIOException` existe precisamente para esto: convertir una `IOException` en no comprobada sin perder nada.

### E2 — El orden de los `catch`

```java
try { … }
catch (IOException e) { … }
catch (NoSuchFileException e) { … }
```

??? success "Solución"

    **No compila:** `NoSuchFileException` hereda de `IOException`, así que el primer `catch` ya la atrapa y el segundo es inalcanzable. Hay que ponerlos **del más concreto al más general**.

### E3 — `orElse` frente a `orElseThrow`

¿Cuándo se usa cada una de estas dos formas de terminar un `Optional`?

```java
Optional<Usuario> u = repositorio.buscar(id);

u.orElse(new Usuario("invitado"));                          // (a)
u.orElseThrow(() -> new UsuarioNoEncontradoException(id));  // (b)
```

??? success "Solución"


    ```java
    Optional<Usuario> u = repositorio.buscar(id);

    u.orElse(new Usuario("invitado"));                          // (a)
    u.orElseThrow(() -> new UsuarioNoEncontradoException(id));  // (b)
    ```

    - **(a)** cuando no encontrarlo es **normal** y hay una alternativa sensata.
    - **(b)** cuando no encontrarlo es **un error** que hay que comunicar.

    Lo que no vale es `u.get()` sin comprobar: lanza `NoSuchElementException` con un mensaje que no dice nada de tu dominio.

### E4 — La causa perdida

¿Qué le falta a este `catch`, y por qué importa?

```java
catch (SQLException e) {
    throw new RuntimeException("Error de base de datos");
}
```

??? success "Solución"


    ```java
    catch (SQLException e) {
        throw new RuntimeException("Error de base de datos");
    }
    ```

    Falta la `e`. Sin ella, la traza se corta ahí y pierdes la consulta que falló, la tabla y el motivo real.

    ```java
    throw new RuntimeException("Error de base de datos", e);
    ```

    Cuesta tres caracteres y es la diferencia entre depurar en cinco minutos o en dos horas.

### E5 — Excepción o `Optional`

Decide para cada método:

(a) `buscarPorCorreo(String)` en un registro de usuarios · (b) `cargarConfiguracion()` al arrancar · (c) `siguienteMensaje()` de una cola que puede estar vacía · (d) `dividir(int, int)`

??? success "Solución"

    | | Qué devuelve | Por qué |
    |---|---|---|
    | (a) Buscar por correo | `Optional<Usuario>` | Que no esté es normal |
    | (b) Cargar configuración | **Excepción** | Sin configuración no se puede arrancar |
    | (c) Siguiente mensaje | `Optional<Mensaje>` | Una cola vacía es un estado normal |
    | (d) Dividir | **Excepción** | Dividir por cero es un error del que llama |

### E6 — Los recursos, en orden

```java
try (var a = abrir("A"); var b = abrir("B")) { … }
```

¿En qué orden se cierran, y qué pasa si `b` falla al cerrarse?

??? success "Solución"

    Se cierran **`b` primero y `a` después** —orden inverso al de apertura— y **los dos se cierran** aunque `b` lance al cerrarse.

    La excepción de `b` no impide que `a` se cierre; se guarda como *supressed* y aparece en la traza bajo `Suppressed:`.
