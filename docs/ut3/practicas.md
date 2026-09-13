# Reto de la unidad — UT3

> **Reto `ut3-datos-base`**, en GitHub Classroom. Se te entrega un proyecto con las clases creadas, los métodos vacíos y **los tests escritos**. Tu trabajo es ponerlos en verde.
>
> Los tests son el enunciado: cada aserción dice qué se espera. Ejecuta `./verificar.sh` para ver el estado de un vistazo, y consulta [cómo se trabaja](../comprobar-tu-trabajo.md) si es tu primer reto.

Debajo tienes las **fases sugeridas** para resolverlo. No es obligatorio seguirlas en orden, pero es el camino más corto.

| # | Práctica | Tema | Qué practicas |
|---|---|---|---|
| P1 | Elige la estructura | [1](01-estructuras-de-datos.md) | List/Set/Map/Deque con criterio |
| P2 | Informe de ventas | [1](01-estructuras-de-datos.md) | `groupingBy`, `Comparator`, estadísticas |
| P3 | Lector de CSV | [2](02-ficheros.md) | NIO.2, `Files.lines`, parseo |
| P4 | Escritor y filtro | [2](02-ficheros.md) | Escritura, `try-with-resources` |
| P5 | De CSV a JSON | [3](03-json-y-jackson.md) | Jackson, serialización |
| P6 | Consumir JSON externo | [3](03-json-y-jackson.md) | Deserialización, campos desconocidos |
| P7 | Reservas validadas | [4](04-fechas-y-validacion.md) | `java.time`, validación, regex |
| P8 | **Catálogo en capas** | [5](05-repositorio-y-capas.md) | Interfaz + repositorio + servicio + test |

---

## P1 — Elige la estructura

Para cada caso, declara la estructura adecuada y justifica en un comentario: (a) últimas 10 búsquedas del usuario, la más reciente primero · (b) emails suscritos · (c) ranking de productos ordenado alfabéticamente sin repetidos · (d) notas de cada alumno · (e) cola de impresión.

??? success "Solución"

    ```
    var busquedas = new ArrayDeque<String>();                  // (a) pila: push/pop, LIFO
    var emails    = new HashSet<String>();  // (b) únicos, orden irrelevante
    var ranking   = new TreeSet<String>();                     // (c) únicos + ordenados
    var notas     = new HashMap<String, List<Integer>>();      // (d) alumno → sus notas
    var impresion = new ArrayDeque<String>();                  // (e) cola: offer/poll, FIFO
    ```


## P2 — Informe de ventas

Con una lista de `record Venta(String vendedor, String producto, int unidades, double precio)`, calcula: facturación por vendedor, producto más vendido en unidades, media de unidades por venta y la lista de vendedores ordenada por facturación descendente.

??? success "Solución"

    ```java
    // Facturación por vendedor
    var facturacion = ventas.stream().collect(Collectors.groupingBy(
        Venta::vendedor, Collectors.summingDouble(v -> v.unidades() * v.precio())));

    // Producto más vendido (unidades)
    var top = ventas.stream()
        .collect(Collectors.groupingBy(Venta::producto, Collectors.summingInt(Venta::unidades)))
        .entrySet().stream().max(Map.Entry.comparingByValue())
        .map(Map.Entry::getKey).orElse("—");

    // Media de unidades
    var media = ventas.stream().mapToInt(Venta::unidades).average().orElse(0);

    // Ranking de vendedores
    var ranking = facturacion.entrySet().stream()
        .sorted(Map.Entry.<String,Double>comparingByValue().reversed())
        .map(Map.Entry::getKey).toList();
    ```


## P3 — Lector de CSV

Crea `datos/productos.csv` con cabecera y 5 productos. Escribe `List<Producto> leer(Path)` que lo convierta en objetos, saltando cabecera y líneas vacías, y muéstralos.

??? success "Solución"

    ```java
    List<Producto> leer(Path ruta) throws IOException {
        try (var lineas = Files.lines(ruta)) {
            return lineas.skip(1)
                .filter(l -> !l.isBlank())
                .map(l -> l.split(","))
                .map(c -> new Producto(Integer.parseInt(c[0].trim()), c[1].trim(),
                                       c[2].trim(), Double.parseDouble(c[3].trim())))
                .toList();
        }
    }
    ```

    Los tres detalles que evalúa el test: `skip(1)`, `filter(isBlank)` y `try-with-resources`.


## P4 — Escritor y filtro

Escribe los productos de más de 100 € en `caros.csv` conservando la cabecera. Después provoca y captura un `NoSuchFileException` leyendo un fichero inexistente.

