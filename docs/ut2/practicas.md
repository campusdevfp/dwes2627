# Prácticas guiadas — UT2

> Programar es como nadar: no se aprende leyendo. Estas 8 prácticas son el entrenamiento del que sale la **sección práctica del test** (100 % de la nota de la unidad).

Todas se ejecutan con `java Fichero.java` (JDK 25), sin proyecto ni configuración. Entrena cada una con el [comprobar tu trabajo con los tests](../comprobar-tu-trabajo/) hasta llegar al **≥ 80 %**.

| # | Práctica | Página | Qué practicas |
|---|----------|--------|---------------|
| P1 | Entorno y primer programa | [1](01-primeros-pasos.md) | JDK, ejecución, errores de compilación |
| P2 | Calculadora de precios | [2](02-sintaxis-y-tipos.md) | Tipos, operadores, división entera |
| P3 | Clasificador con `switch` | [2](02-sintaxis-y-tipos.md) | Control de flujo moderno |
| P4 | Catálogo con records | [3](03-poo-en-java.md) | POO, records, inmutabilidad |
| P5 | Métodos de pago | [3](03-poo-en-java.md) | Interfaces y polimorfismo |
| P6 | Informe de ventas | [4](04-colecciones-y-funcional.md) | Colecciones y streams |
| P7 | Repositorio robusto | [5](05-excepciones-y-optional.md) | Excepciones y `Optional` |
| P8 | Tu primer test | [6](06-proyectos-y-testing.md) | JUnit 5, patrón AAA |

---

## P1 — Entorno y primer programa

**Objetivo:** verificar el entorno y saber leer un error de compilación.

1. Comprueba `java -version` (debe ser 25.x).
2. Crea `Hola.java` que salude con tu nombre e imprima la fecha.
3. **Rompe el programa**: quita un `;`, ejecuta y anota qué dice el compilador (fichero, línea, mensaje).
4. Arréglalo.

**Entregable:** el fichero + captura del error que provocaste.

??? success "Solución"

    ```java
    import java.time.LocalDate;

    void main() {
        var nombre = "Iván";
        IO.println("Hola, " + nombre + ". Hoy es " + LocalDate.now());
    }
    ```
    El error típico: `';' expected` con el número de línea. La clave del ejercicio es **acostumbrarte a leer el compilador** en vez de asustarte.


## P2 — Calculadora de precios

**Objetivo:** dominar tipos y operadores (y la trampa de la división entera).

Escribe un programa que, dado un precio base y una cantidad, calcule: subtotal, descuento del 10 % si se compran más de 5 unidades, IVA del 21 % y total final. Muestra cada paso.

??? success "Solución"

    ```java
    void main() {
        var precioBase = 20.0;      // OJO double, no int
        var cantidad = 8;

        var subtotal = precioBase * cantidad;
        var descuento = cantidad > 5 ? subtotal * 0.10 : 0.0;
        var baseImponible = subtotal - descuento;
        var iva = baseImponible * 0.21;
        var total = baseImponible + iva;

        IO.println("Subtotal:  " + subtotal);
        IO.println("Descuento: " + descuento);
        IO.println("IVA:       " + iva);
        IO.println("TOTAL:     " + total);
    }
    ```
    Si declaras `var precioBase = 20;` (int) los cálculos con `0.10` seguirán funcionando, pero cuidado con operaciones como `9/5` entre enteros. Para dinero real, en producción se usa `BigDecimal`.


## P3 — Clasificador con `switch`

**Objetivo:** usar el `switch` moderno como expresión.

Dado un código de estado HTTP (200, 201, 404, 500…), imprime su significado y si el fallo es del cliente o del servidor. Usa `switch` con `->`.

