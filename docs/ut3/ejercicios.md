# Batería de ejercicios — UT3

**Dominio: una tienda de bicicletas.** Distinto del de las prácticas guiadas a propósito.

**38 ejercicios agrupados por tema**, con solución: 30 fragmentos y **8 programas completos** (E31–E38), que son la práctica integradora de la unidad. Todos se ejecutan con `java Fichero.java` salvo los de Jackson, que necesitan Maven.

| Tema | Ejercicios | Sesiones |
|---|---|:-:|
| 1 · Estructuras de datos | E1–E7 | S1–S2 |
| 2 · Ficheros | E8–E14 | S3–S4 |
| 3 · JSON y Jackson | E15–E20 | S5–S6 |
| 4 · Fechas y validación | E21–E25 | S7 |
| 5 · Repositorio y capas | E26–E30 | S8–S9 |
| **6 · Taller largo: ocho programas** | **E31–E38** | S1–S9 |

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


### E30 ●●● — Convierte tus propios ejercicios en preguntas

Coge tres ejercicios que ya hayas resuelto y **escribe una pregunta de test sobre cada uno**, con sus cuatro opciones y los tres distractores plausibles.

??? success "Solución"

    No hay una única respuesta; hay un método. Para convertir un ejercicio en pregunta, cambia **una sola cosa** de tu código correcto y pregunta por el resultado.

    **Del E5 (doble agrupación):**

    ```java
    Map<String, Map<String, Long>> r = bicis.stream()
            .collect(groupingBy(Bici::marca, TreeMap::new,
                     groupingBy(Bici::tipo, counting())));
    ```

    > ¿Qué cambia si se quita el `TreeMap::new`?
    >
    > **A.** Nada, el resultado es el mismo · **B.** Las marcas dejan de salir ordenadas · **C.** Los tipos dejan de salir ordenados · **D.** No compila

    El distractor bueno es la **C**: es el fallo real, confundir en qué nivel actúa el `TreeMap`.

    **Del E9 (CSV con trampas):**

    ```java
    String[] campos = linea.split(";");
    ```

    > Con la línea `B-004;Orbea;;;;` ¿cuántos elementos tiene `campos`?
    >
    > **A.** 6 · **B.** 2 · **C.** 5 · **D.** 3

    La respuesta es **B**: `split` sin `-1` descarta los vacíos finales. Es el error que más `ArrayIndexOutOfBounds` provoca.

    **Del E16 (fechas ISO):**

    Enseña la salida `"fecha": [2026,3,14]` y pregunta qué falta configurar.

    ---

    **Por qué este ejercicio es el más rentable de la unidad:** escribir el distractor te obliga a saber **por qué** alguien se equivocaría. Y ese «por qué» es justo lo que se pregunta en el examen.

    Hazlo en parejas: cada uno escribe tres preguntas y se las pasa al otro. Las que no sabéis resolver son las que hay que repasar.


---

## Taller largo: ocho programas completos

!!! reto "Del E31 al E38 se entrega un programa que funciona"
    Los treinta primeros son fragmentos; estos son **programas enteros**, uno por bloque del temario. Eran el antiguo «reto con tests»: ahora están aquí, resueltos, porque en esta unidad lo que hay que entrenar es **reconocer**, no entregar.

    El dominio es distinto a propósito: un catálogo de productos, para que no valga copiar del de bicis.

### E31 ● — Elige la estructura y justifícalo

Declara la estructura adecuada para cada caso: (a) últimas 10 búsquedas, la más reciente primero · (b) correos suscritos · (c) ranking alfabético sin repetidos · (d) las notas de cada alumno · (e) cola de impresión.

