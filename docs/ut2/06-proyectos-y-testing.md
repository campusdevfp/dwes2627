# Proyectos, dependencias y testing

> Un fichero suelto vale para aprender; una aplicación real necesita **estructura, dependencias y pruebas**. Esta página cierra la UT2 y te deja listo para Spring Boot.

## 1. Por qué necesitas un gestor de proyectos

Cuando tu aplicación crece necesitas: organizar decenas de ficheros, usar librerías de terceros (y las librerías que *esas* librerías necesitan), compilar, pasar tests y empaquetar. Hacerlo a mano es inviable. Para eso están **Maven** y **Gradle**.

| | Maven | Gradle |
|---|---|---|
| Configuración | `pom.xml` (XML) | `build.gradle.kts` (Kotlin/Groovy) |
| Estilo | Declarativo, muy estándar | Flexible, más conciso |
| Uso | Mayoritario en empresa | Creciente, estándar en Android |

Los dos hacen lo mismo. En clase verás **Maven** (por ser el más extendido) y Spring Initializr te generará el proyecto.

!!! tip "Manual de referencia"
    Este tema explica **por qué** hace falta un gestor de proyectos y cómo se usa en el día a día. Para consultar el ciclo de vida completo, los *scopes*, las dependencias transitivas, los perfiles o los errores típicos, tienes el **[manual de Maven](../../entorno/maven/)**.

    Y si te encuentras un proyecto con `build.gradle.kts` —te pasará—, el **[manual de Gradle](../../entorno/gradle/)** traduce cada concepto.

## 2. Estructura estándar de un proyecto

``` { .text .sinajuste }
mi-proyecto/
├── pom.xml                          ← dependencias y configuración
└── src/
    ├── main/
    │   ├── java/com/miempresa/app/  ← tu código
    │   └── resources/               ← configuración, plantillas
    └── test/
        └── java/com/miempresa/app/  ← tus tests
```

Esa estructura es **una convención universal**: cualquier desarrollador Java del mundo sabe dónde buscar. Y los **paquetes** (`com.miempresa.app.servicios`) organizan el código y evitan choques de nombres.

Un `pom.xml` mínimo con una dependencia:

```xml
<dependencies>
    <dependency>
        <groupId>org.junit.jupiter</groupId>
        <artifactId>junit-jupiter</artifactId>
        <version>5.11.0</version>
        <scope>test</scope>
    </dependency>
</dependencies>
```

Maven descarga esa librería **y todas las que ella necesite** (dependencias transitivas), y las deja disponibles para compilar.

Comandos que usarás a diario:

```bash
mvn compile     # compila
mvn test        # ejecuta los tests
mvn package     # genera el .jar
mvn clean       # borra lo generado
```

## 3. Testing: por qué y cómo

Un **test** es código que comprueba que tu código hace lo que debe. No es burocracia: es lo que te permite **cambiar cosas sin miedo**. Cuando en el trimestre 2 toques el servicio de pedidos, los tests te dirán en 3 segundos si has roto algo.

**JUnit 5** es el estándar:

``` { .java .numerado .annotate title="src/test/java/CalculadoraPreciosTest.java" hl_lines="4 6 7" }
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;   // (1)!

class CalculadoraPreciosTest {                      // (2)!

    @Test                                           // (3)!
    void aplicaDescuentoPorVolumen() {              // (4)!
        var calculadora = new CalculadoraPrecios(); // (5)!

        var resultado = calculadora.total(10, 20.0);

        assertEquals(180.0, resultado);             // (6)!
    }

    @Test
    void lanzaExcepcionSiCantidadNegativa() {
        var calculadora = new CalculadoraPrecios();

        assertThrows(IllegalArgumentException.class,
            () -> calculadora.total(-1, 20.0));     // (7)!
    }
}
```

1.  **Import estático.** Permite escribir `assertEquals(...)` en vez de `Assertions.assertEquals(...)`. Es la convención en JUnit y la verás en todos los proyectos.

2.  **El nombre acaba en `Test`.** No es estética: Maven y Gradle solo ejecutan las clases que casan con `*Test`, `Test*` o `*Tests`. Si la llamas `PruebasCalculadora`, tus tests **no se ejecutan** y la construcción sale verde sin haber probado nada.

3.  **Sin `@Test` el método no se ejecuta.** Sigue siendo un método normal que nadie llama, y de nuevo la construcción pasa sin decir nada.

4.  **El nombre del test describe el comportamiento esperado**, no el método probado. `aplicaDescuentoPorVolumen` te dice qué se ha roto al leer el informe de fallos; `test1` no te dice nada.

5.  **Cada test crea su propio objeto.** JUnit crea una instancia nueva de la clase por cada `@Test`, precisamente para que un test no pueda ensuciar a otro. Los tests deben poder ejecutarse en cualquier orden.

6.  **Un test sin `assert` no prueba nada**: se limita a comprobar que el código no lanza excepciones. Es el error más común al empezar.

7.  **Probar el camino que falla es tan importante como el que funciona.** `assertThrows` comprueba que la excepción salta; el `() -> ...` es una lambda porque el código no debe ejecutarse hasta que JUnit esté vigilando.

**Patrón AAA** — todo test tiene tres partes:

