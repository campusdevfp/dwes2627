# Preparar el examen — UT2

**:material-form-select: Test aplicado · 100 % de la nota · 55 min · S10**

## Formato

**30 preguntas**, una sola respuesta correcta, **sin penalización**. Se aprueba con **15**.

La mayoría son **preguntas de código**: se te da un fragmento y hay que decir qué imprime, si compila, o cuál es el fallo. Estudiar esta unidad leyendo apuntes no funciona; hay que teclear.

## Cómo se reparten las preguntas

| Bloque | Preguntas | Tema |
|---|:-:|---|
| **A · Tipos, variables y control de flujo** | 6 | 1–2 |
| **B · POO: clases, `record`, enums, herencia** | 8 | 3 |
| **C · Colecciones y programación funcional** | 8 | 4 |
| **D · Excepciones y `Optional`** | 4 | 5 |
| **E · Maven y testing** | 4 | 6 |

**B y C son más de la mitad.** Son también los dos bloques de los que depende todo el resto del curso.

## Los cinco tipos de pregunta

**1. ¿Qué imprime?**

```java
var lista = List.of(3, 1, 2);
System.out.println(lista.stream().sorted().map(String::valueOf).collect(joining("-")));
```

**2. ¿Compila?** Casi siempre con una trampa: reasignar un `final`, modificar una `List.of(...)`, olvidar el `break` en un `switch` clásico.

**3. Elige la colección.** *«Necesitas guardar usuarios sin repetidos y consultarlos por DNI muy a menudo.»* → `HashMap`, no `List`.

**4. Encuentra el error.** Un `catch (Exception e) {}` vacío, un `equals` sin `hashCode`, un `Optional.get()` sin comprobar.

**5. Traduce.** De bucle a `stream`, o al revés.

## Lo que hay que tener automatizado

Si dudas en alguna de estas, repásala antes que nada:

| Concepto | La respuesta corta |
|---|---|
| `==` frente a `equals` | `==` compara referencias; `equals` compara contenido |
| `List.of()` | **Inmutable**: `add` lanza `UnsupportedOperationException` |
| `record` | Datos inmutables; genera constructor, *getters*, `equals`, `hashCode`, `toString` |
| `HashMap` frente a `TreeMap` | Sin orden y rápido · ordenado por clave |
| `map` frente a `flatMap` | Transforma cada elemento · aplana colecciones anidadas |
| Excepción comprobada | Hay que declararla o capturarla; hereda de `Exception` |
| `Optional` | Obliga a tratar la ausencia; **nunca** `.get()` sin comprobar |
| `var` | Inferencia en tiempo de compilación; **no** es tipado dinámico |

## Cómo prepararte

1. **Haz los [ejercicios](ejercicios.md) sin mirar la solución.** Es lo más rentable con diferencia.
2. **Ejecuta el código de las preguntas dudosas.** Si no sabes qué imprime, escríbelo y pruébalo: se recuerda mucho mejor.
3. **[Autoevaluación](autoevaluacion.md)** completa, anotando fallos.
4. **[Chuleta](chuleta.md)** el día antes, no la semana antes.

!!! tip "El truco de los 20 minutos"
    Coge cinco fragmentos de código de los temas, tápalos y escribe en un papel qué crees que imprimen. Después ejecútalos. Los que falles son exactamente tus preguntas del examen.

## Errores que más cuestan

1. **`==` con cadenas.** `"hola" == new String("hola")` es `false`. Con `equals`, `true`.
2. **Modificar una `List.of(...)`.** Compila y revienta en ejecución.
3. **`equals` sin `hashCode`.** El objeto se pierde dentro de un `HashSet`. Vuelve a caer en la UT5 con las entidades.
4. **Creer que `var` es tipado dinámico.** El tipo se fija al compilar y no cambia.
5. **`catch (Exception e) {}` vacío.** Silencia el fallo y lo hace imposible de encontrar.
6. **Confundir `map` con `flatMap`.**
7. **`Optional.get()` a pelo.** Usa `orElse`, `orElseThrow` o `ifPresent`.
8. **Olvidar que un `stream` es de un solo uso.** Reutilizarlo lanza `IllegalStateException`.
9. **`switch` clásico sin `break`.** Cae en el siguiente caso. Con `->` no pasa.
10. **Pensar que una clase de test sin `Test` en el nombre se ejecuta.** Maven no la encuentra.

## El día del examen

- Menos de dos minutos por pregunta. Marca y sigue.
- Sin penalización: **contesta todas**.
- En las de «¿qué imprime?», sigue el código línea a línea con lápiz. De cabeza se falla.
