# Autoevaluación — UT2 (Java)

Banco de preguntas por tema, con solucionario. Úsalo tras leer cada página y para preparar el **test de RA2** (100 % de la nota).

Formato: opción múltiple, una correcta. Muchas son de **leer código y decir qué pasa** — igual que el test real.

---

## Tema 1 — Primeros pasos (S1)

1. Para **desarrollar** en Java necesitas… a) solo la JRE · b) **el JDK** · c) solo la JVM · d) un servidor
2. El bytecode `.class` lo ejecuta… a) el SO directamente · b) `javac` · c) **la JVM** · d) el navegador
3. `java Hola.java` (JDK 25)… a) solo compila · b) **compila y ejecuta en un paso** · c) da error · d) crea un .jar
4. `int x = "hola";` produce… a) nada · b) error de ejecución · c) **error de compilación** · d) x vale 0
5. `String s = null; s.length();` produce… a) error de compilación · b) **NullPointerException en ejecución** · c) 0 · d) null
6. La ventaja del bytecode es… a) más rápido que C · b) **portabilidad entre sistemas** · c) no necesita JVM · d) ocupa menos
7. JDK 25 es… a) una versión intermedia · b) **la LTS vigente** · c) obsoleta · d) de pago

> **Soluciones:** 1b · 2c · 3b · 4c · 5b · 6b · 7b

---

## Tema 2 — Sintaxis y tipos (S2–S3)

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

## Tema 3 — POO (S4–S6)

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

## Tema 4 — Colecciones y streams (S7–S8)

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

## Tema 5 — Excepciones y Optional (S9)

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

## Tema 6 — Proyectos y testing (S9)

1. Las dependencias de un proyecto Maven se declaran en… a) `build.gradle` · b) **`pom.xml`** · c) `package.json` · d) `application.properties`
2. El código de producción va en… a) `src/test/java` · b) **`src/main/java`** · c) `target/` · d) la raíz
3. Los tests van en… a) `src/main/java` · b) **`src/test/java`** · c) `resources` · d) donde sea
4. `mvn package` genera… a) los tests · b) **el .jar** · c) la documentación · d) el pom
5. El patrón AAA es… a) Add-Assert-Apply · b) **Arrange-Act-Assert** · c) Api-App-Assert · d) Assert-Any-All
6. Un test **sin aserciones**… a) falla siempre · b) **pasa siempre aunque el código esté mal** · c) no compila · d) es correcto
7. `assertThrows` comprueba que… a) no hay error · b) **se lanza la excepción esperada** · c) el valor es null · d) el test es lento
8. Merece más la pena testear… a) getters triviales · b) **la lógica de negocio y los casos límite** · c) el toString · d) las librerías externas
9. Un buen nombre de test es… a) `test1()` · b) **`aplicaDescuentoDesde6Unidades()`** · c) `pruebaCalculadora()` · d) `x()`

> **Soluciones:** 1b · 2b · 3b · 4b · 5b · 6b · 7b · 8b · 9b

---

## Cómo usarlo

- **Tras cada sesión:** responde el bloque del tema y corrige con el solucionario.
- **La semana del test:** repite los bloques que fallaste y pide simulacros al [comprobar tu trabajo con los tests](../comprobar-tu-trabajo/).
- Recuerda que el test real incluye además preguntas sobre **tu propio código** de las [prácticas](practicas.md): qué imprime, dónde está el fallo, por qué compila o no.
