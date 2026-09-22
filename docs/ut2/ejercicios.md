# Batería de ejercicios — UT2

**Dominio: una liga de baloncesto.** Distinto del de las prácticas guiadas a propósito.

Todos se ejecutan con `java Fichero.java`, sin Maven, salvo el E12. **Escribe la solución antes de desplegarla**: leerla no sirve de nada.

---

### E1 ● — Tipos y conversiones

Lee dos enteros de la entrada, calcula el porcentaje de acierto en tiros y muéstralo con dos decimales.

??? success "Solución"

    ```java
    void main() {
        var sc = new Scanner(System.in);
        IO.println("Tiros anotados:");  int anotados = sc.nextInt();
        IO.println("Tiros intentados:"); int intentados = sc.nextInt();

        double pct = intentados == 0 ? 0 : (anotados * 100.0) / intentados;
        IO.println("Acierto: %.2f %%".formatted(pct));
    }
    ```
    La trampa es `anotados * 100 / intentados`: **división entera**, siempre 0 o 1. El `100.0` fuerza la promoción a `double`. Y la división por cero de enteros lanza excepción, por eso el ternario.


### E2 ● — `switch` moderno

Dada la posición de un jugador (`BASE`, `ALERO`, `PIVOT`), devuelve su descripción con un `switch` de flecha.

??? success "Solución"

    ```java
    String descripcion(Posicion p) {
        return switch (p) {
            case BASE  -> "Dirige el juego";
            case ALERO -> "Anota desde fuera";
            case PIVOT -> "Juega cerca del aro";
        };
    }
    ```
    Con un `enum` y flechas **no hace falta `default`**: el compilador comprueba que están todos los casos. Si mañana añades `ESCOLTA`, el código deja de compilar y te enteras — que es justo lo que quieres.


### E3 ●● — `record` frente a clase

Modela un `Jugador` (nombre, dorsal, posición, puntos) y explica por qué aquí conviene un `record`.

??? success "Solución"

    ```java
    record Jugador(String nombre, int dorsal, Posicion posicion, int puntos) {
        Jugador {
            if (dorsal < 0 || dorsal > 99) throw new IllegalArgumentException("Dorsal fuera de rango");
            if (nombre == null || nombre.isBlank()) throw new IllegalArgumentException("Nombre obligatorio");
        }
        boolean esAnotador() { return puntos > 20; }
    }
    ```
    Es un **portador de datos inmutable**: no tiene identidad propia ni ciclo de vida. El `record` te da constructor, *getters*, `equals`, `hashCode` y `toString` gratis.

    Ese constructor sin paréntesis es el **compacto**: valida antes de asignar. Y ojo, en la UT5 verás que una entidad JPA **no** puede ser `record`, por razones que aquí todavía no importan.


### E4 ●● — `equals` y `hashCode`

Crea un `Equipo` con clase normal y haz que dos equipos con la misma ciudad y nombre se consideren iguales. Demuéstralo con un `HashSet`.

??? success "Solución"

    ```java
    class Equipo {
        private final String ciudad, nombre;

        @Override public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Equipo e)) return false;
            return ciudad.equals(e.ciudad) && nombre.equals(e.nombre);
        }
        @Override public int hashCode() { return Objects.hash(ciudad, nombre); }
    }
    ```
    ```java
    var set = new HashSet<Equipo>();
    set.add(new Equipo("Madrid", "Estudiantes"));
    IO.println(set.contains(new Equipo("Madrid", "Estudiantes")));   // true
    ```
    **Sin `hashCode` esto imprime `false`**, aunque `equals` esté bien: el `HashSet` busca primero por *hash* y ni siquiera llega a comparar. Es el error que más cuesta encontrar, y reaparece en la UT5.


### E5 ●● — Elegir la colección

Para cada caso, di qué colección usarías y por qué: (a) el orden de tiro en un concurso, (b) los dorsales ya asignados, (c) buscar un jugador por su DNI, (d) la clasificación ordenada por puntos.

??? success "Solución"

    | Caso | Colección | Por qué |
    |---|---|---|
    | (a) Orden de tiro | `List` (`ArrayList`) | Importa el orden y admite repetidos |
    | (b) Dorsales asignados | `Set` (`HashSet`) | Sin repetidos, y solo importa si está o no |
    | (c) Buscar por DNI | `Map<String, Jugador>` | Acceso directo por clave, coste constante |
    | (d) Clasificación | `TreeMap` o `List` ordenada | Hace falta orden por valor |

    El (c) es el que más se falla: recorrer una `List` buscando el DNI funciona con 10 jugadores y es un desastre con 100.000.


### E6 ●●● — Streams: agrupar y contar

Dada una `List<Jugador>`, calcula cuántos jugadores hay por posición, ordenado alfabéticamente por posición.

??? success "Solución"

    ```java
    Map<Posicion, Long> porPosicion = jugadores.stream()
        .collect(groupingBy(Jugador::posicion, TreeMap::new, counting()));
    ```
    El `TreeMap::new` en medio es la clave: sin él, `groupingBy` devuelve un `HashMap` y **el orden de las claves es impredecible**. Si el enunciado pide orden, hay que decirlo explícitamente.


### E7 ●●● — Streams: estadísticas y máximo

Del mismo listado, saca la media de puntos, el máximo anotador y el total del equipo.

??? success "Solución"

    ```java
    var stats = jugadores.stream().mapToInt(Jugador::puntos).summaryStatistics();
    IO.println("Media: %.1f · Máx: %d · Total: %d"
        .formatted(stats.getAverage(), stats.getMax(), stats.getSum()));

    Optional<Jugador> mejor = jugadores.stream().max(comparingInt(Jugador::puntos));
    mejor.ifPresent(j -> IO.println("Máximo anotador: " + j.nombre()));
    ```
    `summaryStatistics()` recorre **una sola vez** y da cuenta, suma, mínimo, máximo y media. Hacer tres `stream()` separados es tres veces el trabajo.

    Y `max` devuelve `Optional` porque la lista puede estar vacía. Que el tipo te obligue a pensarlo es la gracia.


