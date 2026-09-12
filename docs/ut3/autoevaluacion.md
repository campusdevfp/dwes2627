# Batería de test — UT3

**El mismo formato que el examen**: código delante, cuatro opciones, una correcta. Cada pregunta lleva su solución razonada en un desplegable.

!!! tip "Cómo usar esta página"
    Tápate la solución, contesta, y **solo entonces** despliega. Si aciertas por eliminación, léela igual: la explicación dice **por qué las otras tres están mal**, y esas tres son fallos que vas a cometer escribiendo código.

    Y lo que de verdad entrena: **cuando dudes, ejecútalo**. `jshell` arranca en dos segundos.

---

## Tema 1 — Estructuras de datos y streams

### T1.1 · ¿Qué imprime?

``` { .java .numerado }
var m = new HashMap<String, Integer>();
m.put("pera", 3);
m.put("manzana", 5);
m.put("kiwi", 1);
IO.println(m.keySet());
```

**a)** `[pera, manzana, kiwi]` · **b)** `[kiwi, manzana, pera]` · **c)** `[manzana, kiwi, pera]` · **d)** No se puede saber

??? success "Solución"

    **d)** Un `HashMap` **no garantiza ningún orden**. Da la casualidad de que con estas tres claves suele salir siempre lo mismo, y por eso mucha gente cree que sí lo hay.

    - Orden de **inserción** → `LinkedHashMap`.
    - Orden **natural** de la clave → `TreeMap`.

    Confiar en el orden de un `HashMap` es el fallo que aparece en cuanto añades una clave más y la salida cambia.

### T1.2 · ¿Qué imprime?

``` { .java .numerado }
var nums = new ArrayList<>(List.of(1, 2, 3, 4, 5));
nums.removeIf(n -> n % 2 == 0);
IO.println(nums);
```

**a)** `[1, 2, 3, 4, 5]` · **b)** `[2, 4]` · **c)** `[1, 3, 5]` · **d)** `ConcurrentModificationException`

??? success "Solución"

    **c)** `removeIf` borra los que **cumplen** la condición: se van los pares.

    La (b) es el error de leerlo al revés. La (d) no ocurre porque `removeIf` itera internamente y sabe lo que está haciendo; eso sí pasaría con:

    ```java
    for (var n : nums) { if (n % 2 == 0) nums.remove(n); }   // ← revienta
    ```

### T1.3 · ¿Por qué falla?

``` { .java .numerado }
var lista = List.of("pera", "kiwi", "manzana");
lista.sort(Comparator.naturalOrder());
```

**a)** No compila · **b)** `UnsupportedOperationException` · **c)** `NullPointerException` · **d)** Funciona

??? success "Solución"

    **b)** `List.of(...)` devuelve una lista **inmutable**. Tiene el método `sort` porque lo hereda de `List`, así que compila, pero al llamarlo lanza `UnsupportedOperationException`.

    Para ordenarla hay que copiarla primero:

    ```java
    var copia = new ArrayList<>(lista);
    copia.sort(Comparator.naturalOrder());
    ```

    o quedarse con una nueva: `lista.stream().sorted().toList()`.

### T1.4 · ¿Cuál es correcta?

> Contar cuántos productos hay en cada categoría, **con las categorías ordenadas alfabéticamente**.

**a)** `.collect(groupingBy(Producto::categoria, counting()))`
**b)** `.collect(groupingBy(Producto::categoria, TreeMap::new, counting()))`
**c)** `.collect(toMap(Producto::categoria, p -> 1))`
**d)** `.sorted().collect(groupingBy(Producto::categoria, counting()))`

??? success "Solución"

    **b)** El `TreeMap::new` en medio es lo único que ordena las claves.

    - **(a)** cuenta bien, pero en un `HashMap`: sin orden.
    - **(c)** lanza `IllegalStateException` en cuanto haya dos productos de la misma categoría, porque `toMap` no sabe qué hacer con la clave repetida. Necesitaría un tercer argumento para resolver el choque.
    - **(d)** es la trampa buena: **ordenar antes de agrupar no ordena el mapa**, porque el `HashMap` de destino no conserva nada. Además `sorted()` sin comparador exige que `Producto` sea `Comparable`.

### T1.5 · ¿Qué tipo devuelve?

