# Simulacro de test — UT2

**30 preguntas, 55 minutos.** Es el examen de la S8: mismo número, mismo formato y mismo reparto.

| Bloque | De dónde sale | Preguntas |
|---|---|:-:|
| **1** | Primeros pasos y sintaxis | 5 |
| **2** | POO: records, clases e interfaces | 5 |
| **3** | Colecciones | 6 |
| **4** | Streams | 7 |
| **5** | Excepciones y `Optional` | 4 |
| **6** | Proyecto Maven | 3 |

!!! success "Todas salen de un ejercicio que ya has hecho"
    Cada pregunta lleva debajo **el ejercicio de la batería del que sale**. Si has hecho la [batería](ejercicios.md), has visto antes todos estos fragmentos en tu pantalla.

    Las opciones incorrectas son **los errores que se cometen escribiendo ese código**, no distractores inventados.

!!! tip "Cómo se hace"
    Tápate la solución, contesta, y **solo entonces** despliega. Si dudas, **ejecútalo**: `jshell` arranca en dos segundos.

Se aprueba con 15 y **no hay penalización por fallo**: contesta todas.

---

## Bloque 1 — Primeros pasos y sintaxis

### 1 · ¿Qué imprime?

``` { .java .numerado }
System.out.println(7 / 2);
System.out.println(7 / 2.0);
```

**a)** `3.5` y `3.5` · **b)** `3` y `3.5` · **c)** `3` y `3` · **d)** `3.5` y `3`

??? success "Solución"

    **b)** Dos `int` dan **división entera**: se trunca, no se redondea (`9/2` es `4`). Basta con que uno de los dos sea `double` para que no.

    → [E1](ejercicios.md)

### 2 · ¿Cuál no compila?

**a)** `var a = 10;` · **b)** `var b = "hola";` · **c)** `var c = null;` · **d)** `var d = new ArrayList<String>();`

??? success "Solución"

    **c)** `null` no dice de qué tipo es, así que `var` no puede deducir nada. Tampoco compila `var c;` sin valor.

    `var` es solo para **variables locales con valor inicial**. Nunca campos ni parámetros.

    → [E2](ejercicios.md)

### 3 · El `switch` moderno

``` { .java .numerado }
var tipo = switch (codigo / 100) {
    case 2 -> "OK";
    case 4 -> "Error del cliente";
};
```

**a)** Funciona · **b)** No compila: falta `default` · **c)** No compila: faltan los `break` · **d)** Funciona pero devuelve `null` si no encaja

??? success "Solución"

    **b)** Un `switch` que **devuelve un valor** tiene que cubrir todos los casos. Con `int` eso significa `default` obligatorio.

    La (c) es el reflejo del `switch` antiguo: con flechas **no hay caída** entre casos y los `break` no existen.

    → [E4](ejercicios.md)

### 4 · ¿Qué imprime?

``` { .java .numerado }
var a = "hola";
var c = new String("hola");
System.out.println(a == c);
System.out.println(a.equals(c));
```

**a)** `true` y `true` · **b)** `false` y `true` · **c)** `true` y `false` · **d)** `false` y `false`

??? success "Solución"

    **b)** `==` compara **si son el mismo objeto**; `equals` compara el **contenido**.

    Lo que hace este fallo tan difícil de encontrar es que con literales (`"hola" == "hola"`) sale `true`, porque la JVM los reutiliza. En cuanto el texto viene de un fichero o de un formulario, deja de salir. **Para textos, siempre `equals`.**

    → [E5](ejercicios.md)

### 5 · Bloques de texto

