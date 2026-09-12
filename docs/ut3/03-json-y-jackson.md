# JSON y Jackson: el formato de las APIs

> **JSON es el idioma en el que hablan las APIs.** En la UT1 lo viste llegar por `curl`; en la UT6 lo generarás con Spring. Aquí aprendes a convertir JSON ⇄ objetos Java a mano, para saber qué hace el framework por debajo.

## 1. Anatomía de un JSON

!!! analogia "Analogía"
    Serializar es **hacer la maleta** y deserializar, deshacerla. La ropa es la misma; lo que cambia es el formato de transporte. Y como en toda maleta, el problema nunca es meterla: es que al abrirla en destino esperaban una camisa donde tú metiste un zapato. De ahí que importe tanto el contrato de campos.

```json
{
  "id": 42,
  "nombre": "Patinete",
  "precio": 120.5,
  "disponible": true,
  "etiquetas": ["movilidad", "urbano"],
  "vendedor": {
    "nombre": "Ana",
    "valoracion": 4.8
  },
  "descuento": null
}
```

Solo hay **seis tipos**: cadena (`"texto"`), número (`42`, `120.5`), booleano (`true`/`false`), `null`, **array** (`[...]`) y **objeto** (`{...}`). Nada más: ni fechas, ni comentarios, ni clases.

| Regla de sintaxis | Ejemplo |
|---|---|
| Las claves **siempre** entre comillas dobles | `"nombre": "Ana"` :material-check: · `nombre: 'Ana'` :material-close: |
| Sin coma final tras el último elemento | `{"a":1, "b":2}` :material-check: · `{"a":1, "b":2,}` :material-close: |
| Sin comentarios | `// esto no vale` :material-close: |

## 2. Correspondencia JSON ⇄ Java

| JSON | Java |
|---|---|
| objeto `{}` | un **record** o clase (o `Map<String,Object>`) |
| array `[]` | `List<T>` |
| cadena | `String` |
| número | `int`, `long`, `double` |
| booleano | `boolean` |
| `null` | `null` (u `Optional` al recibirlo) |

Para el JSON de arriba:

```java
record Vendedor(String nombre, double valoracion) {}
record Producto(int id, String nombre, double precio, boolean disponible,
                List<String> etiquetas, Vendedor vendedor, Double descuento) {}
```

Los **nombres deben coincidir** con las claves del JSON (o mapearse explícitamente, como veremos).

## 3. Jackson: serializar y deserializar

**Jackson** es la librería estándar (la que usa Spring Boot por dentro). Dependencia Maven:

```xml title="pom.xml"
<dependency>
    <groupId>com.fasterxml.jackson.core</groupId>
    <artifactId>jackson-databind</artifactId>
    <version>2.18.0</version>
</dependency>
```

Dos operaciones, y ya está:

```java
import com.fasterxml.jackson.databind.ObjectMapper;

var mapper = new ObjectMapper();

// Objeto → JSON  (serializar)
var producto = new Producto(42, "Patinete", 120.5, true,
                            List.of("movilidad"), new Vendedor("Ana", 4.8), null);
String json = mapper.writeValueAsString(producto);

// Con formato legible
String bonito = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(producto);

// JSON → Objeto  (deserializar)
Producto p = mapper.readValue(json, Producto.class);
IO.println(p.nombre());
```

**Directo a fichero:**

```java
mapper.writeValue(Path.of("producto.json").toFile(), producto);
Producto leido = mapper.readValue(Path.of("producto.json").toFile(), Producto.class);
```

## 4. Listas y estructuras anidadas

Para deserializar una **lista** hay que decirle el tipo genérico (por el *type erasure* de Java):

```java
// Opción 1: TypeReference
List<Producto> lista = mapper.readValue(json, new TypeReference<List<Producto>>() {});

// Opción 2: array y a lista
List<Producto> lista2 = List.of(mapper.readValue(json, Producto[].class));
```

Configuración imprescindible en la vida real:

```java
var mapper = new ObjectMapper()
    // No fallar si el JSON trae campos que mi record no tiene
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    // No escribir los campos null
    .setSerializationInclusion(JsonInclude.Include.NON_NULL);
```

!!! danger "El error más común"
    `UnrecognizedPropertyException: Unrecognized field "stock"` significa que el JSON trae un campo que tu record no declara. Dos soluciones: añadir el campo, o desactivar `FAIL_ON_UNKNOWN_PROPERTIES`. En APIs externas (que pueden añadir campos cuando quieran) **siempre** desactívalo: así tu código no se rompe cuando el proveedor amplíe su respuesta.

