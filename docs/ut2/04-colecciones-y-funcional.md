# 4. Colecciones y programación funcional

Guardar muchas cosas y hacer algo con ellas. Es la mitad del trabajo de un programa de servidor.

!!! tip "Esta página se lee con `jshell` abierto"
    Todo lo de aquí se puede teclear tal cual. Empieza siempre con estos dos `import`, que te ahorran escribirlos luego:

    ```java
    jshell> import java.util.*
    jshell> import java.util.stream.*
    ```

---

## 1. El problema: buscar

Tienes los alumnos de un instituto y quieres el que tiene el número de expediente 40312.

Con una lista hay que **mirar uno a uno** hasta encontrarlo. Con 30 alumnos no se nota; con 100.000, sí. Pruébalo:

```java
jshell> var lista = new ArrayList<Integer>()
jshell> for (int i = 0; i < 200_000; i++) lista.add(i)

jshell> var conjunto = new HashSet<>(lista)

jshell> long t = System.nanoTime(); lista.contains(199_999); (System.nanoTime()-t)/1000 + " µs"
$5 ==> "1483 µs"

jshell> t = System.nanoTime(); conjunto.contains(199_999); (System.nanoTime()-t)/1000 + " µs"
$6 ==> "3 µs"
```

**Quinientas veces más rápido.** Y no es magia: son dos formas distintas de guardar lo mismo.

- La **lista** los pone en fila. Para saber si algo está, hay que recorrerla.
- El **conjunto** calcula un número a partir del contenido (el *hash*) y lo usa como dirección: va directo.

Elegir la colección correcta **no es un detalle de estilo**: es la diferencia entre una aplicación que responde y una que no.

---

## 2. `List`: orden y posiciones

Una lista guarda elementos **en orden**, admite **repetidos** y se accede **por posición**.

```java
jshell> var tareas = new ArrayList<String>()
jshell> tareas.add("Comprar pan")
jshell> tareas.add("Llamar al banco")
jshell> tareas.add("Comprar pan")        // repetido: se admite
jshell> tareas
$4 ==> [Comprar pan, Llamar al banco, Comprar pan]

jshell> tareas.get(1)
$5 ==> "Llamar al banco"

jshell> tareas.size()
$6 ==> 3

jshell> tareas.add(0, "URGENTE")         // insertar al principio
jshell> tareas
$8 ==> [URGENTE, Comprar pan, Llamar al banco, Comprar pan]

jshell> tareas.remove("Comprar pan")     // quita SOLO la primera aparición
$9 ==> true
jshell> tareas
$10 ==> [URGENTE, Llamar al banco, Comprar pan]

jshell> tareas.indexOf("Comprar pan")
$11 ==> 2

jshell> tareas.contains("Llamar al banco")
$12 ==> true
```

!!! warning "`remove(int)` y `remove(Object)` no son lo mismo"
    ```java
    jshell> var nums = new ArrayList<>(List.of(10, 20, 30))
    jshell> nums.remove(1)            // posición 1
    $14 ==> 20
    jshell> nums
    $15 ==> [10, 30]

    jshell> nums.remove(Integer.valueOf(10))   // el objeto 10
    $16 ==> true
    jshell> nums
    $17 ==> [30]
    ```

    Con una `List<Integer>`, `remove(1)` borra **la posición 1**, no el número 1. Es una trampa clásica y cae en el examen.

### Listas fijas frente a listas que crecen

```java
jshell> var fija = List.of("a", "b", "c")
jshell> fija.add("d")
|  Exception java.lang.UnsupportedOperationException

jshell> var mutable = new ArrayList<>(List.of("a", "b", "c"))
jshell> mutable.add("d")
jshell> mutable
$21 ==> [a, b, c, d]
```

`List.of(...)` crea una lista **inmutable**: no se puede añadir ni quitar. Es perfecta para constantes y para devolver datos sin que nadie los toque. Si necesitas modificarla, envuélvela en `new ArrayList<>(...)`.

---

## 3. `Set`: sin repetidos

Un conjunto **no admite duplicados** y, en su versión normal, **no tiene orden**.

