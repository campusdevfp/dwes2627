# Batería de ejercicios — UT3

**Dominio: una tienda de bicicletas.** Distinto del de las prácticas guiadas a propósito.

**30 ejercicios agrupados por tema**, con solución. Todos se ejecutan con `java Fichero.java` salvo los de Jackson, que necesitan Maven.

| Tema | Ejercicios | Sesiones |
|---|---|:-:|
| 1 · Estructuras de datos | E1–E7 | S1–S2 |
| 2 · Ficheros | E8–E14 | S3–S4 |
| 3 · JSON y Jackson | E15–E20 | S5–S6 |
| 4 · Fechas y validación | E21–E25 | S7 |
| 5 · Repositorio y capas | E26–E30 | S8–S9 |

---

# Tema 1 · Estructuras de datos

### E1 ● — Elegir la estructura

Para cada caso, di cuál usarías y por qué: (a) el orden de llegada a un taller, (b) los números de bastidor ya registrados, (c) buscar una bici por bastidor, (d) el ranking por ventas, (e) deshacer la última operación.

??? success "Solución"

    | Caso | Estructura | Por qué |
    |---|---|---|
    | (a) Orden de llegada | `ArrayList` o `ArrayDeque` | Importa el orden |
    | (b) Bastidores registrados | `HashSet` | Sin repetidos, solo importa si está |
    | (c) Buscar por bastidor | `HashMap<String, Bici>` | Acceso directo, coste constante |
    | (d) Ranking | `List` ordenada o `TreeMap` | Hace falta orden por valor |
    | (e) Deshacer | `ArrayDeque` como pila | Último en entrar, primero en salir |

    El (c) es el que más se falla: recorrer una lista buscando el bastidor funciona con 10 bicis y es un desastre con 100.000.


### E2 ● — El coste que no se ve

Mide cuánto tarda buscar 10.000 veces en una `List` de 100.000 elementos frente a un `HashMap`.

??? success "Solución"

    ```java
    var inicio = System.nanoTime();
    for (int i = 0; i < 10_000; i++) lista.stream().filter(b -> b.bastidor().equals(buscado)).findFirst();
    IO.println("Lista: " + (System.nanoTime() - inicio) / 1_000_000 + " ms");
    ```
    La lista tarda **segundos**; el mapa, **milisegundos**. La diferencia es O(n) frente a O(1), y con datos reales decide si tu aplicación sirve o no.


### E3 ●● — Ordenar con criterios encadenados

Ordena las bicis por marca, y dentro de cada marca por precio descendente.

??? success "Solución"

    ```java
    var ordenadas = bicis.stream()
        .sorted(comparing(Bici::marca).thenComparing(comparing(Bici::precio).reversed()))
        .toList();
    ```
    El error típico es `.sorted(comparing(Bici::marca)).sorted(comparing(Bici::precio))`: el segundo `sorted` **deshace** el primero. Los criterios se encadenan, no se apilan.


### E4 ●● — Agrupar y contar

Cuántas bicis hay de cada tipo, con las claves ordenadas alfabéticamente.

??? success "Solución"

    ```java
    Map<Tipo, Long> porTipo = bicis.stream()
        .collect(groupingBy(Bici::tipo, TreeMap::new, counting()));
    ```
    Sin el `TreeMap::new` sale un `HashMap` y **el orden de las claves es impredecible**. Si el enunciado pide orden, hay que pedirlo explícitamente.


### E5 ●●● — Doble agrupación

Por tipo y, dentro de cada tipo, por marca. Ambos niveles ordenados.

??? success "Solución"

    ```java
    Map<Tipo, Map<String, List<String>>> arbol = bicis.stream()
        .collect(groupingBy(Bici::tipo, TreeMap::new,
                 groupingBy(Bici::marca, TreeMap::new,
                 mapping(Bici::modelo, toList()))));
    ```
    El `TreeMap::new` hay que ponerlo en **los dos** niveles. Poner solo el exterior es el fallo del examen, y se ve directamente en la salida.


### E6 ●●● — Estadísticas de una pasada

Media, mínimo, máximo y total de precios, recorriendo **una sola vez**.

