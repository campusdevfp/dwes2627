# Fechas y validación de datos

> Dos cosas que aparecen en **todas** las aplicaciones y que se hacen mal constantemente: manejar fechas y comprobar que los datos que llegan son correctos.

## 1. Fechas con `java.time`

Olvida `Date` y `Calendar` (API antigua, mutable y confusa). Desde Java 8 se usa `java.time`, cuyas clases son **inmutables** y legibles.

| Clase | Para qué | Ejemplo |
|---|---|---|
| `LocalDate` | Solo fecha | `2026-04-12` (fecha de nacimiento) |
| `LocalTime` | Solo hora | `14:30` (hora de apertura) |
| `LocalDateTime` | Fecha y hora, sin zona | `2026-04-12T14:30` |
| `Instant` | Momento exacto UTC | Marca de tiempo de un log |
| `Duration` | Tiempo entre instantes | 90 minutos |
| `Period` | Tiempo entre fechas | 2 años, 3 meses |

```java
import java.time.*;
import java.time.format.DateTimeFormatter;

var hoy = LocalDate.now();
var cumple = LocalDate.of(2007, 3, 15);
var ahora = LocalDateTime.now();

// Operar: devuelven un objeto NUEVO (son inmutables)
var manana   = hoy.plusDays(1);
var haceUnAno = hoy.minusYears(1);

// Comparar
IO.println(cumple.isBefore(hoy));            // true
IO.println(hoy.getDayOfWeek());              // MONDAY...

// Diferencia
var edad = Period.between(cumple, hoy).getYears();
var dias = java.time.temporal.ChronoUnit.DAYS.between(cumple, hoy);
```

**Formatear y parsear:**

```java
var formato = DateTimeFormatter.ofPattern("dd/MM/yyyy");
IO.println(hoy.format(formato));                          // 12/04/2026
var fecha = LocalDate.parse("12/04/2026", formato);

// ISO-8601 por defecto (el estándar para APIs)
IO.println(hoy.toString());                               // 2026-04-12
var iso = LocalDate.parse("2026-04-12");
```

!!! bug "Zonas horarias: el clásico bug de producción"
    `LocalDateTime` **no tiene zona**: "las 14:30" ¿de dónde? Si tu servidor está en Frankfurt y el usuario en Madrid, la misma hora significa cosas distintas.
    **Regla profesional:** guarda siempre en **UTC** (`Instant`) y convierte a la zona del usuario solo al mostrarlo.

    ```
    var instante = Instant.now();                                  // UTC, para la BD
    var enMadrid = instante.atZone(ZoneId.of("Europe/Madrid"));    // para mostrar
    ```

## 2. Validar datos de entrada

Recuerda la regla de oro de la UT1: **nunca confíes en lo que llega**. Toda entrada (formulario, API, fichero) se valida.

**Validación básica:**

```java
record Usuario(String nombre, String email, int edad) {
    Usuario {
        if (nombre == null || nombre.isBlank())
            throw new IllegalArgumentException("El nombre es obligatorio");
        if (edad < 0 || edad > 130)
            throw new IllegalArgumentException("Edad fuera de rango: " + edad);
        if (email == null || !email.contains("@"))
            throw new IllegalArgumentException("Email no válido: " + email);
    }
}
```

Validar en el **constructor compacto** del record garantiza que **no puede existir un objeto inválido**. Es la técnica que más bugs evita.

**Acumular errores** (mejor que fallar en el primero, para avisar al usuario de todo a la vez):

```java
List<String> validar(Usuario u) {
    var errores = new ArrayList<String>();
    if (u.nombre() == null || u.nombre().isBlank()) errores.add("nombre obligatorio");
    if (u.edad() < 18)                              errores.add("debe ser mayor de edad");
    if (!u.email().contains("@"))                   errores.add("email no válido");
    return errores;
}
```

> En la UT4 esto se hará con anotaciones (`@NotBlank`, `@Min`, `@Email`) y Spring lo validará solo. Pero la idea es esta.

## 3. Expresiones regulares

Una **regex** describe un patrón de texto. Para validaciones de formato son imbatibles:

