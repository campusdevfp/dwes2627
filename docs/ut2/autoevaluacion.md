# Simulacro de test — UT2

**81 preguntas con solución.** Mismo formato que el test de la unidad: opción múltiple, **una sola correcta**, sin penalización por fallo.

!!! info "De dónde sale todo lo que se pregunta"
    De **los temas 1 a 6** y de la **[batería de ejercicios](ejercicios.md)**. Nada más.

    La mayoría son de **leer código y decir qué pasa**, porque así es el test real. Estudiar esta unidad leyendo apuntes no funciona: hay que teclear.

| Bloque | De dónde | Preguntas |
|---|---|:-:|
| 1 | Primeros pasos | 7 |
| 2 | Sintaxis y tipos | 9 |
| 3 | POO | 10 |
| 4 | Colecciones y streams | 10 |
| 5 | Excepciones y `Optional` | 9 |
| 6 | Un proyecto Maven | 10 |
| **7** | **Directamente sobre los ejercicios** | **26** |

**El test oficial son 30 preguntas en 55 minutos.** Se aprueba con 15.

---

## Bloque 1 — Primeros pasos

1. Para **desarrollar** en Java necesitas… a) solo la JRE · b) **el JDK** · c) solo la JVM · d) un servidor
2. El bytecode `.class` lo ejecuta… a) el SO directamente · b) `javac` · c) **la JVM** · d) el navegador
3. `java Hola.java` (JDK 25)… a) solo compila · b) **compila y ejecuta en un paso** · c) da error · d) crea un .jar
4. `int x = "hola";` produce… a) nada · b) error de ejecución · c) **error de compilación** · d) x vale 0
5. `String s = null; s.length();` produce… a) error de compilación · b) **NullPointerException en ejecución** · c) 0 · d) null
6. La ventaja del bytecode es… a) más rápido que C · b) **portabilidad entre sistemas** · c) no necesita JVM · d) ocupa menos
7. JDK 25 es… a) una versión intermedia · b) **la LTS vigente** · c) obsoleta · d) de pago

> **Soluciones:** 1b · 2c · 3b · 4c · 5b · 6b · 7b

---

## Bloque 2 — Sintaxis y tipos

1. `IO.println(7 / 2);` imprime… a) 3.5 · b) **3** · c) 4 · d) error
2. `IO.println("5" + 3);` imprime… a) 8 · b) **53** · c) error · d) "5 3"
3. Para comparar el contenido de dos Strings se usa… a) `==` · b) **`.equals()`** · c) `=` · d) `.compare()`
4. `var` significa que… a) el tipo cambia en ejecución · b) **el compilador deduce el tipo, que luego es fijo** · c) es una constante · d) es un objeto
5. `var x = 5; x = "cinco";` → a) funciona · b) **error de compilación** · c) error de ejecución · d) x vale "cinco"
6. En el `switch` moderno con `->`… a) hace falta `break` · b) **no hace falta `break`** · c) no admite varios valores · d) no devuelve valor
7. `if (n % 2 == 0)` comprueba que n sea… a) impar · b) **par** · c) positivo · d) divisible entre 3
8. Para dinero, en producción, es preferible… a) `double` · b) `float` · c) **`BigDecimal`** · d) `int`
9. Un text block se delimita con… a) comillas simples · b) **tres comillas dobles** · c) backticks · d) `<<`

> **Soluciones:** 1b · 2b · 3b · 4b · 5b · 6b · 7b · 8c · 9b

---

## Bloque 3 — POO

1. Los atributos se declaran `private` para… a) que ocupen menos · b) **controlar el acceso y proteger las reglas** · c) que sean más rápidos · d) obligación del compilador
2. Un `record` genera automáticamente… a) solo el constructor · b) **constructor, getters, equals, hashCode y toString** · c) solo getters y setters · d) nada
3. Los getters de `record Producto(String nombre)` se llaman… a) `getNombre()` · b) **`nombre()`** · c) `get()` · d) `nombre`
4. Los records son… a) mutables · b) **inmutables** · c) abstractos · d) interfaces
5. Para el DTO de una API REST conviene… a) clase con setters · b) **record** · c) enum · d) interfaz
6. `@Override` sirve para… a) obligatorio al heredar · b) **avisar al compilador de que redefines: caza errores de nombre** · c) crear un método nuevo · d) hacerlo privado
7. Una interfaz define… a) la implementación · b) **un contrato: qué se puede hacer** · c) atributos privados · d) el constructor
8. El polimorfismo permite… a) heredar de varias clases · b) **tratar igual a distintas implementaciones de una interfaz** · c) cambiar tipos en ejecución · d) evitar interfaces
9. Un `enum` es mejor que Strings para estados porque… a) ocupa menos · b) **el compilador garantiza los valores válidos** · c) es más rápido · d) permite null
10. La regla profesional dice… a) hereda siempre que puedas · b) **composición sobre herencia** · c) no uses interfaces · d) todo público