```java
jshell> var correos = new HashSet<String>()
jshell> correos.add("ana@iesx.es")
$23 ==> true
jshell> correos.add("ana@iesx.es")
$24 ==> false                          // ← ya estaba, no se añade
jshell> correos.add("bruno@iesx.es")
jshell> correos.size()
$26 ==> 2
```

`add` devuelve `true` o `false` según se haya añadido. **Eso sirve para detectar duplicados sin buscar antes:**

```java
jshell> var vistos = new HashSet<String>()
jshell> for (var linea : List.of("A", "B", "A", "C", "B")) {
   ...>     if (!vistos.add(linea)) System.out.println("Repetido: " + linea);
   ...> }
Repetido: A
Repetido: B
```

### Las tres variantes, y cuándo cada una

```java
jshell> var h = new HashSet<>(List.of("pera", "manzana", "kiwi"))
h ==> [kiwi, manzana, pera]              // orden imprevisible

jshell> var l = new LinkedHashSet<>(List.of("pera", "manzana", "kiwi"))
l ==> [pera, manzana, kiwi]              // orden de inserción

jshell> var t = new TreeSet<>(List.of("pera", "manzana", "kiwi"))
t ==> [kiwi, manzana, pera]              // orden alfabético
```

| | Orden | Coste de buscar | Cuándo |
|---|---|---|---|
| `HashSet` | **Ninguno** | Constante | Por defecto |
| `LinkedHashSet` | De inserción | Constante | Cuando importa quién llegó antes |
| `TreeSet` | Natural (alfabético, numérico) | Logarítmico | Cuando hay que sacarlo ordenado |

!!! danger "Un `Set` de objetos propios necesita `equals` y `hashCode`"
    ```java
    jshell> class Alumno { String n; Alumno(String n) { this.n = n; } }
    jshell> var s = new HashSet<Alumno>()
    jshell> s.add(new Alumno("Ana")); s.add(new Alumno("Ana"))
    jshell> s.size()
    $35 ==> 2        // ¡dos «Ana»!
    ```

    El conjunto compara con `equals`, y la clase no lo define. Con un `record` funciona sin hacer nada:

    ```java
    jshell> record Alumno(String n) {}
    jshell> var s2 = new HashSet<Alumno>()
    jshell> s2.add(new Alumno("Ana")); s2.add(new Alumno("Ana"))
    jshell> s2.size()
    $39 ==> 1
    ```

---

## 4. `Map`: clave → valor

Un mapa asocia **una clave** con **un valor**. Es la colección más usada en un servidor: usuario → sus datos, código → producto, día → ventas.

```java
jshell> var notas = new HashMap<String, Integer>()
jshell> notas.put("Ana", 8)
jshell> notas.put("Bruno", 6)
jshell> notas.put("Ana", 9)              // ← misma clave: SUSTITUYE
jshell> notas
$43 ==> {Bruno=6, Ana=9}

jshell> notas.get("Ana")
$44 ==> 9

jshell> notas.get("Nadie")
$45 ==> null                              // ← ojo
```

**`get` de una clave que no existe devuelve `null`**, y eso es media hora de depuración el día que menos te apetece. Hay dos formas de evitarlo:

```java
jshell> notas.getOrDefault("Nadie", 0)
$46 ==> 0

jshell> notas.containsKey("Nadie")
$47 ==> false
```

### Los métodos que ahorran la mitad del código

```java
jshell> var contador = new HashMap<String, Integer>()

jshell> for (var palabra : List.of("sol", "mar", "sol", "sol", "mar")) {
   ...>     contador.merge(palabra, 1, Integer::sum);          // (1)
   ...> }
jshell> contador
$50 ==> {sol=3, mar=2}
```

1. `merge(clave, valorSiNoEstá, cómoCombinar)`. Es contar en una línea.

Sin `merge`, lo mismo son cinco líneas y un `if`:

```java
if (contador.containsKey(palabra)) contador.put(palabra, contador.get(palabra) + 1);
else                               contador.put(palabra, 1);
```

Y para agrupar en listas:

