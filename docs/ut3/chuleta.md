# Chuleta de la UT3

## Colecciones: cuál elegir

| Necesito | Uso |
|---|---|
| Orden + índices | `ArrayList` |
| Únicos, rápido | `HashSet` |
| Únicos ordenados | `TreeSet` |
| Únicos en orden de inserción | `LinkedHashSet` |
| Clave → valor | `HashMap` |
| Clave → valor, claves ordenadas | `TreeMap` |
| Pila (LIFO) / Cola (FIFO) | `ArrayDeque` |

```java
lista.removeIf(x -> cond);                 // borrar sin romper el recorrido
map.getOrDefault(k, 0);
map.computeIfAbsent(k, x -> new ArrayList<>()).add(v);
map.merge(k, 1, Integer::sum);             // contar ocurrencias
```

## Comparator

```
Comparator.comparing(Producto::precio)
Comparator.comparing(Producto::precio).reversed()
Comparator.comparing(Producto::categoria).thenComparing(Producto::precio)
Comparator.comparing(Producto::nombre, String.CASE_INSENSITIVE_ORDER)
lista.sort(cmp);                     // modifica la lista
lista.stream().sorted(cmp).toList(); // devuelve una nueva
```

## Streams para informes

```
.collect(Collectors.groupingBy(Producto::categoria))
.collect(Collectors.groupingBy(Producto::categoria, Collectors.counting()))
.collect(Collectors.groupingBy(Producto::categoria, Collectors.summingDouble(Producto::precio)))
.collect(Collectors.partitioningBy(p -> p.precio() > 100))
.collect(Collectors.joining(", ", "[", "]"))
.mapToDouble(Producto::precio).summaryStatistics()   // min, max, media, suma, count
.reduce(0.0, Double::sum)
```

## CSV

```java
// lo mínimo, con las tres trampas resueltas
try (var lineas = Files.lines(ruta, StandardCharsets.UTF_8)) {
    return lineas.skip(1)                    // cabecera
                 .filter(l -> !l.isBlank())  // líneas en blanco
                 .map(l -> l.split(",", -1)) // campos vacíos del final
                 .map(c -> new Producto(c[0].trim(), …))
                 .toList();
}
```

```java
// con Apache Commons CSV: acceso por NOMBRE de columna
var formato = CSVFormat.DEFAULT.builder()
        .setHeader().setSkipHeaderRecord(true)
        .setIgnoreEmptyLines(true).setTrim(true)
        .setDelimiter(';')                   // Excel en español
        .get();

try (var lector = Files.newBufferedReader(ruta, StandardCharsets.UTF_8);
     var csv = formato.parse(lector)) {
    for (var fila : csv) { fila.get("precio"); }
}
```

```java
String.format(Locale.ROOT, "%.2f", 25.9)     // "25.90" SIEMPRE
// sin Locale.ROOT, en español sale "25,90" y rompe el CSV
```

## JSON con Jackson

```java
var mapper = new ObjectMapper()
    .registerModule(new JavaTimeModule())
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

String json = mapper.writeValueAsString(obj);
String bonito = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
Producto p = mapper.readValue(json, Producto.class);
List<Producto> l = mapper.readValue(json, new TypeReference<List<Producto>>() {});
mapper.writeValue(ruta.toFile(), obj);
```

| JSON | Java |
|---|---|
| `{}` | record / clase |
| `[]` | `List<T>` |
| `"texto"` | `String` |
| `42` / `1.5` | `int` / `double` |
| `true` | `boolean` |

Anotaciones: `@JsonProperty("otro_nombre")` · `@JsonIgnore` · `@JsonFormat(pattern="yyyy-MM-dd")`

## Fechas (`java.time`) — ¡inmutables!

```
LocalDate.now()   LocalDate.of(2026,3,15)   LocalDateTime.now()   Instant.now()
fecha = fecha.plusDays(1);          // hay que ASIGNAR
Period.between(a, b).getYears()
ChronoUnit.DAYS.between(a, b)
fecha.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
LocalDate.parse("2026-04-12")       // ISO por defecto
```

Guarda en `UTC` (Instant), muestra en la zona del usuario.

## Regex

```
s.matches("^\\d{8}[A-Z]$")
static final Pattern P = Pattern.compile("...");
P.matcher(s).matches()
```

\\d dígito · \\w alfanumérico · + uno o más ·

*

cero o más ·

?

opcional · `{n,m}` repeticiones · `[A-Z]` rango · ^ $ anclas

## JDBC

```java
try (var con = DriverManager.getConnection(url, usuario, clave);
     var ps  = con.prepareStatement("SELECT * FROM producto WHERE codigo = ?");
     ) {
    ps.setString(1, codigo);                 // NUNCA concatenar
    try (var rs = ps.executeQuery()) {
        return rs.next() ? Optional.of(aProducto(rs)) : Optional.empty();
    }
}
```

| Para | Método | Devuelve |
|---|---|---|
| `SELECT` | `executeQuery()` | `ResultSet` |
| `INSERT` / `UPDATE` / `DELETE` | `executeUpdate()` | **filas afectadas** |
| `CREATE TABLE` | `execute()` | `boolean` |

```java
return ps.executeUpdate() == 1;   // ¿existía? sin hacer antes un SELECT
```

| URL | Motor |
|---|---|
| `jdbc:h2:./datos/tienda` | H2 en fichero (sobrevive al reinicio) |
| `jdbc:h2:mem:tienda` | H2 en memoria (se pierde) |
| `jdbc:mysql://localhost:3306/tienda` | MySQL |

```sql
codigo VARCHAR(20) NOT NULL UNIQUE     -- deja fallar y traduce el error
precio DECIMAL(10,2) NOT NULL          -- nunca DOUBLE para dinero
```

```yaml
# compose.yaml · lo que se olvida
volumes: [datos-mysql:/var/lib/mysql]   # sin esto, los datos se van
healthcheck: …                          # arrancado ≠ listo
```
