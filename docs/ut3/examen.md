# Preparar el examen — UT3

**:material-form-select: Test práctico · 100 % de la nota de la unidad · S10 · 55 minutos**

!!! info "Test, pero de código"
    No es un test de teoría. **Cada pregunta lleva un fragmento de código delante** y hay que decir qué hace, qué imprime, por qué falla o cuál de las cuatro versiones es la correcta.

    Se puede aprobar sin escribir una línea, pero **no sin saber programar**: las opciones incorrectas son exactamente los fallos que se cometen al escribir ese código.

!!! tip "Se examina con chuleta"
    Se te entrega impresa la **[chuleta de los prácticos del 1.er trimestre](../chuleta-examen-1t.md)**, y no puedes usar nada más.

    Está publicada desde septiembre: **entrena con ella**. Trae sintaxis, no criterio — lo que se evalúa no viene en la hoja.

## Por qué esta unidad cambia de instrumento

La UT3 es el puente entre el lenguaje (UT2) y el desarrollo de verdad (UT4 en adelante). Lo que hay que asegurar aquí es que **lees código y sabes anticipar lo que hace**: qué devuelve un `groupingBy`, por qué `split` se come las columnas vacías, qué pasa si no cierras un `Files.lines`.

Eso se mide mejor con veinte fragmentos distintos que con un solo programa de 55 minutos, donde un error tonto al principio te deja sin poder demostrar el resto.

**A partir de la UT4 se vuelve al examen práctico grande**, que es donde ya toca construir.

## Formato

| | |
|---|---|
| **Duración** | 55 minutos, una sesión |
| **Preguntas** | **30**, todas con código delante |
| **Tipo** | Opción múltiple, cuatro opciones, **una sola correcta** |
| **Puntuación** | Acierto **+1** · fallo **−0,25** · en blanco **0** |
| **Nota** | `(aciertos − fallos/4) / 30 × 10`, redondeado a la décima |
| **Aprobado** | A partir de 5,0 |
| **Material** | La chuleta impresa. Sin ordenador, sin apuntes, sin internet |

!!! warning "Restan los fallos, no las respuestas en blanco"
    Con cuatro opciones, contestar al azar no sale a cuenta: de media, cuatro intentos ciegos son un acierto y tres fallos, es decir, cero.

    **Contesta cuando puedas descartar al menos dos opciones.** Si no descartas ninguna, deja en blanco.

## Los cinco tipos de pregunta

Son siempre estos cinco. Los reconoces a la primera y sabes qué mirar en cada uno.

### 1 · ¿Qué imprime?

Un fragmento corto y cuatro salidas posibles.

``` { .java .numerado }
var m = new TreeMap<String, Integer>();
m.put("pera", 3);
m.put("manzana", 5);
m.put("kiwi", 1);
IO.println(m.keySet());
```

**a)** `[pera, manzana, kiwi]` · **b)** `[kiwi, manzana, pera]` · **c)** `[manzana, kiwi, pera]` · **d)** Orden impredecible

??? success "Solución"

    **b)** `[kiwi, manzana, pera]`

    Un `TreeMap` mantiene las **claves ordenadas** por su orden natural, que en cadenas es el alfabético. La (a) sería un `LinkedHashMap` —orden de inserción— y la (d), un `HashMap`.

    **Cómo se ataca este tipo:** identifica primero la estructura. `HashMap` → sin orden. `LinkedHashMap` → orden de inserción. `TreeMap` → orden natural. Con eso, tres de cada cuatro preguntas de este tipo se responden sin leer el resto.

### 2 · ¿Por qué falla?

Código que compila pero revienta al ejecutarse, o que ni compila.

``` { .java .numerado }
var lineas = Files.readAllLines(Path.of("ventas.csv"));
double total = 0;
for (var l : lineas) {
    total += Double.parseDouble(l.split(",")[2]);
}
```

**a)** No compila: falta el `try/catch` · **b)** `NumberFormatException` en la primera línea · **c)** `ArrayIndexOutOfBoundsException` siempre · **d)** Funciona correctamente

??? success "Solución"

    **b)** La primera línea del CSV es la **cabecera**, y `"precio"` no es un número.

    La (a) no vale porque `readAllLines` lanza `IOException`, que es *checked*: el fallo de compilación existiría si el método no la declarase, pero la pregunta no enseña la firma. La (c) solo pasaría si alguna fila tuviera menos de tres columnas, que no se sabe.

    **Cómo se ataca:** busca en este orden la cabecera sin saltar, el separador equivocado, la coma decimal y el `split` sin `-1`. Son el 80 % de las preguntas de este tipo.