> **Soluciones:** 1b · 2b · 3b · 4b · 5b · 6b · 7b · 8b · 9b · 10b

---

## Bloque 4 — Colecciones y streams

1. Para una colección **sin duplicados** usas… a) List · b) **Set** · c) Map · d) Array
2. Para buscar por clave usas… a) List · b) Set · c) **Map** · d) Queue
3. `List.of("a","b")` devuelve una lista… a) vacía · b) **inmutable** · c) ordenable · d) con null
4. `map.getOrDefault("x", 0)` si la clave no existe devuelve… a) null · b) error · c) **0** · d) ""
5. En un stream, `filter` es una operación… a) terminal · b) **intermedia** · c) de agrupación · d) final
6. Un stream **sin operación terminal**… a) se ejecuta igual · b) **no ejecuta nada** · c) da error · d) devuelve null
7. `productos.stream().map(Producto::nombre)` transforma… a) filtra · b) **cada elemento en otro valor** · c) ordena · d) cuenta
8. Para sumar precios se usa… a) `.sum()` directamente · b) **`.mapToDouble(...).sum()`** · c) `.count()` · d) `.reduce()` obligatorio
9. `Collectors.groupingBy` sirve para… a) ordenar · b) **agrupar en un Map por una clave** · c) filtrar · d) contar solo
10. `String::length` es… a) un error · b) **una referencia a método** · c) una clase · d) un tipo

> **Soluciones:** 1b · 2c · 3b · 4c · 5b · 6b · 7b · 8b · 9b · 10b

---

## Bloque 5 — Excepciones y Optional

1. El bloque `finally` se ejecuta… a) solo si hay error · b) solo si no hay error · c) **siempre** · d) nunca
2. `NullPointerException` es… a) checked · b) **unchecked** · c) un error de compilación · d) un warning
3. `IOException` es… a) **checked: hay que capturarla o declararla** · b) unchecked · c) opcional · d) un enum
4. Un `catch (Exception e) { }` vacío es… a) buena práctica · b) **silenciar el error: la peor práctica** · c) obligatorio · d) igual que no capturar
5. `try-with-resources` sirve para… a) reintentar · b) **cerrar recursos automáticamente** · c) capturar todo · d) lanzar excepciones
6. `Optional` se usa preferentemente… a) en atributos · b) en parámetros · c) **como retorno de métodos que pueden no encontrar nada** · d) en constructores
7. `opt.orElseThrow(...)` sirve para… a) ignorar · b) **obtener el valor o lanzar una excepción con sentido** · c) devolver null · d) capturar
8. Devolver `null` cuando no se encuentra algo provoca… a) mejor rendimiento · b) **NullPointerException aguas abajo** · c) error de compilación · d) nada
9. Una excepción de dominio (`ProductoNoEncontradoException`) en una API se traduce en… a) 200 · b) 500 · c) **404** · d) 301

> **Soluciones:** 1c · 2b · 3a · 4b · 5b · 6c · 7b · 8b · 9c

---

## Bloque 6 — Un proyecto Maven