```java
jshell> var porLetra = new HashMap<Character, List<String>>()
jshell> for (var p : List.of("sol", "mar", "sal", "mes")) {
   ...>     porLetra.computeIfAbsent(p.charAt(0), k -> new ArrayList<>()).add(p);   // (1)
   ...> }
jshell> porLetra
$53 ==> {s=[sol, sal], m=[mar, mes]}
```

1. «Si no hay lista para esa letra, créala; después añade». Otra línea en lugar de cuatro.

### Recorrer un mapa

```java
jshell> for (var e : notas.entrySet()) System.out.println(e.getKey() + " → " + e.getValue());
Bruno → 6
Ana → 9

jshell> notas.forEach((nombre, nota) -> System.out.println(nombre + " → " + nota));
Bruno → 6
Ana → 9

jshell> notas.keySet()
$56 ==> [Bruno, Ana]
jshell> notas.values()
$57 ==> [6, 9]
```

### `HashMap` frente a `TreeMap`

```java
jshell> new HashMap<>(Map.of("pera",1,"manzana",2,"kiwi",3))
$58 ==> {kiwi=3, manzana=2, pera=1}      // sin orden garantizado

jshell> new TreeMap<>(Map.of("pera",1,"manzana",2,"kiwi",3))
$59 ==> {kiwi=3, manzana=2, pera=1}      // alfabético, SIEMPRE
```

Aquí coinciden por casualidad. **Esa casualidad es peligrosa**: mucha gente cree que `HashMap` ordena porque con sus datos de prueba salió ordenado. No lo hace, y el día que cambien los datos cambia el orden.

---

## 5. Cuál elegir

```mermaid
flowchart TD
    A["¿Qué necesito guardar?"] --> B{"¿Busco por una clave?"}
    B -->|Sí| C{"¿Hace falta<br/>orden de claves?"}
    C -->|No| D["HashMap"]
    C -->|Sí| E["TreeMap"]
    B -->|No| F{"¿Puede haber<br/>repetidos?"}
    F -->|No| G{"¿Hace falta orden?"}
    G -->|No| H["HashSet"]
    G -->|"De inserción"| I["LinkedHashSet"]
    G -->|"Natural"| J["TreeSet"]
    F -->|Sí| K{"¿Añado y quito<br/>por los extremos?"}
    K -->|Sí| L["ArrayDeque"]
    K -->|No| M["ArrayList"]
```

Y la tabla, que es la que hay que saberse:

| Caso real | Estructura | Por qué |
|---|---|---|
| Las líneas de un fichero, en orden | `ArrayList` | Orden y repetidos |
| Correos suscritos | `HashSet` | Sin repetidos, orden irrelevante |
| Etiquetas para una nube, ordenadas | `TreeSet` | Sin repetidos y ordenado |
| Producto por su código | `HashMap` | Búsqueda por clave |
| Ventas por día, para un informe | `TreeMap` | Las fechas salen ordenadas |
| Últimas diez acciones para deshacer | `ArrayDeque` | Pila: `push` / `pop` |
| Cola de impresión | `ArrayDeque` | Cola: `offer` / `poll` |

!!! info "`ArrayDeque` hace de pila y de cola"
    ```java
    jshell> var pila = new ArrayDeque<String>()
    jshell> pila.push("uno"); pila.push("dos"); pila.push("tres")
    jshell> pila.pop()
    $63 ==> "tres"                    // el último que entró

    jshell> var cola = new ArrayDeque<String>()
    jshell> cola.offer("uno"); cola.offer("dos"); cola.offer("tres")
    jshell> cola.poll()
    $67 ==> "uno"                     // el primero que entró
    ```

    No uses `Stack` ni `Vector`: están obsoletas desde hace veinte años.

---

## 6. Recorrer y modificar

```java
jshell> var l = new ArrayList<>(List.of(1, 2, 3, 4, 5))

jshell> for (var n : l) { if (n % 2 == 0) l.remove(n); }
|  Exception java.util.ConcurrentModificationException
```

**No se puede modificar una colección mientras se recorre con `for-each`.** El bucle usa un iterador por debajo, y quitar un elemento lo deja obsoleto.