### 3 · ¿Cuál es correcta?

Cuatro versiones del mismo código; solo una hace lo que dice el enunciado.

> Contar cuántos productos hay en cada categoría, **con las categorías ordenadas alfabéticamente**.

**a)** `.collect(groupingBy(Producto::categoria, counting()))`
**b)** `.collect(groupingBy(Producto::categoria, TreeMap::new, counting()))`
**c)** `.collect(toMap(Producto::categoria, p -> 1))`
**d)** `.sorted().collect(groupingBy(Producto::categoria, counting()))`

??? success "Solución"

    **b)** El `TreeMap::new` en medio es lo único que garantiza el orden de las claves.

    La (a) devuelve un `HashMap`: cuenta bien, pero sin orden. La (c) lanza `IllegalStateException` en cuanto haya dos productos de la misma categoría, porque `toMap` no sabe qué hacer con la clave repetida. La (d) es la trampa: **ordenar antes de agrupar no ordena el mapa resultante**, porque el `HashMap` no conserva nada.

### 4 · Completa el hueco

El mismo código con una parte tapada.

``` { .java .numerado }
try (var lineas = Files.lines(csv, StandardCharsets.UTF_8)) {
    return lineas.skip(1)
                 .filter(l -> !l.isBlank())
                 .map(l -> l.split(";", ____))
                 .filter(c -> c.length >= 4)
                 .toList();
}
```

**a)** `0` · **b)** `-1` · **c)** `4` · **d)** nada, sobra el segundo parámetro

??? success "Solución"

    **b)** `-1` conserva las columnas vacías del final.

    Con `0` —o sin el parámetro, que es lo mismo— `"a;b;;"` devuelve `["a","b"]` de longitud 2, y el filtro de la línea siguiente descarta filas que sí eran válidas. Con `4` se dejaría de partir a la cuarta columna, juntando el resto en una sola.

### 5 · ¿Qué tipo devuelve?

Para comprobar que sabes leer una cadena de *streams*.

``` { .java .numerado }
conciertos.stream()
          .collect(groupingBy(Concierto::escenario,
                              mapping(Concierto::artista, toList())));
```

**a)** `Map<String, List<Concierto>>` · **b)** `Map<String, List<String>>` · **c)** `List<String>` · **d)** `Map<String, String>`

??? success "Solución"

    **b)** `Map<String, List<String>>`

    `groupingBy` da un `Map` cuya clave es lo que devuelve el clasificador —aquí `String`, el escenario—. El `mapping` de dentro transforma cada elemento del grupo en su artista y los recoge en una lista.

    **Cómo se ataca:** lee de fuera hacia dentro. El recolector exterior decide si es `Map` o `List`; el interior, qué hay dentro de cada grupo.

## De qué va cada pregunta

Las 30 se reparten en proporción a las horas de cada tema:

| Tema | Preguntas | Lo que se pregunta |
|---|:-:|---|
| **1 · Estructuras y streams** | 8 | Qué estructura elegir, orden, `groupingBy`, `reduce`, `Optional` |
| **2 · Ficheros** | 7 | Rutas, `Files.lines` frente a `readAllLines`, recursos, CSV y sus trampas |
| **3 · JSON y Jackson** | 6 | Serializar, deserializar, fechas ISO, campos que faltan |
| **4 · Fechas y validación** | 5 | `java.time`, rangos, descarte de filas inválidas |
| **5 · Repositorio y capas** | 4 | Qué va en cada capa, por qué se separa |

## Errores que más nota cuestan

Son los mismos de siempre. Ahora, en vez de cometerlos, hay que **reconocerlos en el código de otro**:

1. **No saltar la cabecera** → `NumberFormatException` en la primera línea.
2. **`split(";")` sin el `-1`** → se pierden las columnas vacías del final.
3. **Decimales con coma** → `new BigDecimal("38,00")` lanza `NumberFormatException`.
4. **`Files.lines` sin `try-with-resources`** → el fichero se queda abierto.
5. **`groupingBy` sin `TreeMap::new`** cuando se pide orden.
6. **`toMap` con claves repetidas** → `IllegalStateException`.
7. **Jackson sin `JavaTimeModule`** → excepción, o la fecha como número.
8. **`get()` en vez de `path()`** en un JSON con campos que faltan → `NullPointerException`.
9. **Modificar una lista dentro de un `for-each`** → `ConcurrentModificationException`.
10. **`List.of(...)` y luego `sort`** → `UnsupportedOperationException`: es inmutable.