**Anotaciones** que resuelven el 90 % de los casos:

```java
record Producto(
    int id,
    @JsonProperty("product_name") String nombre,  // la clave del JSON se llama distinto
    @JsonIgnore String notaInterna,                 // no serializar este campo
    @JsonFormat(pattern = "yyyy-MM-dd") LocalDate alta
) {}
```

## 5. Fechas en JSON

JSON no tiene tipo fecha: viajan como texto. Para que Jackson entienda `java.time`, registra el módulo:

```java
var mapper = new ObjectMapper()
    .registerModule(new JavaTimeModule())
    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);   // ISO en vez de número

// Resultado: "alta": "2026-03-15"  en vez de  "alta": 1773532800
```

(Dependencia: `jackson-datatype-jsr310`. En Spring Boot viene ya configurado.)

El estándar es **ISO-8601**: `2026-03-15`, `2026-03-15T14:30:00Z`. Úsalo siempre en APIs: es inequívoco y ordenable como texto.

## 6. Colecciones y genéricos

Deserializar un objeto suelto es fácil. ¿Y una lista?

```java
// MAL  No compila / no funciona: el genérico se pierde en tiempo de ejecución
List<Producto> lista = mapper.readValue(json, List.class);

// BIEN Con TypeReference
List<Producto> lista = mapper.readValue(json, new TypeReference<List<Producto>>() {});

// BIEN O con JavaType
var tipo = mapper.getTypeFactory().constructCollectionType(List.class, Producto.class);
List<Producto> lista2 = mapper.readValue(json, tipo);

// Map
Map<String, List<Producto>> porCat =
    mapper.readValue(json, new TypeReference<Map<String, List<Producto>>>() {});
```

Ese `new TypeReference<...>() {}` con las llaves finales es una **clase anónima**: es el truco que usa Jackson para conservar el tipo genérico, que de otro modo Java borra al compilar (*type erasure*).

Directo desde y hacia fichero:

```java
mapper.writeValue(Path.of("productos.json").toFile(), lista);
List<Producto> leidos = mapper.readValue(Path.of("productos.json").toFile(),
                                          new TypeReference<>() {});
```

## 7. Recorrer JSON sin clase: el árbol

Cuando la estructura es desconocida o irregular, no hace falta un record:

```bash
JsonNode raiz = mapper.readTree(json);

String nombre = raiz.get("cliente").get("nombre").asText();
int pedido    = raiz.get("pedido").asInt();
double total  = raiz.path("total").asDouble(0.0);          // path no revienta si falta

for (JsonNode linea : raiz.get("lineas")) {
    IO.println(linea.get("producto").asText() + " x" + linea.get("unidades").asInt());
}

// Buscar en profundidad, sin saber dónde está
JsonNode encontrado = raiz.findValue("precio");
boolean tieneDescuento = raiz.has("descuento");
```

!!! warning "`get` frente a `path`"
    `get("noexiste")` devuelve **`null`** y la siguiente llamada te da un `NullPointerException`. `path("noexiste")` devuelve un nodo *missing* que se puede encadenar sin peligro y admite valor por defecto. En JSON ajeno, usa **siempre `path`**.

Construir JSON a mano:

```java
ObjectNode nodo = mapper.createObjectNode();
nodo.put("id", 42);
nodo.put("nombre", "Patinete");
nodo.putArray("etiquetas").add("movilidad").add("urbano");
nodo.putObject("vendedor").put("nombre", "Ana");
IO.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(nodo));
```

## 8. Más anotaciones útiles

``` { .java .numerado }
@JsonIgnoreProperties(ignoreUnknown = true)  // a nivel de clase: ignora campos extra
public record ProductoDto(

    @JsonProperty("product_id") int id,              // renombrar la clave

    @JsonProperty(access = Access.WRITE_ONLY)  // se lee del JSON, nunca se escribe
    String password,

    @JsonProperty(access = Access.READ_ONLY)  // se escribe, nunca se acepta del cliente
    Instant creadoEn,

    @JsonInclude(JsonInclude.Include.NON_NULL)       // omitir si es null
    String descripcion,

    @JsonFormat(shape = Shape.STRING, pattern = "dd/MM/yyyy")
    LocalDate fecha,

    @JsonAlias({"cantidad", "qty"})                  // acepta varios nombres de entrada
    int unidades
) {}
```

`WRITE_ONLY` y `READ_ONLY` son la forma correcta de tratar campos asimétricos: la contraseña entra pero no sale; la fecha de creación sale pero no la fija el cliente.