1. **Arrange** (preparar): creas los objetos y datos.
2. **Act** (actuar): ejecutas lo que quieres probar.
3. **Assert** (comprobar): verificas el resultado.

Las aserciones más usadas: `assertEquals`, `assertTrue` / `assertFalse`, `assertNull` / `assertNotNull`, `assertThrows` (comprueba que salta una excepción) y `assertAll` (agrupa varias).

!!! tip "Nombra los tests como frases"
    `aplicaDescuentoPorVolumen()` dice qué debe pasar. `test1()` no dice nada. Cuando un test falla en el pipeline, su nombre es lo primero que lee quien lo arregla.

## 4. Qué merece un test

No todo. Prioriza:

- :material-check: **Lógica de negocio**: cálculos, descuentos, validaciones, reglas.
- :material-check: **Casos límite**: cero, negativos, listas vacías, nulos.
- :material-check: **Errores esperados**: que lance la excepción correcta.
- :material-close: Getters y setters triviales, o código de librerías que ya está probado.

Un test bien puesto por cada regla de negocio te ahorra más tiempo del que cuesta escribirlo. Y en la UT6, cuando montemos servicios de Spring, testear la lógica sin arrancar la web será posible precisamente porque separamos capas.

---

## Pruébalo ahora (15 min)

!!! reto "Trabaja tú ahora"
    Este bloque se hace **en clase, en tu equipo**. No se entrega ni puntúa: es la práctica que hace que el examen te salga.

Sin instalar Maven todavía, escribe la lógica y compruébala "a mano" para entender el mecanismo:

``` { .java .numerado }
class CalculadoraPrecios {
    double total(int cantidad, double precioUnitario) {
        if (cantidad < 0) throw new IllegalArgumentException("Cantidad negativa");
        var total = cantidad * precioUnitario;
        return cantidad > 5 ? total * 0.9 : total;   // 10 % desde 6 unidades
    }
}

void main() {
    var c = new CalculadoraPrecios();

    // "Tests" manuales: comprueba y avisa
    comprobar("sin descuento", 40.0, c.total(2, 20.0));
    comprobar("con descuento", 180.0, c.total(10, 20.0));

    try {
        c.total(-1, 20.0);
        IO.println("MAL  FALLO: debería haber lanzado excepción");
    } catch (IllegalArgumentException e) {
        IO.println("BIEN lanza excepción con cantidad negativa");
    }
}

void comprobar(String nombre, double esperado, double real) {
    IO.println((esperado == real ? "BIEN " : "MAL  ") + nombre + " → esperado " + esperado + ", real " + real);
}
```

Eso que acabas de escribir a mano es exactamente lo que hace JUnit — pero con herramientas, informes y ejecución automática en cada `git push`.

---

## Ejercicios (con solución)

### Ejercicio 1 — Ubica el fichero
En un proyecto Maven, ¿dónde van? (a) `PedidoService.java` · (b) `PedidoServiceTest.java` · (c) `application.properties` · (d) la dependencia de JUnit.

??? success "Solución"

    (a) <code>src/main/java/...</code> · (b) <code>src/test/java/...</code> · (c) <code>src/main/resources/</code> · (d) en el <code>pom.xml</code>, dentro de <code>&lt;dependencies&gt;</code> con <code>&lt;scope&gt;test&lt;/scope&gt;</code>.


### Ejercicio 2 — Escribe el test
Dada esta clase, escribe dos tests JUnit: uno para un email válido y otro para uno inválido.
```java
class ValidadorEmail {
    boolean esValido(String email) {
        return email != null && email.contains("@") && email.contains(".");
    }
}
```

??? success "Solución"

    ```java
    class ValidadorEmailTest {

        @Test
        void aceptaEmailValido() {
            var validador = new ValidadorEmail();
            assertTrue(validador.esValido("ana@mail.com"));
        }

        @Test
        void rechazaEmailSinArroba() {
            var validador = new ValidadorEmail();
            assertFalse(validador.esValido("anamail.com"));
        }

        @Test
        void rechazaNull() {
            assertFalse(new ValidadorEmail().esValido(null));
        }
    }
    ```
    El tercero es el que más valor aporta: los <b>casos límite</b> son donde viven los bugs.


### Ejercicio 3 — ¿Qué falla aquí?
```java
@Test
void test1() {
    var c = new CalculadoraPrecios();
    c.total(10, 20.0);
}
```

??? success "Solución"

    Dos cosas: (1) <b>no hay ninguna aserción</b>, así que el test pasa siempre aunque el cálculo sea erróneo — no comprueba nada; (2) el nombre <code>test1</code> no dice qué se espera. Corregido:

    ```java
    @Test
    void aplicaDescuentoDel10PorCientoAPartirDe6Unidades() {
        var c = new CalculadoraPrecios();
        assertEquals(180.0, c.total(10, 20.0));
    }
    ```


### Ejercicio 4 — Prioriza
Tienes 30 minutos para escribir tests de una tienda. ¿Qué pruebas primero? (a) el getter `getNombre()` · (b) el cálculo del total con descuentos e IVA · (c) que al pedir más unidades que el stock lance excepción · (d) el `toString()` de un record.

??? success "Solución"

    <b>(b) y (c)</b>: lógica de negocio y caso de error. (a) y (d) son código trivial o autogenerado — testearlos da falsa sensación de cobertura sin aportar seguridad.