??? success "Solución"

    ```java
    void main() {
        var codigo = 404;

        var significado = switch (codigo) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 204 -> "No Content";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized (no autenticado)";
            case 403 -> "Forbidden (sin permiso)";
            case 404 -> "Not Found";
            case 500 -> "Internal Server Error";
            default  -> "Desconocido";
        };

        var culpa = switch (codigo / 100) {
            case 2 -> "Éxito";
            case 3 -> "Redirección";
            case 4 -> "Error del CLIENTE";
            case 5 -> "Error del SERVIDOR";
            default -> "?";
        };

        IO.println(codigo + " " + significado + " → " + culpa);
    }
    ```
    Fíjate en `codigo / 100`: la división entera, que en P2 era una trampa, aquí es la solución elegante.


## P4 — Catálogo con records

**Objetivo:** modelar datos con records.

Crea un record `Producto(String nombre, String categoria, double precio)` con validación (precio no negativo, nombre no vacío) y un método `precioConIva()`. Crea una lista de 5 productos e imprímelos.

??? success "Solución"

    ```java
    import java.util.List;

    record Producto(String nombre, String categoria, double precio) {
        Producto {
            if (precio < 0) throw new IllegalArgumentException("Precio negativo: " + precio);
            if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("Nombre vacío");
        }
        double precioConIva() { return precio * 1.21; }
    }

    void main() {
        var catalogo = List.of(
            new Producto("Patinete", "movilidad", 120.0),
            new Producto("Casco", "seguridad", 35.0),
            new Producto("Bici", "movilidad", 450.0),
            new Producto("Candado", "seguridad", 25.0),
            new Producto("Luces", "seguridad", 15.0)
        );

        for (var p : catalogo) {
            IO.println(p.nombre() + " → " + p.precio() + " € (con IVA: " + p.precioConIva() + " €)");
        }
    }
    ```


## P5 — Métodos de pago (interfaces)

**Objetivo:** polimorfismo e inyección de dependencias "a mano".

Define `MetodoPago` con `cobrar(double)`. Implementa `Tarjeta`, `Bizum` y `Transferencia`. Escribe un método `procesarCompra(MetodoPago metodo, double importe)` que funcione con cualquiera.

??? success "Solución"

    ```java
    interface MetodoPago {
        void cobrar(double importe);
    }

    class Tarjeta implements MetodoPago {
        @Override public void cobrar(double i) { IO.println("Tarjeta: " + i + " € con tarjeta"); }
    }
    class Bizum implements MetodoPago {
        @Override public void cobrar(double i) { IO.println("Bizum de " + i + " €"); }
    }
    class Transferencia implements MetodoPago {
        @Override public void cobrar(double i) { IO.println("Transferencia bancaria de " + i + " €"); }
    }

    void procesarCompra(MetodoPago metodo, double importe) {
        IO.println("Procesando compra...");
        metodo.cobrar(importe);       // no sabe NI LE IMPORTA cuál es
    }

    void main() {
        procesarCompra(new Tarjeta(), 50);
        procesarCompra(new Bizum(), 30);
        procesarCompra(new Transferencia(), 200);
    }
    ```
    `procesarCompra` depende de la **interfaz**, no de las clases concretas: eso es DIP, y es justo lo que Spring automatizará inyectando la implementación que corresponda.


## P6 — Informe de ventas (streams)

**Objetivo:** sustituir bucles por streams.

Con el catálogo de P4: (1) nombres de los productos de más de 100 € · (2) precio total del catálogo · (3) el más caro · (4) productos agrupados por categoría · (5) precio medio por categoría.

