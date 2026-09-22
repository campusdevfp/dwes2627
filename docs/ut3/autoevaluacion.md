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

## Tema 6 — Sobre la batería de ejercicios

Estas dieciséis salen directamente de lo que resolviste en la [batería](ejercicios.md), sobre todo de los programas completos E31–E38.

### T6.1 · ¿Qué estructura?

Necesitas las **últimas diez búsquedas, la más reciente primero**.

**a)** `ArrayList` · **b)** `HashSet` · **c)** `ArrayDeque` usado como pila · **d)** `TreeMap`

??? success "Solución"

    **c)** «La más reciente primero» es **LIFO**: último en entrar, primero en salir. Eso es una pila, y en Java moderno la pila es `ArrayDeque` con `push`/`pop`.

    `ArrayList` obligaría a insertar siempre en la posición 0, que desplaza todos los elementos. `HashSet` no tiene orden y además quitaría los repetidos, que aquí sí interesan. `TreeMap` ordena por clave, no por antigüedad.

    Y no: `Stack` no es la respuesta. Está obsoleta —es `synchronized` sin motivo— desde hace dos décadas.

### T6.2 · ¿Por qué falla?

``` { .java .numerado }
var facturacion = ventas.stream().collect(Collectors.groupingBy(
        Venta::vendedor, Collectors.summingDouble(v -> v.unidades() * v.precio())));

var ranking = facturacion.entrySet().stream()
        .sorted(Map.Entry.comparingByValue().reversed())
        .map(Map.Entry::getKey)
        .toList();
```

**a)** `groupingBy` no admite un segundo recolector · **b)** No compila: falta el tipo explícito en `comparingByValue` · **c)** `reversed()` no existe en `Comparator` · **d)** Compila y funciona

??? success "Solución"

    **b)** Hace falta `Map.Entry.<String, Double>comparingByValue().reversed()`.

    Sin los tipos explícitos, el compilador no puede inferir qué `Comparator` es dentro de la cadena y da un error de inferencia que no dice nada útil — de los que te tienen veinte minutos mirando la línea equivocada.

    Es una de esas cosas que no se deducen: se conocen o no. Y por eso está aquí.

### T6.3 · ¿Qué devuelve?

``` { .java .numerado }
var media = ventas.stream().mapToInt(Venta::unidades).average();
```

**a)** `double` · **b)** `Optional<Double>` · **c)** `OptionalDouble` · **d)** `int`

??? success "Solución"

    **c)** `OptionalDouble`, no `Optional<Double>`. Los streams de primitivos (`IntStream`, `LongStream`, `DoubleStream`) tienen sus propios `Optional` sin genéricos, justo para no hacer autoboxing.

    Es `Optional` porque **la lista puede estar vacía**, y la media de nada no existe. Por eso casi siempre se escribe `.average().orElse(0)`.

### T6.4 · ¿Por qué falla?

``` { .java .numerado }
List<Producto> leer(Path ruta) throws IOException {
    var lineas = Files.lines(ruta);
    return lineas.map(l -> l.split(","))
                 .map(c -> new Producto(Integer.parseInt(c[0]), c[1]))
                 .toList();
}
```

**a)** `Files.lines` no existe · **b)** Falta cerrar el stream y falta saltar la cabecera · **c)** `split` devuelve `List` · **d)** No falla

??? success "Solución"

    **b)** Dos fallos, y los dos caen.

    1. **`Files.lines` devuelve un stream que mantiene el fichero abierto.** Hay que envolverlo en un *try-with-resources*. Si no, el descriptor se queda colgado hasta que pase el recolector de basura — y con muchos ficheros se agota el límite del sistema.
    2. **Falta `skip(1)`.** La primera línea es la cabecera, así que `Integer.parseInt("id")` lanza `NumberFormatException` en la primera vuelta.

    Faltaría un tercero si el fichero acaba en línea en blanco: `filter(l -> !l.isBlank())`.

### T6.5 · ¿Qué imprime?

``` { .java .numerado }
IO.println("4,Casco,,".split(",").length);
IO.println("4,Casco,,".split(",", -1).length);
```