```java
// Comprobación rápida
var esEmail = email.matches("^[\\w.+-]+@[\\w-]+\\.[\\w.]{2,}$");

// Reutilizando el patrón (más eficiente si se usa mucho)
import java.util.regex.Pattern;
static final Pattern DNI = Pattern.compile("^\\d{8}[A-HJ-NP-TV-Z]$");
var esDni = DNI.matcher(texto).matches();
```

Los símbolos que cubren casi todo:

| Símbolo | Significa | Ejemplo |
|---|---|---|
| `\d` | Un dígito | `\d{8}` → 8 dígitos |
| `\w` | Letra, dígito o `_` |  |
| `.` | Cualquier carácter |  |
| `+` | Uno o más | `\d+` → uno o más dígitos |
| `*` | Cero o más |  |
| `?` | Opcional |  |
| `{n,m}` | Entre n y m veces | `\d{2,4}` |
| `[abc]` | Uno de esos | `[A-Z]` → mayúscula |
| `^` `$` | Principio y fin | Ancla la coincidencia completa |
| `\\|` | O | `(gato\\|perro)` |

**Extraer** partes con grupos:

```java
var m = Pattern.compile("(\\d{2})/(\\d{2})/(\\d{4})").matcher("12/04/2026");
if (m.matches()) {
    IO.println("Día: " + m.group(1) + " Mes: " + m.group(2) + " Año: " + m.group(3));
}
```

!!! tip "No te pases con las regex"
    Una regex "perfecta" para emails ocupa 400 caracteres y sigue sin ser correcta. Para validar un email de verdad, se envía un correo de confirmación. La regex solo filtra las erratas evidentes. Y recuerda: **en Java hay que escapar la barra** (`\\d`, no `\d`).

## 4. Aritmética y periodos

``` { .java .numerado }
var alta = LocalDate.of(2026, 1, 15);
var hoy  = LocalDate.now();

// Sumar y restar (devuelven un objeto NUEVO: son inmutables)
var vencimiento = alta.plusDays(30).plusMonths(1);
var anteayer    = hoy.minusDays(2);

// Distancia entre fechas
long dias   = ChronoUnit.DAYS.between(alta, hoy);
long meses  = ChronoUnit.MONTHS.between(alta, hoy);
var periodo = Period.between(alta, hoy);       // 3 años, 2 meses, 5 días
IO.println("%d años, %d meses, %d días".formatted(
    periodo.getYears(), periodo.getMonths(), periodo.getDays()));

// Entre instantes
var duracion = Duration.between(inicio, fin);
IO.println(duracion.toMinutes() + " min");

// Comparar
alta.isBefore(hoy);  alta.isAfter(hoy);  alta.isEqual(hoy);

// Ajustes útiles
hoy.withDayOfMonth(1);                                  // primer día del mes
hoy.with(TemporalAdjusters.lastDayOfMonth());           // último día del mes
hoy.with(TemporalAdjusters.next(DayOfWeek.MONDAY));     // próximo lunes
```

!!! warning "Las fechas son inmutables"
    ```
    fecha.plusDays(1);            // MAL  no hace nada: el resultado se pierde
    fecha = fecha.plusDays(1);    // BIEN hay que reasignar
    ```

    Es exactamente el mismo despiste que con `String.trim()`. Todo `java.time` es inmutable, y eso es bueno: una fecha nunca cambia a tus espaldas.

### Zonas horarias

```java
var local  = LocalDateTime.now();                        // sin zona: ambiguo
var madrid = ZonedDateTime.now(ZoneId.of("Europe/Madrid"));
var utc    = Instant.now();                              // el instante absoluto

// Convertir
var enMadrid = utc.atZone(ZoneId.of("Europe/Madrid"));
var aUtc     = madrid.toInstant();
```

**Regla profesional:** guarda y transmite en **UTC** (`Instant`), y convierte a la zona del usuario solo al mostrar. Si guardas `LocalDateTime` sin zona, el día del cambio de hora tendrás registros duplicados o inexistentes, y con usuarios en varios países no sabrás qué hora es «las 14:00».

### Formatear y parsear