### E8 ●●● — `map` frente a `flatMap`

Cada `Equipo` tiene una `List<Jugador>`. Obtén la lista plana de todos los jugadores de la liga, ordenados por puntos descendente.

??? success "Solución"

    ```java
    List<Jugador> todos = equipos.stream()
        .flatMap(e -> e.jugadores().stream())
        .sorted(comparingInt(Jugador::puntos).reversed())
        .toList();
    ```
    Con `map` obtendrías un `Stream<List<Jugador>>` —una lista de listas—. `flatMap` **aplana**: convierte cada elemento en un flujo y los concatena.

    La regla para acordarse: si al terminar tienes colecciones dentro de colecciones, querías `flatMap`.


### E9 ●● — Excepciones propias

Crea `DorsalOcupadoException` y lánzala al fichar a un jugador con un dorsal ya usado. Decide si comprobada o no comprobada, y justifícalo.

??? success "Solución"

    ```java
    class DorsalOcupadoException extends RuntimeException {
        DorsalOcupadoException(int dorsal) {
            super("El dorsal " + dorsal + " ya está ocupado");
        }
    }
    ```
    **No comprobada** (`RuntimeException`): es un error de uso, no una condición del entorno que quien llama pueda gestionar de forma razonable. Las comprobadas se reservan para lo recuperable —un fichero que puede no existir, una red que puede caerse—.

    En la UT4 verás que Spring traduce estas excepciones a códigos HTTP, y ahí esta decisión se vuelve visible: una `RuntimeException` propia acaba siendo un **409 Conflict**.


### E10 ●● — `Optional` bien usado

Escribe `buscarPorDorsal(int)` que devuelva `Optional<Jugador>`, y úsalo de tres formas sin llamar nunca a `.get()`.

??? success "Solución"

    ```java
    Optional<Jugador> buscarPorDorsal(int dorsal) {
        return jugadores.stream().filter(j -> j.dorsal() == dorsal).findFirst();
    }
    ```
    ```java
    String nombre = buscarPorDorsal(7).map(Jugador::nombre).orElse("Dorsal libre");
    buscarPorDorsal(7).ifPresent(j -> IO.println("Ficha: " + j));
    Jugador j = buscarPorDorsal(7).orElseThrow(() -> new DorsalOcupadoException(7));
    ```
    `.get()` sin comprobar es un `NullPointerException` con otro nombre y peor disfraz. Si te ves escribiendo `if (o.isPresent()) o.get()`, usa `ifPresent`.


### E11 ●●● — Refactorizar bucles a streams

Traduce este código y explica qué gana:

```java
List<String> resultado = new ArrayList<>();
for (Jugador j : jugadores) {
    if (j.puntos() > 15) {
        String s = j.nombre().toUpperCase();
        if (!resultado.contains(s)) resultado.add(s);
    }
}
Collections.sort(resultado);
```

??? success "Solución"

    ```java
    List<String> resultado = jugadores.stream()
        .filter(j -> j.puntos() > 15)
        .map(j -> j.nombre().toUpperCase())
        .distinct()
        .sorted()
        .toList();
    ```
    Gana en legibilidad —se lee como la frase que describe el problema— y en rendimiento: `resultado.contains(s)` dentro de un bucle es coste cuadrático, mientras que `distinct()` usa un conjunto por debajo.

    Y no siempre gana: un bucle con varias condiciones cruzadas y efectos laterales suele quedar **más claro como bucle**. La regla es la legibilidad, no la moda.


### E12 ●●● — Proyecto Maven con tests

Monta un proyecto Maven con la clase `Liga` y escribe cuatro tests con JUnit 5: fichar correctamente, dorsal duplicado, buscar existente y buscar inexistente.

??? success "Solución"

    ```java
    class LigaTest {
        private Liga liga;
        @BeforeEach void preparar() { liga = new Liga(); }

        @Test void fichaUnJugadorNuevo() {
            liga.fichar(new Jugador("Ana", 7, BASE, 0));
            assertThat(liga.tamano()).isEqualTo(1);
        }

        @Test void dorsalDuplicadoLanzaExcepcion() {
            liga.fichar(new Jugador("Ana", 7, BASE, 0));
            assertThatThrownBy(() -> liga.fichar(new Jugador("Luis", 7, PIVOT, 0)))
                .isInstanceOf(DorsalOcupadoException.class)
                .hasMessageContaining("7");
        }

        @Test void buscaElQueExiste() {
            liga.fichar(new Jugador("Ana", 7, BASE, 0));
            assertThat(liga.buscarPorDorsal(7)).isPresent();
        }

        @Test void devuelveVacioSiNoExiste() {
            assertThat(liga.buscarPorDorsal(99)).isEmpty();
        }
    }
    ```
    Los cuatro tests cubren el patrón que vas a repetir todo el curso: **camino bueno, error, encontrado y no encontrado**. El `@BeforeEach` garantiza que cada test parte de cero; compartir estado entre tests es la causa número uno de tests que pasan solos y fallan juntos.


---

## Taller: escribir Java moderno

!!! reto "Del E13 al E27 se escriben y se ejecutan"
    La UT2 es la última unidad en la que se aprende **el lenguaje**. Desde la UT4 ya no hay tiempo: se da por sabido.

    Todos se ejecutan con `java Fichero.java`, sin Maven.

### E13 ● — Bloques de texto

Imprime la ficha de un jugador en varias líneas, sin concatenar y sin `\n`.

