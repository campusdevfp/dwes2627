# UT1 — Arquitecturas y tecnologías web en servidor

**6 h · 6 sesiones · Trimestre 1.º** · Evaluación: :material-form-select: **test aplicado (100 %)**

> **RA1:** Selecciona las arquitecturas y tecnologías de programación web en entorno servidor, analizando sus capacidades y características propias.

Esta unidad es el **mapa del territorio**: el vocabulario y los modelos que usarás todo el curso — y que debe manejar cualquier desarrollador en 2026. No es una unidad de mirar: en ella lanzas peticiones reales, escribes **tu primer servidor en Java** y despliegas un contenedor.


## Al terminar sabrás hacer

Marca cada casilla cuando puedas hacerlo **sin mirar los apuntes**. Lo que quede sin marcar la semana del examen es exactamente lo que hay que repasar.

- [ ] Explicar **el recorrido completo de una petición** desde que se escribe la URL hasta que llega la respuesta.
- [ ] Distinguir qué se ejecuta **en el cliente** y qué **en el servidor**, y por qué esa frontera importa.
- [ ] Comparar monolito, capas y microservicios, y **justificar** cuál encaja en un escenario dado.
- [ ] Leer e interpretar una petición y una respuesta **HTTP** completas: método, ruta, cabeceras, cuerpo y código de estado.
- [ ] Diferenciar los tipos de **API** y decir qué problema resuelve cada uno.
- [ ] Situar **Java y Spring** en el mapa de lenguajes y tecnologías de servidor de 2026.
- [ ] Describir cómo se **despliega** una aplicación y qué hay que vigilar cuando está en marcha.

## Calendario: qué se hace en cada sesión

| Sesión (55') | En clase | Lectura previa |
|--------------|----------|----------------|
| **S1** | Presentación · **el entorno del módulo** · el viaje de una petición | [Entorno](../entorno/index.md) + [1. El viaje de una petición](01-el-viaje-de-una-peticion.md) |
| **S2** | Cliente y servidor · arquitecturas: del monolito al *serverless* · MVC y SOLID | [2. Cliente y servidor](02-cliente-y-servidor.md) + [3. Arquitecturas](03-arquitecturas-y-mvc.md) |
| **S3** | HTTP de principio a fin: mensajes, métodos, códigos, cookies y HTTPS (*live coding* con `curl`) | [4. HTTP a fondo](04-http-y-apis.md) completo |
| **S4** | APIs: diseño REST, seguridad con JWT y tipos (GraphQL, gRPC, WebSocket) | [5. APIs a fondo](05-apis-a-fondo.md) completo |
| **S5** | Web dinámica y Java en el servidor · servidores, despliegue, seguridad y logs · repaso | [6. Web dinámica](06-web-dinamica-y-lenguajes.md) + [8. Servidores](08-servidores-y-despliegue.md) + [9. Seguridad](09-seguridad-y-logs.md) |
| **S6** | :material-form-select: **Test de RA1 (100 % de la nota)** — 30 preguntas, 55 min | Repasa con la [autoevaluación](autoevaluacion.md) |

Cada página del tema incluye **Pruébalo ahora** (para hacer en tu equipo) y **Ejercicios con solución**.

## Cómo se evalúa: UN único test (100 %)

En esta unidad **no hay examen práctico aparte**. La nota completa sale de **un test de 30 preguntas (55 min, en la S10)** que mezcla:

- **Teoría** (lo esencial de las páginas del tema).
- **Práctica aplicada**: preguntas construidas sobre lo que hiciste en clase — salidas de `curl`, tu `Servidor.java`, el JWT de jwt.io, diseño de endpoints, Docker…
- **El caso HuertoVecino** ([práctica formativa](practica-evaluable.md)): la sección final del test es ese caso.

Se supera con **≥ 5** (15 aciertos de 30). Traducción práctica: **el test se estudia haciendo**. Quien completa las prácticas reconoce casi todas las preguntas; quien solo memoriza apuntes, no.

## Cómo entrenar

- [Batería de ejercicios](ejercicios.md): 12 ejercicios resueltos del tipo que cae en el test.
- [Prácticas guiadas](practicas.md): los ejercicios hands-on de los que sale la sección práctica del test.
- [Caso HuertoVecino](practica-evaluable.md): la práctica formativa de la que sale la sección de caso.
- [comprobar tu trabajo con los tests](../comprobar-tu-trabajo.md): feedback de tus entregas + simulacros de test (con ejemplos reales de uso).
- [Autoevaluación](autoevaluacion.md): ~80 preguntas tipo test con soluciones.
- [Chuleta](chuleta.md): códigos, métodos y vocabulario en una página.
- [Preparar el examen](examen.md): formato, reparto de preguntas y errores que más cuestan.