??? success "Solución"

    ```java
    var stats = bicis.stream().mapToDouble(b -> b.precio().doubleValue()).summaryStatistics();
    IO.println("Media %.2f · Min %.2f · Max %.2f · Total %.2f"
        .formatted(stats.getAverage(), stats.getMin(), stats.getMax(), stats.getSum()));
    ```
    Hacer cuatro `stream()` separados es cuatro veces el trabajo. Y para dinero, mejor `BigDecimal` con `reduce`: `summaryStatistics` obliga a pasar por `double`.


### E7 ●●● — El bucle lento

Reescribe esto y explica por qué el original es cuadrático.

```java
List<String> marcas = new ArrayList<>();
for (Bici b : bicis) if (!marcas.contains(b.marca())) marcas.add(b.marca());
```

??? success "Solución"

    ```java
    List<String> marcas = bicis.stream().map(Bici::marca).distinct().sorted().toList();
    ```
    `marcas.contains(...)` recorre la lista entera **en cada iteración**: con 10.000 bicis son hasta 50 millones de comparaciones. `distinct()` usa un conjunto por debajo y es lineal.

    Es el mismo patrón que en la UT2 (E11) y reaparece en la UT9 al deduplicar una ingesta.


---

# Tema 2 · Ficheros

### E8 ● — Leer y contar

Lee un fichero de texto y cuenta líneas, palabras y caracteres.

??? success "Solución"

    ```java
    try (var lineas = Files.lines(ruta, StandardCharsets.UTF_8)) {
        var lista = lineas.toList();
        IO.println("Líneas: " + lista.size());
        IO.println("Palabras: " + lista.stream().mapToLong(l -> l.split("\\s+").length).sum());
        IO.println("Caracteres: " + lista.stream().mapToInt(String::length).sum());
    }
    ```
    El `try-with-resources` no es opcional: `Files.lines` mantiene el fichero abierto hasta que se cierra el flujo. Sin él, en Windows no podrás ni borrar el fichero después.


### E9 ●● — CSV con sus trampas

Lee un CSV con separador `;`, decimales con coma y líneas en blanco.

??? success "Solución"

    ```java
    try (var lineas = Files.lines(ruta, StandardCharsets.UTF_8)) {
        return lineas.skip(1)
            .filter(l -> !l.isBlank())
            .map(l -> l.split(";", -1))
            .filter(c -> c.length >= 5)
            .map(c -> new Bici(c[0].trim(), c[1].trim(),
                               new BigDecimal(c[3].trim().replace(',', '.'))))
            .toList();
    }
    ```
    Las tres trampas: el separador, el `-1` del `split` —que conserva los campos vacíos del final— y la coma decimal.


### E10 ●● — Contar los descartes

Cuenta cuántas filas se descartan y por qué motivo.

??? success "Solución"

    ```java
    record Resultado(List<Bici> validas, Map<String, Integer> motivos) {}

    var motivos = new TreeMap<String, Integer>();
    // … dentro del bucle:
    if (c.length < 5)        { motivos.merge("columnas insuficientes", 1, Integer::sum); continue; }
    if (c[1].isBlank())      { motivos.merge("sin marca", 1, Integer::sum); continue; }
    if (precio.signum() <= 0){ motivos.merge("precio no válido", 1, Integer::sum); continue; }
    ```
    Una carga que descarta en silencio es una caja negra. Cuando falten registros, sin este mapa no hay forma de explicar por qué.


### E11 ●● — Filtrar y escribir

Lee el catálogo, filtra las bicis de menos de 500 € y escribe el resultado en otro CSV.

??? success "Solución"

    ```java
    var baratas = catalogo.stream()
        .filter(b -> b.precio().compareTo(new BigDecimal("500")) < 0)
        .map(b -> String.join(";", b.bastidor(), b.marca(), b.precio().toString()))
        .toList();

    Files.write(destino,
        Stream.concat(Stream.of("bastidor;marca;precio"), baratas.stream()).toList(),
        StandardCharsets.UTF_8, CREATE, TRUNCATE_EXISTING);
    ```
    El `TRUNCATE_EXISTING` es importante: sin él, si el fichero existía y era más largo, quedan restos del anterior al final.


### E12 ●●● — Escritura atómica

Que un fallo a mitad de la escritura no deje el fichero corrupto.