??? success "Solución"

    ```java
    var busquedas = new ArrayDeque<String>();                 // (1)
    var correos   = new HashSet<String>();                    // (2)
    var ranking   = new TreeSet<String>();                    // (3)
    var notas     = new HashMap<String, List<Integer>>();     // (4)
    var impresion = new ArrayDeque<String>();                 // (5)
    ```

    1.  Como **pila**: `push` y `pop`. El último en entrar es el primero en salir.
    2.  Sin repetidos y el orden da igual. Búsqueda en tiempo constante.
    3.  Sin repetidos **y ordenado**. Cuesta más que `HashSet`, y a cambio no hay que ordenar después.
    4.  Un alumno → sus notas. La clave es lo que buscas.
    5.  Como **cola**: `offer` y `poll`. El primero en entrar es el primero en salir.

    `ArrayDeque` sirve de pila **y** de cola; lo que cambia son los métodos que usas. No uses `Stack` ni `Vector`: están obsoletos desde hace veinte años.

    | | Busca por | Orden | Repetidos |
    |---|---|---|:-:|
    | `ArrayList` | Posición | De inserción | Sí |
    | `HashSet` | Contenido | Ninguno | No |
    | `TreeSet` | Contenido | Natural | No |
    | `HashMap` | Clave | Ninguno | Claves no |
    | `TreeMap` | Clave | Natural | Claves no |
    | `ArrayDeque` | Extremos | De inserción | Sí |

### E32 ●● — Informe de ventas

Con `record Venta(String vendedor, String producto, int unidades, double precio)`, calcula: facturación por vendedor, producto más vendido en unidades, media de unidades y el ranking de vendedores por facturación descendente.

??? success "Solución"

    ```java
    var facturacion = ventas.stream().collect(Collectors.groupingBy(
            Venta::vendedor,
            Collectors.summingDouble(v -> v.unidades() * v.precio())));

    var top = ventas.stream()
            .collect(Collectors.groupingBy(Venta::producto,
                     Collectors.summingInt(Venta::unidades)))
            .entrySet().stream()
            .max(Map.Entry.comparingByValue())                    // (1)
            .map(Map.Entry::getKey)
            .orElse("—");

    var media = ventas.stream().mapToInt(Venta::unidades).average().orElse(0);

    var ranking = facturacion.entrySet().stream()
            .sorted(Map.Entry.<String, Double>comparingByValue().reversed())   // (2)
            .map(Map.Entry::getKey)
            .toList();
    ```

    1.  Para sacar el máximo de un mapa hay que **volver a hacer stream sobre `entrySet()`**. Es el paso que más cuesta ver.
    2.  Ojo al `<String, Double>` explícito: sin él, el compilador no sabe inferir el tipo dentro de `sorted` y da un error que no dice nada.

    `average()` devuelve **`OptionalDouble`**, no `double`, porque la lista puede estar vacía. De ahí el `orElse(0)`.

### E33 ●● — Lector de CSV

Crea `datos/productos.csv` con cabecera y cinco productos. Escribe `List<Producto> leer(Path)` que lo convierta en objetos, saltando la cabecera y las líneas en blanco.

??? success "Solución"

    ```java
    List<Producto> leer(Path ruta) throws IOException {
        try (var lineas = Files.lines(ruta, StandardCharsets.UTF_8)) {   // (1)
            return lineas
                    .skip(1)                                             // (2)
                    .filter(l -> !l.isBlank())                           // (3)
                    .map(l -> l.split(",", -1))                          // (4)
                    .map(c -> new Producto(
                            Integer.parseInt(c[0].trim()),
                            c[1].trim(),
                            c[2].trim(),
                            Double.parseDouble(c[3].trim())))
                    .toList();
        }
    }
    ```

    1.  `Files.lines` devuelve un stream que **hay que cerrar**: mantiene el fichero abierto. De ahí el *try-with-resources*. Y el juego de caracteres se dice siempre: si no, usa el del sistema y en Windows salen los acentos rotos.
    2.  `skip(1)` para la cabecera. Sin él, `Integer.parseInt("id")` revienta.
    3.  Las líneas en blanco del final son la causa número uno de `ArrayIndexOutOfBounds`.
    4.  El `-1` conserva los campos vacíos del final. Sin él, `"4,Casco,,"` devuelve **dos** elementos y no cuatro.

    Esos cuatro detalles son, literalmente, cuatro preguntas del test.

### E34 ●● — Escribir y capturar el fallo

Escribe los productos de más de 100 € en `caros.csv` conservando la cabecera. Después provoca y captura un `NoSuchFileException`.

