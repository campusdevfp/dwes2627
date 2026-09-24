# UT3 — Datos: estructuras, intercambio y base de datos

**10 h · 10 sesiones · Trimestre 1.º** · Evaluación: :material-form-select: **test práctico (100 %)**

> **RA3:** Escribe bloques de sentencias embebidos en lenguajes de marcas, seleccionando y utilizando las estructuras de programación.

Última unidad de fundamentos, y la que junta todo con datos de verdad: elegir la estructura correcta, leer **CSV**, hablar **JSON** —el formato de las APIs—, manejar fechas y dinero sin equivocarse, y terminar con un **CRUD contra una base de datos**, primero H2 y después MySQL en Docker.

!!! tip "El puente hacia Spring"
    Todo lo de esta unidad reaparece automatizado en la UT4 y la UT5. Si ahora escribes **a mano** el JSON, la conexión y el `SELECT`, cuando Jackson y JPA lo hagan por ti sabrás qué está pasando por debajo — y podrás depurarlo cuando haga algo raro, que lo hará.


## Al terminar sabrás hacer

Marca cada casilla cuando puedas hacerlo **sin mirar los apuntes**. Lo que quede sin marcar la semana del examen es exactamente lo que hay que repasar.

- [ ] Elegir la **estructura de datos** correcta —`List`, `Set`, `Map`— y defender la elección.
- [ ] **Agrupar y resumir** un montón de datos con `groupingBy` y comparadores.
- [ ] Leer y escribir **CSV** con Apache Commons CSV, sin que las comas ni los acentos lo rompan.
- [ ] Convertir objetos a **JSON** y al revés con Jackson, con las fechas en ISO.
- [ ] Manejar **fechas** con `java.time` y **dinero** con `BigDecimal`, sin los errores clásicos.
- [ ] **Validar** en el constructor para que un objeto no pueda existir mal formado.
- [ ] Montar un **CRUD con JDBC** contra H2 y contra MySQL en Docker, sin inyección SQL.

## Calendario

| Sesión (55') | En clase | Lectura previa |
|---|---|---|
| **S1** | Elegir la estructura: lista, conjunto y mapa, con el coste medido | [1. Estructuras aplicadas](01-estructuras-de-datos.md) §1–2 |
| **S2** | Agrupar, ordenar y sacar estadísticas de un montón de datos | [1. Estructuras aplicadas](01-estructuras-de-datos.md) §3–6 |
| **S3** | Leer y escribir CSV: a mano, y con Apache Commons CSV | [2. CSV y JSON](02-csv-y-json.md) §1–3 |
| **S4** | JSON y Jackson: objetos, listas, fechas y campos que no esperas | [2. CSV y JSON](02-csv-y-json.md) §4–7 |
| **S5** | De CSV a JSON: el programa completo, con descartes contados | [2. CSV y JSON](02-csv-y-json.md) §8 |
| **S6** | `java.time`: inmutabilidad, rangos y formatos | [3. Fechas y validación](03-fechas-y-validacion.md) §1–4 |
| **S7** | Validar en el constructor · expresiones regulares · `BigDecimal` | [3. Fechas y validación](03-fechas-y-validacion.md) §5–8 |
| **S8** | **JDBC y un CRUD con H2**, con la inyección SQL provocada en clase | [4. CRUD contra base de datos](04-base-de-datos.md) §1–7 |
| **S9** | **MySQL en Docker**: el mismo código, otra base de datos | [4. CRUD contra base de datos](04-base-de-datos.md) §8 |
| **S10** | :material-form-select: **Test práctico de RA3 (100 %)** | [Batería de test](autoevaluacion.md) |

## Cómo se evalúa

:material-form-select: **Test práctico (100 %)**, una sesión de 55 min, en la S10.

**30 preguntas, y todas llevan código delante.** No es teoría: hay que decir qué imprime un fragmento, por qué falla otro, cuál de cuatro versiones hace lo que pide el enunciado, qué falta en un hueco o qué tipo devuelve una cadena de *streams*.

Acierto **+1**, fallo **−0,25**, en blanco **0**. Se examina con la chuleta impresa y sin ordenador.

!!! info "Por qué esta unidad se examina con test"
    La UT3 es el puente entre el lenguaje y el desarrollo de verdad. Lo que hay que asegurar aquí es que **lees código y sabes anticipar lo que hace**, y eso se mide mejor con treinta fragmentos distintos que con un solo programa, donde un error tonto al principio te deja sin poder demostrar el resto.

    **Desde la UT4 se vuelve al examen práctico grande**, que es donde ya toca construir.

!!! warning "Que sea un test no significa que no haya que programar"
    Las opciones incorrectas de cada pregunta son **exactamente los fallos que se cometen escribiendo ese código**. Se reconoce un `ConcurrentModificationException` en un fragmento porque te ha saltado antes en tu propia pantalla.

    La batería sigue siendo la preparación, y sigue haciéndose programando.

**Lo que se evalúa:** elección de estructuras y agrupaciones · las trampas del CSV · JSON con Jackson y fechas ISO · inmutabilidad de `java.time`, rangos y `BigDecimal` · JDBC, `PreparedStatement` e inyección SQL.

## Material

| | |
|---|---|
| [**Batería de ejercicios**](ejercicios.md) | 40 con solución: 32 fragmentos y **8 programas completos** (E33–E40), que son la práctica integradora |
| [**Batería de test**](autoevaluacion.md) | 48 preguntas del mismo tipo que las del examen, con solución razonada, y un simulacro cronometrado de 30 |
| [Chuleta de la UT3](chuleta.md) | Colecciones, ficheros, JSON y fechas en una página |
| [Comprobar tu trabajo](../comprobar-tu-trabajo.md) | Pega tu código y recibe comentarios |