??? success "Solución"

    ```java
    void main() {
        var j = new Jugador("Ana Ruiz", 7, Posicion.BASE, 18);

        var ficha = """
                +------------------------------+
                | %-20s #%2d |
                | %-24s     |
                | Puntos: %-3d                 |
                +------------------------------+
                """.formatted(j.nombre(), j.dorsal(), j.posicion(), j.puntos());

        IO.println(ficha);
    }
    ```

    - La **sangría común se quita sola**: se mide desde la línea menos sangrada, incluida la del cierre. Por eso el `"""` final marca el margen izquierdo.
    - No hacen falta `\n`: los saltos son los del bloque.
    - `formatted` es `String.format`, pero encadenable.

    Para partir una línea larga en el código **sin** que salga partida, termina con `\` :

    ```java
    var sql = """
            SELECT nombre, dorsal \
            FROM jugador WHERE puntos > ?
            """;
    ```

### E14 ● — `var`: cuándo sí y cuándo no

Corrige estos cuatro usos y justifica cada decisión.

```java
var x = servicio.buscar();
var lista = new ArrayList<Jugador>();
var total = calcularMedia(jugadores);
var i = 0;
```

??? success "Solución"

    ```java
    List<Jugador> x = servicio.buscar();   // ni el nombre ni la derecha dicen el tipo
    var lista = new ArrayList<Jugador>();  // bien: el tipo está a la derecha
    double total = calcularMedia(...);     // «total» no dice si es double o BigDecimal
    var i = 0;                             // bien: obvio
    ```

    **La regla:** `var` cuando el tipo se lee en la misma línea o es evidente. Si para saber qué tienes delante hay que ir a mirar una firma, escribe el tipo.

    Y lo que `var` **no** es: tipado dinámico. El tipo se fija al compilar.

    ```java
    var i = 0;
    i = "hola";     // no compila
    ```

### E15 ●● — `instanceof` con patrón

Reescribe esto sin castings.

```java
String describir(Object o) {
    if (o instanceof Jugador) {
        Jugador j = (Jugador) o;
        return j.nombre() + " #" + j.dorsal();
    } else if (o instanceof String) {
        String s = (String) o;
        return s.toUpperCase();
    }
    return "desconocido";
}
```

??? success "Solución"

    ```java
    String describir(Object o) {
        if (o instanceof Jugador j) {
            return "%s #%d".formatted(j.nombre(), j.dorsal());
        }
        if (o instanceof String s && !s.isBlank()) {
            return s.toUpperCase();
        }
        return "desconocido";
    }
    ```

    La variable del patrón **solo existe donde el patrón se cumple**, así que el compilador impide usarla fuera. Eso elimina el `ClassCastException` por construcción.

    Fíjate en el `&&`: a su derecha el patrón ya se ha cumplido, así que se puede seguir comprobando cosas sobre `s`.

    Con un `record` se puede además **desestructurar**:

    ```java
    if (o instanceof Jugador(String nombre, int dorsal, var pos, var puntos)) {
        return "%s #%d".formatted(nombre, dorsal);
    }
    ```

### E16 ●● — `switch` con patrones y `sealed`

Modela los eventos de un partido y calcula los puntos de cada uno.

??? success "Solución"

    ```java
    sealed interface Evento permits Canasta, Falta, Tiempo {}

    record Canasta(String jugador, int puntos) implements Evento {}
    record Falta(String jugador, boolean tecnica)  implements Evento {}
    record Tiempo(int minuto)                      implements Evento {}

    int puntosDe(Evento e) {
        return switch (e) {
            case Canasta(String jugador, int puntos) when puntos == 3 -> {
                IO.println("¡Triple de " + jugador + "!");
                yield 3;
            }
            case Canasta c -> c.puntos();
            case Falta f   -> 0;
            case Tiempo t  -> 0;
        };
    }
    ```

    Lo importante, y la razón de ser de `sealed`: **no hay `default`**. El compilador sabe que solo existen tres implementaciones y comprueba que están las tres.

    El día que añadas `record Cambio(...) implements Evento`, este `switch` **deja de compilar** y te obliga a decidir qué hacer. Con una cadena de `if` te enterarías en producción.

    `when` añade una condición al patrón, y `yield` devuelve el valor desde un caso con llaves.

### E17 ●● — Comparador con desempate

Ordena por puntos de mayor a menor y, a igualdad, por nombre alfabético.

??? success "Solución"

    ```java
    var orden = jugadores.stream()
            .sorted(Comparator.comparingInt(Jugador::puntos).reversed()
                              .thenComparing(Jugador::nombre))
            .toList();
    ```

    El error clásico es poner el `.reversed()` al final:

    ```java
    // MAL: invierte TODO, incluido el desempate por nombre
    comparing(Jugador::puntos).thenComparing(Jugador::nombre).reversed()
    ```

    `reversed()` invierte **el comparador construido hasta ese punto**. Por eso va pegado a lo que quieres invertir.

    Y `comparingInt` evita el autoboxing en cada comparación. Con 20 jugadores da igual; con 200.000 filas, no.

### E18 ●● — Partir en dos con `partitioningBy`

Separa los anotadores (más de 15 puntos) del resto y cuenta cuántos hay en cada grupo.

??? success "Solución"

    ```java
    Map<Boolean, List<Jugador>> grupos = jugadores.stream()
            .collect(Collectors.partitioningBy(j -> j.puntos() > 15));

    IO.println("Anotadores: " + grupos.get(true).size());
    IO.println("Resto:      " + grupos.get(false).size());
    ```

    Y si solo quieres el recuento:

    ```java
    Map<Boolean, Long> conteo = jugadores.stream()
            .collect(Collectors.partitioningBy(j -> j.puntos() > 15, Collectors.counting()));
    ```

    **Frente a `groupingBy`:** `partitioningBy` siempre devuelve **las dos claves**, aunque un grupo esté vacío. `groupingBy` no crearía la clave del grupo vacío y el `get` daría `null`.

### E19 ●●● — Dos cuentas en una pasada con `teeing`

Calcula en **un solo recorrido** la media de puntos y el nombre del máximo anotador.

??? success "Solución"

    ```java
    record Resumen(double media, String maximo) {}

    Resumen r = jugadores.stream().collect(Collectors.teeing(
            Collectors.averagingInt(Jugador::puntos),
            Collectors.maxBy(Comparator.comparingInt(Jugador::puntos)),
            (media, mejor) -> new Resumen(media, mejor.map(Jugador::nombre).orElse("—"))));
    ```

    `teeing` aplica **dos recolectores a la vez** sobre el mismo flujo y combina los resultados. Sin él harían falta dos pasadas.

    Si lo que quieres son estadísticas numéricas, hay algo más simple todavía:

    ```java
    IntSummaryStatistics est = jugadores.stream()
            .mapToInt(Jugador::puntos)
            .summaryStatistics();
    // getCount(), getSum(), getMin(), getMax(), getAverage()
    ```

### E20 ●● — `Optional` encadenado

Dado el dorsal, devuelve el nombre en mayúsculas, o `"SIN ASIGNAR"` si no existe o está en blanco.

??? success "Solución"

    ```java
    String nombrePorDorsal(int dorsal) {
        return jugadores.stream()
                .filter(j -> j.dorsal() == dorsal)
                .findFirst()
                .map(Jugador::nombre)
                .filter(n -> !n.isBlank())
                .map(String::toUpperCase)
                .orElse("SIN ASIGNAR");
    }
    ```

    **Lo que no se hace nunca:**

    ```java
    if (opt.isPresent()) { return opt.get().toUpperCase(); } else { return "SIN ASIGNAR"; }
    ```

    Funciona, y es escribir con `Optional` exactamente el mismo `if (x != null)` de siempre. `Optional` está para encadenar.

    `map` frente a `flatMap`: si la función ya devuelve un `Optional`, es `flatMap`; si no, te quedas con un `Optional<Optional<T>>`.

### E21 ●● — Varios recursos en un `try`

Copia las líneas de un fichero a otro en mayúsculas, cerrando los dos recursos.

??? success "Solución"

    ```java
    void copiarEnMayusculas(Path origen, Path destino) throws IOException {
        try (var lineas = Files.lines(origen, StandardCharsets.UTF_8);
             var salida = Files.newBufferedWriter(destino, StandardCharsets.UTF_8)) {

            lineas.map(String::toUpperCase).forEach(l -> {
                try {
                    salida.write(l);
                    salida.newLine();
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
    }
    ```

    1. **Se cierran en orden inverso** al de apertura, y se cierran **todos** aunque uno falle al cerrarse.
    2. El `try/catch` de dentro del `forEach` es feo pero obligatorio: una lambda no puede propagar una excepción comprobada.

    Si eso te molesta, un bucle lo evita:

    ```java
    for (var l : (Iterable<String>) lineas::iterator) {
        salida.write(l.toUpperCase());
        salida.newLine();
    }
    ```

### E22 ●●● — Envolver una excepción sin perder la causa

`cargarPlantilla` falla con `IOException` y quien la llama no debería enterarse de que hay ficheros por medio.

??? success "Solución"

    ```java
    public class PlantillaNoDisponibleException extends RuntimeException {
        public PlantillaNoDisponibleException(String mensaje, Throwable causa) {
            super(mensaje, causa);          // <- la causa, SIEMPRE
        }
    }

    List<Jugador> cargarPlantilla(Path fichero) {
        try {
            return leer(fichero);
        } catch (IOException e) {
            throw new PlantillaNoDisponibleException(
                    "No se ha podido cargar la plantilla desde " + fichero, e);
        }
    }
    ```

    **Los dos errores típicos:**

    ```java
    catch (IOException e) { }                       // 1. tragársela
    catch (IOException e) { e.printStackTrace(); }  // 2. imprimirla y seguir
    ```

    El primero hace que el fallo desaparezca y el programa continúe con datos a medias. El segundo ensucia la salida y tampoco detiene nada.

    Y el tercero, más sutil: **perder la causa**. Sin la `e`, la traza se corta ahí y pierdes la línea del fallo original. Pasarla cuesta cuatro caracteres.

### E23 ●● — Inmutabilidad de verdad

Este `Equipo` parece inmutable y no lo es. Encuentra el fallo.

```java
public final class Equipo {
    private final String nombre;
    private final List<Jugador> jugadores;

    public Equipo(String nombre, List<Jugador> jugadores) {
        this.nombre = nombre;
        this.jugadores = jugadores;
    }
    public List<Jugador> jugadores() { return jugadores; }
}
```

??? success "Solución"

    Hay **dos** fugas, una en cada dirección:

    ```java
    var lista = new ArrayList<>(List.of(ana, bruno));
    var e = new Equipo("Rayo", lista);

    lista.add(carla);          // 1. por el constructor
    e.jugadores().clear();     // 2. por el getter
    ```

    El `final` protege la **referencia**, no el contenido de la lista.

    ```java
    public Equipo(String nombre, List<Jugador> jugadores) {
        this.nombre = nombre;
        this.jugadores = List.copyOf(jugadores);   // copia inmutable
    }
    ```

    `List.copyOf` hace las dos cosas: copia —así el `add` de fuera ya no le afecta— y devuelve una lista **inmutable**, con lo que el `clear()` lanza `UnsupportedOperationException`.

    Ojo: **no admite nulos**. Si la lista puede traerlos, fíltralos antes.

    Con un `record` pasa lo mismo, y se arregla en el constructor compacto:

    ```java
    record Equipo(String nombre, List<Jugador> jugadores) {
        Equipo { jugadores = List.copyOf(jugadores); }
    }
    ```

### E24 ●● — `enum` con datos y comportamiento

Modela las posiciones con su número de camiseta y un método que diga si es exterior.

??? success "Solución"

    ```java
    enum Posicion {
        BASE(1, true), ESCOLTA(2, true), ALERO(3, true),
        ALA_PIVOT(4, false), PIVOT(5, false);

        private final int numero;
        private final boolean exterior;

        Posicion(int numero, boolean exterior) {
            this.numero = numero;
            this.exterior = exterior;
        }

        public int numero()       { return numero; }
        public boolean exterior() { return exterior; }

        public static Posicion porNumero(int n) {
            return Arrays.stream(values())
                    .filter(p -> p.numero == n)
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Posición " + n));
        }
    }
    ```

    Un `enum` en Java **es una clase**: puede tener campos, constructor y métodos. Eso permite meter el dato donde vive el concepto, en vez de repartirlo en un `switch` por cada consulta.

    `values()` devuelve un **array nuevo cada vez**, así que dentro de un bucle conviene guardarlo en una variable.

### E25 ●●● — Tu propia interfaz funcional

Escribe un método `aplicarA` que reciba una lista y una transformación. Sin usar `Function`.

??? success "Solución"

    ```java
    @FunctionalInterface
    interface Transformacion<E, S> {
        S aplicar(E entrada);

        default <T> Transformacion<E, T> luego(Transformacion<S, T> siguiente) {
            return e -> siguiente.aplicar(this.aplicar(e));
        }
    }

    <E, S> List<S> aplicarA(List<E> lista, Transformacion<E, S> t) {
        var salida = new ArrayList<S>(lista.size());
        for (var e : lista) {
            salida.add(t.aplicar(e));
        }
        return List.copyOf(salida);
    }
    ```

    Y se usa igual que las del JDK:

    ```java
    Transformacion<Jugador, String> aNombre = Jugador::nombre;
    Transformacion<Jugador, String> aGritar = aNombre.luego(String::toUpperCase);

    List<String> nombres = aplicarA(jugadores, aGritar);
    ```

    `@FunctionalInterface` no es obligatoria, pero **hace que el compilador avise** si alguien añade un segundo método abstracto y rompe todas las lambdas.

    Lo que hay que llevarse de aquí: una lambda no es magia, es **una implementación de una interfaz de un solo método**.

### E26 ●● — Modificar mientras recorres

Elimina los jugadores con menos de 5 puntos. Escribe la versión que falla y la que funciona.

??? success "Solución"

    ```java
    // FALLA: ConcurrentModificationException
    for (var j : jugadores) {
        if (j.puntos() < 5) jugadores.remove(j);
    }
    ```

    El `for-each` usa un `Iterator` por debajo, y el `remove` de la lista lo deja obsoleto. La excepción salta en el siguiente `next()`.

    **Tres formas correctas**, de mejor a peor:

    ```java
    jugadores.removeIf(j -> j.puntos() < 5);            // 1. la que se usa

    var it = jugadores.iterator();                      // 2. con más control
    while (it.hasNext()) {
        if (it.next().puntos() < 5) it.remove();        // remove DEL ITERADOR
    }

    var filtrados = jugadores.stream()                  // 3. sin mutar nada
            .filter(j -> j.puntos() >= 5).toList();
    ```

    Y un aviso: **la lista tiene que ser mutable**. Sobre una `List.of(...)`, las dos primeras lanzan `UnsupportedOperationException`.

### E27 ●●● — Refactor completo a Java 25

Moderniza este método. Está escrito como en 2010.

```java
public String informe(List<Jugador> jugadores) {
    String salida = "";
    int total = 0;
    Jugador mejor = null;
    for (int i = 0; i < jugadores.size(); i++) {
        Jugador j = jugadores.get(i);
        if (j != null && j.getPuntos() > 0) {
            total = total + j.getPuntos();
            if (mejor == null || j.getPuntos() > mejor.getPuntos()) {
                mejor = j;
            }
            salida = salida + j.getNombre() + ": " + j.getPuntos() + "\n";
        }
    }
    if (mejor != null) {
        salida = salida + "Mejor: " + mejor.getNombre();
    } else {
        salida = salida + "Sin datos";
    }
    return salida;
}
```

??? success "Solución"

    ```java
    String informe(List<Jugador> jugadores) {
        var anotadores = jugadores.stream()
                .filter(Objects::nonNull)
                .filter(j -> j.puntos() > 0)
                .toList();

        var lineas = anotadores.stream()
                .map(j -> "%s: %d".formatted(j.nombre(), j.puntos()))
                .collect(Collectors.joining("\n"));

        var mejor = anotadores.stream()
                .max(Comparator.comparingInt(Jugador::puntos))
                .map(j -> "Mejor: " + j.nombre())
                .orElse("Sin datos");

        return lineas.isEmpty() ? mejor : lineas + "\n" + mejor;
    }
    ```

    | Antes | Ahora | Por qué |
    |---|---|---|
    | `salida = salida + ...` dentro del bucle | `Collectors.joining` | Cada `+` crea un `String` nuevo. Con 10.000 filas se nota |
    | `for` con índice | `stream` | Se lee **qué** hace, no **cómo** recorre |
    | `mejor == null` como centinela | `max(...)` y `Optional` | El `null` deja de existir |
    | `j != null` disperso | `filter(Objects::nonNull)` | Una línea, y al principio |
    | `getPuntos()` | `puntos()` | Es un `record` |
    | `+` para formatear | `formatted` | Se lee la plantilla de un vistazo |

    **Lo que NO hay que hacer** es convertirlo todo en un único *stream* gigante. Tres pasos con nombre —`anotadores`, `lineas`, `mejor`— se leen muchísimo mejor que quince operaciones encadenadas.

---

## Taller largo: ocho programas completos

!!! reto "Del E28 al E35 se entrega un programa que funciona"
    Los anteriores son fragmentos; estos son **programas enteros**, uno por tema. Son lo que antes iban como «prácticas guiadas»: ahora están aquí, con su solución, porque no tenía sentido tenerlos en dos sitios.

    Todos se ejecutan con `java Fichero.java`, sin proyecto ni configuración.

### E28 ● — Entorno y primer programa

Comprueba que `java -version` dice 25.x. Escribe `Hola.java` que salude con tu nombre e imprima la fecha. Después **rómpelo**: quita un `;`, ejecuta y anota qué dice el compilador.

??? success "Solución"

    ```java
    import java.time.LocalDate;

    void main() {
        var nombre = "Iván";
        IO.println("Hola, " + nombre + ". Hoy es " + LocalDate.now());
    }
    ```

    Al quitar el `;` sale algo así:

    ```
    Hola.java:5: error: ';' expected
        IO.println("Hola, " + nombre)
                                    ^
    1 error
    ```

    El compilador te da **fichero, línea, columna y motivo**. Lo único que hay que aprender aquí es a leerlo en vez de asustarse: el 90 % de los errores de compilación se arreglan mirando la línea que señala, o la de arriba.

    Ojo con eso último: cuando falta un `;`, el compilador señala **la línea siguiente**, porque es donde se da cuenta.

### E29 ● — Calculadora de precios

Dado un precio base y una cantidad, calcula subtotal, descuento del 10 % si se compran más de 5 unidades, IVA del 21 % y total. Muestra cada paso.

??? success "Solución"

    ```java
    void main() {
        var precioBase = 20.0;          // (1)
        var cantidad = 8;

        var subtotal = precioBase * cantidad;
        var descuento = cantidad > 5 ? subtotal * 0.10 : 0.0;
        var base = subtotal - descuento;
        var iva = base * 0.21;
        var total = base + iva;

        IO.println("Subtotal:  " + subtotal);
        IO.println("Descuento: " + descuento);
        IO.println("IVA:       " + iva);
        IO.println("TOTAL:     " + total);
    }
    ```

    1.  `20.0` y no `20`. Con `var`, el literal decide el tipo: `20` sería `int`.

    La trampa de este ejercicio es la **división entera**. Si en algún punto escribes `cantidad / 5` con los dos enteros, Java descarta los decimales sin avisar:

    ```java
    IO.println(9 / 5);      // 1, no 1.8
    IO.println(9 / 5.0);    // 1.8
    ```

    Y el aviso que vale para siempre: **para dinero de verdad no se usa `double`**, se usa `BigDecimal`. `0.1 + 0.2` en `double` da `0.30000000000000004`. En la UT3 se ve por qué y cómo se hace bien.

### E30 ● — Clasificador de códigos HTTP

Dado un código de estado, imprime su significado y de quién es la culpa. Con `switch` de flecha, como expresión.

??? success "Solución"

    ```java
    void main() {
        var codigo = 404;

        var significado = switch (codigo) {
            case 200 -> "OK";
            case 201 -> "Created";
            case 204 -> "No Content";
            case 400 -> "Bad Request";
            case 401 -> "Unauthorized (no autenticado)";
            case 403 -> "Forbidden (sin permiso)";
            case 404 -> "Not Found";
            case 409 -> "Conflict";
            case 415 -> "Unsupported Media Type";
            case 500 -> "Internal Server Error";
            default  -> "Desconocido";
        };

        var culpa = switch (codigo / 100) {
            case 2 -> "Éxito";
            case 3 -> "Redirección";
            case 4 -> "Error del CLIENTE";
            case 5 -> "Error del SERVIDOR";
            default -> "?";
        };

        IO.println(codigo + " " + significado + " → " + culpa);
    }
    ```

    Lo bonito: `codigo / 100`. La división entera, que en el E29 era una trampa, aquí es la solución — `404 / 100` da `4`.

    Y las tres diferencias con el `switch` de siempre:

    | Antiguo | Moderno |
    |---|---|
    | Hace falta `break` | No: no hay caída entre casos |
    | Es una sentencia | **Es una expresión**: devuelve valor |
    | Un valor por `case` | Varios: `case 200, 201, 204 ->` |

### E31 ●● — Catálogo con records

Crea un `record Producto(String nombre, String categoria, double precio)` que valide en el constructor y tenga `precioConIva()`. Monta una lista de cinco e imprímelos.

??? success "Solución"

    ```java
    import java.util.List;

    record Producto(String nombre, String categoria, double precio) {
        Producto {                                            // (1)
            if (precio < 0) {
                throw new IllegalArgumentException("Precio negativo: " + precio);
            }
            if (nombre == null || nombre.isBlank()) {
                throw new IllegalArgumentException("Nombre vacío");
            }
        }

        double precioConIva() { return precio * 1.21; }
    }

    void main() {
        var catalogo = List.of(
            new Producto("Patinete", "movilidad", 120.0),
            new Producto("Casco", "seguridad", 35.0),
            new Producto("Bici", "movilidad", 450.0),
            new Producto("Candado", "seguridad", 25.0),
            new Producto("Luces", "seguridad", 15.0));

        for (var p : catalogo) {
            IO.println("%-10s %7.2f €  (IVA inc.: %7.2f €)"
                    .formatted(p.nombre(), p.precio(), p.precioConIva()));
        }
    }
    ```

    1.  **Constructor compacto**: sin paréntesis ni parámetros. Se ejecuta antes de asignar los campos, así que ahí es donde va la validación.

    Un `record` te da gratis: constructor, *getters* con el nombre del campo (`p.nombre()`, no `getNombre()`), `equals`, `hashCode` y `toString`. Y los campos son **finales**: no hay `setNombre`.

    Cuándo **no** usar un record: cuando el objeto tiene que cambiar de estado. Un `Pedido` que pasa de PENDIENTE a ENVIADO no es un record.

### E32 ●● — Métodos de pago

Define `MetodoPago` con `cobrar(double)`. Impleméntalo con `Tarjeta`, `Bizum` y `Transferencia`. Escribe `procesarCompra` de forma que funcione con cualquiera.

??? success "Solución"

    ```java
    interface MetodoPago {
        void cobrar(double importe);
    }

    class Tarjeta implements MetodoPago {
        @Override public void cobrar(double i) { IO.println("Con tarjeta: " + i + " €"); }
    }
    class Bizum implements MetodoPago {
        @Override public void cobrar(double i) { IO.println("Bizum de " + i + " €"); }
    }
    class Transferencia implements MetodoPago {
        @Override public void cobrar(double i) { IO.println("Transferencia de " + i + " €"); }
    }

    void procesarCompra(MetodoPago metodo, double importe) {
        IO.println("Procesando compra…");
        metodo.cobrar(importe);          // (1)
    }

    void main() {
        procesarCompra(new Tarjeta(), 50);
        procesarCompra(new Bizum(), 30);
        procesarCompra(new Transferencia(), 200);
    }
    ```

    1.  No sabe **ni le importa** cuál es. Eso es polimorfismo.

    Lo que hay que ver: `procesarCompra` depende de la **interfaz**, no de las clases concretas. Por eso el día que aparezca `PagoCripto` no hay que tocar una línea de `procesarCompra`.

    Es exactamente la **inversión de dependencias** (la *D* de SOLID), hecha a mano. En la UT4, Spring hace este mismo `new` por ti y te entrega la implementación que toque: se llama inyección de dependencias, y ahora ya sabes qué problema resuelve.

### E33 ●●● — Informe de ventas

Con el catálogo del E31, saca en cinco líneas: (1) los nombres de más de 100 € · (2) el total · (3) el más caro · (4) agrupados por categoría · (5) el precio medio por categoría.

??? success "Solución"

    ```java
    import java.util.*;
    import java.util.stream.*;

    void main() {
        var catalogo = List.of(
            new Producto("Patinete", "movilidad", 120.0),
            new Producto("Casco", "seguridad", 35.0),
            new Producto("Bici", "movilidad", 450.0),
            new Producto("Candado", "seguridad", 25.0),
            new Producto("Luces", "seguridad", 15.0));

        IO.println(catalogo.stream()
                .filter(p -> p.precio() > 100)
                .map(Producto::nombre)
                .toList());                                   // [Patinete, Bici]

        IO.println(catalogo.stream()
                .mapToDouble(Producto::precio).sum());        // 645.0

        IO.println(catalogo.stream()
                .max(Comparator.comparingDouble(Producto::precio))
                .map(Producto::nombre).orElse("—"));          // Bici

        IO.println(catalogo.stream()
                .collect(Collectors.groupingBy(Producto::categoria)));

        IO.println(catalogo.stream()
                .collect(Collectors.groupingBy(Producto::categoria,
                         Collectors.averagingDouble(Producto::precio))));
        // {movilidad=285.0, seguridad=25.0}
    }
    ```

    Tres cosas que se preguntan:

    - `max` devuelve **`Optional`**, porque la lista podría estar vacía. Por eso hace falta `.map(...).orElse(...)`.
    - `groupingBy` devuelve un **`HashMap`**: el orden de las claves **no** está garantizado. Si lo necesitas ordenado, `groupingBy(clave, TreeMap::new, downstream)`.
    - `mapToDouble(...).sum()` evita el autoboxing de `reduce`. Con cinco productos da igual; con doscientos mil, no.

### E34 ●●● — Repositorio robusto

Escribe `CatalogoRepositorio` con `Optional<Producto> buscarPorNombre(String)` y `Producto obtenerObligatorio(String)`, que lance una excepción propia si no existe. Pruébalo con uno que exista y otro que no.

??? success "Solución"

    ```java
    import java.util.*;

    class ProductoNoEncontradoException extends RuntimeException {
        ProductoNoEncontradoException(String nombre) {
            super("No existe el producto: " + nombre);
        }
    }

    class CatalogoRepositorio {
        private final List<Producto> productos;

        CatalogoRepositorio(List<Producto> productos) {
            this.productos = List.copyOf(productos);          // (1)
        }

        Optional<Producto> buscarPorNombre(String nombre) {
            return productos.stream()
                    .filter(p -> p.nombre().equalsIgnoreCase(nombre))
                    .findFirst();
        }

        Producto obtenerObligatorio(String nombre) {
            return buscarPorNombre(nombre)
                    .orElseThrow(() -> new ProductoNoEncontradoException(nombre));
        }
    }

    void main() {
        var repo = new CatalogoRepositorio(List.of(
                new Producto("Patinete", "movilidad", 120.0)));

        IO.println(repo.buscarPorNombre("Patinete").map(Producto::precio).orElse(0.0));
        IO.println(repo.buscarPorNombre("Moto").isPresent());   // false

        try {
            repo.obtenerObligatorio("Moto");
        } catch (ProductoNoEncontradoException e) {
            IO.println("Capturada: " + e.getMessage());
        }
    }
    ```

    1.  Copia inmutable, por lo del E23: si guardas la lista que te dan, quien la creó puede seguir modificándola.

    **Por qué dos métodos y no uno.** Son dos situaciones distintas:

    | Método | Cuándo | Qué dice |
    |---|---|---|
    | `buscarPorNombre` | Puede no estar, y es normal | Devuelve `Optional` |
    | `obtenerObligatorio` | Si no está, es un error | Lanza |

    Un repositorio que devuelve `null` obliga a quien lo llama a acordarse de comprobarlo. `Optional` lo obliga el compilador.

    Y la excepción de dominio no es decoración: en la UT4 se traduce automáticamente a un **404** sin que el servicio sepa nada de HTTP.

### E35 ●●● — Tu primer test

Para una `CalculadoraPrecios` con la regla «10 % de descuento desde 6 unidades», escribe tres tests: caso normal, caso con descuento y caso de error.

??? success "Solución"

    ```java
    import org.junit.jupiter.api.Test;
    import static org.junit.jupiter.api.Assertions.*;

    class CalculadoraPreciosTest {

        private final CalculadoraPrecios calculadora = new CalculadoraPrecios();

        @Test
        void noAplicaDescuentoConMenosDeSeisUnidades() {
            assertEquals(40.0, calculadora.total(2, 20.0), 0.001);   // (1)
        }

        @Test
        void aplicaDiezPorCientoDesdeSeisUnidades() {
            assertEquals(180.0, calculadora.total(10, 20.0), 0.001);
        }

        @Test
        void rechazaCantidadNegativa() {
            assertThrows(IllegalArgumentException.class,
                    () -> calculadora.total(-1, 20.0));
        }
    }
    ```

    1.  El tercer parámetro es la **tolerancia**. Comparar `double` con `==` falla por redondeo: `assertEquals(0.3, 0.1 + 0.2)` no pasa.

    Lo que se evalúa en este ejercicio no es la sintaxis de JUnit, son **tres decisiones**:

    - **Los nombres describen la regla de negocio**, no el método. Si falla `aplicaDiezPorCientoDesdeSeisUnidades`, ya sabes qué se rompió sin abrir el código. `test1` no dice nada.
    - **Hay un caso de error.** Un conjunto de tests que solo prueba el camino feliz no prueba casi nada.
    - **Está el límite.** Con 6 unidades exactas, ¿hay descuento? Los fallos viven en los bordes: prueba 5, 6 y 7.

    La estructura de los tres es la misma, y se llama **AAA**: *Arrange* (preparar), *Act* (ejecutar), *Assert* (comprobar).

---

## Reparto sugerido

| Ejercicio | Nivel | Sesión | Encaje |
|---|:-:|:-:|---|
| E1 · Tipos y conversiones | ● | S2 | La división entera |
| E2 · `switch` moderno | ● | S2 | |
| E3 · `record` | ●● | S4 | Prepara la UT5 |
| E4 · `equals`/`hashCode` | ●● | S4 | **Reaparece en la UT5** |
| E5 · Elegir colección | ●● | S6 | |
| E6 · Agrupar y contar | ●●● | S7 | **Reaparece en la UT3** |
| E7 · Estadísticas | ●●● | S7 | |
| E8 · `map` / `flatMap` | ●●● | S7 | El que más se falla |
| E9 · Excepciones propias | ●● | S8 | Prepara la UT4 |
| E10 · `Optional` | ●● | S8 | |
| E11 · Bucles a streams | ●●● | S8 | |
| E12 · Maven y tests | ●●● | S9 | Prepara todo el curso |
| **E13 · Bloques de texto** | ● | S3 | Reaparece en la UT6 con JSON |
| **E14 · `var` bien y mal** | ● | S3 | Criterio, no sintaxis |
| **E15 · `instanceof` con patrón** | ●● | S5 | |
| **E16 · `switch` y `sealed`** | ●● | S5 | Lo más moderno de la unidad |
| **E17 · Comparador con desempate** | ●● | S6 | El `.reversed()` mal puesto |
| **E18 · `partitioningBy`** | ●● | S7 | |
| **E19 · `teeing`** | ●●● | S7 | Una pasada, dos cuentas |
| **E20 · `Optional` encadenado** | ●● | S8 | Sin `isPresent` |
| **E21 · Varios recursos** | ●● | S8 | **Reaparece en la UT3** |
| **E22 · Envolver excepciones** | ●●● | S8 | Prepara la UT4 |
| **E23 · Inmutabilidad de verdad** | ●● | S4 | La fuga por el *getter* |
| **E24 · `enum` con datos** | ●● | S5 | |
| **E25 · Interfaz funcional propia** | ●●● | S8 | Explica qué es una lambda |
| **E26 · Modificar al recorrer** | ●● | S6 | Cae en el examen |
| **E27 · Refactor a Java 25** | ●●● | S9 | Cierra la unidad |
| **E28 · Entorno y primer programa** | ● | S1 | Leer al compilador |
| **E29 · Calculadora de precios** | ● | S2 | La división entera |
| **E30 · Clasificador HTTP** | ● | S2 | Enlaza con la UT1 |
| **E31 · Catálogo con records** | ●● | S4 | |
| **E32 · Métodos de pago** | ●● | S5 | **Prepara la UT4** |
| **E33 · Informe de ventas** | ●●● | S7 | |
| **E34 · Repositorio robusto** | ●●● | S8 | **Prepara la UT3 y la UT4** |
| **E35 · Tu primer test** | ●●● | S9 | El patrón de todo el curso |

!!! tip "Los doce primeros son el núcleo"
    Del **E13 al E27** es ampliación; del **E28 al E35** son programas completos, uno por tema. En un grupo que va rodado se hacen en clase; en uno que va justo, quedan como refuerzo guiado. El **E27** conviene hacerlo siempre: es la unidad entera en un solo ejercicio.

---

## Del ejercicio a la pregunta de test

El examen de esta unidad son **30 preguntas**, y la mayoría son **preguntas de código**: se da un fragmento y hay que decir qué imprime, si compila o dónde está el fallo ([simulacro aquí](autoevaluacion.md)).

| Ejercicios | Preguntas | Qué se pregunta |
|---|:-:|---|
| **E1–E2 · E13–E14** · Sintaxis y tipos | 5 | La división entera; `var` y qué tipo infiere; bloques de texto y la sangría |
| **E3–E4 · E23–E24** · POO y `record` | 6 | Qué genera un `record`; `equals` sin `hashCode`; la lista que se cuela por el getter; `enum` con campos |
| **E15–E16** · Patrones | 4 | `instanceof` con patrón y su ámbito; por qué un `switch` sobre `sealed` no lleva `default` |
| **E5–E8 · E17–E19 · E26** · Colecciones y streams | 9 | Qué imprime un `HashMap` frente a un `TreeMap`; `map` o `flatMap`; dónde va el `.reversed()`; `ConcurrentModificationException` |
| **E9–E10 · E20–E22** · Excepciones y `Optional` | 6 | Qué se imprime al tragarse una excepción; orden de cierre en el *try*; `orElse` frente a `orElseThrow` |

!!! reto "Las tres costumbres que transfieren"
    En esta unidad el efecto es todavía más directo que en la UT1, porque las preguntas **son** fragmentos de código como los de los ejercicios.

    1. **Predice antes de ejecutar.** Antes de darle a *Run*, escribe en un papel qué va a imprimir. Si aciertas, lo entendiste; si no, acabas de encontrar tu hueco. Es exactamente el tipo de pregunta «¿qué imprime?».

    2. **Rompe tu propia solución.** Cuando un ejercicio te salga bien, quítale el `hashCode`, mueve el `.reversed()` al final, cambia el `TreeMap` por un `HashMap`, borra la causa del `throw`. Ejecuta y **apunta qué cambia**. Eso es el tipo «¿por qué falla?».

    3. **Escribe tú la pregunta.** Coge un ejercicio resuelto e invéntate cuatro opciones. Los tres distractores te obligan a saber por qué alguien se equivocaría, que es justo lo que se pregunta.

    Dos minutos por ejercicio. Con los veintisiete son menos de una hora en toda la unidad, repartida — y rinde más que cualquier repaso de la víspera.