``` { .java .numerado }
conciertos.stream()
          .collect(groupingBy(Concierto::escenario,
                              mapping(Concierto::artista, toList())));
```

**a)** `Map<String, List<Concierto>>` · **b)** `Map<String, List<String>>` · **c)** `List<String>` · **d)** `Map<String, String>`

??? success "Solución"

    **b)** `Map<String, List<String>>`

    Se lee de fuera adentro: `groupingBy` produce un `Map` cuya clave es lo que devuelve el clasificador —el escenario, un `String`—; el `mapping` de dentro convierte cada concierto del grupo en su artista y los recoge en una lista.

    Sin el `mapping`, la respuesta sería la (a).

### T1.6 · ¿Qué imprime?

``` { .java .numerado }
record P(String nombre, double precio) {}
var ps = List.of(new P("A", 10), new P("B", 30), new P("C", 20));
IO.println(ps.stream().max(Comparator.comparing(P::precio)));
```

**a)** `B` · **b)** `P[nombre=B, precio=30.0]` · **c)** `Optional[P[nombre=B, precio=30.0]]` · **d)** `30.0`

??? success "Solución"

    **c)** `max` devuelve un **`Optional`**, porque la lista podría estar vacía y entonces no habría máximo. Al imprimirlo se ve el envoltorio.

    Es el error clásico de la unidad: intentar asignarlo directamente a `P`. Hay que decidir qué pasa si no hay nada:

    ```java
    ps.stream().max(comparing(P::precio))
      .map(P::nombre)
      .orElse("sin productos");
    ```

### T1.7 · ¿Por qué falla?

``` { .java .numerado }
Set<Punto> vistos = new HashSet<>();
class Punto { int x, y; }       // sin equals ni hashCode
vistos.add(new Punto(1, 1));
vistos.add(new Punto(1, 1));
IO.println(vistos.size());
```

**a)** Imprime 1 · **b)** Imprime 2 · **c)** No compila · **d)** `NullPointerException`

??? success "Solución"

    **b)** Imprime **2**. Sin `equals` ni `hashCode`, Java compara **por identidad**: son dos objetos distintos en memoria, así que el `Set` los admite los dos aunque tengan los mismos valores.

    No es un error que salte: el programa funciona y los duplicados se cuelan. Por eso en esta unidad se usan `record`, que los generan solos y comparan por valor.

### T1.8 · Completa el hueco

> Acumular en un mapa los productos de cada categoría, **funcione o no la clave la primera vez**.

``` { .java .numerado }
Map<String, List<Producto>> porCat = new HashMap<>();
for (var p : productos) {
    porCat.____(p.categoria(), k -> new ArrayList<>()).add(p);
}
```

**a)** `get` · **b)** `put` · **c)** `computeIfAbsent` · **d)** `getOrDefault`

??? success "Solución"

    **c)** `computeIfAbsent` crea la lista **solo si la clave no existía**, la mete en el mapa y la devuelve. Por eso se le puede encadenar el `.add(p)` directamente.

    - **(a)** devolvería `null` la primera vez → `NullPointerException`.
    - **(b)** no encaja: `put` recibe el valor, no una función.
    - **(d)** devuelve la lista por defecto pero **no la guarda en el mapa**, así que cada vuelta añade a una lista que se tira.

---

## Tema 2 — Ficheros

### T2.1 · ¿Por qué falla?

``` { .java .numerado }
var lineas = Files.readAllLines(Path.of("ventas.csv"));
double total = 0;
for (var l : lineas) {
    total += Double.parseDouble(l.split(",")[2]);
}
```

**a)** No compila · **b)** `NumberFormatException` en la primera línea · **c)** `ArrayIndexOutOfBoundsException` siempre · **d)** Funciona

??? success "Solución"

    **b)** La primera línea es la **cabecera**, y `"precio"` no es un número.

    Se arregla saltándola:

    ```java
    for (var l : lineas.subList(1, lineas.size())) { … }
    // o, con streams:
    lineas.stream().skip(1)
    ```

    La (c) solo pasaría si alguna fila tuviera menos de tres columnas, cosa que el fragmento no permite saber.

### T2.2 · ¿Qué imprime?