## Cómo prepararte

1. **Haz la [batería de test](autoevaluacion.md)**, que tiene el mismo formato y los mismos cinco tipos de pregunta.
2. **Escribe el código de los ejercicios**, aunque el examen sea de test. Se reconoce un `ConcurrentModificationException` en un fragmento porque te ha saltado antes en tu propia pantalla, no porque lo hayas leído.
3. **Ejecuta los fragmentos que no tengas claros.** Con `jshell` es inmediato:

   ```bash
   jshell
   jshell> var m = new java.util.TreeMap<String,Integer>();
   jshell> m.put("pera", 3); m.put("kiwi", 1);
   jshell> m.keySet()
   ```

4. **Repasa la [batería de ejercicios](ejercicios.md) y las [prácticas](practicas.md)**: de ahí salen los fragmentos.
5. **Ten la [chuleta](chuleta.md) delante** mientras entrenas, para llegar al examen sabiendo dónde está cada cosa.

!!! success "Lo que de verdad entrena para este examen"
    Programar y equivocarse. Cada excepción que te salte en clase es una pregunta del examen que ya tienes contestada.

## Simulacro de cinco preguntas

!!! example "Cronométrate: menos de dos minutos por pregunta"

    **1.** ¿Qué imprime?

    ``` { .java .numerado }
    var nums = new ArrayList<>(List.of(1, 2, 3, 4, 5));
    nums.removeIf(n -> n % 2 == 0);
    IO.println(nums);
    ```
    **a)** `[1, 2, 3, 4, 5]` · **b)** `[2, 4]` · **c)** `[1, 3, 5]` · **d)** `ConcurrentModificationException`

    **2.** ¿Por qué falla?

    ``` { .java .numerado }
    var precios = List.of("10.50", "20.00", "abc");
    var total = precios.stream()
                       .mapToDouble(Double::parseDouble)
                       .sum();
    ```
    **a)** No compila · **b)** `NumberFormatException` al llegar a `"abc"` · **c)** Devuelve 30.5 e ignora `"abc"` · **d)** Devuelve 0

    **3.** ¿Cuál escribe el JSON con las fechas como `"2026-07-10"`?

    **a)** `new ObjectMapper()`
    **b)** `new ObjectMapper().registerModule(new JavaTimeModule())`
    **c)** `JsonMapper.builder().addModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).build()`
    **d)** `new ObjectMapper().enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)`

    **4.** ¿Qué devuelve `lista.stream().max(comparing(P::precio))`?

    **a)** `P` · **b)** `Optional<P>` · **c)** `double` · **d)** `List<P>`

    **5.** Completa: descartar las filas cuya fecha esté fuera del intervalo, **incluidos los extremos**.

    ``` { .java .numerado }
    .filter(v -> ____)
    ```
    **a)** `v.fecha().isAfter(desde) && v.fecha().isBefore(hasta)`
    **b)** `!v.fecha().isBefore(desde) && !v.fecha().isAfter(hasta)`
    **c)** `v.fecha().compareTo(desde) > 0`
    **d)** `v.fecha().equals(desde) || v.fecha().equals(hasta)`

??? success "Soluciones del simulacro"

    **1 · c)** `[1, 3, 5]`. `removeIf` borra los que **cumplen** la condición, es decir, los pares. Y no lanza `ConcurrentModificationException` porque itera por dentro; eso pasaría con un `for-each` y un `remove`.

    **2 · b)** `NumberFormatException`. Los *streams* no ignoran los errores: la excepción sube y se lleva por delante el cálculo entero. Para descartar los malos habría que filtrar antes.

    **3 · c)** Hacen falta **las dos cosas**: el módulo que sabe escribir `LocalDate` **y** desactivar la escritura como número. Con la (b) sale `[2026,7,10]`; con la (d), peor todavía.

    **4 · b)** `Optional<P>`. La lista puede estar vacía y entonces no hay máximo. Es el error clásico: intentar asignarlo directamente a `P`.

    **5 · b)** Con `isAfter`/`isBefore` a secas, los extremos quedan **fuera**. La forma de incluirlos es negar la contraria: *«no es anterior a desde y no es posterior a hasta»*.
