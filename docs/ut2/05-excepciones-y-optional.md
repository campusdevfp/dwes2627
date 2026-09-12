# Excepciones y Optional: fallar bien

> En un servidor, las cosas fallan: la BD no responde, el usuario manda basura, el fichero no existe. La diferencia entre un backend profesional y uno amateur está en **cómo gestiona los fallos**.

## 1. Qué es una excepción

Un objeto que representa un error y **interrumpe** el flujo normal del programa hasta que alguien lo captura.

```java
var lista = List.of("a", "b");
IO.println(lista.get(5));    // lanza IndexOutOfBoundsException
```

Si nadie la captura, el programa termina y se imprime la **traza** (*stack trace*): la cadena de llamadas hasta el fallo. Aprende a leerla de abajo arriba: la primera línea de *tu* código es casi siempre la culpable.

## 2. `try / catch / finally`

```java
try {
    var resultado = 10 / divisor;
    IO.println(resultado);
} catch (ArithmeticException e) {
    IO.println("No se puede dividir por cero: " + e.getMessage());
} finally {
    IO.println("Esto se ejecuta SIEMPRE (haya fallo o no)");
}
```

Puedes capturar varias:

```java
} catch (NumberFormatException | ArithmeticException e) {
    IO.println("Error de cálculo: " + e.getMessage());
}
```

!!! danger "Los tres pecados capitales"
    ```java
    catch (Exception e) { }                    // MAL  silenciar: el error desaparece
    catch (Exception e) { e.printStackTrace(); } // MAL  solo imprimir y seguir como si nada
    catch (Exception e) { throw new RuntimeException(e); } // OJO envolver sin aportar contexto
    ```
    Un `catch` vacío es la peor línea que puedes escribir: el fallo ocurre, nadie se entera, y el bug aparece tres semanas después en producción.

## 3. Checked vs. unchecked

| | Checked | Unchecked (`RuntimeException`) |
|---|---|---|
| ¿Obliga el compilador? | **Sí**: hay que capturarla o declararla | No |
| Representa | Algo previsible del entorno | Un error de programación o de datos |
| Ejemplos | `IOException`, `SQLException` | `NullPointerException`, `IllegalArgumentException` |

```java
// Checked: el compilador exige gestionarla
void leer() throws IOException {
    Files.readString(Path.of("datos.txt"));
}

// Unchecked: puedes lanzarla sin declarar nada
if (precio < 0) throw new IllegalArgumentException("Precio negativo");
```

## 4. Excepciones propias

En una aplicación real creas las tuyas, con nombres del **dominio**:

```java
public class ProductoNoEncontradoException extends RuntimeException {
    public ProductoNoEncontradoException(Long id) {
        super("No existe el producto con id " + id);
    }
}
```

```java
throw new ProductoNoEncontradoException(42);
```

Esto conecta directamente con la UT1: en la API, esa excepción se traducirá en un **404** con su mensaje explicativo (Problem Details). Cada excepción de dominio acabará mapeada a su código HTTP.

## 5. `try-with-resources`

Cuando abres algo (fichero, conexión), hay que cerrarlo. Java lo hace por ti:

```java
try (var lector = Files.newBufferedReader(Path.of("datos.txt"))) {
    IO.println(lector.readLine());
}   // se cierra solo, incluso si hay excepción
```

## 6. `Optional`: el fin del `null` a ciegas 

El `NullPointerException` es el error más común de Java. `Optional` hace **explícito** que un valor puede no existir:

```java
Optional<Producto> buscar(Long id) {
    return productos.stream()
        .filter(p -> p.id().equals(id))
        .findFirst();      // devuelve Optional: puede haber o no
}
```

```java
var p = buscar(42L);

p.ifPresent(prod -> IO.println(prod.nombre()));            // haz algo si hay
var nombre = p.map(Producto::nombre).orElse("Desconocido"); // valor por defecto
var prod = p.orElseThrow(() -> new ProductoNoEncontradoException(42L)); // o revienta con sentido
```

:material-alert: **No hagas `optional.get()` sin comprobar**: es volver al punto de partida. Y no uses `Optional` para atributos o parámetros: su sitio es el **retorno** de métodos que pueden no encontrar nada (exactamente lo que devolverán los repositorios de Spring Data).

---

## Pruébalo ahora (10 min)

!!! reto "Trabaja tú ahora"
    Este bloque se hace **en clase, en tu equipo**. No se entrega ni puntúa: es la práctica que hace que el examen te salga.

``` { .java .numerado }
void main() {
    // 1. Provoca y captura
    try {
        var numero = Integer.parseInt("abc");
        IO.println(numero);
    } catch (NumberFormatException e) {
        IO.println("No era un número: " + e.getMessage());
    }

    // 2. finally siempre se ejecuta
    try {
        IO.println(10 / 0);
    } catch (ArithmeticException e) {
        IO.println("Capturada: " + e.getMessage());
    } finally {
        IO.println("Siempre paso por aquí");
    }

    // 3. Optional en acción
    Optional<String> vacio = Optional.empty();
    IO.println(vacio.orElse("valor por defecto"));
}
```

Después, **quita el `try/catch` del punto 2** y ejecuta: verás la traza completa y cómo el programa muere ahí. Ese es el aspecto de un 500 en producción.

---

## Ejercicios (con solución)

### Ejercicio 1 — ¿Qué imprime?
```java
try {
    IO.println("A");
    throw new IllegalStateException("boom");
} catch (IllegalStateException e) {
    IO.println("B");
} finally {
    IO.println("C");
}
IO.println("D");
```

??? success "Solución"

    <code>A</code>, <code>B</code>, <code>C</code>, <code>D</code>. Se lanza tras "A", el catch imprime "B", el finally <b>siempre</b> imprime "C", y como la excepción quedó capturada el programa continúa con "D".


### Ejercicio 2 — Corrige el código
```java
public Producto buscar(Long id) {
    try {
        return repositorio.findById(id);
    } catch (Exception e) {
        return null;
    }
}
```
Señala los dos problemas graves y reescríbelo.

??? success "Solución"

    Problemas: (1) captura <code>Exception</code> genérica y <b>silencia</b> cualquier fallo (incluso un error de conexión a BD); (2) devuelve <code>null</code>, obligando a quien llame a acordarse de comprobarlo → <code>NullPointerException</code> asegurado.

    ```java
    public Producto buscar(Long id) {
        return repositorio.findById(id)
            .orElseThrow(() -> new ProductoNoEncontradoException(id));
    }
    ```
    Ahora el fallo es explícito, tiene nombre de dominio y en la API se traducirá en un <b>404</b> claro.


### Ejercicio 3 — Checked o unchecked
Clasifica: (a) el fichero de configuración no existe · (b) llega un id negativo por parámetro · (c) se cae la conexión con la base de datos · (d) llamas a un método sobre una variable null.

??? success "Solución"

    (a) <b>Checked</b> (<code>IOException</code>) · (b) <b>Unchecked</b> (<code>IllegalArgumentException</code>: error de uso) · (c) <b>Checked</b> (<code>SQLException</code>) · (d) <b>Unchecked</b> (<code>NullPointerException</code>: bug de programación).


### Ejercicio 4 — Optional
Escribe un método que reciba una lista de productos y un nombre, y devuelva el precio del producto o `0.0` si no existe. Usa `Optional`, sin `if (x == null)`.

??? success "Solución"

    ```java
    double precioDe(List<Producto> productos, String nombre) {
        return productos.stream()
            .filter(p -> p.nombre().equalsIgnoreCase(nombre))
            .findFirst()
            .map(Producto::precio)
            .orElse(0.0);
    }
    ```