## 9. Jackson dentro de Spring Boot

Spring Boot ya crea un `ObjectMapper` configurado y lo usa automáticamente en `@RequestBody` y `@ResponseBody`. **No lo instancies tú**: configúralo.

```yaml title="src/main/resources/application.yml"
spring:
  jackson:
    default-property-inclusion: non_null
    serialization:
      indent-output: true
      write-dates-as-timestamps: false
    deserialization:
      fail-on-unknown-properties: false
    time-zone: Europe/Madrid
```

Y si necesitas algo a medida:

```java
@Bean
public Jackson2ObjectMapperBuilderCustomizer personalizar() {
    return builder -> builder
        .simpleDateFormat("yyyy-MM-dd")
        .serializationInclusion(JsonInclude.Include.NON_EMPTY);
}
```

!!! bug "El fallo silencioso más caro"
    `fail-on-unknown-properties: false` es imprescindible al consumir APIs ajenas… pero peligroso en la entrada de la tuya: si un cliente envía `{"nombre":"X","preciO":10}` (con la O mayúscula por error), Jackson **ignora el campo** y tu producto se crea con precio `null` o `0` sin avisar a nadie. Por eso la validación con `@NotNull` en los DTO no es opcional: es la red que atrapa estos casos.

---

## Pruébalo ahora (15 min)

!!! reto "Trabaja tú ahora"
    Este bloque se hace **en clase, en tu equipo**. No se entrega ni puntúa: es la práctica que hace que el examen te salga.

Sin instalar Jackson, puedes entender el mapeo. **Predice el JSON** que generaría este objeto y luego compruébalo mentalmente con las reglas:

```java
record Vendedor(String nombre, double valoracion) {}
record Producto(int id, String nombre, double precio,
                List<String> etiquetas, Vendedor vendedor) {}

var p = new Producto(42, "Patinete", 120.5,
                     List.of("movilidad", "urbano"),
                     new Vendedor("Ana", 4.8));
```

??? success "JSON resultante"

    ```json
    {"id":42,"nombre":"Patinete","precio":120.5,"etiquetas":["movilidad","urbano"],"vendedor":{"nombre":"Ana","valoracion":4.8}}
    ```

    Fíjate: el record anidado se convierte en un **objeto anidado**, y la lista en un **array**. Es automático.


**Con Maven** (si ya tienes proyecto), añade Jackson y ejecuta:

```java
var mapper = new ObjectMapper();
IO.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(p));

var json = """
    {"id":7,"nombre":"Casco","precio":35.0,"etiquetas":["seguridad"],
     "vendedor":{"nombre":"Luis","valoracion":4.2}}
    """;
var recuperado = mapper.readValue(json, Producto.class);
IO.println(recuperado.vendedor().nombre());   // Luis
```

---

## Ejercicios (con solución)

### Ejercicio 1 — Diseña los records

Dado este JSON, escribe los records necesarios:

```json
{
  "pedido": 1001,
  "fecha": "2026-04-12",
  "cliente": {"id": 7, "nombre": "Ana"},
  "lineas": [
    {"producto": "Patinete", "unidades": 1, "precio": 120.0},
    {"producto": "Casco", "unidades": 2, "precio": 35.0}
  ]
}
```

??? success "Solución"

    ```java
    record Cliente(int id, String nombre) {}
    record Linea(String producto, int unidades, double precio) {}
    record Pedido(int pedido, LocalDate fecha, Cliente cliente, List<Linea> lineas) {}
    ```

    Para que `fecha` funcione hace falta `registerModule(new JavaTimeModule())`.


### Ejercicio 2 — ¿Qué error da y cómo se arregla?

Tu record es `record Producto(int id, String nombre) {}` y recibes:

```json
{"id":1,"nombre":"Casco","stock":40}
```

??? success "Solución"

    `UnrecognizedPropertyException` por el campo stock, que el record no tiene. Arreglos: (1) añadirlo al record; (2) mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false) — lo recomendable al consumir APIs de terceros.


### Ejercicio 3 — Detecta los errores de sintaxis JSON

```json
{
  nombre: "Ana",
  'edad': 30,
  "activo": True,
  "tags": ["a", "b",],
}
```

??? success "Solución"

    Cuatro errores: (1) nombre sin comillas dobles; (2) 'edad' con comillas simples; (3) True debe ser true en minúscula (eso es Python); (4) comas finales sobrantes en el array y en el objeto. Correcto:
    ```json
    {"nombre":"Ana","edad":30,"activo":true,"tags":["a","b"]}
    ```