??? success "Solución"

    ```java
    var tmp = Files.createTempFile(destino.getParent(), "tmp", ".csv");
    Files.write(tmp, lineas, StandardCharsets.UTF_8);
    Files.move(tmp, destino, REPLACE_EXISTING, ATOMIC_MOVE);
    ```
    Se escribe en un temporal **en la misma carpeta** —el movimiento atómico solo funciona dentro del mismo sistema de ficheros— y se sustituye de golpe.

    Si el proceso muere a mitad, el fichero original sigue intacto. Es lo que hace cualquier editor de texto al guardar.


### E13 ●● — Recorrer una carpeta

Inventario de una carpeta: nombre, tamaño y fecha de todos los `.csv`, incluidos los de subcarpetas.

??? success "Solución"

    ```java
    try (var rutas = Files.walk(carpeta)) {
        rutas.filter(Files::isRegularFile)
             .filter(p -> p.toString().endsWith(".csv"))
             .sorted(comparing(p -> p.getFileName().toString()))
             .forEach(p -> {
                 try {
                     IO.println("%-30s %8d bytes  %s".formatted(
                         p.getFileName(), Files.size(p),
                         Files.getLastModifiedTime(p).toInstant()));
                 } catch (IOException e) { throw new UncheckedIOException(e); }
             });
    }
    ```
    El `UncheckedIOException` dentro del `forEach` es el patrón habitual: las lambdas no pueden lanzar excepciones comprobadas.


### E14 ●●● — Errores de E/S bien tratados

Trata por separado: fichero que no existe, sin permisos, y contenido mal formado.

??? success "Solución"

    ```java
    public List<Bici> cargar(Path ruta) {
        if (!Files.exists(ruta))    throw new CatalogoNoEncontradoException(ruta);
        if (!Files.isReadable(ruta)) throw new CatalogoNoLegibleException(ruta);
        try (var lineas = Files.lines(ruta, StandardCharsets.UTF_8)) {
            return procesar(lineas);
        } catch (MalformedInputException e) {
            throw new CatalogoCorruptoException("Codificación incorrecta: ¿es UTF-8?", e);
        } catch (IOException e) {
            throw new CatalogoException("Error leyendo " + ruta, e);
        }
    }
    ```
    El `MalformedInputException` es el que aparece con un fichero en ISO-8859-1 leído como UTF-8. Distinguirlo permite dar un mensaje útil en vez de un genérico.


---

# Tema 3 · JSON y Jackson

### E15 ● — De objeto a JSON

Serializa una lista de bicis a un fichero, con formato legible.

??? success "Solución"

    ```java
    var mapper = JsonMapper.builder().addModule(new JavaTimeModule()).build();
    mapper.writerWithDefaultPrettyPrinter().writeValue(new File("bicis.json"), bicis);
    ```


### E16 ●● — Fechas ISO y sin nulos

Configura el mapeador para que las fechas salgan legibles y no aparezcan campos nulos.

??? success "Solución"

    ```java
    var mapper = JsonMapper.builder()
        .addModule(new JavaTimeModule())
        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        .serializationInclusion(JsonInclude.Include.NON_NULL)
        .build();
    ```
    Sin la segunda línea, un `LocalDate` sale como `[2026,7,10]` o como un número enorme. Es el fallo que más se ve en las entregas.


### E17 ●● — De JSON a objeto

Lee un JSON de una lista y conviértelo en `List<Bici>`.

??? success "Solución"

    ```java
    List<Bici> bicis = mapper.readValue(fichero, new TypeReference<List<Bici>>() {});
    ```
    El `TypeReference` es obligatorio por el borrado de tipos: `readValue(f, List.class)` devuelve una lista de `LinkedHashMap`, no de bicis, y el error aparece más tarde y en otro sitio.


### E18 ●●● — JSON ajeno con campos que sobran

Consume un JSON que trae campos que no te interesan y que puede cambiar.

??? success "Solución"

    ```java
    @JsonIgnoreProperties(ignoreUnknown = true)
    record RespuestaExterna(String id, String nombre, @JsonProperty("precio_eur") BigDecimal precio) {}
    ```
    Sin `ignoreUnknown`, el día que el proveedor añada un campo tu aplicación empieza a fallar sin que hayas tocado nada. Y `@JsonProperty` mapea nombres que no siguen tu convención.


### E19 ●●● — De CSV a JSON agrupado

