# UT2 — Java moderno: el lenguaje del servidor

**8 h · 8 sesiones · Trimestre 1.º** · Evaluación: :material-form-select: **test aplicado (100 %)**

> **RA2:** Escribe sentencias ejecutables por un servidor web, reconociendo y aplicando procedimientos de integración del código.

En la UT1 aprendiste *qué* pasa en un servidor. Aquí aprendes **a escribirlo**. Trabajamos con **Java 25 (LTS)**, el lenguaje de todo el resto del curso: en el trimestre 1 construiremos APIs REST con Spring Boot y en el 2, páginas dinámicas.

!!! tip "Vienes de 1.º con base de programación"
    No empezamos de cero: ya sabes qué es una variable, un bucle y una clase. Esta unidad es una **conversión rápida a Java** y, sobre todo, al **Java moderno** (records, `var`, streams, virtual threads) — que se parece poco al Java de los tutoriales de 2015.


## Al terminar sabrás hacer

Marca cada casilla cuando puedas hacerlo **sin mirar los apuntes**. Lo que quede sin marcar la semana del test es exactamente lo que hay que repasar.

- [ ] Escribir, compilar y ejecutar programas **Java 25** desde la consola, sin depender del IDE.
- [ ] Usar con soltura los **tipos, `var`, `record` y el `switch` moderno**.
- [ ] Aplicar **POO**: clases, interfaces y `record`, y saber cuándo toca cada uno.
- [ ] Manipular **colecciones** y encadenar operaciones con la API de *streams*.
- [ ] Gestionar errores con **excepciones y `Optional`**, sin devolver `null`.
- [ ] Montar un proyecto con **Maven**, añadirle una dependencia y empaquetarlo en un `.jar` ejecutable.

## Calendario

| Sesión (55') | En clase | Lectura previa |
|---|---|---|
| **S1** | Entorno y primer programa · tipos, `var`, cadenas y `switch` moderno | [1. Primeros pasos](01-primeros-pasos.md) + [2. Sintaxis y tipos](02-sintaxis-y-tipos.md) |
| **S2** | Clases, encapsulación, **`record`** y `enum`, en `jshell` | [3. POO en Java](03-poo-en-java.md) §1–4 |
| **S3** | **Interfaces** y el ejemplo largo de pedidos, paso a paso | [3. POO en Java](03-poo-en-java.md) §5–6 |
| **S4** | Colecciones a fondo: `List`, `Set`, `Map` y cuál elegir | [4. Colecciones y funcional](04-colecciones-y-funcional.md) §1–6 |
| **S5** | Lambdas, **streams** y `groupingBy` | [4. Colecciones y funcional](04-colecciones-y-funcional.md) §7–8 |
| **S6** | Excepciones y `Optional`: cuándo cada uno | [5. Excepciones y Optional](05-excepciones-y-optional.md) |
| **S7** | **Proyecto Maven paso a paso**: se junta toda la unidad | [6. Un proyecto Maven](06-proyecto-maven.md) |
| **S8** | :material-form-select: **Test de RA2 (100 %)** — 30 preguntas | [Simulacro](autoevaluacion.md) |

Cada página incluye **Pruébalo ahora** —código que se teclea en `jshell`— y **Ejercicios con solución**.

!!! tip "Las sesiones 2 a 6 se dan con `jshell` en pantalla"
    Los temas 3, 4 y 5 están escritos para eso: cada concepto lleva un fragmento que se teclea y devuelve el resultado al instante. Abre una terminal, escribe `jshell` y ve copiando.

## Antes de la S1

- [ ] JDK 25 (LTS) instalado desde Adoptium — comprueba con `java -version`.
- [ ] IntelliJ IDEA Community (recomendado) o VS Code con el Extension Pack for Java.
- [ ] Git instalado (lo usarás para entregar y para el proyecto de aula).

## Cómo se evalúa

**Un test de 30 preguntas en 55 minutos, y nada más.** Se aprueba con 15 y no hay penalización por fallo.

La mayoría son **preguntas de código**: un fragmento delante y decir qué imprime, si compila o dónde está el fallo. Salen de los **temas 1 a 6** y de la **batería de ejercicios**, de ningún otro sitio.

**El test se estudia programando.** Quien hace la batería reconoce las preguntas; quien solo lee apuntes, no.

!!! info "Ojo a lo que viene después"
    UT1, UT2 y UT3 se evalúan con test. **A partir de la UT4 los exámenes son prácticos**: programar en el ordenador con el proyecto delante. Lo que aprendas aquí es la herramienta con la que aprobarás el resto del curso.

## Material de entrenamiento

| | |
|---|---|
| [**Batería de ejercicios**](ejercicios.md) | 35 ejercicios con solución: 27 fragmentos y 8 programas completos, uno por tema. Es la práctica de la unidad |
| [**Simulacro de test**](autoevaluacion.md) | 80 preguntas con solución + una selección de 30 para cronometrar |
| [Chuleta de Java 25](chuleta.md) | La sintaxis esencial en una página |
| [Comprobar tu trabajo](../comprobar-tu-trabajo.md) | Pega tu código y recibe comentarios |