```java
var f = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
String texto = LocalDateTime.now().format(f);
var vuelta   = LocalDateTime.parse("15/03/2026 14:30", f);

// ISO-8601: el estándar de las APIs
LocalDate.parse("2026-03-15");
Instant.parse("2026-03-15T14:30:00Z");

// Con idioma
var largo = DateTimeFormatter.ofPattern("EEEE d 'de' MMMM 'de' yyyy", new Locale("es","ES"));
IO.println(LocalDate.now().format(largo));     // domingo 15 de marzo de 2026
```

Un `parse` con formato incorrecto lanza `DateTimeParseException`. Captúralo siempre que la fecha venga del usuario o de un fichero.

## 5. Validación en capas

No toda la validación es igual, y confundirlas es un error de diseño frecuente:

| Tipo | Dónde | Ejemplo | Código HTTP |
|---|---|---|---|
| **Formato** | DTO de entrada, con anotaciones | «el email tiene forma de email» | 400 |
| **Negocio** | Servicio | «ese email ya está registrado» | 409 |
| **Integridad** | Repositorio / BD | clave única, clave ajena | 409 / 500 |

```java
// Formato: lo hace la anotación, antes de tu código
public record CrearSocioDto(@NotBlank String nombre,
                            @Email String email,
                            @Past LocalDate nacimiento) {}

// Negocio: solo tú puedes saberlo, y requiere consultar el estado
public Socio crear(CrearSocioDto dto) {
    if (repositorio.existePorEmail(dto.email()))
        throw new EmailDuplicadoException(dto.email());
    if (Period.between(dto.nacimiento(), LocalDate.now()).getYears() < 14)
        throw new DatosInvalidosException("Los menores de 14 años necesitan tutor");
    return repositorio.guardar(mapper.aModelo(dto));
}
```

Anotaciones de fecha que conviene recordar: `@Past`, `@PastOrPresent`, `@Future`, `@FutureOrPresent`.

### Validación manual, cuando aún no hay Spring

```java
public record Socio(String nombre, String email, LocalDate nacimiento) {

    public Socio {                                     // constructor compacto
        Objects.requireNonNull(nombre, "El nombre es obligatorio");
        if (nombre.isBlank())
            throw new IllegalArgumentException("El nombre no puede estar vacío");
        if (!email.matches("^[\\w.+-]+@[\\w-]+\\.[a-zA-Z]{2,}$"))
            throw new IllegalArgumentException("Email no válido: " + email);
        if (nacimiento.isAfter(LocalDate.now()))
            throw new IllegalArgumentException("La fecha de nacimiento no puede ser futura");
        nombre = nombre.trim();                        // también sirve para NORMALIZAR
    }

    public int edad() { return Period.between(nacimiento, LocalDate.now()).getYears(); }
}
```

El constructor compacto de un `record` es el sitio ideal: si se ejecuta, el objeto **existe válido**; si no, no llega a existir. Un objeto que no puede estar en estado inválido elimina de golpe una familia entera de bugs.

## 6. Más sobre expresiones regulares

```java
var patron = Pattern.compile("^(\\d{4})-(\\d{2})-(\\d{2})$");   // compila UNA vez
var m = patron.matcher("2026-03-15");
if (m.matches()) {
    IO.println("Año: " + m.group(1) + " Mes: " + m.group(2));   // grupos de captura
}

// Buscar todas las ocurrencias
Pattern.compile("\\d+").matcher("hay 3 cascos y 12 bicis")
       .results().map(MatchResult::group).forEach(IO::println);   // 3, 12

// Reemplazar
"tel: 600-123-456".replaceAll("\\d", "*");        // tel: ***-***-***

// Partir por varios separadores
"a, b;c , d".split("\\s*[,;]\\s*");               // [a, b, c, d]
```

| Símbolo | Significado |
|---|---|
| `\d` `\w` `\s` | dígito · alfanumérico o `_` · espacio |
| `.` | cualquier carácter |
| `+` `*` `?` | uno o más · cero o más · opcional |
| `{n,m}` | entre n y m repeticiones |
| `^` `$` | principio y fin de la cadena |
| `[abc]` `[^abc]` | uno de estos · ninguno de estos |
| `(...)` | grupo de captura |
| `a\|b` | a o b |