``` { .java .numerado }
IO.println("a;b;;".split(";").length);
IO.println("a;b;;".split(";", -1).length);
```

**a)** `2` y `2` · **b)** `4` y `4` · **c)** `2` y `4` · **d)** `4` y `2`

??? success "Solución"

    **c)** `2` y `4`.

    Por defecto `split` **descarta las cadenas vacías del final**. Con `-1` no descarta nada.

    En una carga de CSV esto es un desastre silencioso: si la última columna viene vacía —y viene constantemente— tu `campo[3]` lanza `ArrayIndexOutOfBoundsException` **solo en algunas filas**, así que con tres líneas de prueba no aparece.

    Aviso extra: `split` recibe una **expresión regular**. Si el separador es `|`, hay que escribir `"\\|"` o no separa nada.

### T2.3 · ¿Cuál es correcta?

> Contar las líneas que contienen `ERROR` en un log de 2 GB, **sin cargarlo entero en memoria**.

**a)** `Files.readAllLines(p).stream().filter(l -> l.contains("ERROR")).count()`
**b)** `Files.readString(p).lines().filter(l -> l.contains("ERROR")).count()`
**c)** `try (var s = Files.lines(p)) { return s.filter(l -> l.contains("ERROR")).count(); }`
**d)** `Files.lines(p).filter(l -> l.contains("ERROR")).count()`

??? success "Solución"

    **c)** `Files.lines` es **perezoso**: va línea a línea sin cargar el fichero. Y como abre el fichero, hay que cerrarlo, de ahí el *try-with-resources*.

    - **(a)** y **(b)** cargan los 2 GB en memoria antes de filtrar.
    - **(d)** filtra bien pero **deja el fichero abierto**. Es la respuesta que casi todo el mundo marca, y por eso es la opción incorrecta más interesante del tema.

### T2.4 · ¿Qué hace?

``` { .java .numerado }
Files.writeString(Path.of("salida.txt"), "hola\n");
Files.writeString(Path.of("salida.txt"), "adiós\n");
```

**a)** El fichero contiene `hola` y `adiós` · **b)** Solo `adiós` · **c)** Solo `hola` · **d)** Falla la segunda

??? success "Solución"

    **b)** Solo `adiós`. Por defecto `writeString` **sobrescribe**.

    Para añadir al final hay que decirlo:

    ```java
    Files.writeString(p, "adiós\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
    ```

### T2.5 · ¿Por qué falla?

``` { .java .numerado }
var precio = new BigDecimal(campo[6].trim());   // campo[6] vale "38,00"
```

**a)** `NumberFormatException` · **b)** Devuelve 38 · **c)** Devuelve 3800 · **d)** Devuelve 38.00

??? success "Solución"

    **a)** `BigDecimal` espera el **punto** como separador decimal. Con una coma lanza `NumberFormatException`.

    ```java
    new BigDecimal(campo[6].trim().replace(',', '.'))
    ```

    Y ojo con el efecto dominó: si capturas esa excepción y cuentas la fila como descartada, acabas cargando **cero** conciertos y fallando todo lo que venga detrás.

### T2.6 · Completa el hueco

``` { .java .numerado }
try (var lineas = Files.lines(csv, ____)) {
    …
}
```

**a)** nada, sobra el segundo parámetro · **b)** `StandardCharsets.UTF_8` · **c)** `"UTF-8"` · **d)** `Charset.systemDefault()`

??? success "Solución"

    **b)** Siempre **explícita**, y siempre la del fichero.

    Sin el segundo parámetro se usa la codificación por defecto del sistema, que en tu Windows y en el servidor Linux **no es la misma**: el programa funciona en clase y sale `Ã±` al desplegar. La (d) es lo mismo, pero escrito a propósito.

    La (c) no compila: el parámetro es un `Charset`, no un `String`.

### T2.7 · ¿Qué imprime?

``` { .java .numerado }
var p = Path.of("datos", "2026", "ventas.csv");
IO.println(p.getFileName());
IO.println(p.getParent());
```

**a)** `ventas.csv` y `datos/2026` · **b)** `datos` y `ventas.csv` · **c)** `ventas.csv` y `datos` · **d)** `csv` y `datos/2026`

