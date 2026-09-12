# Sintaxis y tipos: la base del lenguaje

> Todo lo que necesitas para escribir lógica en Java: tipos, variables, operadores, cadenas y control de flujo — con las novedades modernas que hacen el código mucho más legible.

## 1. Tipos primitivos y objetos

Java tiene **dos familias** de tipos, y confundirlas causa la mitad de los errores de principiante:

| Primitivos (minúscula) | Qué guardan | Ejemplo |
|---|---|---|
| `int` | Enteros (±2.100 millones) | `int edad = 25;` |
| `long` | Enteros grandes | `long poblacion = 8_000_000_000L;` |
| `double` | Decimales | `double precio = 19.99;` |
| `boolean` | `true` / `false` | `boolean activo = true;` |
| `char` | Un carácter | `char inicial = 'A';` |

Y los **objetos** (mayúscula): `String`, `LocalDate`, `List`… Tienen métodos y pueden valer `null`.

```java
int a = 5;              // primitivo: guarda el valor 5
String s = "hola";      // objeto: guarda una referencia
Integer b = null;       // objeto: puede ser null. Un int NO puede.
```

!!! warning "Decimales y dinero"
    `double` tiene errores de redondeo: `0.1 + 0.2` da `0.30000000000000004`. Para **dinero** se usa `BigDecimal`. Lo verás en la API de la tienda.

## 2. `var`: menos ruido, mismo tipado

Desde Java 10, el compilador puede **deducir** el tipo:

```java
var nombre = "Ana";                    // String
var edad = 30;                         // int
var precios = new ArrayList<Double>(); // ArrayList<Double>
```

:material-alert: `var` **no es tipado dinámico**: el tipo se fija en la declaración y no cambia. `var x = 5; x = "hola";` no compila. Y solo vale para variables locales con valor inicial.

## 3. Cadenas: `String` y text blocks

```java
var nombre = "Ana";
var saludo = "Hola, " + nombre;                 // concatenación
var formato = "Hola, %s. Tienes %d años".formatted(nombre, 30);  // formato
IO.println(saludo.toUpperCase());               // HOLA, ANA
IO.println(saludo.length());                    // 9
IO.println(saludo.contains("Ana"));             // true
```

**Text blocks** (Java 15+) para textos multilínea — imprescindibles para JSON y SQL:

```java
var json = """
    {
      "nombre": "Ana",
      "rol": "ADMIN"
    }
    """;
```

!!! bug "El error del `==` con Strings"
    ```java
    var a = new String("hola");
    var b = new String("hola");
    IO.println(a == b);        // false → compara REFERENCIAS
    IO.println(a.equals(b));   // true  → compara CONTENIDO
    ```
    **Regla:** para objetos usa siempre `.equals()`. `==` solo para primitivos. Cae en todos los exámenes.

## 4. Control de flujo

```java
// Condicional
if (edad >= 18) {
    IO.println("Mayor de edad");
} else if (edad >= 16) {
    IO.println("Casi");
} else {
    IO.println("Menor");
}

// Bucle for clásico
for (int i = 0; i < 5; i++) IO.println(i);

// for-each (el que más usarás)
var nombres = List.of("Ana", "Luis", "Marta");
for (var n : nombres) IO.println(n);

// while
var intentos = 0;
while (intentos < 3) { intentos++; }
```

Operadores que conviene tener claros: `==` `!=` `<` `>` `<=` `>=` para comparar; `&&` (y), `||` (o), `!` (no) para combinar; `%` para el resto de la división (`if (n % 2 == 0)` → par).

## 5. El `switch` moderno (Java 14+)

Olvida el `switch` con `break` por todas partes. Hoy es una **expresión** que devuelve valor:

```java
var dia = 3;
var nombre = switch (dia) {
    case 1, 2, 3, 4, 5 -> "Laborable";
    case 6, 7          -> "Fin de semana";
    default            -> "Día inválido";
};
IO.println(nombre);   // Laborable
```

Sin `break` (no hay *fall-through* accidental), con varios valores por rama y devolviendo un resultado. Además admite **pattern matching** (Java 21+):

```java
Object dato = 42;
var descripcion = switch (dato) {
    case Integer i when i > 100 -> "Entero grande: " + i;
    case Integer i              -> "Entero: " + i;
    case String s               -> "Texto de " + s.length() + " letras";
    case null                   -> "Nada";
    default                     -> "Otra cosa";
};
```

---

## Pruébalo ahora (10 min)

!!! reto "Trabaja tú ahora"
    Este bloque se hace **en clase, en tu equipo**. No se entrega ni puntúa: es la práctica que hace que el examen te salga.

Guarda como `Pruebas.java` y ejecuta con `java Pruebas.java`. **Antes de ejecutar, predice cada salida**:

```java
void main() {
    IO.println(7 / 2);           // ¿?
    IO.println(7 / 2.0);         // ¿?
    IO.println(7 % 2);           // ¿?
    IO.println("5" + 3);         // ¿?
    IO.println(0.1 + 0.2);       // ¿?

    var a = "hola";
    var b = "hola";
    IO.println(a.equals(b));     // ¿?
}
```

??? success "Salidas y por qué"

    <code>3</code> (división entera: ambos son int) · <code>3.5</code> (uno es double → decimal) · <code>1</code> (resto) · <code>53</code> (¡concatenación! el String manda) · <code>0.30000000000000004</code> (precisión del double) · <code>true</code>.


---

## Ejercicios (con solución)

### Ejercicio 1 — ¿Qué imprime?
```java
var x = 10;
var y = 3;
IO.println(x / y);
IO.println((double) x / y);
IO.println(x > y && y > 5);
```

??? success "Solución"

    <code>3</code> (división entera) · <code>3.3333333333333335</code> (el cast a double fuerza división decimal) · <code>false</code> (la primera es true pero <code>y > 5</code> es false, y <code>&&</code> exige ambas).


### Ejercicio 2 — Reescribe con `switch` moderno
Convierte esto en una expresión `switch` de una sola asignación:
```java
String categoria;
int nota = 7;
if (nota >= 9) categoria = "Sobresaliente";
else if (nota >= 7) categoria = "Notable";
else if (nota >= 5) categoria = "Aprobado";
else categoria = "Suspenso";
```

??? success "Solución"

    ```java
    var nota = 7;
    var categoria = switch (nota) {
        case 9, 10 -> "Sobresaliente";
        case 7, 8  -> "Notable";
        case 5, 6  -> "Aprobado";
        default    -> "Suspenso";
    };
    ```
    (Con rangos abiertos seguiría siendo más natural el `if`; el `switch` brilla con valores discretos. Saber **cuándo** usar cada uno también es criterio.)


### Ejercicio 3 — Validador de contraseña
Escribe un programa que, dada una variable `password`, imprima "Válida" si tiene 8 o más caracteres **y** contiene un dígito; si no, "No válida" indicando qué falla.

??? success "Solución"

    ```java
    void main() {
        var password = "abc12345";
        var longitudOk = password.length() >= 8;
        var tieneDigito = password.matches(".*\\d.*");

        if (longitudOk && tieneDigito) {
            IO.println("Válida");
        } else {
            IO.println("No válida:"
                + (longitudOk ? "" : " faltan caracteres")
                + (tieneDigito ? "" : " falta un dígito"));
        }
    }
    ```
    Recuerda de la UT1: esta validación en el cliente sería solo UX; **la que cuenta va en el servidor**.