??? success "Solución"

    ```java
    void escribir(Path ruta, List<Producto> productos) throws IOException {
        var sb = new StringBuilder("id,nombre,categoria,precio\n");
        productos.forEach(p -> sb.append("%d,%s,%s,%.2f%n"
            .formatted(p.id(), p.nombre(), p.categoria(), p.precio())));
        Files.writeString(ruta, sb.toString());
    }

    void main() throws IOException {
        var todos = leer(Path.of("datos/productos.csv"));
        escribir(Path.of("datos/caros.csv"),
                 todos.stream().filter(p -> p.precio() > 100).toList());

        try {
            Files.readString(Path.of("datos/fantasma.csv"));
        } catch (NoSuchFileException e) {
            IO.println("No existe: " + e.getFile());
        }
    }
    ```


## P5 — De CSV a JSON

Convierte tu CSV en un `productos.json` con formato legible usando Jackson.

??? success "Solución"

    ```java
    var mapper = new ObjectMapper();
    mapper.writerWithDefaultPrettyPrinter()
          .writeValue(Path.of("datos/productos.json").toFile(), leer(csv));
    ```

    Abre el JSON generado: cada record se ha convertido en un objeto y la lista en un array, **sin escribir código de conversión**.


## P6 — Consumir JSON externo

Te llega este JSON de una API. Diseña el record y deserialízalo sin que falle por el campo extra `stock`.

```json
[{"id":1,"nombre":"Patinete","precio":120.0,"stock":8},
 {"id":2,"nombre":"Casco","precio":35.0,"stock":40}]
```

??? success "Solución"

    ```java
    record ProductoApi(int id, String nombre, double precio) {}

    var mapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    List<ProductoApi> productos =
        mapper.readValue(json, new TypeReference<List<ProductoApi>>() {});
    ```

    Sin esa configuración: UnrecognizedPropertyException: "stock". Al consumir APIs de terceros, desactívalo siempre.


## P7 — Reservas validadas

Crea `record Reserva(String cliente, String email, LocalDate entrada, LocalDate salida)` que valide en el constructor: cliente no vacío, email con formato correcto (regex), entrada no pasada y salida posterior a entrada. Añade `noches()`.

??? success "Solución"

    ```java
    record Reserva(String cliente, String email, LocalDate entrada, LocalDate salida) {
        private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+\\.[\\w.]{2,}$");

        Reserva {
            if (cliente == null || cliente.isBlank())
                throw new IllegalArgumentException("Cliente obligatorio");
            if (email == null || !EMAIL.matcher(email).matches())
                throw new IllegalArgumentException("Email no válido: " + email);
            if (entrada.isBefore(LocalDate.now()))
                throw new IllegalArgumentException("La entrada no puede ser pasada");
            if (!salida.isAfter(entrada))
                throw new IllegalArgumentException("La salida debe ser posterior a la entrada");
        }

        long noches() { return ChronoUnit.DAYS.between(entrada, salida); }
    }
    ```


## P8 — Catálogo en capas (integrador)

Monta el proyecto completo: `Producto` (record) · `ProductoRepositorio` (interfaz) · `ProductoRepositorioCsv` · `ProductoServicio` · `App`. Después añade `ProductoRepositorioJson` y comprueba que **el servicio no cambia**. Termina con un test usando un repositorio en memoria.

??? success "Solución (esqueleto y claves)"

    La solución completa está desarrollada en la [página 5](05-repositorio-y-capas.md). Las claves que se evalúan:

    1. El **servicio recibe la interfaz** por constructor, nunca crea la implementación con `new` dentro.
    2. El **repositorio no tiene reglas de negocio** (nada de IVA ni umbrales).
    3. `buscarPorId` devuelve **`Optional`**, y el servicio decide si lanzar excepción.
    4. Cambiar CSV → JSON solo toca **una línea del `main`**.
    5. El test usa un **repositorio en memoria**, sin tocar el disco.

    ```java
    // La línea que lo demuestra todo:
    ProductoRepositorio repo = new ProductoRepositorioJson(Path.of("datos/productos.json"));
    var servicio = new ProductoServicio(repo);   // ← ni se entera del cambio
    ```


---

## Cómo entrenar para el test

El test de la UT3 pregunta sobre **estas prácticas**: por qué falla un parseo de CSV, qué devuelve un `groupingBy`, qué error da Jackson con un campo extra, qué imprime una fecha tras `plusDays` sin asignar, o en qué capa va cada responsabilidad. Repasa tus soluciones y pide simulacros al [comprobar tu trabajo con los tests](../comprobar-tu-trabajo.md).