??? success "Solución"

    **a)** `getFileName()` da el **último** elemento y `getParent()`, la ruta sin él.

    `Path.of` con varios argumentos construye la ruta con el separador **del sistema**, así que el mismo código funciona en Windows y en Linux. Concatenar con `"/"` o `"\\"` a mano es el fallo que rompe la entrega al cambiar de máquina.

---

## Tema 3 — JSON y Jackson

### T3.1 · ¿Cuál escribe las fechas como `"2026-07-10"`?

**a)** `new ObjectMapper()`
**b)** `new ObjectMapper().registerModule(new JavaTimeModule())`
**c)** `JsonMapper.builder().addModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS).build()`
**d)** `new ObjectMapper().enable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)`

??? success "Solución"

    **c)** Hacen falta **las dos cosas**.

    - **(a)** ni siquiera sabe escribir `LocalDate`: lanza *«Java 8 date/time type not supported by default»*.
    - **(b)** ya sabe, pero por defecto escribe `[2026,7,10]`.
    - **(d)** activa justo lo contrario.

    En una aplicación Spring Boot esto viene hecho: `spring-boot-starter-json` registra el módulo y desactiva los *timestamps*. En Java puro, hay que montarlo.

### T3.2 · ¿Por qué falla?

``` { .java .numerado }
var raiz = mapper.readTree(json);
var ciudad = raiz.get("direccion").get("ciudad").asText();
```

**a)** No compila · **b)** `NullPointerException` si falta `direccion` · **c)** Devuelve `""` si falta · **d)** Lanza `JsonProcessingException`

??? success "Solución"

    **b)** `get` devuelve **`null`** cuando el campo no existe, y encadenar sobre `null` revienta.

    La alternativa segura es `path`, que devuelve un nodo «ausente» en vez de `null`:

    ```java
    var ciudad = raiz.path("direccion").path("ciudad").asText("desconocida");
    ```

    Con datos de terceros —la UT9 entera— esto no es un detalle: los campos faltan constantemente.

### T3.3 · ¿Qué imprime?

``` { .java .numerado }
record Persona(String nombre, String apodo) {}
var m = JsonMapper.builder()
        .serializationInclusion(JsonInclude.Include.NON_NULL)
        .build();
IO.println(m.writeValueAsString(new Persona("Ana", null)));
```

**a)** `{"nombre":"Ana","apodo":null}` · **b)** `{"nombre":"Ana"}` · **c)** `{"nombre":"Ana","apodo":""}` · **d)** Lanza excepción

??? success "Solución"

    **b)** `NON_NULL` **omite** los campos nulos: no salen en la salida.

    Es lo que se pide cuando el enunciado dice «sin campos nulos». Sin esa configuración saldría la (a).

### T3.4 · ¿Cuál es correcta?

> Leer un JSON con un campo que tu `record` no tiene, **sin que falle**.

**a)** No se puede: hay que declarar todos los campos
**b)** `mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)`
**c)** Anotar el `record` con `@JsonIgnore`
**d)** Funciona por defecto

??? success "Solución"

    **b)** Por defecto Jackson **falla** ante un campo desconocido, con `UnrecognizedPropertyException`.

    También vale anotar la clase con `@JsonIgnoreProperties(ignoreUnknown = true)`, que es más quirúrgico. La (c) es otra cosa: `@JsonIgnore` marca un campo **tuyo** para que no se serialice.

    En Spring Boot esto viene desactivado por defecto, así que el fallo solo lo ves en Java puro. Otro motivo para entender ahora lo que luego el framework te hace.

### T3.5 · ¿Qué tipo hay que pasarle?

``` { .java .numerado }
List<Producto> ps = mapper.readValue(json, ____);
```

**a)** `List.class` · **b)** `Producto[].class` y luego `Arrays.asList` · **c)** `new TypeReference<List<Producto>>() {}` · **d)** `List<Producto>.class`

??? success "Solución"

    **c)** Por el **borrado de tipos**, en tiempo de ejecución `List<Producto>` y `List<String>` son la misma clase. `TypeReference` conserva el tipo genérico para que Jackson sepa qué construir.

    - **(a)** compila y te devuelve una lista de `LinkedHashMap`, no de `Producto`. Falla más tarde, con un `ClassCastException` desconcertante.
    - **(b)** funciona de verdad, y es la alternativa aceptable.
    - **(d)** no compila: no existe el literal de clase de un tipo genérico.