1. Las dependencias de un proyecto Maven se declaran en… a) `build.gradle` · b) **`pom.xml`** · c) `package.json` · d) `application.properties`
2. El código va en… a) `src/test/java` · b) **`src/main/java`** · c) `target/` · d) la raíz
3. Un CSV de datos o un `.properties` van en… a) `src/main/java` · b) **`src/main/resources`** · c) `target/` · d) la raíz
4. `mvn package` genera… a) la documentación · b) **el `.jar` en `target/`** · c) el `pom.xml` · d) las dependencias
5. `target/` en Git… a) se sube siempre · b) **no se sube: es generado** · c) se sube comprimido · d) es obligatorio
6. Sin `<maven.compiler.release>25</maven.compiler.release>`… a) no pasa nada · b) **falla al compilar un `record`** · c) va más lento · d) no descarga dependencias
7. Las coordenadas de una librería son… a) nombre y autor · b) **`groupId`, `artifactId` y `version`** · c) la URL · d) el `.jar`
8. `mvn dependency:tree` sirve para… a) borrar dependencias · b) **ver todas, incluidas las indirectas** · c) actualizar versiones · d) compilar
9. `java -jar mi.jar` responde «no main manifest attribute» porque… a) falta Java · b) **el `.jar` no declara su clase principal** · c) el código no compila · d) falta `target/`
10. `mvn -o compile` significa… a) compilar solo · b) **compilar sin conexión, con lo ya descargado** · c) compilar optimizado · d) compilar los tests

> **Soluciones:** 1b · 2b · 3b · 4b · 5b · 6b · 7b · 8b · 9b · 10b

## Bloque 7 — Sobre los ejercicios

Estas 26 salen directamente de la [batería](ejercicios.md). Son del tipo que más pesa en el test: **código delante y decidir**.

**1.** ¿Qué imprime?

```java
IO.println(9 / 5);
IO.println(9 / 5.0);
```

a) `1.8` y `1.8` · b) **`1` y `1.8`** · c) `1.8` y `1` · d) Error de compilación

**2.** En `var precioBase = 20;`, el tipo inferido es…
a) `double` · b) **`int`** · c) `Number` · d) `var`

**3.** Si falta un `;`, el compilador suele señalar…
a) La primera línea del fichero · b) **La línea siguiente, que es donde se da cuenta** · c) Siempre la línea exacta · d) Ninguna: falla en ejecución

**4.** `switch (codigo / 100)` con `codigo = 404` entra por el caso…
a) `case 404` · b) **`case 4`** · c) `case 40` · d) `default`

**5.** En un `switch` de flecha…
a) Hace falta `break` · b) **No hace falta: no hay caída entre casos** · c) No admite varios valores por caso · d) No puede devolver valor

**6.** El constructor compacto de un `record` se escribe…
a) `Producto(String n, double p) { ... }` · b) **`Producto { ... }`** · c) `compact Producto() { ... }` · d) `record() { ... }`

**7.** Un `record` **no** genera automáticamente…
a) `equals` · b) `hashCode` · c) `toString` · d) **Métodos `setX`**

**8.** ¿Cuándo **no** conviene un `record`?
a) Cuando hay muchos campos · b) **Cuando el objeto debe cambiar de estado** · c) Cuando hay que compararlo · d) Nunca

**9.** En `procesarCompra(MetodoPago metodo, double importe)`, que dependa de la interfaz y no de `Tarjeta` es un ejemplo de…
a) Responsabilidad única · b) **Inversión de dependencias** · c) Liskov · d) Segregación de interfaces

**10.** ¿Qué devuelve `catalogo.stream().max(Comparator.comparingDouble(Producto::precio))`?
a) Un `Producto` · b) **Un `Optional<Producto>`** · c) Un `double` · d) Una `List`

**11.** `Collectors.groupingBy(Producto::categoria)` devuelve un mapa cuyo orden de claves…
a) Es alfabético · b) Es el de inserción · c) **No está garantizado: es un `HashMap`** · d) Es inverso

**12.** Para que ese mapa salga ordenado hay que escribir…
a) `.sorted()` antes · b) **`groupingBy(clave, TreeMap::new, downstream)`** · c) `groupingBy(clave).sort()` · d) No se puede

**13.** ¿Por qué `mapToDouble(...).sum()` es preferible a un `reduce` con `Double`?
a) Es más corto · b) **Evita el autoboxing** · c) Es la única forma · d) Devuelve `Optional`

**14.** Un repositorio que devuelve `null` cuando no encuentra…
a) Es correcto y eficiente · b) **Obliga a quien llama a acordarse de comprobarlo; `Optional` lo obliga el compilador** · c) Es obligatorio en Java 25 · d) Lanza excepción

**15.** `orElseThrow(() -> new ProductoNoEncontradoException(nombre))` se usa cuando…
a) La ausencia es normal · b) **La ausencia es un error** · c) Siempre · d) Nunca

**16.** ¿Qué imprime?

```java
System.out.println(0.1 + 0.2);
```