??? success "Solución"

    ```java
    void escribir(Path ruta, List<Producto> productos) throws IOException {
        var sb = new StringBuilder("id,nombre,categoria,precio\n");
        for (var p : productos) {
            sb.append("%d,%s,%s,%.2f%n"
                    .formatted(p.id(), p.nombre(), p.categoria(), p.precio()));
        }
        Files.writeString(ruta, sb.toString(), StandardCharsets.UTF_8);
    }

    void main() throws IOException {
        var todos = leer(Path.of("datos/productos.csv"));
        escribir(Path.of("datos/caros.csv"),
                 todos.stream().filter(p -> p.precio() > 100).toList());

        try {
            Files.readString(Path.of("datos/fantasma.csv"));
        } catch (NoSuchFileException e) {
            IO.println("No existe: " + e.getFile());       // (1)
        }
    }
    ```

    1.  `NoSuchFileException` trae `getFile()` con la ruta. Capturar `IOException` a secas también funciona y pierdes esa información.

    Dos cosas sobre el formato: `%.2f` usa la coma decimal si la configuración regional es española, y eso **rompe un CSV separado por comas**. Para ficheros de intercambio conviene `Locale.ROOT`:

    ```java
    String.format(Locale.ROOT, "%.2f", 12.5)   // "12.50" siempre
    ```

    Y `%n` es el salto de línea **del sistema**; `\n` es siempre `\n`. Para un fichero que se va a leer en otra máquina, `\n` es más predecible.

### E35 ●● — De CSV a JSON

Convierte tu CSV en un `productos.json` legible, con Jackson.

??? success "Solución"

    ```java
    var mapper = new ObjectMapper();
    mapper.writerWithDefaultPrettyPrinter()
          .writeValue(Path.of("datos/productos.json").toFile(), leer(csv));
    ```

    Tres líneas. Abre el JSON generado: cada `record` se ha convertido en un objeto y la lista en un array, **sin escribir nada de conversión**.

    Lo que hay que saber para el test:

    | Quiero | Se hace con |
    |---|---|
    | Objeto → JSON | `writeValue` / `writeValueAsString` |
    | JSON → objeto | `readValue` |
    | Que salga con sangría | `writerWithDefaultPrettyPrinter()` |
    | Fechas en ISO, no como números | `registerModule(new JavaTimeModule())` + desactivar `WRITE_DATES_AS_TIMESTAMPS` |
    | Que no salgan los nulos | `@JsonInclude(NON_NULL)` |

    Sin el módulo de `java.time`, un `LocalDate` sale como `[2026,3,14]`. Es la pregunta de Jackson que más cae.

### E36 ●● — Consumir JSON ajeno

Te llega esto de una API. Diseña el record y deserialízalo sin que falle por el campo `stock`, que no te interesa.

```json
[{"id":1,"nombre":"Patinete","precio":120.0,"stock":8},
 {"id":2,"nombre":"Casco","precio":35.0,"stock":40}]
```

??? success "Solución"

    ```java
    record ProductoApi(int id, String nombre, double precio) {}

    var mapper = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);   // (1)

    List<ProductoApi> productos =
            mapper.readValue(json, new TypeReference<List<ProductoApi>>() {});      // (2)
    ```

    1.  Sin esto: `UnrecognizedPropertyException: "stock"`. Al consumir APIs de terceros se desactiva **siempre**: el día que añadan un campo, tu programa se cae en producción sin que hayas tocado nada.
    2.  Para una **lista** hace falta `TypeReference`. Con `readValue(json, List.class)` obtienes una lista de `LinkedHashMap`, no de tus objetos: el tipo genérico se pierde al compilar.

    La alternativa a desactivarlo globalmente es `@JsonIgnoreProperties(ignoreUnknown = true)` sobre el record. Hace lo mismo, acotado a esa clase.

### E37 ●●● — Reservas validadas

Crea `record Reserva(String cliente, String email, LocalDate entrada, LocalDate salida)` que valide en el constructor: cliente no vacío, correo con formato, entrada no pasada y salida posterior a la entrada. Añade `noches()`.

