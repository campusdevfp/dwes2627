# UT2 — Java moderno: el lenguaje del servidor

**8 h · 8 sesiones · Trimestre 1.º** · Evaluación: :material-form-select: **test aplicado (100 %)**

> **RA2:** Escribe sentencias ejecutables por un servidor web, reconociendo y aplicando procedimientos de integración del código.

En la UT1 aprendiste *qué* pasa en un servidor. Aquí aprendes **a escribirlo**. Trabajamos con **Java 25 (LTS)**, el lenguaje de todo el resto del curso: en el trimestre 1 construiremos APIs REST con Spring Boot y en el 2, páginas dinámicas.

!!! tip "Vienes de 1.º con base de programación"
    No empezamos de cero: ya sabes qué es una variable, un bucle y una clase. Esta unidad es una **conversión rápida a Java** y, sobre todo, al **Java moderno** (records, `var`, streams, virtual threads) — que se parece poco al Java de los tutoriales de 2015.


## Al terminar sabrás hacer

Marca cada casilla cuando puedas hacerlo **sin mirar los apuntes**. Lo que quede sin marcar la semana del examen es exactamente lo que hay que repasar.

- [ ] Escribir, compilar y ejecutar programas **Java 25** desde la consola, sin depender del IDE.
- [ ] Usar con soltura los **tipos, `var`, `record` y el `switch` moderno**.
- [ ] Aplicar **POO**: clases, interfaces y `record`, y saber cuándo toca cada uno.
- [ ] Manipular **colecciones** y encadenar operaciones con la API de *streams*.
- [ ] Gestionar errores con **excepciones y `Optional`**, sin devolver `null`.
- [ ] Montar un proyecto con **Maven o Gradle** y escribir sus primeros **tests con JUnit 5**.

## Calendario

| Sesión (55') | En clase | Lectura previa |
|---|---|---|
| **S1** | Entorno: JDK 25, IntelliJ, tu primer programa | [IntelliJ](../entorno/intellij/) + [1. Primeros pasos](01-primeros-pasos/) |
| **S2** | Tipos, `var`, operadores, control de flujo y el `switch` moderno | [2. Sintaxis y tipos](02-sintaxis-y-tipos/) completo |
| **S3** | Clases y objetos · **records**, enums y `equals`/`toString` | [3. POO en Java](03-poo-en-java/) §1–4 |
| **S4** | Herencia, interfaces y polimorfismo | [3. POO en Java](03-poo-en-java/) §5–6 |
| **S5** | Colecciones: `List`, `Map`, `Set` | [4. Colecciones y funcional](04-colecciones-y-funcional/) §1–2 |
| **S6** | Lambdas y **Streams** (el gran salto) | [4. Colecciones y funcional](04-colecciones-y-funcional/) §3–4 |
| **S7** | Excepciones y `Optional` · Maven, librerías y testing con JUnit 5 | [5. Excepciones y Optional](05-excepciones-y-optional/) + [6. Proyectos y testing](06-proyectos-y-testing/) |
| **S8** | :material-form-select: **Test de RA2 (100 %)** — 30 preguntas | Repasa con la [autoevaluación](autoevaluacion/) |

Cada página incluye **Pruébalo ahora** (código que ejecutas) y **Ejercicios con solución**.

## Cómo se evalúa: UN único test (100 %)

Igual que en la UT1: un test de **30 preguntas (55 min)** que mezcla teoría y, sobre todo, **práctica aplicada**: leer código y decir qué imprime, detectar el error de compilación, elegir la colección correcta, interpretar un stream, corregir un `catch`…

!!! info "Ojo a lo que viene después"
    UT1, UT2 y UT3 se evalúan con test. **A partir de la UT4 los exámenes son prácticos**: programar en el ordenador. Lo que aprendas aquí es la herramienta con la que aprobarás el resto del curso.

Se supera con **≥ 5**. Como en la UT1: **el test se estudia programando**. Quien hace los ejercicios reconoce las preguntas; quien solo lee apuntes, no.

## Antes de la S1

- [ ] JDK 25 (LTS) instalado desde Adoptium — comprueba con `java -version`.
- [ ] IntelliJ IDEA Community (recomendado) o VS Code con el Extension Pack for Java.
- [ ] Git instalado (lo usarás para entregar y para el proyecto de aula).

## Material de entrenamiento

- [Batería de ejercicios](ejercicios.md)
    : 12 ejercicios resueltos sobre otro dominio (una liga de baloncesto).
- [Prácticas guiadas](practicas.md)
    : ejercicios de programación con enunciado y solución.
- Autoevaluación
    : banco de preguntas tipo test con soluciones.
- [Comprobar tu trabajo](../comprobar-tu-trabajo/) — los tests del reto de tu unidad.
    : pega tu código y recibe feedback + simulacros del test.
- [Chuleta de Java 25](chuleta.md)
    : la sintaxis esencial en una página.
- [Preparar el examen](examen.md)
    : formato, reparto de preguntas y los diez errores que más cuestan.