!!! tip "Compila el patrón fuera del bucle"
    `"texto".matches(regex)` recompila la expresión **en cada llamada**. Si la usas en un bucle o en un validador que se ejecuta miles de veces, declara `private static final Pattern P = Pattern.compile(...)` y reutilízalo. Puede suponer un ×10 en rendimiento.

!!! danger "No valides emails con una regex perfecta"
    La expresión regular oficial del estándar RFC 5322 tiene más de 6.000 caracteres, y aun así aceptaría direcciones que no existen. Una regex sencilla que descarte lo evidente y, después, **un correo de verificación**: esa es la única validación real de un email.

---

## Pruébalo ahora (12 min)

!!! reto "Trabaja tú ahora"
    Este bloque se hace **en clase, en tu equipo**. No se entrega ni puntúa: es la práctica que hace que el examen te salga.

```bash
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

void main() {
    // 1. Fechas
    var hoy = LocalDate.now();
    var nacimiento = LocalDate.of(2007, 3, 15);
    IO.println("Edad: " + Period.between(nacimiento, hoy).getYears());
    IO.println("Días vividos: " + ChronoUnit.DAYS.between(nacimiento, hoy));
    IO.println("Formato español: " + hoy.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
    IO.println("¿Es fin de semana? " +
        (hoy.getDayOfWeek() == DayOfWeek.SATURDAY || hoy.getDayOfWeek() == DayOfWeek.SUNDAY));

    // 2. Inmutabilidad: ¡ojo!
    var fecha = LocalDate.of(2026, 1, 1);
    fecha.plusDays(10);                 // OJO no hace nada: devuelve otra fecha
    IO.println(fecha);                  // sigue siendo 2026-01-01
    var correcta = fecha.plusDays(10);  // BIEN hay que asignarlo
    IO.println(correcta);               // 2026-01-11

    // 3. Validación con regex
    var emails = java.util.List.of("ana@mail.com", "sin-arroba", "a@b.es");
    for (var e : emails) {
        IO.println(e + " → " + e.matches("^[\\w.+-]+@[\\w-]+\\.[\\w.]{2,}$"));
    }
}
```

El punto 2 es el que más cae en el examen: **las fechas son inmutables**, igual que los Strings.

---

## Ejercicios (con solución)

### Ejercicio 1 — ¿Qué imprime?

```java
var f = LocalDate.of(2026, 5, 10);
f.plusMonths(2);
IO.println(f);
```

??? success "Solución"

    2026-05-10. `plusMonths` devuelve una fecha nueva y aquí se descarta. Igual que `s.toUpperCase()` sin asignar. Correcto: f = f.plusMonths(2);


### Ejercicio 2 — Elige el tipo

(a) fecha de nacimiento · (b) instante exacto en que se registró un pedido, para un sistema con usuarios en varios países · (c) hora de apertura de la tienda · (d) cuánto duró una operación.

??? success "Solución"

    (a) `LocalDate` · (b) Instant (UTC; se convierte a la zona del usuario al mostrarlo) · (c) `LocalTime` · (d) Duration.


### Ejercicio 3 — Validador de matrícula

Escribe una validación para matrículas españolas actuales: **4 dígitos + 3 letras mayúsculas** (sin vocales, pero simplifícalo a A-Z).

??? success "Solución"

    ```java
    static final Pattern MATRICULA = Pattern.compile("^\\d{4}[A-Z]{3}$");

    boolean esMatricula(String s) {
        return s != null && MATRICULA.matcher(s).matches();
    }
    // "1234BCD" → true · "123BCD" → false · "1234bcd" → false (minúsculas)
    ```


### Ejercicio 4 — Record validado

Crea un record `Reserva(String cliente, LocalDate entrada, LocalDate salida)` que **no permita** crearse si la salida es anterior o igual a la entrada, ni si la entrada es pasada.

??? success "Solución"

    ```java
    record Reserva(String cliente, LocalDate entrada, LocalDate salida) {
        Reserva {
            if (cliente == null || cliente.isBlank())
                throw new IllegalArgumentException("Cliente obligatorio");
            if (entrada.isBefore(LocalDate.now()))
                throw new IllegalArgumentException("La entrada no puede ser pasada");
            if (!salida.isAfter(entrada))
                throw new IllegalArgumentException("La salida debe ser posterior a la entrada");
        }
        long noches() { return ChronoUnit.DAYS.between(entrada, salida); }
    }
    ```

    Con esto, **es imposible** que exista en tu programa una reserva con fechas incoherentes.