??? success "Solución"

    ```java
    record Reserva(String cliente, String email, LocalDate entrada, LocalDate salida) {

        private static final Pattern CORREO =
                Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[\\w.]{2,}$");   // (1)

        Reserva {
            if (cliente == null || cliente.isBlank()) {
                throw new IllegalArgumentException("Cliente obligatorio");
            }
            if (email == null || !CORREO.matcher(email).matches()) {
                throw new IllegalArgumentException("Correo no válido: " + email);
            }
            if (entrada.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("La entrada no puede ser pasada");
            }
            if (!salida.isAfter(entrada)) {                              // (2)
                throw new IllegalArgumentException("La salida debe ser posterior");
            }
        }

        long noches() { return ChronoUnit.DAYS.between(entrada, salida); }
    }
    ```

    1.  El `Pattern` es **`static final`**: compilarlo en cada validación es caro y no cambia nunca.
    2.  `!salida.isAfter(entrada)` y no `salida.isBefore(entrada)`: así también se rechaza que sean el mismo día. Es la diferencia entre `<` y `<=`, y es donde viven los fallos.

    Y el recordatorio permanente de `java.time`: **todo es inmutable**.

    ```java
    var f = LocalDate.of(2026, 3, 14);
    f.plusDays(7);              // no hace nada: se pierde
    var nueva = f.plusDays(7);  // así sí
    ```

    `ChronoUnit.DAYS.between` da **noches**, no días de estancia: del 1 al 3 son 2 noches. Ese `±1` es el fallo clásico de cualquier aplicación de reservas.

### E38 ●●● — Catálogo en capas, entero

Monta: `Producto` (record) · `ProductoRepositorio` (interfaz) · `ProductoRepositorioCsv` · `ProductoServicio` · `App`. Después añade `ProductoRepositorioJson` y comprueba que **el servicio no cambia**. Termina con un test que use un repositorio en memoria.

??? success "Solución"

    ```java
    interface ProductoRepositorio {
        List<Producto> listar();
        Optional<Producto> buscarPorId(int id);
    }

    class ProductoRepositorioCsv implements ProductoRepositorio {
        private final Path fichero;
        ProductoRepositorioCsv(Path fichero) { this.fichero = fichero; }

        @Override public List<Producto> listar() { /* el E33 */ }
        @Override public Optional<Producto> buscarPorId(int id) {
            return listar().stream().filter(p -> p.id() == id).findFirst();
        }
    }

    class ProductoServicio {
        private final ProductoRepositorio repositorio;

        ProductoServicio(ProductoRepositorio repositorio) {    // (1)
            this.repositorio = repositorio;
        }

        Producto obtener(int id) {
            return repositorio.buscarPorId(id)
                    .orElseThrow(() -> new ProductoNoEncontradoException(id));
        }

        double precioConIva(int id) { return obtener(id).precio() * 1.21; }
    }
    ```

    1.  **Por constructor, y la interfaz.** Nunca `new ProductoRepositorioCsv(...)` dentro del servicio.

    Las cinco cosas que se comprueban:

    | | |
    |---|---|
    | 1 | El servicio **recibe** la interfaz; no crea la implementación |
    | 2 | El repositorio **no tiene reglas de negocio**: nada de IVA ni de umbrales |
    | 3 | `buscarPorId` devuelve `Optional`; **el servicio** decide si eso es un error |
    | 4 | Cambiar de CSV a JSON toca **una línea**, la del `main` |
    | 5 | El test usa un repositorio en memoria, **sin tocar el disco** |

    La línea que lo demuestra todo:

    ```java
    ProductoRepositorio repo = new ProductoRepositorioJson(Path.of("datos/productos.json"));
    var servicio = new ProductoServicio(repo);   // ni se entera del cambio
    ```

    Y el test, que es lo que hace que todo lo anterior tenga sentido:

    ```java
    @Test
    void lanzaSiNoExiste() {
        ProductoRepositorio enMemoria = new ProductoRepositorio() {
            public List<Producto> listar() { return List.of(); }
            public Optional<Producto> buscarPorId(int id) { return Optional.empty(); }
        };
        var servicio = new ProductoServicio(enMemoria);

        assertThrows(ProductoNoEncontradoException.class, () -> servicio.obtener(99));
    }
    ```

    Sin la interfaz, este test necesitaría un fichero CSV de prueba en el disco. **Por eso existe la interfaz**, y esa es la respuesta que hay que saber dar.

    En la UT4 esto mismo lo hace Spring por ti: la interfaz es la misma, el `new` lo pone el marco de trabajo y se llama inyección de dependencias.

