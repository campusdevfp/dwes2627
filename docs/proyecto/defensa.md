# Cómo se defiende cada entrega

En el primer trimestre la corrección era objetiva: los tests en verde y ya. Aquí también los hay — pero además **hay que defender lo hecho**, porque la mitad del aprendizaje de un reto está en explicar por qué lo resolvisteis así y no de otra manera.

## La demo de los viernes

**Cinco minutos. Cronometrados.** Se corta a los cinco, aunque estéis a mitad.

| Minuto | Qué |
|:-:|---|
| 0–1 | **La pregunta del reto**, dicha con vuestras palabras, y qué decidisteis |
| 1–4 | **La aplicación funcionando.** En marcha, no en capturas |
| 4–5 | **Lo que no salió** y qué haríais con una semana más |

!!! warning "Tres formas de suspender una demo que funciona"
    1. **Empezar por el código.** Nadie ve un `@Configuration` proyectado y entiende nada. Se empieza por lo que hace, se enseña el código solo si alguien pregunta.
    2. **Leer.** Si hay que leer, es que no se ha ensayado.
    3. **Callar lo que falló.** El minuto 4–5 no es un castigo: es donde se ve si sabéis dónde estáis. Un equipo que dice «esto lo dejamos fuera a propósito y por esto» va por delante de uno que dice «está todo bien».

## Las tres preguntas

Después de cada demo, tres preguntas al azar, a **un miembro al azar**. Siempre del mismo tipo:

=== "1 · ¿Por qué así?"

    > «Habéis guardado el JSON crudo en Mongo. ¿Por qué no en una tabla de Postgres con una columna `jsonb`?»

    Se busca que exista **una razón**, no que sea la razón que yo tenía en la cabeza. Una decisión distinta bien argumentada vale exactamente igual.

    Lo que no vale: «porque lo pone en los apuntes» ni «porque nos salió así».

=== "2 · ¿Y si…?"

    > «¿Qué pasa si mañana la fuente duplica el tamaño de la respuesta?»
    >
    > «¿Y si dos usuarios editan la misma alerta a la vez?»

    Se busca que hayáis **pensado más allá del caso feliz**. No hace falta tenerlo resuelto; hace falta saber que existe.

=== "3 · Enséñame dónde"

    > «Enséñame la línea donde se decide que un técnico no puede lanzar una ingesta.»

    Treinta segundos para encontrarla. Se busca que **conozcáis vuestro propio código** — los tres, no solo quien lo escribió.

    Esta es la que más descoloca y la más justa: si no sabes moverte por el proyecto de tu equipo, no has participado en él.

## Qué se mira en cada reto

La misma rúbrica los seis, para que sepáis desde el principio dónde se mira:

| | Insuficiente | Correcto | Notable |
|---|---|---|---|
| **Responde a la pregunta** | Se ha programado algo, pero no resuelve lo que se preguntaba | Resuelve la pregunta del reto | La resuelve y descubre un caso que el enunciado no contemplaba |
| **Decisiones** | «Salió así» | Se justifica cada decisión técnica | Se justifica y se dice qué se descartó y por qué |
| **Funciona de verdad** | Funciona en un portátil concreto | `docker compose up` y anda | Anda, y aguanta que le apaguen cosas |
| **Reparto** | Uno hizo el trabajo | Los tres tocan código y lo conocen | Los tres pueden defender cualquier parte |
| **Lo que no salió** | Se oculta | Se reconoce | Se reconoce, se prioriza y hay plan |

## El diario de equipo

Un fichero `DIARIO.md` en el repositorio. **Tres líneas por sesión**, escritas por quien lleva el rol de producto esa semana:

```markdown
## S14 · 2027-01-19
- Hecho: filtro por barrio en el listado; fragmento de paginación reutilizado.
- Atascados en: el `th:each` anidado no veía la variable del externo. Resuelto con `th:with`.
- Mañana: formulario de alta de alerta con validación.
```

Cuesta dos minutos y sirve para tres cosas: no perder el hilo entre sesiones, tener la retro escrita, y ser **la evidencia principal de la [FFE](../index.md)** — que es un 10 % del módulo y se sostiene precisamente en esto: iniciativa, autonomía y trabajo en equipo demostrados a lo largo del trimestre, no en un día.

!!! tip "El diario no es para el profesor"
    Es para vosotros dentro de tres semanas, cuando no recordéis por qué pusisteis ese `@Transactional`. Que además sirva de evidencia es un efecto secundario.

## Y el examen, ¿qué?

El examen práctico de cada unidad —que es el **100 % de la nota** de UT7, UT8 y UT9— parte del proyecto: se te da una versión limpia del código común y se te pide **añadir una funcionalidad concreta, a solas, con tiempo tasado y con tests que la comprueban**.

Es decir: exactamente lo que llevas haciendo tres meses, pero sin tu equipo al lado. Por eso el reparto de trabajo se nota tanto.
