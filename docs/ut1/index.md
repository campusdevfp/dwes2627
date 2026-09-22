# UT1 — Arquitecturas y tecnologías web en servidor

**6 h · 6 sesiones · Trimestre 1.º** · Evaluación: :material-form-select: **test (100 %)**

> **RA1:** Selecciona las arquitecturas y tecnologías de programación web en entorno servidor, analizando sus capacidades y características propias.

Esta unidad es el **mapa del territorio**: el vocabulario y los modelos que usarás todo el curso. No es una unidad de mirar — se lanzan peticiones de verdad desde la terminal desde la primera sesión.

!!! info "Qué entra y qué no"
    **Entra:** los temas **1 a 6** y la [batería de ejercicios](ejercicios.md). De ahí sale todo lo que pregunta el test.

    **No entra:** los temas [7 · Java en 2026](07-java-en-2026.md), [8 · Servidores y despliegue](08-servidores-y-despliegue.md) y [9 · Seguridad y monitorización](09-seguridad-y-logs.md). Son **divulgación**: ordenan el panorama y merecen una lectura, pero no se preguntan ni hay ejercicios sobre ellos.

## Al terminar sabrás hacer

Marca cada casilla cuando puedas hacerlo **sin mirar los apuntes**. Lo que quede sin marcar la semana del test es exactamente lo que hay que repasar.

- [ ] Explicar **el recorrido completo de una petición** desde que se escribe la URL hasta que se pinta la página.
- [ ] Decidir qué se ejecuta **en el cliente** y qué **en el servidor**, y por qué esa frontera importa.
- [ ] Comparar monolito, capas, microservicios y *serverless*, y **justificar** cuál encaja en un caso dado.
- [ ] Repartir responsabilidades en **MVC** y reconocer los cinco principios **SOLID**.
- [ ] Leer una petición y una respuesta **HTTP** completas, y elegir el **código de estado** correcto.
- [ ] Diseñar los **endpoints** de una API REST y proteger lo que hay que proteger.
- [ ] Distinguir **estático y dinámico**, **SSR y CSR**, y situar Java y Spring en el mapa.

## Calendario

| Sesión (55') | En clase | Lectura previa |
|---|---|---|
| **S1** | Presentación · **el entorno del módulo** · el viaje de una petición | [Entorno](../entorno/index.md) + [1. El viaje de una petición](01-el-viaje-de-una-peticion.md) |
| **S2** | Cliente y servidor · arquitecturas: del monolito al *serverless* · MVC y SOLID | [2. Cliente y servidor](02-cliente-y-servidor.md) + [3. Arquitecturas](03-arquitecturas-y-mvc.md) |
| **S3** | HTTP de principio a fin, con `curl` en pantalla: mensajes, métodos, códigos y cookies | [4. HTTP a fondo](04-http-y-apis.md) |
| **S4** | APIs: diseño REST, JWT y tipos (GraphQL, gRPC, WebSocket) | [5. APIs a fondo](05-apis-a-fondo.md) |
| **S5** | Web dinámica y Java en el servidor · tu primer servidor en un fichero | [6. Web dinámica](06-web-dinamica-y-lenguajes.md) |
| **S6** | :material-form-select: **Test de RA1 (100 % de la nota)** — 30 preguntas, 55 min | [Simulacro](autoevaluacion.md) + [resumen](10-resumen-y-autoevaluacion.md) |

Cada sesión abre con un **micro-reto de 25 minutos** antes de explicar nada: ver [cómo se trabaja el 1.er trimestre](../retos-1t.md).

## Cómo se evalúa

**Un test de 30 preguntas en 55 minutos, y nada más.** Se aprueba con 15 aciertos y **no hay penalización por fallo**, así que se contestan todas.

No es un test de definiciones. Las preguntas ponen delante **una salida de `curl`, un trozo de JSON o un caso concreto** y hay que decidir. Salen de dos sitios y de ninguno más:

| De dónde | Qué pregunta |
|---|---|
| **Temas 1 a 6** | Los conceptos, aplicados |
| **[Batería de ejercicios](ejercicios.md)** | Lo que hiciste tú: códigos que provocaste, tiempos que mediste, el JWT que abriste |

Traducción práctica: **el test se estudia haciendo**. Quien completa la batería reconoce casi todas las preguntas; quien solo lee los apuntes, no.

## Cómo entrenar

| | |
|---|---|
| [**Batería de ejercicios**](ejercicios.md) | 34 ejercicios con solución, de los temas 1 a 6. Es la práctica de la unidad |
| [**Simulacro de test**](autoevaluacion.md) | 72 preguntas con solución + una selección de 30 para cronometrar |
| [Resumen de la unidad](10-resumen-y-autoevaluacion.md) | Las diez ideas y el glosario |
| [Chuleta](chuleta.md) | Códigos, métodos y vocabulario en una página |