??? success "Solución"

    ```java
    import java.util.*;
    import java.util.stream.*;

    void main() {
        var catalogo = List.of(
            new Producto("Patinete", "movilidad", 120.0),
            new Producto("Casco", "seguridad", 35.0),
            new Producto("Bici", "movilidad", 450.0),
            new Producto("Candado", "seguridad", 25.0),
            new Producto("Luces", "seguridad", 15.0));

        // 1
        IO.println(catalogo.stream()
            .filter(p -> p.precio() > 100)
            .map(Producto::nombre)
            .toList());                                     // [Patinete, Bici]

        // 2
        IO.println(catalogo.stream().mapToDouble(Producto::precio).sum());   // 645.0

        // 3
        IO.println(catalogo.stream()
            .max(Comparator.comparing(Producto::precio))
            .map(Producto::nombre).orElse("—"));            // Bici

        // 4
        IO.println(catalogo.stream()
            .collect(Collectors.groupingBy(Producto::categoria)));

        // 5
        IO.println(catalogo.stream()
            .collect(Collectors.groupingBy(Producto::categoria,
                     Collectors.averagingDouble(Producto::precio))));
        // {movilidad=285.0, seguridad=25.0}
    }
    ```


## P7 — Repositorio robusto

**Objetivo:** gestionar la ausencia de datos sin `null`.

Escribe una clase `CatalogoRepositorio` con `Optional<Producto> buscarPorNombre(String)` y `Producto obtenerObligatorio(String)` que lance una excepción propia si no existe. Pruébala con un producto que exista y otro que no.

??? success "Solución"

    ```java
    import java.util.*;

    class ProductoNoEncontradoException extends RuntimeException {
        ProductoNoEncontradoException(String nombre) {
            super("No existe el producto: " + nombre);
        }
    }

    class CatalogoRepositorio {
        private final List<Producto> productos;
        CatalogoRepositorio(List<Producto> productos) { this.productos = productos; }

        Optional<Producto> buscarPorNombre(String nombre) {
            return productos.stream()
                .filter(p -> p.nombre().equalsIgnoreCase(nombre))
                .findFirst();
        }

        Producto obtenerObligatorio(String nombre) {
            return buscarPorNombre(nombre)
                .orElseThrow(() -> new ProductoNoEncontradoException(nombre));
        }
    }

    void main() {
        var repo = new CatalogoRepositorio(List.of(
            new Producto("Patinete", "movilidad", 120.0)));

        // 120.0
        IO.println(repo.buscarPorNombre("Patinete").map(Producto::precio).orElse(0.0));
        IO.println(repo.buscarPorNombre("Moto").isPresent());  // false

        try {
            repo.obtenerObligatorio("Moto");
        } catch (ProductoNoEncontradoException e) {
            IO.println("Capturada: " + e.getMessage());
        }
    }
    ```
    Esa excepción de dominio se traducirá en un **404** cuando montemos la API.


## P8 — Tu primer test

**Objetivo:** escribir tests con sentido.

Para la `CalculadoraPrecios` de la página 6, escribe (en pseudo-JUnit o JUnit real si ya tienes el proyecto) **tres tests**: caso normal, caso con descuento y caso de error.

??? success "Solución"

    ```java
    class CalculadoraPreciosTest {

        @Test
        void noAplicaDescuentoConPocasUnidades() {
            var c = new CalculadoraPrecios();
            assertEquals(40.0, c.total(2, 20.0));
        }

        @Test
        void aplicaDescuentoDel10PorCientoDesde6Unidades() {
            var c = new CalculadoraPrecios();
            assertEquals(180.0, c.total(10, 20.0));
        }

        @Test
        void lanzaExcepcionConCantidadNegativa() {
            var c = new CalculadoraPrecios();
            assertThrows(IllegalArgumentException.class, () -> c.total(-1, 20.0));
        }
    }
    ```
    Los nombres describen la **regla de negocio**: si uno falla, sabes qué se rompió sin abrir el código.


---

## Cómo entrenar para el test

El test de la UT2 pregunta sobre **estas prácticas**: qué imprime un fragmento, dónde está el error de compilación, qué colección usar, qué hace un stream, por qué falla un `catch`. Repasa tus soluciones, pásalas al [comprobar tu trabajo con los tests](../comprobar-tu-trabajo/) y pídele simulacros de las que lleves flojas.