### Ejercicio 5 — Aritmética de fechas

Un préstamo dura 15 días naturales. Escribe `boolean estaVencido(LocalDate inicio)` y `long diasDeRetraso(LocalDate inicio)`.

??? success "Solución"

    ```java
    private static final int DIAS_PRESTAMO = 15;

    LocalDate fechaLimite(LocalDate inicio) { return inicio.plusDays(DIAS_PRESTAMO); }

    boolean estaVencido(LocalDate inicio) {
        return LocalDate.now().isAfter(fechaLimite(inicio));
    }

    long diasDeRetraso(LocalDate inicio) {
        var limite = fechaLimite(inicio);
        return Math.max(0, ChronoUnit.DAYS.between(limite, LocalDate.now()));
    }
    ```

    El `Math.max(0,...)` evita devolver números negativos cuando aún no ha vencido. Y extraer `fechaLimite` como método propio hace que la regla de los 15 días esté en un solo sitio.


### Ejercicio 6 — El bug de la zona horaria

Una aplicación guarda `LocalDateTime.now()` y un usuario en Canarias ve las citas una hora desplazadas. ¿Qué pasa y cómo se arregla?

??? success "Solución"

    `LocalDateTime` no lleva zona: guarda «14:30» sin decir de dónde. El servidor la escribió en horario peninsular y el cliente la interpreta en el suyo, así que el mismo dato significa dos instantes distintos. Solución: guardar y transmitir `UTC` con Instant (o `ZonedDateTime` si necesitas conservar la zona de origen) y convertir a la zona del usuario solo al presentar. En JSON, ISO-8601 con la Z: "2026-03-15T13:30:00Z".


### Ejercicio 7 — Validación en su sitio

Clasifica: (a) el email tiene arroba · (b) el email ya existe en la base de datos · (c) el precio es positivo · (d) no hay stock suficiente · (e) el año no es futuro · (f) el socio tiene multas pendientes.

??? success "Solución"

    Formato (DTO, con anotaciones, → 400): (a), (c), (e). Son comprobables mirando solo el dato. Negocio (servicio, → 409): (b), (d), (f). Requieren consultar el estado del sistema. La prueba de fuego: si puedes decidirlo sin tocar la base de datos, es formato; si necesitas consultar algo, es negocio.


### Ejercicio 8 — Validador completo

Escribe un `record Reserva(String codigo, LocalDate entrada, LocalDate salida, int personas)` que se valide a sí mismo: código con formato `RES-2026-0001`, salida posterior a entrada, entrada no pasada, y entre 1 y 8 personas. Añade el método `noches()`.

??? success "Solución"

    ```java
    public record Reserva(String codigo, LocalDate entrada, LocalDate salida, int personas) {

        private static final Pattern CODIGO = Pattern.compile("^RES-\\d{4}-\\d{4}$");

        public Reserva {
            Objects.requireNonNull(codigo, "El código es obligatorio");
            Objects.requireNonNull(entrada, "La fecha de entrada es obligatoria");
            Objects.requireNonNull(salida, "La fecha de salida es obligatoria");

            if (!CODIGO.matcher(codigo).matches())
                throw new IllegalArgumentException("Código no válido: " + codigo);
            if (entrada.isBefore(LocalDate.now()))
                throw new IllegalArgumentException("La entrada no puede ser en el pasado");
            if (!salida.isAfter(entrada))
                throw new IllegalArgumentException("La salida debe ser posterior a la entrada");
            if (personas < 1 || personas > 8)
                throw new IllegalArgumentException("Entre 1 y 8 personas, no " + personas);
        }

        public long noches() { return ChronoUnit.DAYS.between(entrada, salida); }
    }
    ```

    Tres detalles que se evalúan: el Pattern es static final (se compila una sola vez), los mensajes de error incluyen el valor recibido (depurar sin eso es un suplicio), y `noches()` es un método del propio record — el comportamiento vive junto a los datos, no en una clase «Utils».

