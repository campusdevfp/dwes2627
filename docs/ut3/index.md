# UT3 — Estructuras de datos, ficheros e intercambio

**10 h · 10 sesiones · Trimestre 1.º** · Evaluación: :material-form-select: **test práctico (100 %)**

> **RA3:** Escribe bloques de sentencias embebidos en lenguajes de marcas, seleccionando y utilizando las estructuras de programación.

Última unidad de fundamentos. Aquí tu código empieza a **manejar datos de verdad**: estructuras complejas, ficheros, **JSON** (el formato de las APIs), fechas y validación. Y termina montando un **repositorio en capas** — el esqueleto que en la UT4 rellenará Spring.

!!! tip "El puente hacia Spring"
    Todo lo de esta unidad reaparece en la UT4 automatizado por el framework. Si entiendes ahora **a mano** cómo se serializa un JSON o cómo se separa un repositorio, cuando Spring lo haga por ti sabrás qué está pasando por debajo — y podrás depurarlo.


## Al terminar sabrás hacer

Marca cada casilla cuando puedas hacerlo **sin mirar los apuntes**. Lo que quede sin marcar la semana del examen es exactamente lo que hay que repasar.

- [ ] Elegir la **estructura de datos** correcta —`List`, `Set`, `Map`, `Deque`— y defender la elección.
- [ ] **Leer y escribir ficheros** de texto y CSV cerrando siempre los recursos.
- [ ] Convertir objetos a **JSON** y al revés con Jackson, controlando el contrato de campos.
- [ ] Trabajar con **fechas** de la API `java.time` y **validar** datos de entrada.
- [ ] Aislar el acceso a datos tras el **patrón Repositorio** y separar el programa en capas.

## Calendario

| Sesión (55') | En clase | Lectura previa |
|---|---|---|
| **S1** | Elegir estructura y ordenar: `List`, `Set`, `Map`, `Deque`, `Comparator` | [1. Estructuras de datos](01-estructuras-de-datos/) §1–3 |
| **S2** | `groupingBy`, `reduce` y estadísticas sobre datos reales | [1. Estructuras de datos](01-estructuras-de-datos/) §4–6 |
| **S3** | Rutas y ficheros de texto con NIO.2 | [2. Ficheros](02-ficheros/) §1–2 |
| **S4** | CSV: leer, escribir, recursos y errores de E/S | [2. Ficheros](02-ficheros/) §3–4 |
| **S5** | JSON: estructura y mapeo a objetos | [3. JSON y Jackson](03-json-y-jackson/) §1–2 |
| **S6** | Jackson: serializar, deserializar, anidados y fechas | [3. JSON y Jackson](03-json-y-jackson/) §3–5 |
| **S7** | Fechas con `java.time` · validación y expresiones regulares | [4. Fechas y validación](04-fechas-y-validacion/) |
| **S8** | El patrón **Repositorio**, las capas y el proyecto integrador | [5. Repositorio y capas](05-repositorio-y-capas/) completo |
| **S9** | Laboratorio con la batería · repaso y dudas | [Batería de ejercicios](ejercicios/) + [Preparar el examen](examen/) |
| **S10** | :material-form-select: **Test práctico de RA3 (100 %)** | [Preparar el examen](examen/) |

## Cómo se evalúa

:material-form-select: **Test práctico (100 %)**, una sesión de 55 min, en la S10.

**30 preguntas, y todas llevan código delante.** No es teoría: hay que decir qué imprime un fragmento, por qué falla otro, cuál de cuatro versiones hace lo que pide el enunciado, qué falta en un hueco o qué tipo devuelve una cadena de *streams*.

Acierto **+1**, fallo **−0,25**, en blanco **0**. Se examina con la chuleta impresa y sin ordenador.

!!! info "Por qué esta unidad se examina con test"
    La UT3 es el puente entre el lenguaje y el desarrollo de verdad. Lo que hay que asegurar aquí es que **lees código y sabes anticipar lo que hace**, y eso se mide mejor con treinta fragmentos distintos que con un solo programa, donde un error tonto al principio te deja sin poder demostrar el resto.

    **Desde la UT4 se vuelve al examen práctico grande**, que es donde ya toca construir.

!!! warning "Que sea un test no significa que no haya que programar"
    Las opciones incorrectas de cada pregunta son **exactamente los fallos que se cometen escribiendo ese código**. Se reconoce un `ConcurrentModificationException` en un fragmento porque te ha saltado antes en tu propia pantalla.

    Las prácticas y la batería siguen siendo la preparación, y siguen haciéndose programando.

**Lo que se evalúa** (detalle en la [página de preparación](examen/)): elección de estructuras y orden · *streams* y agrupaciones · lectura de ficheros y las trampas del CSV · JSON con Jackson y fechas ISO · validación y rangos · qué va en cada capa.

## Material

- [Prácticas guiadas](practicas/)
    — 8 ejercicios con solución que construyen el proyecto integrador.
- [Batería de ejercicios prácticos](ejercicios/)
    — 12 ejercicios de examen resueltos, con datos de partida y salidas esperadas.
- [Preparar el examen](examen/)
    — formato, los cinco tipos de pregunta con ejemplos resueltos y un simulacro.
- [Batería de test](autoevaluacion/)
    — preguntas del mismo tipo que las del examen, con solución razonada.
- [comprobar tu trabajo con los tests](../comprobar-tu-trabajo/)
    — feedback de tu código + simulacros de examen.
- [Chuleta de la UT3](chuleta/)
    — colecciones, ficheros, JSON y fechas en una página.