### T3.6 · ¿Qué imprime?

``` { .java .numerado }
var json = "{\"total\": 10.5}";
var raiz = new ObjectMapper().readTree(json);
IO.println(raiz.get("total").asInt());
```

**a)** `10.5` · **b)** `10` · **c)** `11` · **d)** Lanza excepción

??? success "Solución"

    **b)** `asInt()` **trunca**, no redondea. Y no avisa.

    Es el tipo de fallo que no rompe nada y falsea un informe entero. Para dinero, `decimalValue()`; para decimales, `asDouble()`.

---

## Tema 4 — Fechas y validación

### T4.1 · Completa el hueco

> Filtrar por intervalo **incluyendo los extremos**.

``` { .java .numerado }
.filter(v -> ____)
```

**a)** `v.fecha().isAfter(desde) && v.fecha().isBefore(hasta)`
**b)** `!v.fecha().isBefore(desde) && !v.fecha().isAfter(hasta)`
**c)** `v.fecha().compareTo(desde) > 0`
**d)** `v.fecha().equals(desde) || v.fecha().equals(hasta)`

??? success "Solución"

    **b)** `isAfter` e `isBefore` son **estrictos**: con la (a) los dos extremos quedan fuera.

    La forma de incluirlos es negar la contraria: *«no es anterior a `desde` y no es posterior a `hasta`»*.

    Es la pregunta que más se falla del tema, y en un examen práctico se traduce en que faltan dos filas del informe.

### T4.2 · ¿Qué imprime?

``` { .java .numerado }
var d = LocalDate.of(2026, 1, 31).plusMonths(1);
IO.println(d);
```

**a)** `2026-02-31` · **b)** `2026-03-03` · **c)** `2026-02-28` · **d)** Lanza excepción

??? success "Solución"

    **c)** `2026-02-28`. `java.time` **ajusta al último día válido** del mes destino.

    Tiene una consecuencia que sorprende: **la operación no es reversible**. `plusMonths(1).minusMonths(1)` sobre el 31 de enero devuelve el 28 de enero, no el 31.

### T4.3 · ¿Cuánto vale?

``` { .java .numerado }
var a = LocalTime.of(19, 30);
var b = LocalTime.of(20, 45);
IO.println(Duration.between(a, b).toMinutes());
```

**a)** `75` · **b)** `1` · **c)** `115` · **d)** `1.25`

??? success "Solución"

    **a)** `75` minutos. `Duration.between` devuelve una `Duration`, y `toMinutes()` da el total en minutos, no «los minutos que sobran de las horas».

    La (b) sería `toHours()`, que trunca. Confundirlas es lo que hace que una comprobación de solapes de «menos de 90 minutos» no detecte nada.

### T4.4 · ¿Qué imprime?

``` { .java .numerado }
IO.println(LocalDate.parse("2026-7-10"));
```

**a)** `2026-07-10` · **b)** `DateTimeParseException` · **c)** `null` · **d)** `2026-07-01`

??? success "Solución"

    **b)** El formato ISO exige **dos dígitos** en el mes y en el día: `2026-07-10`.

    `LocalDate.parse` sin formateador solo acepta ISO estricto. Para otros formatos hay que decírselo:

    ```java
    LocalDate.parse("10/07/2026", DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    ```

    En una ingesta con datos ajenos, esta excepción es la que más filas descarta.

### T4.5 · ¿Cuál es correcta?

> Validar que una cadena es un correo, de forma razonable para un ejercicio de clase.

**a)** `s.contains("@")`
**b)** `s.matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$")`
**c)** `s.split("@").length == 2`
**d)** Una expresión regular que cumpla el RFC 5322 entero

??? success "Solución"

    **b)** Comprueba lo razonable: algo antes de la arroba, un dominio y una extensión de al menos dos letras.

    - **(a)** acepta `"@"` a secas.
    - **(c)** acepta `"@x"`.
    - **(d)** es un error de criterio: la expresión regular completa del RFC ocupa cientos de caracteres, nadie la mantiene y **acepta cosas que ningún servidor de correo admite**. La validación de verdad de un correo es mandarle un mensaje.

---

