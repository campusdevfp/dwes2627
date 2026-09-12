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
