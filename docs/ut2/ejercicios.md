# Batería de ejercicios — UT2

**32 ejercicios con solución**, ordenados **igual que los temas** y de menos a más dentro de cada bloque. No hay saltos: cada ejercicio usa solo lo que ya ha salido antes.

| | |
|:-:|---|
| ● | Se resuelve en 5 minutos, con lo que acabas de leer |
| ●● | Hay que juntar dos ideas del tema |
| ●●● | Programa completo o refactor |

!!! tip "Cómo se hace esto"
    Los ● y ●● se teclean en **`jshell`**; los ●●● son ficheros `.java` que se ejecutan con `java Fichero.java`.

    **Predice la salida antes de ejecutar.** El test de la S8 son exactamente estas preguntas, y solo las reconoce quien se ha equivocado antes.

---

# Bloque 1 · Primeros pasos y sintaxis

> Tema [1. Primeros pasos](01-primeros-pasos.md) y [2. Sintaxis y tipos](02-sintaxis-y-tipos.md)

## E1 ● — División entera

¿Qué imprime cada línea?

```java
System.out.println(7 / 2);
System.out.println(7 / 2.0);
System.out.println(7 % 2);
System.out.println(0.1 + 0.2);
```

??? success "Solución"

    **`3`, `3.5`, `1` y `0.30000000000000004`.**

    - `7 / 2` son dos `int`: división **entera**, se trunca. Para obtener `3.5` basta con que uno de los dos sea `double`.
    - `0.1 + 0.2` no da `0.3` porque `double` es binario y esos decimales no tienen representación exacta. **Para dinero se usa `BigDecimal`**, nunca `double`.

## E2 ● — `var`: cuándo sí y cuándo no

¿Cuáles compilan?

```java
var a = 10;
var b = "hola";
var c;
var d = null;
var e = new ArrayList<String>();
```

??? success "Solución"

    Compilan **`a`, `b` y `e`**.

    - `var c;` no: `var` deduce el tipo **del valor**, y aquí no hay valor.
    - `var d = null;` tampoco: `null` no dice de qué tipo es.

    `var` solo vale para variables locales con valor inicial. Nunca para campos ni parámetros. Y úsalo cuando el tipo se vea a la derecha: `var x = servicio.calcular();` esconde información y es peor que escribir el tipo.

## E3 ● — Bloques de texto

