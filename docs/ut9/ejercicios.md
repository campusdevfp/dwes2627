# Batería de ejercicios — UT9

**Dominio: bibliotecas municipales.** Distinto del de clase (comercios) a propósito.

Fuentes que se usan: un CSV público con las bibliotecas de un municipio y la API de [Open Library](https://openlibrary.org/developers/api), que no necesita clave.

---

### E1 ● — Leer las reglas antes de escribir código

Para la API de Open Library, averigua y documenta: licencia de los datos, límite de peticiones, formato de respuesta y qué pasa si un ISBN no existe.

??? success "Solución"

    | | |
    |---|---|
    | Licencia de datos | Dominio público (CC0) |
    | Límite | Sin clave; piden un `User-Agent` identificativo y no abusar |
    | Formato | JSON |
    | ISBN inexistente | Devuelve `{}` con **200**, no 404 |

    La última fila es la trampa: si esperas un 404 para detectar «no existe», tu código va a dar por bueno un objeto vacío. Hay que comprobar el contenido, no solo el código de estado.


### E2 ●● — Cliente con timeouts y User-Agent

Monta el `RestClient` de Open Library como es debido.

??? success "Solución"

    ```java
    @Bean
    RestClient openLibrary() {
        var f = new SimpleClientHttpRequestFactory();
        f.setConnectTimeout(Duration.ofSeconds(3));
        f.setReadTimeout(Duration.ofSeconds(5));
        return RestClient.builder()
            .baseUrl("https://openlibrary.org")
            .defaultHeader("User-Agent", "BibliotecasIES/1.0 (dwes@iesejemplo.es)")
            .requestFactory(f)
            .build();
    }
    ```
    El `User-Agent` con contacto no es cortesía vacía: si tu integración da problemas, te avisan en vez de bloquearte.


### E3 ●● — DTO propio y respuesta vacía

Busca un libro por ISBN y mapéalo a un DTO tuyo. Devuelve `Optional.empty()` si no existe.

??? success "Solución"

    ```java
    public record LibroDto(String titulo, List<String> autores, Integer anio) {}

    public Optional<LibroDto> porIsbn(String isbn) {
        try {
            var r = cliente.get().uri("/isbn/{i}.json", isbn)
                      .retrieve().body(RespuestaOpenLibrary.class);
            return (r == null || r.title() == null) ? Optional.empty()
                                                    : Optional.of(convertir(r));
        } catch (RestClientException e) {
            log.warn("Open Library no responde para {}", isbn);
            return Optional.empty();
        }
    }
    ```
    ```java
    @JsonIgnoreProperties(ignoreUnknown = true)
    record RespuestaOpenLibrary(String title, Integer number_of_pages, List<Autor> authors) {}
    ```
    Los dos casos vacíos —respuesta sin `title` y excepción— acaban igual: `Optional.empty()`. Quien llama no tiene que distinguirlos.


### E4 ●●● — Que la ficha no se caiga

La ficha del libro debe mostrarse aunque Open Library no responda.

??? success "Solución"

    ```java
    @GetMapping("/libros/{isbn}")
    public String ficha(@PathVariable String isbn, Model modelo) {
        modelo.addAttribute("libro", repo.porIsbn(isbn).orElseThrow());
        modelo.addAttribute("enriquecido", openLibrary.porIsbn(isbn).orElse(null));
        return "libros/ficha";
    }
    ```
    ```html
    <section th:if="${enriquecido}">
        <h3 th:text="${enriquecido.titulo}">…</h3>
    </section>
    <p th:unless="${enriquecido}" class="aviso">
        No hemos podido obtener información adicional en este momento.
    </p>
    ```
    Comprobación: trucar `/etc/hosts` y recargar. La ficha debe verse con el aviso. Si sale un error 500, la fuente secundaria está tumbando la página principal.


### E5 ●● — Caché de una semana

Los datos bibliográficos no cambian. Cachéalos.

??? success "Solución"

    ```java
    @Cacheable(value = "libros", key = "#isbn")
    public Optional<LibroDto> porIsbn(String isbn) { … }
    ```
    ```yaml
    spring.cache.caffeine.spec: maximumSize=2000,expireAfterWrite=7d
    ```
    Una semana es defendible: el título y el autor de un libro publicado no cambian. Y el README debe explicar el porqué, que es criterio de rúbrica.


### E6 ●●● — Ingerir el CSV de bibliotecas

ETL del fichero municipal con las trampas de siempre.

??? success "Solución"

    ```java
    try (var lineas = Files.lines(csv, Charset.forName("ISO-8859-1"))) {
        lineas.skip(1)
              .filter(l -> !l.isBlank())
              .map(l -> l.split(";", -1))
              .filter(c -> c.length >= 6 && !c[1].isBlank())
              .forEach(this::cargar);
    }
    ```
    Las tres decisiones que se corrigen: la **codificación** (`ISO-8859-1`), el **separador** (`;`) y el **`-1`** del `split`, que conserva las columnas vacías del final. Sin el `-1`, una biblioteca sin teléfono desplaza todas las columnas siguientes y nadie se entera.


### E7 ●●● — Idempotencia demostrada

Que ejecutar la ingesta dos veces no duplique, y un test que lo pruebe.

??? success "Solución"

    ```java
    @Table(uniqueConstraints = @UniqueConstraint(columnNames = "codigo_origen"))
    ```
    ```java
    repo.findByCodigoOrigen(b.getCodigoOrigen())
        .ifPresentOrElse(e -> e.actualizarDesde(b), () -> repo.save(b));
    ```
    ```java
    @Test void dosEjecucionesMismosDatos() {
        servicio.ingerir();
        long n = repo.count();
        servicio.ingerir();
        assertThat(repo.count()).isEqualTo(n);
    }
    ```
    La restricción en la tabla no es redundante con el `findBy`: entre la comprobación y el `save` cabe otra ejecución concurrente. Es el mismo razonamiento del RA6.


### E8 ●● — Contar y explicar los descartes

Que la ingesta registre cuántas filas se descartaron y por qué.

??? success "Solución"

    ```java
    public record ResultadoIngesta(int leidos, int nuevos, int actualizados, List<String> descartes) {}
    ```
    ```java
    log.info("Ingesta: {} leídos, {} nuevos, {} actualizados, {} descartados",
             r.leidos(), r.nuevos(), r.actualizados(), r.descartes().size());
    r.descartes().stream().limit(20).forEach(d -> log.debug("  descarte: {}", d));
    ```
    El `limit(20)` evita llenar el log con 4.000 líneas. Los descartes completos van a la tabla `EjecucionIngesta`.


### E9 ●● — Exportar a Excel

Listado de bibliotecas descargable en `.xlsx`.

??? success "Solución"

    ```java
    public interface Exportador { byte[] aExcel(List<BibliotecaDto> datos); }
    ```
    ```java
    @GetMapping(value = "/bibliotecas.xlsx",
                produces = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    public ResponseEntity<byte[]> excel() {
        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=bibliotecas.xlsx")
            .body(exportador.aExcel(servicio.listar()));
    }
    ```
    La interfaz `Exportador` es lo que se corrige: si Apache POI aparece en el controlador, no está aislado.


### E10 ●●● — Analítica en la base de datos

Bibliotecas por distrito, con su ratio por cada 10.000 habitantes, ordenado.

??? success "Solución"

    ```java
    @Query("""
           select d.nombre as distrito,
                  count(b) as total,
                  count(b) * 10000.0 / d.habitantes as ratio
           from Biblioteca b join b.distrito d
           where b.activa = true
           group by d.nombre, d.habitantes
           order by ratio desc
           """)
    List<RatioDistrito> ratioPorDistrito();
    ```
    El cruce entre el censo de bibliotecas y el padrón responde algo que ninguna de las dos fuentes sabía: **dónde faltan bibliotecas**. Eso es la aplicación híbrida del tema 1.

    Comprobación al corregir: `show-sql` activado y **una** consulta con `group by`. Si aparece un `select` completo seguido de un `stream()`, es cero.


### E11 ●● — Evolución mensual con comparación

Préstamos por mes del último año, con la variación frente al mes anterior.

??? success "Solución"

    ```sql
    select mes, total,
           lag(total) over (order by mes) as anterior,
           round(100.0 * (total - lag(total) over (order by mes))
                 / nullif(lag(total) over (order by mes), 0), 1) as variacion
    from (select date_trunc('month', fecha) mes, count(*) total
          from prestamo where fecha >= now() - interval '12 months'
          group by 1) t
    order by mes;
    ```
    El `nullif(..., 0)` evita la división por cero del primer mes. Es el detalle que separa una consulta que funciona en clase de una que funciona con datos reales.


### E12 ●●● — Tests sin internet

Los tres escenarios de la API externa, con `MockRestServiceServer`.

??? success "Solución"

    ```java
    @RestClientTest(OpenLibraryServicio.class)
    class OpenLibraryServicioTest {
        @Autowired OpenLibraryServicio servicio;
        @Autowired MockRestServiceServer servidor;

        @Test void exito() {
            servidor.expect(requestTo(containsString("/isbn/9788401352836.json")))
                    .andRespond(withSuccess("{\"title\":\"Cien años de soledad\"}", APPLICATION_JSON));
            assertThat(servicio.porIsbn("9788401352836")).isPresent();
        }

        @Test void respuestaVaciaNoEsUnLibro() {
            servidor.expect(requestTo(anyString())).andRespond(withSuccess("{}", APPLICATION_JSON));
            assertThat(servicio.porIsbn("000")).isEmpty();
        }

        @Test void servidorCaido() {
            servidor.expect(requestTo(anyString())).andRespond(withServerError());
            assertThat(servicio.porIsbn("000")).isEmpty();
        }
    }
    ```
    El segundo test es el que corresponde al E1: Open Library devuelve `{}` con 200 cuando el ISBN no existe. Sin ese test, el fallo aparece en producción con un libro sin título.

    Y todo esto pasa con el wifi apagado.


---

## Reparto sugerido

| Ejercicio | Nivel | Sesión |
|---|:-:|:-:|
| E1 · Leer las reglas | ● | S4 |
| E2 · Cliente con timeouts | ●● | S5 |
| E3 · DTO propio | ●● | S6 |
| E4 · Que la ficha no se caiga | ●●● | S7 |
| E5 · Caché justificada | ●● | S9 |
| E6 · Ingerir el CSV | ●●● | S13 |
| E7 · Idempotencia | ●●● | S15 |
| E8 · Contar descartes | ●● | S17 |
| E9 · Exportar a Excel | ●● | S20 |
| E10 · Analítica en SQL | ●●● | S22 |
| E11 · Evolución con `lag()` | ●● | S23 |
| E12 · Tests sin internet | ●●● | S25 |