¿Qué hace un `\` al final de una línea dentro de un `"""`?

**a)** Escapa la comilla siguiente · **b)** Une esa línea con la siguiente, sin salto · **c)** Es un error de sintaxis · **d)** Indenta la línea siguiente

??? success "Solución"

    **b)** Sirve para escribir una línea larga partida en el código sin que el salto acabe en el texto.

    Y lo otro que hay que saber: **la indentación común se quita sola**, y manda la línea menos indentada, incluida la de cierre `"""`.

    → [E3](ejercicios.md)

---

## Bloque 2 — POO

### 6 · ¿Qué imprime?

``` { .java .numerado }
record Punto(int x, int y) {}
var a = new Punto(1, 2);
var b = new Punto(1, 2);
System.out.println(a.equals(b));
System.out.println(a == b);
```

**a)** `true` y `true` · **b)** `true` y `false` · **c)** `false` y `false` · **d)** `false` y `true`

??? success "Solución"

    **b)** El `record` genera `equals` comparando los campos, así que dos puntos iguales **son iguales**. Pero siguen siendo **dos objetos distintos**, así que `==` es `false`.

    Un `record` trae gratis `toString`, `equals`, `hashCode` y los captadores, que se llaman `x()`, no `getX()`.

    → [E7](ejercicios.md)

### 7 · El constructor compacto

``` { .java .numerado }
record Producto(String nombre, double precio) {
    Producto {
        if (precio < 0) throw new IllegalArgumentException("Precio negativo");
        nombre = nombre.trim();
    }
}
```

**a)** No compila: falta asignar `this.nombre` · **b)** Compila y el `trim` se aplica · **c)** Compila pero el `trim` no hace nada · **d)** No compila: falta el paréntesis

??? success "Solución"

    **b)** En el constructor compacto puedes **reasignar los parámetros**, y Java los guarda en los campos después. Ni paréntesis ni `this.nombre = nombre`.

    Es la forma de que un objeto **no pueda existir nunca en estado inválido**.

    → [E8](ejercicios.md)

### 8 · El record que no era inmutable

``` { .java .numerado }
record Equipo(String nombre, List<String> jugadores) {}
var lista = new ArrayList<>(List.of("Ana"));
var e = new Equipo("Rojo", lista);
lista.add("Intruso");
System.out.println(e.jugadores());
```

**a)** `[Ana]` · **b)** `[Ana, Intruso]` · **c)** `UnsupportedOperationException` · **d)** `[]`

??? success "Solución"

    **b) `[Ana, Intruso]`.** El `record` protege **la referencia**, no el contenido: quien te dio la lista puede seguir tocándola.

    ```java
    Equipo { jugadores = List.copyOf(jugadores); }
    ```

    `List.copyOf` copia **y** congela.

    → [E9](ejercicios.md)

### 9 · El `enum`

**a)** Es una lista de constantes `int` · **b)** Es una clase con un número fijo de instancias, y puede tener campos y métodos · **c)** No puede tener constructor · **d)** Se compara con `equals`, nunca con `==`

??? success "Solución"

    **b)** Un `enum` **es una clase**: puede llevar campos, constructor y métodos.

    La (d) está al revés: como solo existe una instancia de cada valor, `==` es correcto y además seguro con `null`.

    → [E10](ejercicios.md)

### 10 · Para qué sirve una interfaz

`ServicioPedidos` recibe un `Notificador` por el constructor. ¿Cuál es la ventaja principal?

**a)** Escribir menos código · **b)** Añadir un notificador nuevo sin tocar el servicio, y poder probarlo con uno falso · **c)** Es más rápido · **d)** Obliga a usar `record`

??? success "Solución"

    **b)** El servicio **no sabe** cómo se envía. Añadir Telegram es una clase nueva y cero líneas tocadas.

    Y la consecuencia grande: puedes pasarle un `NotificadorFalso` que solo apunte lo que le piden y **probar el servicio sin enviar nada**. Es la inyección de dependencias que en la UT4 hará Spring por ti.

    → [E11](ejercicios.md)

---

## Bloque 3 — Colecciones

### 11 · ¿Qué imprime?

``` { .java .numerado }
var m = new HashMap<String, Integer>();
m.put("pera", 3);
m.put("pera", 7);
System.out.println(m.size());
System.out.println(m.get("kiwi"));
```

**a)** `2` y `0` · **b)** `1` y `null` · **c)** `2` y `null` · **d)** `1` y `0`

??? success "Solución"

    **b)** `put` con una clave que ya existe **sustituye**, así que hay una sola entrada. Y `get` de una clave que no está devuelve **`null`**.

    Si después haces `m.get("kiwi") + 1`, tienes un `NullPointerException`. Por eso existe `getOrDefault("kiwi", 0)`.

    → [E13](ejercicios.md)

### 12 · ¿Qué imprime?

``` { .java .numerado }
var l = new ArrayList<>(List.of(10, 20, 30));
l.remove(1);
System.out.println(l);
```

**a)** `[10, 20, 30]` · **b)** `[20, 30]` · **c)** `[10, 30]` · **d)** Excepción

??? success "Solución"

    **c) `[10, 30]`.** Con `List<Integer>`, `remove(int)` borra **la posición**, no el valor.

    Para borrar el número 1: `l.remove(Integer.valueOf(1))`. Con `List<String>` no hay ambigüedad.

    → [E14](ejercicios.md)

### 13 · Qué colección

Tienes que guardar el stock de cada código de producto, con 20.000 productos y búsquedas constantes.

**a)** `ArrayList<Producto>` · **b)** `HashMap<String, Integer>` · **c)** `HashSet<Producto>` · **d)** `TreeSet<Producto>`

??? success "Solución"

    **b)** Búsqueda **directa por clave**.

    La (a) es la respuesta que se da y la que hunde el programa: buscar en una lista la recorre entera. Con 20 productos no se nota; con 20.000, la diferencia medida es de unos **1.500 µs a 3 µs**.

    → [E15](ejercicios.md)

### 14 · Contar apariciones

``` { .java .numerado }
var cuenta = new HashMap<String, Integer>();
for (var p : palabras) {
    cuenta.???(p, 1, Integer::sum);
}
```

**a)** `put` · **b)** `merge` · **c)** `computeIfAbsent` · **d)** `putIfAbsent`

??? success "Solución"

    **b) `merge`**: «si no está, pon 1; si está, súmale 1».

    `computeIfAbsent` es la otra que hay que conocer, pero sirve para lo otro: **crear una lista vacía** si no hay nada para esa clave, y después añadir.

    → [E16](ejercicios.md) · [E17](ejercicios.md)

### 15 · ¿Qué pasa?

``` { .java .numerado }
var l = new ArrayList<>(List.of(1, 2, 3, 4));
for (var n : l) { if (n % 2 == 0) l.remove(n); }
```

**a)** `[1, 3]` · **b)** `ConcurrentModificationException` · **c)** `IndexOutOfBoundsException` · **d)** No compila

??? success "Solución"

    **b)** No se puede modificar una colección mientras se recorre con `for-each`: el `remove` deja obsoleto al iterador y la excepción salta en la vuelta siguiente.

    ```java
    l.removeIf(n -> n % 2 == 0);
    ```

    → [E18](ejercicios.md)

### 16 · ¿Qué lanza?

``` { .java .numerado }
var fija = List.of("a", "b");
fija.add("c");
```

**a)** Nada, añade · **b)** `UnsupportedOperationException` · **c)** No compila · **d)** `IllegalStateException`

??? success "Solución"

    **b)** `List.of(...)` crea una lista **inmutable**. Lo mismo con `removeIf` o `set`.

    Para poder modificarla: `new ArrayList<>(List.of(...))`. Y compila perfectamente: el fallo aparece al ejecutar.

    → [E18](ejercicios.md)

---

## Bloque 4 — Streams

Las preguntas de este bloque usan esta lista:

``` { .java .numerado }
record Producto(String nombre, String categoria, double precio) {}
// Patinete/movilidad/120 · Casco/seguridad/35 · Bici/movilidad/450
// Candado/seguridad/25 · Luces/seguridad/15
```

### 17 · ¿Qué devuelve?

``` { .java .numerado }
productos.stream()
         .filter(p -> p.precio() < 100)
         .map(Producto::nombre)
         .sorted()
         .toList();