Las tres formas correctas, de mejor a peor:

```java
jshell> var a = new ArrayList<>(List.of(1,2,3,4,5))
jshell> a.removeIf(n -> n % 2 == 0)        // 1 · la que se usa
jshell> a
$71 ==> [1, 3, 5]

jshell> var b = new ArrayList<>(List.of(1,2,3,4,5))
jshell> var it = b.iterator()               // 2 · con más control
jshell> while (it.hasNext()) { if (it.next() % 2 == 0) it.remove(); }
jshell> b
$75 ==> [1, 3, 5]

jshell> var c = List.of(1,2,3,4,5).stream()
   ...>          .filter(n -> n % 2 != 0).toList();    // 3 · sin mutar nada
c ==> [1, 3, 5]
```

---

## 7. Lambdas: un método sin nombre

Una **lambda** es una función escrita donde se usa. Lo que hay que entender es que **no es magia**: es la implementación de una interfaz de un solo método.

```java
jshell> interface Validador { boolean vale(String texto); }

jshell> Validador noVacio = t -> !t.isBlank();     // (1)

jshell> noVacio.vale("hola")
$79 ==> true
jshell> noVacio.vale("   ")
$80 ==> false
```

1. Esto es exactamente lo mismo que escribir una clase que implemente `Validador`, pero en una línea.

La sintaxis, en tres formas:

```java
jshell> Validador a = t -> !t.isBlank();                        // una expresión
jshell> Validador b = (String t) -> !t.isBlank();               // con el tipo
jshell> Validador c = t -> { return !t.isBlank(); };            // con llaves y return
```

Y las **referencias a método**, que son lambdas abreviadas:

```java
jshell> List.of("uno","dos").forEach(s -> System.out.println(s));  // lambda
jshell> List.of("uno","dos").forEach(System.out::println);         // lo mismo, más corto

jshell> List.of("b","a","c").stream().map(String::toUpperCase).toList()
$86 ==> [B, A, C]
```

---

## 8. Streams: describir, no recorrer

Un **stream** es una tubería: entran datos, se transforman por el camino y sale un resultado. Se escribe **qué** quieres, no **cómo** recorrerlo.

Este bucle:

```java
var caros = new ArrayList<String>();
for (var p : productos) {
    if (p.precio() > 100) {
        caros.add(p.nombre().toUpperCase());
    }
}
Collections.sort(caros);
```

es este stream:

```java
var caros = productos.stream()
        .filter(p -> p.precio() > 100)
        .map(p -> p.nombre().toUpperCase())
        .sorted()
        .toList();
```

Pruébalo entero:

```java
jshell> record Producto(String nombre, String categoria, double precio) {}

jshell> var productos = List.of(
   ...>     new Producto("Patinete", "movilidad", 120.0),
   ...>     new Producto("Casco", "seguridad", 35.0),
   ...>     new Producto("Bici", "movilidad", 450.0),
   ...>     new Producto("Candado", "seguridad", 25.0),
   ...>     new Producto("Luces", "seguridad", 15.0))

jshell> productos.stream().filter(p -> p.precio() > 100).map(Producto::nombre).toList()
$90 ==> [Patinete, Bici]
```

### Las cinco operaciones que hacen falta

```java
jshell> productos.stream().filter(p -> p.precio() < 30).toList()        // 1 · quedarse con algunos
$91 ==> [Producto[nombre=Candado,...], Producto[nombre=Luces,...]]

jshell> productos.stream().map(Producto::nombre).toList()               // 2 · transformar
$92 ==> [Patinete, Casco, Bici, Candado, Luces]

jshell> productos.stream().sorted(Comparator.comparingDouble(Producto::precio)).map(Producto::nombre).toList()
$93 ==> [Luces, Candado, Casco, Patinete, Bici]                         // 3 · ordenar

jshell> productos.stream().mapToDouble(Producto::precio).sum()          // 4 · reducir a un número
$94 ==> 645.0

jshell> productos.stream().max(Comparator.comparingDouble(Producto::precio))
$95 ==> Optional[Producto[nombre=Bici, categoria=movilidad, precio=450.0]]   // 5 · el máximo
```

