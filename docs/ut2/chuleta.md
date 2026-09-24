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

## Colecciones

```java
List<String> l = new ArrayList<>();   l.add("a"); l.get(0); l.size(); l.remove(0);
Set<String> s = new HashSet<>();      s.add("a"); s.contains("a");
Map<String,Integer> m = new HashMap<>();
m.put("k", 1); m.get("k"); m.getOrDefault("x", 0); m.containsKey("k");

var fija = List.of("a", "b");   // inmutable
```

## Streams

```java
lista.stream()
     .filter(p -> p.precio() > 100)
     .map(Producto::nombre)
     .sorted()
     .toList();

.count()  .distinct()  .limit(5)  .anyMatch(...)  .findFirst()
.mapToDouble(Producto::precio).sum()
.max(Comparator.comparing(Producto::precio))
.collect(Collectors.groupingBy(Producto::categoria))
```

:material-alert: Sin operación **terminal** (`toList`, `count`, `sum`, `forEach`) el stream **no ejecuta nada**.

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

## Maven

```bash
mvn compile    mvn test    mvn package    mvn clean
```

```
src/main/java       ← código
src/main/resources  ← configuración
src/test/java       ← tests
pom.xml             ← dependencias
```

## Convenciones

`camelCase` variables y métodos · `PascalCase` clases · `MAYUS_CON_GUION` constantes · paquetes en minúscula (`com.empresa.app`) · una clase pública por fichero, con el mismo nombre.