## Tema 5 — Repositorio y capas

### T5.1 · ¿Dónde va cada cosa?

Coloca en su capa: *(a)* abrir el CSV y devolver una lista · *(b)* calcular la recaudación por escenario · *(c)* imprimir la tabla alineada · *(d)* descartar las filas con aforo negativo.

??? success "Solución"

    | | Capa | Por qué |
    |---|---|---|
    | (a) abrir el CSV | **Repositorio** | Es acceso a datos. Es lo único que sabe que hay un fichero |
    | (b) recaudación por escenario | **Servicio** | Es una regla de negocio |
    | (c) imprimir la tabla | **Presentación** | Es la única que sabe que hay una consola |
    | (d) descartar aforo negativo | **Depende** | Si es «este fichero trae basura», repositorio. Si es «un concierto sin aforo no es válido para el negocio», servicio |

    La (d) es la interesante, y la respuesta honesta es que hay que decidirlo y ser coherente. En este módulo se hace **en el repositorio**: la limpieza de la fuente se queda en la frontera con la fuente.

    **La prueba de que están bien separadas:** cambiar el CSV por una base de datos debería tocar solo el repositorio; cambiar la consola por una web, solo la presentación.

### T5.2 · ¿Por qué esto está mal?

``` { .java .numerado }
public class ConciertoRepositorio {
    public List<Concierto> cargar(Path csv) throws IOException {
        var lista = /* ... leer el fichero ... */;
        System.out.printf("Cargados %d conciertos%n", lista.size());
        return lista;
    }
}
```

**a)** No compila · **b)** El repositorio no debe imprimir · **c)** Debería devolver `Optional` · **d)** Está bien

??? success "Solución"

    **b)** El `System.out` ata el repositorio a la consola.

    Se ve enseguida al querer reutilizarlo: el día que esa carga se ejecute desde una web o desde un test, seguirá escribiendo en la salida estándar sin que nadie se lo haya pedido. Y en un test, ensucia la salida.

    Lo que devuelve el repositorio es **datos**; quien decide si se enseñan y cómo es la presentación. Si hace falta dejar constancia, se usa un *logger*, no `System.out`.

### T5.3 · ¿Cuál es correcta?

> El servicio necesita los conciertos. ¿Cómo se los damos?

**a)** El servicio crea el repositorio dentro: `new ConciertoRepositorio()`
**b)** El servicio lo recibe en el constructor
**c)** El repositorio es `static` y se llama directamente
**d)** El servicio lee el fichero él mismo

??? success "Solución"

    **b)** **Por el constructor.** Es inyección de dependencias hecha a mano, y es exactamente lo que la UT4 le va a pedir a Spring que haga solo.

    ```java
    public class InformeService {
        private final FuenteConciertos fuente;
        public InformeService(FuenteConciertos fuente) { this.fuente = fuente; }
    }
    ```

    Lo que se gana, y es la razón de todo el tema: **el servicio se puede probar sin fichero**. Le pasas un doble con conciertos inventados y compruebas los cálculos.

    - **(a)** y **(c)** dejan el servicio atado al fichero para siempre.
    - **(d)** borra la separación de capas.

### T5.4 · ¿Qué gana el programa con la interfaz?

``` { .java .numerado }
public interface FuenteConciertos {
    ResultadoCarga cargar();
}
```

**a)** Va más rápido · **b)** Se puede cambiar el origen sin tocar el servicio, y probarlo con un doble · **c)** Es obligatorio en Java · **d)** Ahorra memoria

??? success "Solución"

    **b)** Las dos cosas, y la segunda es la que se nota a diario.

    - **Cambiar el origen**: el día que los conciertos vengan de PostgreSQL o de una API, se escribe otra implementación y el servicio no se entera.
    - **Probar sin fichero**: en el test se le pasa un doble que devuelve una lista fija. El test corre en milisegundos, sin disco y sin depender de que el CSV exista.

    Ninguna de las otras tres tiene nada que ver: una interfaz no acelera nada, no es obligatoria y no ahorra memoria.

---

!!! success "Cuando hayas hecho las 30"
    Vuelve a la [página de preparación del examen](../examen/) y haz el **simulacro cronometrado**. Menos de dos minutos por pregunta, que es el ritmo real.