Lee el CSV y escribe un JSON con las bicis agrupadas por tipo.

??? success "Solución"

    ```java
    Map<String, List<Bici>> agrupadas = catalogo.stream()
        .collect(groupingBy(b -> b.tipo().name(), TreeMap::new, toList()));
    mapper.writerWithDefaultPrettyPrinter().writeValue(new File("catalogo.json"), agrupadas);
    ```
    Un `Map` se serializa como objeto JSON con las claves como propiedades. Con `TreeMap`, además, salen ordenadas — y un JSON con orden estable se puede comparar con `diff` entre ejecuciones.


### E20 ●● — Recorrer un JSON sin clase

Extrae un dato de un JSON del que no quieres crear la clase entera.

??? success "Solución"

    ```java
    JsonNode raiz = mapper.readTree(json);
    String titulo = raiz.path("datos").path(0).path("titulo").asText("desconocido");
    int total = raiz.path("meta").path("total").asInt(0);
    ```
    `path()` frente a `get()`: `path` devuelve un nodo vacío si no existe, así que se puede encadenar sin comprobar nulos. `get()` devuelve `null` y encadenarlo revienta.


---

# Tema 4 · Fechas y validación

### E21 ● — La fecha que no cambia

Explica qué imprime y por qué.

```java
var fecha = LocalDate.of(2026, 7, 10);
fecha.plusDays(10);
IO.println(fecha);
```

??? success "Solución"

    Imprime **2026-07-10**. `java.time` es **inmutable**: `plusDays` devuelve una fecha nueva y no toca la original.

    ```java
    fecha = fecha.plusDays(10);      // 
    ```
    Casi toda la clase falla esta pregunta la primera vez.


### E22 ●● — Cálculos con fechas

Días entre dos fechas, si una revisión está vencida, y la próxima revisión a seis meses.

??? success "Solución"

    ```java
    long dias = ChronoUnit.DAYS.between(compra, hoy);
    boolean vencida = ultimaRevision.plusMonths(12).isBefore(LocalDate.now());
    LocalDate proxima = ultimaRevision.plusMonths(6);
    ```
    `Period.between` da años, meses y días por separado; `ChronoUnit.DAYS.between` da el total. Confundirlos es el error del tema.


### E23 ●● — Formatear y analizar

Muestra la fecha como `10/07/2026` y lee una escrita así.

??? success "Solución"

    ```java
    var f = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    String texto = fecha.format(f);
    LocalDate leida = LocalDate.parse("10/07/2026", f);
    ```
    Ojo con `MM` (mes) y `mm` (minutos): `dd/mm/yyyy` da un mes disparatado y ningún error.


### E24 ●● — Validar en el constructor

`Bici` que se valide sola: bastidor de 17 caracteres, marca obligatoria, precio positivo.

??? success "Solución"

    ```java
    record Bici(String bastidor, String marca, BigDecimal precio) {
        Bici {
            if (bastidor == null || bastidor.length() != 17)
                throw new IllegalArgumentException("El bastidor son 17 caracteres");
            if (marca == null || marca.isBlank())
                throw new IllegalArgumentException("La marca es obligatoria");
            if (precio == null || precio.signum() <= 0)
                throw new IllegalArgumentException("El precio debe ser positivo");
        }
    }
    ```
    El constructor compacto valida **antes** de asignar. Un objeto que existe es un objeto válido, y eso elimina comprobaciones repartidas por todo el código.


### E25 ●●● — Expresiones regulares útiles

Valida matrícula, correo y código postal, y explica por qué el correo es un caso especial.

??? success "Solución"

    ```java
    static final Pattern MATRICULA = Pattern.compile("^\\d{4}[BCDFGHJKLMNPRSTVWXYZ]{3}$");
    static final Pattern CP        = Pattern.compile("^(0[1-9]|[1-4]\\d|5[0-2])\\d{3}$");
    static final Pattern CORREO    = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    ```
    El correo **no se valida bien con una expresión regular**: la especificación admite cosas que ninguna regex razonable cubre. La comprobación de verdad es **enviar un mensaje** y que el usuario confirme. Una regex sencilla evita erratas obvias y ya está.

    Compilar el patrón una vez como constante importa: dentro de un bucle, `Pattern.compile` en cada iteración es coste puro.