Escribe con un bloque de texto un JSON de ejemplo, y di qué hace el `\` final de línea.

??? success "Solución"

    ```java
    var json = """
        {
          "nombre": "Ana",
          "edad": 34
        }""";
    System.out.println(json);
    ```

    - **No hay que escapar las comillas.**
    - La **indentación común** se quita sola: manda la línea menos indentada (incluida la de cierre).
    - Cerrar `"""` en la misma línea que el contenido evita el salto final.
    - Un `\` al final de una línea **une esa línea con la siguiente** sin salto.

## E4 ●● — `switch` moderno

Convierte esto a `switch` con flechas, sin `break`:

```java
String tipo;
switch (codigo / 100) {
    case 2: tipo = "OK"; break;
    case 4: tipo = "Error del cliente"; break;
    case 5: tipo = "Error del servidor"; break;
    default: tipo = "Otro";
}
```

??? success "Solución"

    ```java
    var tipo = switch (codigo / 100) {
        case 2 -> "OK";
        case 4 -> "Error del cliente";
        case 5 -> "Error del servidor";
        default -> "Otro";
    };
    System.out.println(tipo);
    ```

    El `switch` moderno **devuelve un valor**, no hay caída entre casos y el compilador exige que estén todos los casos cubiertos. Se acabaron los bugs por un `break` olvidado.

## E5 ●● — `==` frente a `equals`

```java
var a = "hola";
var b = "hola";
var c = new String("hola");

System.out.println(a == b);
System.out.println(a == c);
System.out.println(a.equals(c));
```

??? success "Solución"

    **`true`, `false` y `true`.**

    `a` y `b` apuntan al mismo literal reutilizado por la JVM; `c` es un objeto nuevo. `==` compara **si son el mismo objeto**; `equals` compara **el contenido**.

    **Para textos, siempre `equals`.** Que a veces `==` salga `true` es lo que hace este fallo tan difícil de encontrar.

## E6 ●●● — Calculadora de precios

`Precios.java`: dado un importe y un tipo de cliente (`NORMAL`, `SOCIO`, `EMPLEADO`), aplica el descuento (0 %, 10 %, 25 %), suma el 21 % de IVA e imprime el desglose con dos decimales.

??? success "Solución"

    ```java
    // Precios.java
    void main() {
        for (var tipo : new String[]{"NORMAL", "SOCIO", "EMPLEADO", "OTRO"}) {
            mostrar(100.0, tipo);
        }
    }

    void mostrar(double base, String tipo) {
        double descuento = switch (tipo) {
            case "SOCIO"    -> 0.10;
            case "EMPLEADO" -> 0.25;
            default         -> 0.0;
        };
        double conDescuento = base * (1 - descuento);
        double total = conDescuento * 1.21;
        IO.println("%-9s base %.2f  dto %.0f%%  →  %.2f € (IVA incl.)"
                .formatted(tipo, base, descuento * 100, total));
    }
    ```

    ```
    NORMAL    base 100,00  dto 0%  →  121,00 € (IVA incl.)
    SOCIO     base 100,00  dto 10%  →  108,90 € (IVA incl.)
    EMPLEADO  base 100,00  dto 25%  →  90,75 € (IVA incl.)
    OTRO      base 100,00  dto 0%  →  121,00 € (IVA incl.)
    ```

    Java 25 permite `void main()` sin clase y sin `static`, y `IO.println`. Y `formatted` se lee mucho mejor que concatenar con `+`.

---

# Bloque 2 · POO: records, clases e interfaces

> Tema [3. POO en Java](03-poo-en-java.md)

## E7 ● — `record` frente a clase

¿Qué imprime?

```java
record Punto(int x, int y) {}

var a = new Punto(1, 2);
var b = new Punto(1, 2);

System.out.println(a);
System.out.println(a.equals(b));
System.out.println(a == b);
System.out.println(a.x());
```

??? success "Solución"

    **`Punto[x=1, y=2]`, `true`, `false` y `1`.**

    Un `record` trae gratis `toString`, `equals`, `hashCode` y los captadores. La misma clase escrita a mano son 40 líneas.

    `a == b` sigue siendo `false`: son dos objetos distintos, aunque `equals` diga que valen lo mismo.

    Los captadores se llaman `x()`, no `getX()`.

## E8 ●● — Validar en el constructor

Haz que `new Producto("", -5)` falle en el momento de crearse.

```java
record Producto(String nombre, double precio) {}
```

??? success "Solución"

    ```java
    record Producto(String nombre, double precio) {
        Producto {
            if (nombre == null || nombre.isBlank())
                throw new IllegalArgumentException("El nombre no puede estar vacío");
            if (precio < 0)
                throw new IllegalArgumentException("Precio negativo: " + precio);
            nombre = nombre.trim();
        }
    }

    System.out.println(new Producto("  Casco ", 35));
    // Producto[nombre=Casco, precio=35.0]

    new Producto("", -5);
    // lanza java.lang.IllegalArgumentException: El nombre no puede estar vacío
    ```

    Eso es el **constructor compacto**: sin paréntesis ni asignaciones. Puedes reasignar los parámetros (como el `trim`) y Java los guarda después.

    Un objeto que valida al nacer **nunca existe en estado inválido**. Es la mitad de la ventaja de los records.

## E9 ●● — Inmutabilidad de verdad

Este `Equipo` dice ser inmutable. Rómpelo.

```java
record Equipo(String nombre, List<String> jugadores) {}
```

??? success "Solución"

    ```java
    var lista = new ArrayList<>(List.of("Ana", "Bruno"));
    var e = new Equipo("Rojo", lista);

    lista.add("Intruso");
    System.out.println(e.jugadores());    // [Ana, Bruno, Intruso]
    ```

    El `record` protege **la referencia**, no el contenido. Quien te dio la lista puede seguir modificándola.

    ```java
    record Equipo(String nombre, List<String> jugadores) {
        Equipo {
            jugadores = List.copyOf(jugadores);   // copia inmutable
        }
    }
    var e2 = new Equipo("Rojo", lista);
    lista.add("Otro");
    System.out.println(e2.jugadores());    // [Ana, Bruno, Intruso]  ← no cambia
    ```

    `List.copyOf` hace las dos cosas: copia y congela.

## E10 ●● — `enum` con datos

Crea un `enum EstadoHttp` con el código y el mensaje, y un método `esError()`.

??? success "Solución"

    ```java
    enum EstadoHttp {
        OK(200, "OK"),
        NO_ENCONTRADO(404, "Not Found"),
        ERROR_SERVIDOR(500, "Internal Server Error");

        private final int codigo;
        private final String mensaje;

        EstadoHttp(int codigo, String mensaje) {
            this.codigo = codigo;
            this.mensaje = mensaje;
        }

        int codigo()       { return codigo; }
        boolean esError()  { return codigo >= 400; }
    }

    for (var e : EstadoHttp.values())
        System.out.println(e + " " + e.codigo() + " error=" + e.esError());
    // OK 200 error=false
    // NO_ENCONTRADO 404 error=true
    // ERROR_SERVIDOR 500 error=true
    ```

    Un `enum` no es una lista de constantes: **es una clase** con campos y métodos, y con un número fijo de instancias.

## E11 ●● — Para qué sirve una interfaz

Escribe un `Notificador` con dos implementaciones y un servicio que no cambie al añadir la tercera.

??? success "Solución"

    ```java
    interface Notificador {
        void enviar(String destino, String mensaje);
    }

    class NotificadorEmail implements Notificador {
        public void enviar(String d, String m) { System.out.println("[email] " + d + ": " + m); }
    }

    class NotificadorSms implements Notificador {
        public void enviar(String d, String m) { System.out.println("[sms] " + d + ": " + m); }
    }

    class ServicioPedidos {
        private final Notificador notificador;
        ServicioPedidos(Notificador n) { this.notificador = n; }
        void confirmar(String cliente) {
            notificador.enviar(cliente, "Pedido confirmado");
        }
    }

    new ServicioPedidos(new NotificadorEmail()).confirmar("ana@iesx.es");
    // [email] ana@iesx.es: Pedido confirmado
    new ServicioPedidos(new NotificadorSms()).confirmar("600111222");
    // [sms] 600111222: Pedido confirmado
    ```

    `ServicioPedidos` **no sabe** cómo se envía. Añadir Telegram es una clase nueva y cero líneas tocadas.

    Y la consecuencia grande, que verás en la UT4: puedes pasarle un `NotificadorFalso` que solo apunte lo que le piden, y **probar el servicio sin enviar nada**.

## E12 ●●● — Catálogo con records

`Catalogo.java`: un `record Articulo(String codigo, String nombre, double precio)`, una lista de cinco, y un menú que imprime el catálogo, el total y el artículo más caro.

??? success "Solución"

    ```java
    // Catalogo.java
    import java.util.*;

    record Articulo(String codigo, String nombre, double precio) {
        Articulo {
            if (precio < 0) throw new IllegalArgumentException("Precio negativo");
        }
        String etiqueta() { return "%-6s %-12s %8.2f €".formatted(codigo, nombre, precio); }
    }

    void main() {
        var catalogo = List.of(
            new Articulo("A1", "Casco",    35.00),
            new Articulo("A2", "Bici",    450.00),
            new Articulo("A3", "Candado",  25.50),
            new Articulo("A4", "Luces",    15.00),
            new Articulo("A5", "Patinete",120.00));

        IO.println("=== Catálogo ===");
        catalogo.forEach(a -> IO.println("  " + a.etiqueta()));

        double total = 0;
        Articulo caro = catalogo.get(0);
        for (var a : catalogo) {
            total += a.precio();
            if (a.precio() > caro.precio()) caro = a;
        }
        IO.println("\nTotal:     %.2f €".formatted(total));
        IO.println("Más caro:  " + caro.nombre());
    }
    ```

    Hecho **con bucles a propósito**: en el bloque 4 lo reescribirás con streams y verás la diferencia.

---

# Bloque 3 · Colecciones

> Tema [4. Colecciones y streams](04-colecciones-y-funcional.md) §1–4

## E13 ● — Lo que devuelve un `Map`

```java
var m = new HashMap<String, Integer>();
m.put("pera", 3);
m.put("manzana", 5);
m.put("pera", 7);

System.out.println(m.size());
System.out.println(m.get("pera"));
System.out.println(m.get("kiwi"));
System.out.println(m.getOrDefault("kiwi", 0));
```

??? success "Solución"

    **`2`, `7`, `null` y `0`.**

    `put` con una clave que ya existe **sustituye**. Y `get` de una clave que no está devuelve `null`: si después haces `+ 1`, tienes un `NullPointerException`. Por eso existe `getOrDefault`.

## E14 ● — El `remove` traicionero

```java
var l = new ArrayList<>(List.of(10, 20, 30));
l.remove(1);
System.out.println(l);
```

??? success "Solución"

    **`[10, 30]`.** Con `List<Integer>`, `remove(int)` borra la **posición**.

    Para borrar el valor 1: `l.remove(Integer.valueOf(1))`. Con `List<String>` no hay ambigüedad.

## E15 ● — Elegir la colección

(a) los DNI ya registrados · (b) el historial de páginas visitadas · (c) el stock de cada código · (d) etiquetas sin repetir y en orden alfabético

??? success "Solución"

    | | Cuál | Por qué |
    |---|---|---|
    | (a) | `HashSet<String>` | Solo importa si está; sin repetidos |
    | (b) | `ArrayList<String>` | Orden y repetidos |
    | (c) | `HashMap<String,Integer>` | Búsqueda directa por clave |
    | (d) | `TreeSet<String>` | Sin repetidos y ordenado |

    La **(c)** es la que más se falla. Una `List<Producto>` que se recorre buscando funciona con veinte productos y se arrastra con veinte mil.

## E16 ●● — Contar sin `if`

Cuenta cuántas veces aparece cada palabra, con **una sola línea** dentro del bucle.

```java
var texto = List.of("sol", "mar", "sol", "aire", "mar", "sol");
```

??? success "Solución"

    ```java
    var cuenta = new HashMap<String, Integer>();
    texto.forEach(p -> cuenta.merge(p, 1, Integer::sum));
    System.out.println(cuenta);       // {aire=1, sol=3, mar=2}
    ```

    `merge` se lee: «si no está, pon 1; si está, súmale 1». Sin él son cinco líneas y un `if`.

## E17 ●● — Agrupar a mano

Agrupa las palabras por su primera letra, **sin streams**.

??? success "Solución"

    ```java
    var porLetra = new HashMap<Character, List<String>>();
    for (var p : List.of("sol", "mar", "sal", "mes", "azul")) {
        porLetra.computeIfAbsent(p.charAt(0), k -> new ArrayList<>()).add(p);
    }
    System.out.println(porLetra);     // {a=[azul], s=[sol, sal], m=[mar, mes]}
    ```

    `computeIfAbsent` dice: «si no hay lista para esa letra, créala». Después se añade siempre.

    En el bloque siguiente esto será una línea con `groupingBy` — pero conviene haberlo hecho a mano una vez.

## E18 ●● — Modificar mientras recorres

Escribe la versión que **falla** y la que funciona para quitar los pares de una lista.

??? success "Solución"

    ```java
    var l = new ArrayList<>(List.of(1, 2, 3, 4, 5));

    // FALLA
    for (var n : l) { if (n % 2 == 0) l.remove(n); }
    // lanza java.util.ConcurrentModificationException

    // BIEN
    l.removeIf(n -> n % 2 == 0);
    System.out.println(l);            // [1, 3, 5]
    ```

    El `for-each` usa un iterador por debajo y el `remove` lo deja obsoleto. Y ojo: `removeIf` necesita una lista **mutable**; sobre `List.of(...)` lanza `UnsupportedOperationException`.

---

# Bloque 4 · Streams

> Tema [4. Colecciones y streams](04-colecciones-y-funcional.md) §5–7

Todos los ejercicios de este bloque usan estos datos. Pégalos en `jshell`:

```java
import java.util.*;
import java.util.stream.*;

record Producto(String nombre, String categoria, double precio, int stock) {}

var productos = List.of(
    new Producto("Patinete", "movilidad", 120.0, 4),
    new Producto("Casco",    "seguridad",  35.0, 12),
    new Producto("Bici",     "movilidad", 450.0, 2),
    new Producto("Candado",  "seguridad",  25.0, 0),
    new Producto("Luces",    "seguridad",  15.0, 30));
```

## E19 ● — Filtrar y transformar

Los nombres de los productos que cuestan menos de 100 €, en mayúsculas y ordenados.

??? success "Solución"

    ```java
    productos.stream()
             .filter(p -> p.precio() < 100)
             .map(p -> p.nombre().toUpperCase())
             .sorted()
             .toList();
    // [CANDADO, CASCO, LUCES]
    ```

    Se lee de arriba abajo: primero se cae lo caro, después lo que queda se convierte en texto, después se ordena.

    **Detalle:** después del `map` ya no hay productos, hay textos. Un `sorted(Comparator.comparingDouble(Producto::precio))` detrás del `map` **no compila**.

## E20 ● — Sumar y contar

El precio total de la categoría `movilidad`, y cuántos productos hay sin stock.

??? success "Solución"

    ```java
    productos.stream()
             .filter(p -> p.categoria().equals("movilidad"))
             .mapToDouble(Producto::precio)
             .sum();                                       // 570.0

    productos.stream().filter(p -> p.stock() == 0).count(); // 1
    ```

    `mapToDouble` convierte el stream a números y desbloquea `sum()`, `average()` y `max()`.

## E21 ●● — Comparador con desempate

Ordena por categoría **descendente** y, dentro de cada una, por nombre ascendente.

??? success "Solución"

    ```java
    productos.stream()
             .sorted(Comparator.comparing(Producto::categoria).reversed()
                               .thenComparing(Producto::nombre))
             .map(Producto::nombre).toList();
    // [Candado, Casco, Luces, Bici, Patinete]
    ```

    **Dónde va el `.reversed()` es la pregunta.** Invierte **todo lo encadenado hasta ese punto**, así que va pegado a lo que quieres invertir. Si lo pones al final, invierte también el nombre.

## E22 ●● — Agrupar y contar

Cuántos productos hay de cada categoría, **con las categorías en orden alfabético**.

??? success "Solución"

    ```java
    productos.stream().collect(Collectors.groupingBy(
            Producto::categoria, TreeMap::new, Collectors.counting()));
    // {movilidad=2, seguridad=3}
    ```

    Sin el `TreeMap::new` sale un `HashMap` y **el orden no está garantizado**. Que con cinco productos salga siempre igual es exactamente lo que engaña: el día que cambien los datos, cambia el orden.

    El `TreeMap::new` va **en medio**, entre el criterio y lo que se hace con cada grupo.

## E23 ●● — Valor por categoría

El valor del inventario (`precio × stock`) de cada categoría, ordenado.

??? success "Solución"

    ```java
    productos.stream().collect(Collectors.groupingBy(
            Producto::categoria, TreeMap::new,
            Collectors.summingDouble(p -> p.precio() * p.stock())));
    // {movilidad=1380.0, seguridad=870.0}
    ```

    Y si lo que quieres de cada grupo son solo los nombres:

    ```java
    productos.stream().collect(Collectors.groupingBy(
            Producto::categoria, TreeMap::new,
            Collectors.mapping(Producto::nombre, Collectors.toList())));
    // {movilidad=[Patinete, Bici], seguridad=[Casco, Candado, Luces]}
    ```

    Siempre el mismo esquema: `groupingBy(cómo agrupo, [en qué mapa], qué hago con cada grupo)`.

## E24 ●●● — De bucles a streams

Reescribe el `Catalogo.java` del E12 con streams, y añade el desglose por categoría.

??? success "Solución"

    ```java
    // Informe.java
    import java.util.*;
    import java.util.stream.*;

    record Articulo(String codigo, String nombre, String categoria, double precio) {}

    void main() {
        var catalogo = List.of(
            new Articulo("A1", "Casco",    "seguridad",  35.00),
            new Articulo("A2", "Bici",     "movilidad", 450.00),
            new Articulo("A3", "Candado",  "seguridad",  25.50),
            new Articulo("A4", "Luces",    "seguridad",  15.00),
            new Articulo("A5", "Patinete", "movilidad", 120.00));

        IO.println("=== Por precio ===");
        catalogo.stream()
                .sorted(Comparator.comparingDouble(Articulo::precio))
                .forEach(a -> IO.println("  %-10s %8.2f €".formatted(a.nombre(), a.precio())));

        IO.println("\n=== Por categoría ===");
        catalogo.stream()
                .collect(Collectors.groupingBy(Articulo::categoria, TreeMap::new,
                         Collectors.summingDouble(Articulo::precio)))
                .forEach((c, v) -> IO.println("  %-12s %8.2f €".formatted(c, v)));

        IO.println("\nTotal:    %.2f €".formatted(
                catalogo.stream().mapToDouble(Articulo::precio).sum()));
        IO.println("Más caro: " + catalogo.stream()
                .max(Comparator.comparingDouble(Articulo::precio))
                .map(Articulo::nombre).orElse("—"));
    }
    ```

    Compara con el E12: **las 12 líneas de bucles y acumuladores se han quedado en 4**, y el `max` ya no necesita una variable centinela.

---

# Bloque 5 · Excepciones y `Optional`

> Tema [5. Excepciones y Optional](05-excepciones-y-optional.md)

## E25 ● — `finally`

```java
try {
    System.out.println(10 / 0);
} catch (ArithmeticException e) {
    System.out.println("A");
} finally {
    System.out.println("B");
}
System.out.println("C");
```

??? success "Solución"

    **`A`, `B` y `C`.** El `catch` corta la excepción, `finally` se ejecuta siempre y el programa continúa. Sin el `catch`, saldría `B` y el programa moriría sin llegar a `C`.

## E26 ●● — Tu propia excepción

Crea `ProductoNoEncontradoException` y un `buscarProducto(String codigo)` que la lance con un mensaje que incluya el código.

??? success "Solución"

    ```java
    class ProductoNoEncontradoException extends RuntimeException {
        ProductoNoEncontradoException(String codigo) {
            super("No existe el producto con código " + codigo);
        }
    }

    var catalogo = Map.of("A1", "Casco", "B2", "Bici");

    String buscarProducto(String codigo) {
        var nombre = catalogo.get(codigo);
        if (nombre == null) throw new ProductoNoEncontradoException(codigo);
        return nombre;
    }

    System.out.println(buscarProducto("A1"));   // Casco
    buscarProducto("Z9");
    // lanza ProductoNoEncontradoException: No existe el producto con código Z9
    ```

    El constructor recibe **el dato**, no el mensaje montado: así todos los mensajes salen iguales.

## E27 ●● — De excepción a `Optional`

`precio(String texto)` que devuelva `Optional<Double>`: vacío si el texto no es un número o si es negativo.

??? success "Solución"

    ```java
    Optional<Double> precio(String texto) {
        try {
            double d = Double.parseDouble(texto);
            return d < 0 ? Optional.empty() : Optional.of(d);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
    System.out.println(precio("12.50"));   // Optional[12.5]
    System.out.println(precio("-3"));      // Optional.empty
    System.out.println(precio("caro"));    // Optional.empty
    ```

    `parseDouble` avisa con una **excepción**, pero para quien llama a `precio` un texto malo es un caso normal, así que se traduce a **`Optional`**. El `catch` no está vacío: transforma.

    Es exactamente el patrón que usarás en la UT3 para leer un CSV sucio.

## E28 ●● — Encadenar sin un solo `if`

Con `buscar(int id)` que devuelve `Optional<Cliente>`, escribe en **una expresión** el dominio del correo (lo de detrás de la `@`), o `"sin correo"`.

??? success "Solución"

    ```java
    record Cliente(int id, String nombre, String email) {}
    var clientes = List.of(new Cliente(1, "Ana", "ana@iesx.es"));

    Optional<Cliente> buscar(int id) {
        return clientes.stream().filter(c -> c.id() == id).findFirst();
    }

    String dominio(int id) {
        return buscar(id)
                .map(Cliente::email)
                .filter(e -> e.contains("@"))
                .map(e -> e.substring(e.indexOf('@') + 1))
                .orElse("sin correo");
    }
    System.out.println(dominio(1));    // iesx.es
    System.out.println(dominio(99));   // sin correo
    ```

    Tres cosas pueden faltar y **no hay un solo `if`**: en cuanto la caja se vacía, los pasos siguientes se saltan.

## E29 ●● — Dos malos usos

Di qué está mal:

```java
// (a)
try { guardar(pedido); } catch (Exception e) { }

// (b)
if (buscar(1).isPresent()) System.out.println(buscar(1).get().nombre());
```

??? success "Solución"

    **(a)** El pedido **no se ha guardado** y el programa sigue como si sí. El fallo aparecerá días después, en otro sitio, sin rastro.

    ```java
    try {
        guardar(pedido);
    } catch (SQLException e) {
        throw new PedidoException("No se pudo guardar el pedido " + pedido.id(), e);
    }
    ```

    Se captura **lo concreto** y se relanza **con la causa**: ese `Caused by:` es lo que lleva al origen.

    **(b)** Compila, pero es `Optional` escrito como si fuera `null`: hace la búsqueda **dos veces** y usa `get()`.

    ```java
    buscar(1).map(Cliente::nombre).ifPresent(System.out::println);
    ```

---

# Bloque 6 · Proyecto Maven

> Tema [6. Un proyecto Maven](06-proyecto-maven.md)

## E30 ● — Leer el `pom.xml`

Quita `<maven.compiler.release>` del `pom.xml` y compila. ¿Qué error sale y por qué?

??? success "Solución"

    Sin esa propiedad, Maven compila con una versión antigua por defecto y todo lo moderno revienta:

    ```
    [ERROR] ... records are not supported in -source 8
    [ERROR] ... use -source 16 or higher to enable records
    ```

    ```xml
    <properties>
      <maven.compiler.release>25</maven.compiler.release>
      <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>
    ```

    `release` (y no `source`/`target`) garantiza además que no uses por accidente una API que no existe en esa versión. Y sin `sourceEncoding`, las tildes se rompen en cuanto alguien compile en otro sistema.

## E31 ●● — Añadir una dependencia

Añade Jackson, escribe algo que lo use y comprueba qué ha arrastrado.

??? success "Solución"

    ```xml
    <dependency>
      <groupId>com.fasterxml.jackson.core</groupId>
      <artifactId>jackson-databind</artifactId>
      <version>2.18.2</version>
    </dependency>
    ```

    ```bash
    mvn dependency:tree
    ```

    ```
    \- com.fasterxml.jackson.core:jackson-databind:jar:2.18.2:compile
       +- com.fasterxml.jackson.core:jackson-annotations:jar:2.18.2:compile
       \- com.fasterxml.jackson.core:jackson-core:jar:2.18.2:compile
    ```

    Una dependencia **arrastra las suyas**. Por eso un proyecto con cinco líneas en el `pom.xml` acaba con treinta `.jar`, y por eso conviene mirar el árbol de vez en cuando.

## E32 ●●● — El catálogo, en un proyecto Maven

Monta el E24 como proyecto Maven con la estructura estándar y empaquétalo en un `.jar` ejecutable.

??? success "Solución"

    ```
    catalogo/
    ├── pom.xml
    └── src/main/java/es/iesx/catalogo/
        ├── Articulo.java
        └── Main.java
    ```

    ```xml
    <properties>
      <maven.compiler.release>25</maven.compiler.release>
      <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
      <exec.mainClass>es.iesx.catalogo.Main</exec.mainClass>
    </properties>

    <build><plugins>
      <plugin>
        <groupId>org.apache.maven.plugins</groupId>
        <artifactId>maven-shade-plugin</artifactId>
        <version>3.6.0</version>
        <executions><execution>
          <phase>package</phase>
          <goals><goal>shade</goal></goals>
          <configuration>
            <transformers>
              <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                <mainClass>es.iesx.catalogo.Main</mainClass>
              </transformer>
            </transformers>
          </configuration>
        </execution></executions>
      </plugin>
    </plugins></build>
    ```

    ```bash
    mvn clean package
    java -jar target/catalogo-1.0-SNAPSHOT.jar
    ```

    Dentro de un proyecto Maven, `Main.java` vuelve a necesitar `public static void main(String[] args)`: el `void main()` suelto es para ejecutar ficheros sueltos.

    **El `.jar` sin el shade plugin no arranca** si hay dependencias: no las lleva dentro. Es el fallo clásico de la primera entrega.

---

## Reparto sugerido

| Sesión | Bloque | Ejercicios |
|:-:|---|---|
| **S1** | 1 · Primeros pasos y sintaxis | E1 – E6 |
| **S2** | 2 · POO §1–4 | E7 – E10 |
| **S3** | 2 · POO §5–6 | E11 – E12 |
| **S4** | 3 · Colecciones | E13 – E18 |
| **S5** | 4 · Streams | E19 – E24 |
| **S6** | 5 · Excepciones y `Optional` | E25 – E29 |
| **S7** | 6 · Proyecto Maven | E30 – E32 |
| **S8** | — | [Simulacro de test](autoevaluacion.md) |

!!! success "Si vas justo de tiempo"
    El mínimo para aprobar el test: **E1, E5, E7, E13, E14, E19, E22, E25, E27**. Son los nueve que más veces aparecen en el simulacro.

    Pero el **E22** (`groupingBy` no ordena) y el **E27** (excepción → `Optional`) son los dos que separan un 5 de un 8.