a) `0.3` · b) **`0.30000000000000004`** · c) Error · d) `0.30`

**17.** En el proyecto del catálogo, el servicio recibe `CatalogoRepositorio` por constructor. Si en su lugar hiciera `new CatalogoEnMemoria()` dentro…
a) Sería más eficiente · b) **Quedaría atado a esa implementación y no se podría cambiar ni sustituir** · c) No compilaría · d) Daría igual

**18.** `Optional.ofNullable(mapa.get(clave))` sirve para…
a) Acelerar la búsqueda · b) **Convertir el posible `null` del mapa en un `Optional`** · c) Ordenar el mapa · d) Evitar duplicados

**19.** En el catálogo se usa `LinkedHashMap` y no `HashMap` porque…
a) Es más rápido · b) **El listado sale en el orden en que se cargaron los productos** · c) Admite claves nulas · d) Ocupa menos

**20.** ¿Qué imprime?

```java
var lista = new ArrayList<>(List.of(1, 2, 3));
for (var n : lista) { if (n == 2) lista.remove(n); }
```

a) `[1, 3]` · b) **`ConcurrentModificationException`** · c) `[1, 2, 3]` · d) Error de compilación

**21.** La forma correcta de eliminar mientras recorres es…
a) `for` con índice hacia delante · b) **`lista.removeIf(...)`** · c) `lista.remove()` dentro del `for-each` · d) `lista.clear()`

**22.** `comparing(P::puntos).thenComparing(P::nombre).reversed()` invierte…
a) Solo los puntos · b) **Todo, incluido el desempate por nombre** · c) Solo el nombre · d) Nada

**23.** ¿Qué tiene de malo `catch (IOException e) { }`?
a) No compila · b) **Hace desaparecer el fallo y el programa sigue con datos a medias** · c) Es lento · d) Nada

**24.** Al envolver una excepción, pasar la causa (`super(mensaje, e)`) sirve para…
a) Que compile · b) **No perder la traza del fallo original** · c) Cifrar el mensaje · d) Reintentar

**25.** En un `try` con varios recursos, se cierran…
a) En el orden de apertura · b) **En orden inverso, y todos aunque uno falle al cerrarse** · c) Solo el primero · d) Solo si no hay excepción

**26.** `List.copyOf(lista)` hace dos cosas:
a) Ordena y copia · b) **Copia y devuelve una lista inmutable** · c) Copia y permite `add` · d) Solo comprueba nulos

> **Soluciones bloque 7:** 1b · 2b · 3b · 4b · 5b · 6b · 7d · 8b · 9b · 10b · 11c · 12b · 13b · 14b · 15b · 16b · 17b · 18b · 19b · 20b · 21b · 22b · 23b · 24b · 25b · 26b

---

## Simulacro cronometrado

Con la unidad hecha, siéntate **55 minutos con un reloj** y contesta, sin mirar nada:

- **Del tema 2:** las 9 preguntas.
- **Del tema 3:** las 5 primeras.
- **Del tema 4:** las 6 primeras.
- **Del tema 5:** las 4 primeras.
- **Del bloque 6:** las preguntas 1, 6 y 9.
- **Del bloque 7:** las preguntas 1, 4, 20 y 23.

Son **30 preguntas**, la misma proporción que el test real: la mayoría de código, y la mitad salidas de la batería.

| Aciertos | Lectura |
|:-:|---|
| **24 o más** | Vas sobrado |
| **18 a 23** | Aprobado holgado. Vuelve al bloque que peor te fue |
| **15 a 17** | Justo. Rehaz los ejercicios del E28 al E35 |
| **menos de 15** | El problema no es de memoria: falta teclear |

!!! tip "Cómo se estudia esta unidad"
    Leyendo, no. El test pone **código delante** y pregunta qué imprime, si compila o dónde está el fallo. Eso solo se entrena de una forma:

    1. **Predice antes de ejecutar.** Escribe en un papel qué va a salir, y después ejecuta.
    2. **Rompe tu propia solución.** Quítale el `hashCode`, mueve el `.reversed()`, borra la causa del `throw`. Apunta qué cambia.
    3. **Escribe tú la pregunta**, con sus tres distractores.

    Las respuestas de este banco siguen un orden fijo para corregir rápido; en el test real **las opciones van mezcladas**, así que no memorices letras.