```

**a)** `[Casco, Candado, Luces]` · **b)** `[Candado, Casco, Luces]` · **c)** `[Luces, Candado, Casco]` · **d)** Una lista de `Producto`

??? success "Solución"

    **b)** Se cae lo caro, lo que queda se convierte en texto y **después** se ordena alfabéticamente: `Candado` < `Casco` < `Luces`.

    La (a) es el orden de la lista original, sin ordenar. La (d) olvida que el `map` ya ha cambiado el tipo.

    → [E19](ejercicios.md)

### 18 · ¿Qué pasa aquí?

``` { .java .numerado }
productos.stream()
         .map(Producto::nombre)
         .sorted(Comparator.comparingDouble(Producto::precio))
         .toList();
```

**a)** Ordena por precio · **b)** No compila · **c)** Ordena alfabéticamente · **d)** Lanza `ClassCastException`

??? success "Solución"

    **b) No compila.** Después del `map` los elementos ya no son productos, **son textos**, y un texto no tiene precio.

    Es la consecuencia práctica de que un stream se lea **de arriba abajo**: cada operación trabaja sobre lo que le dejó la anterior.

    → [E19](ejercicios.md)

### 19 · ¿Qué imprime?

``` { .java .numerado }
var s = productos.stream();
System.out.println(s.count());
System.out.println(s.count());
```

**a)** `5` y `5` · **b)** `5` y `0` · **c)** `5` y luego `IllegalStateException` · **d)** No compila

??? success "Solución"

    **c)** *stream has already been operated upon or closed*.

    **Un stream se usa una vez**: se crea, se usa y se tira. Nunca se guarda en una variable para reutilizarlo.

    → [E20](ejercicios.md)

### 20 · La suma

¿Cómo sumas los precios?

**a)** `.map(Producto::precio).sum()` · **b)** `.mapToDouble(Producto::precio).sum()` · **c)** `.sum(Producto::precio)` · **d)** `.collect(Collectors.sum())`

??? success "Solución"

    **b)** `mapToDouble` convierte el stream a un `DoubleStream`, y **eso** es lo que desbloquea `sum()`, `average()` y `summaryStatistics()`.

    Con `map` tendrías un `Stream<Double>`, que no tiene `sum()`.

    → [E20](ejercicios.md)

### 21 · Dónde va el `.reversed()`

Quieres categoría **descendente** y, dentro, nombre ascendente.

**a)** `comparing(cat).thenComparing(nombre).reversed()` · **b)** `comparing(cat).reversed().thenComparing(nombre)` · **c)** `comparing(cat).thenComparing(nombre.reversed())` · **d)** Las dos primeras son iguales

??? success "Solución"

    **b)** `reversed()` invierte **todo lo encadenado hasta ese punto**, así que va pegado a lo que quieres invertir.

    La (a) invierte también el nombre, que es el fallo que comete la mayoría porque «reversed va al final» parece razonable.

    → [E21](ejercicios.md)

### 22 · ¿En qué orden salen las claves?

``` { .java .numerado }
productos.stream().collect(
    Collectors.groupingBy(Producto::categoria, Collectors.counting()));