**a)** `4` y `4` · **b)** `2` y `4` · **c)** `4` y `2` · **d)** `2` y `2`

??? success "Solución"

    **b)** `2` y `4`.

    Por defecto, `split` **descarta los campos vacíos del final**. Con `-1` los conserva.

    Es el origen del `ArrayIndexOutOfBoundsException` más común de toda la unidad: el CSV tiene cuatro columnas, pero en las filas donde las últimas están vacías te llega un array de dos, y `c[3]` revienta.

    La regla: **para leer CSV, siempre `split(",", -1)`**.

### T6.6 · ¿Cuál es correcta?

Quieres escribir `12.5` en un CSV que se va a leer en otro país.

**a)** `String.format("%.2f", 12.5)` · **b)** `String.format(Locale.ROOT, "%.2f", 12.5)` · **c)** `"" + 12.5` · **d)** `Double.toString(12.5)`

??? success "Solución"

    **b)** Con la configuración regional española, `%.2f` escribe **`12,50`** — con coma. En un CSV separado por comas eso parte el campo en dos y corrompe el fichero.

    `Locale.ROOT` fuerza el punto decimal, siempre.

    La **c)** y la **d)** también dan punto, pero pierden el control del número de decimales: `12.5` sale como `12.5` y no como `12.50`.

    Es la clase de fallo que no aparece en tu máquina y sí en la del cliente.

### T6.7 · ¿Qué imprime?

``` { .java .numerado }
var f = LocalDate.of(2026, 3, 14);
f.plusDays(7);
IO.println(f);
```

**a)** `2026-03-21` · **b)** `2026-03-14` · **c)** Error de compilación · **d)** `null`

??? success "Solución"

    **b)** `2026-03-14`. **Todo `java.time` es inmutable.** `plusDays` no modifica la fecha: devuelve una nueva, y aquí se tira.

    ```java
    var nueva = f.plusDays(7);   // así sí
    ```

    El compilador no avisa porque la expresión es válida; simplemente no hace nada. Exactamente el mismo error que `cadena.trim();` sin asignar.

### T6.8 · ¿Cuántas noches?

``` { .java .numerado }
var entrada = LocalDate.of(2026, 7, 1);
var salida  = LocalDate.of(2026, 7, 3);
IO.println(ChronoUnit.DAYS.between(entrada, salida));
```

**a)** `2` · **b)** `3` · **c)** `1` · **d)** `0`

??? success "Solución"

    **a)** `2`. Del 1 al 3 hay **dos noches**: la del 1 al 2 y la del 2 al 3.

    Si lo que quieres es el número de **días de estancia** —incluyendo los dos extremos— son tres, y hay que sumar uno.

    Ese `±1` es el fallo clásico de cualquier aplicación de reservas, y por eso está en el test. Antes de escribir la fórmula, decide qué estás contando.

### T6.9 · ¿Por qué falla?

``` { .java .numerado }
if (!salida.isAfter(entrada)) {
    throw new IllegalArgumentException("La salida debe ser posterior");
}
```

Alguien lo «simplifica» a `if (salida.isBefore(entrada))`. ¿Qué se rompe?

**a)** Nada · **b)** Deja de rechazar que entrada y salida sean el mismo día · **c)** No compila · **d)** Rechaza reservas válidas

??? success "Solución"

    **b)** `isBefore` es estrictamente menor. Con entrada y salida el mismo día, `isBefore` es `false` y la reserva **pasa**: una reserva de cero noches.

    `!isAfter(...)` cubre «anterior **o igual**». Es la diferencia entre `<` y `<=`, y es justo donde viven los fallos de los rangos.

    Cuando escribas una validación de rango, prueba siempre **los tres casos**: antes, igual y después.

### T6.10 · ¿Qué hace falta?

``` { .java .numerado }
record ProductoApi(int id, String nombre, double precio) {}

var mapper = new ObjectMapper();
List<ProductoApi> ps = mapper.readValue(json,
        new TypeReference<List<ProductoApi>>() {});
```

El JSON trae además un campo `stock`. ¿Qué pasa?