Fíjate en el último: **devuelve `Optional`**, porque la lista podría estar vacía. Se termina siempre así:

```java
jshell> productos.stream().max(Comparator.comparingDouble(Producto::precio))
   ...>           .map(Producto::nombre).orElse("—")
$96 ==> "Bici"
```

!!! warning "Un stream se usa una vez"
    ```java
    jshell> var s = productos.stream()
    jshell> s.count()
    $98 ==> 5
    jshell> s.count()
    |  Exception java.lang.IllegalStateException: stream has already been operated upon or closed
    ```

    No se guarda en una variable para reutilizarlo. Se crea, se usa y se tira.

### Agrupar: `groupingBy`

Es la operación que más se usa en un servidor y la que más cae en el examen.

```java
jshell> productos.stream().collect(Collectors.groupingBy(Producto::categoria))
$100 ==> {seguridad=[Casco, Candado, Luces], movilidad=[Patinete, Bici]}

jshell> productos.stream().collect(
   ...>     Collectors.groupingBy(Producto::categoria, Collectors.counting()))
$101 ==> {seguridad=3, movilidad=2}

jshell> productos.stream().collect(
   ...>     Collectors.groupingBy(Producto::categoria,
   ...>                           Collectors.averagingDouble(Producto::precio)))
$102 ==> {seguridad=25.0, movilidad=285.0}
```

El segundo parámetro dice **qué hacer con cada grupo**: dejarlo como lista (por defecto), contarlo, sumarlo, promediarlo.

Y si hace falta que las claves salgan ordenadas:

```java
jshell> productos.stream().collect(
   ...>     Collectors.groupingBy(Producto::categoria, TreeMap::new, Collectors.counting()))
$103 ==> {movilidad=2, seguridad=3}
```

!!! danger "`groupingBy` devuelve un `HashMap`"
    Sin el `TreeMap::new`, **el orden de las claves no está garantizado**. Si tu informe tiene que salir ordenado, hay que pedirlo.

---

## Pruébalo ahora (12 min)

Con la lista `productos` de arriba en `jshell`:

1. Los nombres de los de la categoría `seguridad`, en mayúsculas y ordenados.
2. El precio total de la categoría `movilidad`.
3. Un `Map<String, List<String>>` con los **nombres** agrupados por categoría (no los productos enteros).
4. El producto más barato, devolviendo `"ninguno"` si la lista estuviera vacía.
5. Cuenta cuántas veces aparece cada letra inicial usando `merge`.

??? success "Las cinco soluciones"

    ```java
    // 1
    productos.stream()
             .filter(p -> p.categoria().equals("seguridad"))
             .map(p -> p.nombre().toUpperCase())
             .sorted()
             .toList();                       // [CANDADO, CASCO, LUCES]

    // 2
    productos.stream()
             .filter(p -> p.categoria().equals("movilidad"))
             .mapToDouble(Producto::precio)
             .sum();                          // 570.0

    // 3
    productos.stream().collect(Collectors.groupingBy(
            Producto::categoria,
            Collectors.mapping(Producto::nombre, Collectors.toList())));

    // 4
    productos.stream()
             .min(Comparator.comparingDouble(Producto::precio))
             .map(Producto::nombre)
             .orElse("ninguno");              // Luces

    // 5
    var cuenta = new HashMap<Character, Integer>();
    productos.forEach(p -> cuenta.merge(p.nombre().charAt(0), 1, Integer::sum));
    ```

    El 3 es el que cuesta: `Collectors.mapping` transforma **dentro** de cada grupo antes de recogerlo.

---

## Ejercicios (con solución)

??? success "E1 · ¿Qué imprime?"

    ```java
    var m = new HashMap<String, Integer>();
    m.put("pera", 3);
    m.put("manzana", 5);
    m.put("pera", 7);
    System.out.println(m.size());
    System.out.println(m.get("pera"));
    System.out.println(m.get("kiwi"));
    ```

    **`2`, `7` y `null`.**

    - `size()` es 2: `put` con una clave que ya existe **sustituye**, no añade.
    - `get` de una clave inexistente devuelve `null`. Si después haces `m.get("kiwi") + 1`, tienes un `NullPointerException`.