```

**a)** Alfabético · **b)** El de aparición en la lista · **c)** No está garantizado · **d)** Inverso

??? success "Solución"

    **c) No está garantizado.** `groupingBy` devuelve un **`HashMap`**.

    ```java
    Collectors.groupingBy(Producto::categoria, TreeMap::new, Collectors.counting())
    ```

    Que con cinco productos salga siempre igual es exactamente lo que engaña: el día que cambien los datos, cambia el orden y tu informe sale distinto sin que hayas tocado nada.

    **Es la pregunta que más cae.**

    → [E22](ejercicios.md)

### 23 · El esquema de `groupingBy`

Quieres, por categoría, **la suma de los precios**.

**a)** `groupingBy(cat, summingDouble(Producto::precio))` · **b)** `groupingBy(cat, mapping(Producto::precio))` · **c)** `groupingBy(cat).sum()` · **d)** `summingDouble(groupingBy(cat))`

??? success "Solución"

    **a)** Siempre el mismo esquema:

    ```
    groupingBy( cómo hago los montones , qué hago con cada montón )
    ```

    El segundo parámetro decide: `counting()` cuántos, `summingDouble(...)` cuánto suman, `averagingDouble(...)` la media, `mapping(..., toList())` quedarse solo con un campo.

    → [E23](ejercicios.md)

---

## Bloque 5 — Excepciones y `Optional`

### 24 · ¿Qué imprime?

``` { .java .numerado }
try {
    System.out.println(10 / 0);
} catch (ArithmeticException e) {
    System.out.println("A");
} finally {
    System.out.println("B");
}
System.out.println("C");
```

**a)** `A B C` · **b)** `A B` · **c)** `B C` · **d)** `A C`

??? success "Solución"

    **a)** El `catch` corta la excepción, `finally` se ejecuta siempre y el programa continúa.

    Sin el `catch` saldría solo `B` y el programa moriría sin llegar a `C`.

    → [E25](ejercicios.md)

### 25 · Comprobada o no

¿Cuál de estas obliga el compilador a capturar o declarar?

**a)** `NullPointerException` · **b)** `IllegalArgumentException` · **c)** `IOException` · **d)** `NumberFormatException`

??? success "Solución"

    **c)** `IOException` hereda de `Exception`, no de `RuntimeException`: es **comprobada**.

    La regla práctica: **comprobada** = algo externo puede fallar aunque tu código sea perfecto (disco, red, base de datos). **No comprobada** = tu código tiene un fallo.

    → Tema [5](05-excepciones-y-optional.md)

### 26 · El `catch` vacío

``` { .java .numerado }
try { guardar(pedido); } catch (Exception e) { }
```

**a)** Está bien, no queremos que se caiga · **b)** El pedido no se guarda y nadie se entera · **c)** No compila · **d)** Solo falta un `finally`

??? success "Solución"

    **b)** El programa sigue como si todo hubiera ido bien, con datos a medias. El fallo aparece días después, en otro sitio, sin una línea que explique por qué.

    Un `catch` decente siempre hace **una** de tres cosas: arreglarlo, informar, o **volver a lanzar con la causa**:

    ```java
    catch (SQLException e) {
        throw new PedidoException("No se pudo guardar el pedido " + id, e);
    }
    ```

    Esa `e` al final es la que produce el `Caused by:` que lleva al origen.

    → [E29](ejercicios.md)

### 27 · ¿Qué está mal?

``` { .java .numerado }
if (buscar(1).isPresent()) System.out.println(buscar(1).get().nombre());
```

**a)** Nada · **b)** Busca dos veces y usa `get()`; se escribe con `map` e `ifPresent` · **c)** No compila · **d)** `isPresent` no existe

??? success "Solución"

    **b)** Es `Optional` escrito como si fuera `null`.

    ```java
    buscar(1).map(Cliente::nombre).ifPresent(System.out::println);
    ```

    Y `get()` sin comprobar es el `NullPointerException` del que querías escapar con otro nombre: lanza `NoSuchElementException: No value present`.

    → [E28](ejercicios.md) · [E29](ejercicios.md)

---

## Bloque 6 — Proyecto Maven

### 28 · ¿Qué falta?

El proyecto no compila un `record`: *«records are not supported in -source 8»*.

**a)** Falta el JDK 25 · **b)** Falta `<maven.compiler.release>25</maven.compiler.release>` · **c)** Falta una dependencia · **d)** Falta `<packaging>jar</packaging>`

??? success "Solución"

    **b)** Sin esa propiedad Maven usa una versión antigua por defecto, aunque tengas instalado el JDK 25.

    Y la acompañante, que evita que las tildes se rompan en otro sistema:

    ```xml
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    ```

    → [E30](ejercicios.md)

### 29 · El `.jar` que no arranca

Empaquetas con `mvn package`, ejecutas `java -jar` y sale `NoClassDefFoundError` de una clase de Jackson.

**a)** Falta el `Main-Class` · **b)** El `.jar` no lleva las dependencias dentro · **c)** Falta recompilar · **d)** Jackson no funciona en un `.jar`

??? success "Solución"

    **b)** Un `.jar` normal solo lleva **tus** clases. Las dependencias se quedan fuera.

    Se resuelve con el **maven-shade-plugin**, que las empaqueta todas dentro. Es el fallo clásico de la primera entrega.

    → [E32](ejercicios.md)

### 30 · `mvn dependency:tree`

Añades una dependencia y el árbol muestra tres. ¿Por qué?

**a)** Maven se ha equivocado · **b)** Una dependencia arrastra las suyas · **c)** Están duplicadas · **d)** Son versiones distintas de la misma

??? success "Solución"

    **b)** Son **dependencias transitivas**. `jackson-databind` necesita `jackson-core` y `jackson-annotations`, y Maven las trae solas.

    Por eso un `pom.xml` con cinco líneas acaba con treinta `.jar`, y por eso conviene mirar el árbol de vez en cuando: ahí se ven los conflictos de versión.

    → [E31](ejercicios.md)

---

## Cómo se corrige

Sin penalización: **nota = aciertos / 3**.

| Aciertos (de 30) | Nota | Qué significa |
|:-:|:-:|---|
| 27 – 30 | 9 – 10 | Dominas la unidad |
| 21 – 26 | 7 – 8 | Sólido; repasa los fallos concretos |
| 15 – 20 | 5 – 6 | Aprobado justo: rehaz la batería del bloque que peor te fue |
| < 15 | — | Vuelve a los temas con `jshell` abierto, no releyendo |

!!! tip "Lo que hay que hacer después"
    Apunta **el número de bloque** de cada fallo, no la pregunta. Si tres fallos son del bloque 4, lo que hay que repasar no son tres preguntas: son los streams enteros, y se repasan **rehaciendo el E24 desde cero**.

    Releer la solución de una pregunta que fallaste da la sensación de haberlo arreglado. No lo arregla.