---

# Tema 5 · Repositorio y capas

### E26 ●● — La interfaz primero

Define `BiciRepositorio` como interfaz y una implementación en memoria.

??? success "Solución"

    ```java
    public interface BiciRepositorio {
        List<Bici> buscarTodas();
        Optional<Bici> buscarPorBastidor(String bastidor);
        Bici guardar(Bici bici);
        boolean borrar(String bastidor);
    }
    ```
    La interfaz es el contrato. En la UT4 aparecerá una implementación con Spring y en la UT5 otra con JPA, **sin tocar nada de lo que la usa**.


### E27 ●● — Cambiar CSV por JSON tocando una línea

Dos implementaciones del mismo repositorio, y que el servicio no se entere.

??? success "Solución"

    ```java
    class BiciRepositorioCsv  implements BiciRepositorio { … }
    class BiciRepositorioJson implements BiciRepositorio { … }
    ```
    ```java
    BiciRepositorio repo = new BiciRepositorioJson(ruta);   // ← la única línea que cambia
    var servicio = new BiciServicio(repo);
    ```
    Este ejercicio vale por toda la explicación teórica de las interfaces. Hazlo y comprueba que el servicio y sus tests siguen pasando sin tocarlos.


### E28 ●● — El servicio con las reglas

`vender(bastidor)`: no existe → excepción; ya vendida → otra excepción; si no, marca y devuelve.

??? success "Solución"

    ```java
    public Bici vender(String bastidor) {
        var bici = repositorio.buscarPorBastidor(bastidor)
            .orElseThrow(() -> new BiciNoEncontradaException(bastidor));
        if (bici.vendida()) throw new BiciYaVendidaException(bastidor);
        return repositorio.guardar(bici.conVendida(true));
    }
    ```
    El orden importa: **primero existencia, después estado**. Es exactamente el criterio que se evalúa en la UT4 y en el examen del trimestre.


### E29 ●●● — Catálogo en capas completo

Monta repositorio, servicio y una capa de presentación por consola, con las tres separadas.

??? success "Solución"

    ``` { .text .sinajuste }
    src/
    ├── modelo/Bici.java  Tipo.java
    ├── repositorio/BiciRepositorio.java  BiciRepositorioCsv.java
    ├── servicio/BiciServicio.java  excepciones
    └── ui/Consola.java  Main.java
    ```
    La regla que se comprueba: **`Consola` no importa nada de `repositorio`**. Si lo hace, las capas están rotas.

    ```bash
    grep -r "import.*repositorio" src/ui/     # no debe devolver nada
    ```


### E30 ●●● — Simulacro de examen (55 min)

Con `datos/bicis.csv`: cargar contando descartes, cuatro informes con colecciones, exportar a JSON y un resumen por consola. Cronométrate.

??? success "Solución"

    Es el examen del primer trimestre con otro dominio. La solución completa está en las tres partes anteriores; lo que se entrena aquí es **el tiempo**.

    Reparto recomendado de los 55 minutos:

    | Minutos | Qué |
    |:-:|---|
    | 0–15 | Carga del CSV, con descartes contados |
    | 15–35 | Los cuatro informes |
    | 35–45 | Exportación a JSON |
    | 45–52 | Resumen por consola |
    | 52–55 | `mvn test` y empaquetar el `.zip` |

    Si en el minuto 20 no tienes la carga funcionando, **salta a los informes con datos de prueba a mano**. Entregar cuatro apartados a medias puntúa más que uno perfecto.


---

## Cómo usarlos en clase

| Momento | Ejercicios |
|---|---|
| Para arrancar la sesión, 10 min | E1 · E8 · E15 · E21 · E26 |
| Taller de la sesión, 25-30 min | E4 · E9 · E16 · E24 · E28 |
| Los que hay que hacer sí o sí | **E5 · E9 · E10 · E16 · E27** |
| Para quien va sobrado | E7 · E12 · E14 · E19 · E25 · E29 |
| Repaso antes del examen | E5 · E10 · E16 · E30 |

!!! tip "El que más se parece al examen"
    El **E30** es literalmente el examen con otro dominio, cronometrado. Hazlo una semana antes y sabrás exactamente dónde estás.

    Y el **E10** (contar los descartes) vale un 20 % del examen de RA3 él solo.