??? success "E2 · El `remove` traicionero"

    ```java
    var l = new ArrayList<>(List.of(10, 20, 30));
    l.remove(1);
    System.out.println(l);
    ```

    **`[10, 30]`.** `remove(int)` borra la **posición**. Para borrar el valor 1 habría que escribir `l.remove(Integer.valueOf(1))`.

    Con `List<String>` no hay ambigüedad; con `List<Integer>`, sí.

??? success "E3 · Elige la colección"

    (a) los DNI ya registrados · (b) el historial de páginas visitadas · (c) el stock por código de producto · (d) las etiquetas de un artículo, sin repetir y en orden alfabético · (e) las tareas pendientes, la más antigua primero

    | | Cuál | Por qué |
    |---|---|---|
    | (a) DNI registrados | `HashSet` | Solo importa si está; sin repetidos |
    | (b) Historial | `ArrayList` | Orden y puede haber repetidos |
    | (c) Stock por código | `HashMap<String,Integer>` | Búsqueda por clave |
    | (d) Etiquetas | `TreeSet` | Sin repetidos y ordenado |
    | (e) Tareas | `ArrayDeque` como cola | `offer` / `poll` |

??? success "E4 · Contar sin `if`"

    Cuenta cuántas veces aparece cada palabra en una lista, en una sola línea dentro del bucle.

    ```java
    var texto = List.of("sol", "mar", "sol", "aire", "mar", "sol");
    var cuenta = new HashMap<String, Integer>();
    texto.forEach(p -> cuenta.merge(p, 1, Integer::sum));
    // {aire=1, sol=3, mar=2}
    ```

    O con streams, sin mapa a mano:

    ```java
    texto.stream().collect(Collectors.groupingBy(p -> p, Collectors.counting()));
    // {aire=1, sol=3, mar=2}
    ```

    Los dos valen. El primero se lee mejor dentro de un bucle que ya existe; el segundo, cuando ya estás en una cadena de streams.

??? success "E5 · El orden que no estaba garantizado"

    ```java
    record Venta(String vendedor, double importe) {}
    var ventas = List.of(new Venta("Ana", 300), new Venta("Bruno", 150),
                         new Venta("Ana", 200), new Venta("Carla", 500));

    var porVendedor = ventas.stream().collect(
            Collectors.groupingBy(Venta::vendedor,
                                  Collectors.summingDouble(Venta::importe)));
    ```

    `porVendedor` es `{Bruno=150.0, Ana=500.0, Carla=500.0}` — **sin orden fiable**, porque es un `HashMap`.

    Para un informe alfabético:

    ```java
    Collectors.groupingBy(Venta::vendedor, TreeMap::new,
                          Collectors.summingDouble(Venta::importe));
    // {Ana=500.0, Bruno=150.0, Carla=500.0}
    ```

    Y para el ranking por importe hay que volver a hacer stream sobre las entradas:

    ```java
    porVendedor.entrySet().stream()
               .sorted(Map.Entry.<String,Double>comparingByValue().reversed())
               .map(Map.Entry::getKey)
               .toList();                      // [Ana, Carla, Bruno] (o Carla, Ana)
    ```

    El `<String,Double>` explícito hace falta: sin él, el compilador no infiere el tipo dentro de `sorted` y da un error que no dice nada.

??? success "E6 · `ConcurrentModificationException`"

    Escribe la versión que falla y las dos que funcionan, para quitar de una lista los nombres de menos de cuatro letras.

    ```java
    // FALLA
    for (var n : nombres) { if (n.length() < 4) nombres.remove(n); }

    // BIEN · 1
    nombres.removeIf(n -> n.length() < 4);

    // BIEN · 2
    var it = nombres.iterator();
    while (it.hasNext()) { if (it.next().length() < 4) it.remove(); }
    ```

    Y el aviso: las dos correctas necesitan una lista **mutable**. Sobre `List.of(...)` lanzan `UnsupportedOperationException`.