**a)** Se ignora sin más · **b)** `UnrecognizedPropertyException` · **c)** `stock` se guarda en el record · **d)** Devuelve una lista vacía

??? success "Solución"

    **b)** Por defecto Jackson **falla** ante un campo que no sabe colocar.

    Se arregla de dos formas:

    ```java
    new ObjectMapper().configure(
            DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    ```

    ```java
    @JsonIgnoreProperties(ignoreUnknown = true)
    record ProductoApi(int id, String nombre, double precio) {}
    ```

    Y hay que hacerlo **siempre** que consumas una API ajena. Si no, el día que el proveedor añada un campo —y lo hará, sin avisarte— tu aplicación deja de funcionar sin que hayas tocado una línea.

### T6.11 · ¿Por qué `TypeReference`?

Si escribes `mapper.readValue(json, List.class)` en vez de usar `TypeReference`, obtienes…

**a)** Lo mismo · **b)** Una `List<LinkedHashMap>`, no de tus objetos · **c)** Un error de compilación · **d)** `null`

??? success "Solución"

    **b)** Una lista de `LinkedHashMap`. Y lo peor: **compila y no falla hasta que usas un elemento**, donde salta un `ClassCastException` lejos de la causa.

    El motivo es el borrado de tipos: en tiempo de ejecución, `List<ProductoApi>` y `List` son lo mismo, así que Jackson no tiene forma de saber qué poner dentro. `TypeReference` es una clase anónima cuyo único propósito es **conservar el tipo genérico** para que Jackson lo pueda leer.

### T6.12 · ¿Qué está mal?

``` { .java .numerado }
class ProductoServicio {
    private final ProductoRepositorioCsv repositorio;

    ProductoServicio() {
        this.repositorio = new ProductoRepositorioCsv(Path.of("datos/productos.csv"));
    }
}
```

**a)** Nada · **b)** Depende de la implementación y la crea él mismo: no se puede probar ni cambiar el origen · **c)** Falta `static` · **d)** El campo no debería ser `final`

??? success "Solución"

    **b)** Dos fallos que en realidad son el mismo.

    1. Depende de **`ProductoRepositorioCsv`**, la clase concreta, y no de la interfaz. Cambiar a JSON obliga a tocar el servicio.
    2. Hace el **`new` dentro**. Como nadie puede sustituir ese objeto desde fuera, para probar el servicio hace falta **un fichero CSV de verdad en el disco**.

    La versión correcta recibe la interfaz por constructor:

    ```java
    ProductoServicio(ProductoRepositorio repositorio) {
        this.repositorio = repositorio;
    }
    ```

    Esto es la *D* de SOLID, y es exactamente el problema que resuelve la inyección de dependencias de Spring en la UT4.

### T6.13 · ¿Dónde va?

«El precio con IVA es el precio por 1,21.» ¿En qué clase?

**a)** En el repositorio · **b)** En el servicio · **c)** En el `main` · **d)** En el fichero CSV

??? success "Solución"

    **b)** Es una **regla de negocio**, y las reglas viven en el servicio.

    El repositorio solo sabe traer y guardar. Si le metes el IVA, el día que necesites el precio sin IVA —para un informe, para una exportación— tendrás que deshacerlo, y el día que cambies a JSON tendrás que copiar la regla en la otra implementación.

    La prueba: **la misma regla tiene que valer aunque los datos vengan de otro sitio**. Si es así, es de negocio.

### T6.14 · ¿Qué devuelve el repositorio?

``` { .java .numerado }
Optional<Producto> buscarPorId(int id);
```

¿Por qué `Optional` y no devolver `null` o lanzar la excepción ahí mismo?

**a)** Por rendimiento · **b)** Porque el repositorio no sabe si no encontrarlo es un error; eso lo decide el servicio · **c)** Porque lo exige Jackson · **d)** Por convenio, sin más

