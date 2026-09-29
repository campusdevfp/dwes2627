# Chuleta de Java 25

> Toda la sintaxis de la unidad en una página. Tenla abierta mientras programas.

## Ejecutar

```bash
java -version          # comprobar (debe decir 25.x)
java Hola.java         # compilar y ejecutar de una vez (JDK 25)
jwebserver -p 8000     # servidor web estático incluido en el JDK
```

## Programa mínimo

```java
void main() {
    IO.println("Hola");
}
```

## Tipos y variables

```java
int edad = 25;                 double precio = 19.99;
boolean activo = true;         char inicial = 'A';
String nombre = "Ana";         var lista = new ArrayList<String>();   // inferido
final double IVA = 0.21;       // constante
```

| Trampa | Resultado |
|---|---|
| `7 / 2` | `3` (división entera) |
| `7 / 2.0` | `3.5` |
| `"5" + 3` | `"53"` (concatenación) |
| `0.1 + 0.2` | `0.30000000000000004` |
| `a == b` con Strings | compara referencias → usa `.equals()` |

## Cadenas

```java
s.length()   s.toUpperCase()   s.contains("x")   s.isBlank()
s.equals(t)  s.equalsIgnoreCase(t)  s.split(",")  s.strip()
"Hola, %s (%d)".formatted(nombre, edad)

var texto = """
    multilínea
    """;
```

## Control de flujo

```java
if (x > 0) { } else if (x == 0) { } else { }

for (int i = 0; i < 5; i++) { }
for (var item : lista) { }
while (cond) { }

var r = switch (dia) {
    case 1, 2, 3, 4, 5 -> "Laborable";
    case 6, 7          -> "Finde";
    default            -> "?";
};
```

## Clases y records

```java
public class Producto {
    private String nombre;
    public Producto(String nombre) { this.nombre = nombre; }
    public String getNombre() { return nombre; }
}

public record Producto(String nombre, double precio) {}   // getters: nombre(), precio()

public enum Estado { PENDIENTE, ENVIADO }

public interface Notificador { void enviar(String m); }
public class Email implements Notificador {
    @Override public void enviar(String m) { }
}
```

## Colecciones: cuál elegir

| Necesito | Uso |
|---|---|
| Orden + índices | `ArrayList` |
| Únicos, rápido | `HashSet` |
| Únicos ordenados | `TreeSet` |
| Únicos en orden de inserción | `LinkedHashSet` |
| Clave → valor | `HashMap` |
| Clave → valor, claves ordenadas | `TreeMap` |
| Pila (LIFO) / cola (FIFO) | `ArrayDeque` |

```java
List<String> l = new ArrayList<>();   l.add("a"); l.get(0); l.size(); l.remove(0);
Set<String>  s = new HashSet<>();     s.add("a"); s.contains("a");
Map<String,Integer> m = new HashMap<>();
m.put("k", 1); m.get("k"); m.getOrDefault("x", 0); m.containsKey("k");

var fija = List.of("a", "b");              // inmutable: add lanza excepción
```

```java
lista.removeIf(x -> cond);                 // borrar sin romper el recorrido
m.getOrDefault(k, 0);                      // en vez de get + null
m.computeIfAbsent(k, x -> new ArrayList<>()).add(v);
m.merge(k, 1, Integer::sum);               // contar ocurrencias
```

:material-alert: `remove(int)` borra **la posición**; `remove(Object)` borra el valor. Con `List<Integer>` es la trampa clásica.

## Comparator

```java
Comparator.comparing(Producto::precio)
Comparator.comparingDouble(Producto::precio)          // sin autoboxing
Comparator.comparing(Producto::precio).reversed()
Comparator.comparing(Producto::categoria).reversed()
          .thenComparing(Producto::nombre)            // reversed SOLO la categoría
Comparator.comparing(Producto::nombre, String.CASE_INSENSITIVE_ORDER)

lista.sort(cmp);                      // modifica la lista
lista.stream().sorted(cmp).toList();  // devuelve una nueva
```

:material-alert: `.reversed()` invierte **todo lo encadenado hasta ese punto**.

## Streams

```java
lista.stream()
     .filter(p -> p.precio() > 100)
     .map(Producto::nombre)
     .sorted()
     .toList();
```

```java
.count()   .distinct()   .limit(5)   .skip(2)
.anyMatch(...)  .allMatch(...)  .noneMatch(...)  .findFirst()   // Optional
.mapToDouble(Producto::precio).sum()       // .average()  también Optional
.max(Comparator.comparingDouble(Producto::precio))   // devuelve Optional
```

:material-alert: Sin operación **terminal** (`toList`, `count`, `sum`, `forEach`) el stream **no ejecuta nada**. Y un stream **se usa una vez**.

## Recolectores

```java
.collect(Collectors.groupingBy(Producto::categoria))
.collect(Collectors.groupingBy(Producto::categoria, Collectors.counting()))
.collect(Collectors.groupingBy(Producto::categoria, Collectors.summingDouble(Producto::precio)))
.collect(Collectors.groupingBy(Producto::categoria, TreeMap::new, Collectors.counting()))
.collect(Collectors.groupingBy(Producto::categoria,
         Collectors.mapping(Producto::nombre, Collectors.toList())))
.collect(Collectors.joining(", ", "[", "]"))
```

:material-alert: `groupingBy` devuelve un **`HashMap`**: el orden de las claves **no** está garantizado. Para ordenarlo, `TreeMap::new` **en cada nivel**.

```java
// ranking: ordenar un mapa por su valor
mapa.entrySet().stream()
    .sorted(Map.Entry.<String, Double>comparingByValue().reversed())   // el tipo, obligatorio
    .map(Map.Entry::getKey)
    .toList();
```

## Excepciones y Optional

```java
try {
    ...
} catch (NumberFormatException | ArithmeticException e) {
    IO.println(e.getMessage());
} finally {
    // siempre
}

throw new IllegalArgumentException("mensaje");

try (var r = Files.newBufferedReader(path)) { }   // se cierra solo

opt.isPresent()   opt.orElse(defecto)   opt.map(X::y)
opt.orElseThrow(() -> new MiExcepcion())
```

| | Checked | Unchecked |
|---|---|---|
| Obliga el compilador | Sí | No |
| Ejemplos | `IOException`, `SQLException` | `NullPointerException`, `IllegalArgumentException` |

## Maven, lo justo

```xml
<properties>
  <maven.compiler.release>25</maven.compiler.release>
  <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  <exec.mainClass>es.iesx.app.Main</exec.mainClass>
</properties>
```

```bash
mvn compile            # compila a target/classes
mvn exec:java          # ejecuta exec.mainClass
mvn clean package      # genera target/app-1.0.0.jar
mvn dependency:tree    # todas las librerías, incluidas las indirectas
mvn -o compile         # sin conexión
```

| Dónde va | Qué |
|---|---|
| `src/main/java/` | El código |
| `src/main/resources/` | Datos y configuración (viajan dentro del `.jar`) |
| `target/` | Generado. **No se sube a Git** |

Coordenadas de una librería: `groupId:artifactId:version`.

## Convenciones

`camelCase` variables y métodos · `PascalCase` clases · `MAYUS_CON_GUION` constantes · paquetes en minúscula (`com.empresa.app`) · una clase pública por fichero, con el mismo nombre.
