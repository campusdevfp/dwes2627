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

## Ficheros (NIO.2)

```
Path.of("datos", "productos.csv")          // nunca concatenes con "/"
Files.exists(p)  Files.size(p)  Files.createDirectories(dir)
Files.writeString(p, texto)
Files.writeString(p, texto, StandardOpenOption.APPEND)
Files.readString(p)          Files.readAllLines(p)
try (var l = Files.lines(p)) { ... }        // grandes: perezoso + cerrar
Files.copy(o, d, StandardCopyOption.REPLACE_EXISTING)
Files.deleteIfExists(p)
```

Todo lanza `IOException` (checked). Usa **try-with-resources** siempre que abras algo.

### CSV

```
lineas.skip(1)                     // saltar cabecera ← error clásico
      .filter(l -> !l.isBlank())   // ignorar líneas vacías
      .map(l -> l.split(","))
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

## Arquitectura en capas

```
Modelo (record)  ←  Repositorio (interfaz + impl)  ←  Servicio (reglas)  ←  App
```

- El servicio depende de la interfaz del repositorio, nunca de la implementación.
- El repositorio no aplica reglas de negocio.
- Para testear, se pasa un repositorio en memoria.