??? success "Solución"

    **b)** «No está» es un **hecho**, no un error. Si el repositorio lanzara la excepción, estaría decidiendo por quien lo llama.

    Y hay casos en los que no encontrarlo es perfectamente normal: comprobar si un identificador está libre, por ejemplo.

    El servicio es el que sabe qué significa en cada caso:

    ```java
    repositorio.buscarPorId(id)
               .orElseThrow(() -> new ProductoNoEncontradoException(id));
    ```

    Frente a `null`, la ventaja es que **el compilador te obliga** a tratar el caso. Con `null` te obliga la `NullPointerException`, y en producción.

### T6.15 · ¿Qué demuestra este test?

``` { .java .numerado }
ProductoRepositorio enMemoria = new ProductoRepositorio() {
    public List<Producto> listar() { return List.of(); }
    public Optional<Producto> buscarPorId(int id) { return Optional.empty(); }
};
var servicio = new ProductoServicio(enMemoria);

assertThrows(ProductoNoEncontradoException.class, () -> servicio.obtener(99));
```

**a)** Que el CSV se lee bien · **b)** Que el servicio lanza la excepción cuando no hay dato, sin tocar el disco · **c)** Que la interfaz es innecesaria · **d)** Que `Optional` es lento

??? success "Solución"

    **b)** Y fíjate en lo que **no** hace: no abre ningún fichero, no necesita que exista un CSV de prueba y corre en milisegundos.

    Eso es posible **solo porque el servicio depende de la interfaz**. Si dependiera de `ProductoRepositorioCsv`, este test sería imposible sin preparar datos en el disco — y entonces ya no estarías probando el servicio, estarías probando el disco.

    Esta es la razón práctica de todo lo anterior. Cuando en un examen te pregunten «¿para qué sirve la interfaz?», la respuesta buena no es «para desacoplar»: es **esto**.

### T6.16 · ¿Cuántas líneas hay que tocar?

Tienes el catálogo funcionando con CSV y te piden pasarlo a JSON. El servicio y el resto del programa están bien hechos. ¿Cuántas líneas cambias, además de escribir la nueva implementación?

**a)** Ninguna · **b)** Una · **c)** Todas las del servicio · **d)** Depende del tamaño del fichero

??? success "Solución"

    **b)** Una: la del `main` donde se decide qué implementación se construye.

    ```java
    ProductoRepositorio repo = new ProductoRepositorioJson(Path.of("datos/productos.json"));
    var servicio = new ProductoServicio(repo);   // ni se entera
    ```

    Si al hacer el cambio te ves tocando el servicio, **las capas no están bien separadas** — y eso es lo que se mira en el ejercicio E38.

    En la UT4 esa línea desaparece del `main` y pasa a ser una anotación: Spring decide qué implementación inyectar. El principio es el mismo; lo que cambia es quién escribe el `new`.

---

## Simulacro cronometrado

Cuando hayas hecho las 46, siéntate **55 minutos con un reloj** y responde estas 30 seguidas, sin desplegar nada:

> **T1.1 · T1.3 · T1.5 · T1.6 · T1.8 · T2.1 · T2.2 · T2.5 · T2.6 · T3.1 · T3.2 · T3.5 · T4.1 · T4.3 · T4.5 · T5.1 · T5.3 · T6.1 · T6.2 · T6.4 · T6.5 · T6.6 · T6.7 · T6.8 · T6.10 · T6.11 · T6.12 · T6.13 · T6.15 · T6.16**

**Menos de dos minutos por pregunta**, que es el ritmo real del examen.

| Aciertos | Lectura |
|:-:|---|
| **24 o más** | Vas sobrado |
| **18 a 23** | Aprobado holgado. Repasa el bloque que peor te fue |
| **15 a 17** | Justo. Rehaz los programas E31–E38 |
| **menos de 15** | Faltan los ejercicios. No es cuestión de releer |

!!! tip "El formato del examen"
    **30 preguntas en 55 minutos**, opción múltiple con una sola correcta. Acierto **+1**, fallo **−0,25**, en blanco **0**.

    Con esa penalización, contestar al azar entre cuatro opciones sale neutro; **descartar una sola opción ya hace que compense arriesgar**. Lo que no compensa es dejar en blanco una pregunta en la que tienes intuición.