### Ejercicio 4 — De CSV a JSON

Escribe el método que lee el `productos.csv` de la página anterior y escribe un `productos.json` equivalente.

??? success "Solución"

    ```java
    void csvAJson(Path csv, Path json) throws IOException {
        List<Producto> productos;
        try (var lineas = Files.lines(csv)) {
            productos = lineas.skip(1).filter(l -> !l.isBlank())
                .map(l -> l.split(","))
                .map(c -> new Producto(Integer.parseInt(c[0]), c[1], c[2],
                                       Double.parseDouble(c[3])))
                .toList();
        }
        new ObjectMapper()
            .writerWithDefaultPrettyPrinter()
            .writeValue(json.toFile(), productos);
    }
    ```

    Acabas de escribir un **conversor de formatos**: exactamente el tipo de utilidad que se pide en un trabajo real.


### Ejercicio 5 — Deserializar una lista

Tienes `productos.json` con un array. ¿Por qué `mapper.readValue(json, List.class)` no sirve?

??? success "Solución"

    Porque Java borra los genéricos al compilar (type erasure): `List.class` no lleva información de qué contiene, así que Jackson construye una List<LinkedHashMap>. El código compila, pero al hacer `lista.get(0).nombre()` obtienes un `ClassCastException` en ejecución. Correcto:
    ```java
    List<Producto> lista = mapper.readValue(json, new TypeReference<List<Producto>>() {});
    ```

    Las llaves finales crean una clase anónima cuya superclase

    sí

    conserva el tipo genérico; es de ahí de donde lo lee Jackson.


### Ejercicio 6 — JSON irregular

Del siguiente JSON necesitas la lista de nombres de producto, pero algunas líneas no traen el campo `producto`:

```json
{"lineas":[{"producto":"Casco","uds":2},{"uds":1},{"producto":"Bici","uds":1}]}
```

??? success "Solución"

    ```bash
    var raiz = mapper.readTree(json);
    var nombres = new ArrayList<String>();
    for (JsonNode linea : raiz.path("lineas")) {
        var nodo = linea.path("producto");        // path, no get
        if (!nodo.isMissingNode()) nombres.add(nodo.asText());
    }
    // [Casco, Bici]
    ```

    Con `get("producto")` la segunda línea devolvería null y `.asText()` lanzaría `NullPointerException`. path devuelve un nodo missing que se puede consultar sin peligro; también valdría linea.path("producto").asText("(desconocido)").


### Ejercicio 7 — Campos asimétricos

Diseña el DTO de un usuario en el que la contraseña se pueda enviar pero nunca se devuelva, y la fecha de alta se devuelva pero nunca la fije el cliente.

??? success "Solución"

    ```java
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record UsuarioDto(
        @JsonProperty(access = JsonProperty.Access.READ_ONLY) Integer id,
        @NotBlank String username,
        @NotBlank @Size(min = 8)
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String password,
        @JsonProperty(access = JsonProperty.Access.READ_ONLY)
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        Instant creadoEn
    ) {}
    ```

    Sin `WRITE_ONLY`, cualquier `GET /usuarios/1` devolvería la contraseña. Sin `READ_ONLY` en id, un cliente podría enviarlo y suplantar otro registro (mass assignment). Aun así, lo más seguro es usar dos DTO distintos, uno de entrada y otro de salida.


### Ejercicio 8 — De CSV a JSON

Convierte `productos.csv` en `productos.json` con formato legible, agrupando por categoría.

??? success "Solución"

    ```java
    record Producto(int id, String nombre, String categoria, double precio) {}

    void main() throws IOException {
        List<Producto> productos;
        try (var lineas = Files.lines(Path.of("datos/productos.csv"))) {
            productos = lineas.skip(1).filter(l -> !l.isBlank())
                .map(l -> l.split(","))
                .map(c -> new Producto(Integer.parseInt(c[0].trim()), c[1].trim(),
                                       c[2].trim(), Double.parseDouble(c[3].trim())))
                .toList();
        }

        Map<String, List<Producto>> porCategoria = productos.stream()
            .collect(Collectors.groupingBy(Producto::categoria, TreeMap::new,
                     Collectors.toList()));

        var mapper = new ObjectMapper();
        mapper.writerWithDefaultPrettyPrinter()
              .writeValue(Path.of("datos/productos.json").toFile(), porCategoria);
    }
    ```

    Este ejercicio junta las tres piezas de la unidad: ficheros (leer CSV), colecciones (agrupar) y `JSON` (serializar). Es exactamente lo que hace un backend cuando importa datos.