---

## Del ejercicio a la pregunta de test

El examen de esta unidad es un **test práctico sobre fragmentos de código** ([batería de test aquí](autoevaluacion.md)). No se pide escribir un programa: se pide leer código y saber qué hace.

La correspondencia es directa. Cada bloque de ejercicios alimenta un bloque de preguntas:

| Ejercicios | Preguntas del examen | Qué se pregunta exactamente |
|---|:-:|---|
| **E1–E7** · Estructuras | 8 preguntas | Qué imprime un `HashMap` frente a un `TreeMap`; qué devuelve `groupingBy`; en qué nivel actúa el `TreeMap::new`; el tipo que devuelve `counting()` |
| **E8–E14** · Ficheros | 7 preguntas | Por qué falla un `split` sin `-1`; qué pasa sin `skip(1)`; el separador cambiado; `Files.lines` sin cerrar; la ruta relativa |
| **E15–E20** · JSON | 6 preguntas | La fecha que sale como `[2026,3,14]`; el campo nulo que aparece; `get` frente a `path`; el campo desconocido que revienta |
| **E21–E25** · Fechas y validación | 5 preguntas | `LocalDate` es inmutable: el `plusDays` que se pierde; solapes de rangos; el `BigDecimal` con `double` |
| **E26–E29** · Repositorio y capas | 4 preguntas | Qué capa puede importar a cuál; el orden existencia → estado; qué devuelve el repositorio cuando no encuentra |

!!! reto "Las tres cosas que de verdad transfieren"
    Hacer ejercicios **no** prepara para un test por sí solo. Lo que transfiere es esto, y se puede practicar desde la primera sesión:

    1. **Predecir antes de ejecutar.** Antes de darle a *Run*, escribe en un papel qué va a salir. Si aciertas, entendiste; si no, acabas de encontrar tu hueco. **Esta es la única costumbre que hay que coger**, y es exactamente lo que pide el tipo de pregunta 1.

    2. **Romper tu propia solución.** Cuando un ejercicio te salga bien, quítale el `-1` al `split`, cambia el `TreeMap` por un `HashMap`, borra el `skip(1)`. Mira el error que sale y **apúntalo**. El tipo de pregunta 2 —«¿por qué falla?»— es literalmente eso.

    3. **Escribir la pregunta tú (E30).** Inventar los tres distractores te obliga a saber por qué alguien se equivocaría. Es el paso que convierte «sé hacerlo» en «sé reconocerlo».

    Después de cada ejercicio de esta batería, dedica **dos minutos** a los puntos 1 y 2. Son 60 minutos en toda la unidad y valen más que cualquier repaso de la víspera.

---

## Cómo usarlos en clase

| Momento | Ejercicios |
|---|---|
| Para arrancar la sesión, 10 min | E1 · E8 · E15 · E21 · E26 |
| Taller de la sesión, 25-30 min | E4 · E9 · E16 · E24 · E28 |
| Los que hay que hacer sí o sí | **E5 · E9 · E10 · E16 · E27** |
| Para quien va sobrado | E7 · E12 · E14 · E19 · E25 · E29 |
| Repaso antes del test | E5 · E10 · E16 · E30 + [autoevaluación](autoevaluacion.md) |

!!! tip "Los dos que más caen"
    El **E9** (CSV con sus trampas) y el **E16** (fechas ISO y sin nulos) concentran entre los dos **seis de las treinta preguntas**. Si vas justo de tiempo, esos dos antes que ninguno.

    Y el **E30** hazlo siempre: es el puente entre haber resuelto los ejercicios y saber contestar sobre ellos.
