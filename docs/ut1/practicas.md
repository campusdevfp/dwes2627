# Prácticas guiadas tangibles — UT1 · RA1

Ejercicios **hands-on** para hacer en el aula (en el bloque de *pair work*) o en casa. Cada uno produce algo **tangible** (una captura, un fichero, un diagrama, una colección) que se puede entregar y revisar con la rúbrica.

> Aunque el RA1 es teórico, aquí se "toca" la tecnología: peticiones reales, un servidor levantado, un contenedor corriendo. Así el vocabulario deja de ser abstracto.

**Herramientas base:** navegador con DevTools, terminal, [Bruno](https://www.usebruno.com) o `curl`, [httpbin.org](https://httpbin.org), [jwt.io](https://jwt.io). Docker es opcional (solo P7).

| # | Práctica | Tema/Sesión | Herramientas | Entregable |
|---|----------|-------------|--------------|------------|
| P1 | Radiografía HTTP | T4 / S3 | curl o Bruno | Tabla/colección de peticiones + capturas |
| P2 | Autopsia de una web | T1–T2 / S1 | DevTools | Informe de peticiones y recursos |
| P3 | Explora una API REST | T5 / S3 | Bruno o curl | Ficha de 3 endpoints |
| P4 | Desmonta un JWT | T5 / S3 | jwt.io | Análisis del token |
| P5 | Monta tu servidor web | T8 / S5 | jwebserver (JDK)/Nginx | Captura sirviendo una web |
| P6 | Estática vs. dinámica | T6 / S4 | Java (JDK 25+) | Dos páginas y captura comparada |
| P7 | Dockeriza un "hola mundo" | T9 / S5 | Docker | Dockerfile + contenedor corriendo |
| P8 | Diagrama de arquitectura | T3 / S2 | Mermaid | Diagrama + decisiones |

Cada práctica se autoevalúa con el [comprobar tu trabajo con los tests](../comprobar-tu-trabajo.md) usando su rúbrica. **Ojo: el test de la unidad (100 % de la nota) pregunta directamente sobre estas prácticas.** Objetivo de entrenamiento: ≥ 80 % en cada una.

---

## P1 — Radiografía HTTP (T4)
**Objetivo:** ver y entender peticiones/respuestas HTTP reales. *(CE1.a)*
**Pasos:**
1. Contra `https://httpbin.org`, lanza (con `curl -i` o Bruno):
   - un **GET** normal,
   - un **POST** enviando `nombre=<tu nombre>`,
   - una petición que provoque un **404**,
   - una que devuelva tus **cabeceras** (`/headers`).
2. Para cada una anota: método, URL, **código de estado**, y el `Content-Type` de la respuesta.
**Entregable:** una tabla con las 4-6 peticiones (método, URL, código, formato) + captura de cada respuesta. Si usas Bruno, exporta la **colección** (`.json`).
**Rúbrica:** peticiones correctas (40 %) · códigos bien identificados (30 %) · distingue respuesta HTML vs JSON (20 %) · presentación (10 %).

## P2 — Autopsia de una web con DevTools (T1–T2)
**Objetivo:** identificar los componentes y el tráfico real de una web. *(CE1.a)*
**Pasos:**
1. Abre una web real y `F12 → Network`. Recarga.
2. Cuenta **cuántas peticiones** se lanzan y clasifícalas por tipo (documento, CSS, JS, imagen, **llamadas a API/XHR**).
3. Localiza 2 peticiones con códigos distintos (ej. 200 y 304) y explica la diferencia.
**Entregable:** informe breve (media página) con nº de peticiones, tabla por tipo, 2 capturas señaladas y una frase: "¿qué hace aquí el front y qué el back?".
**Rúbrica:** clasificación correcta (40 %) · lectura de códigos (30 %) · distinción front/back (20 %) · claridad (10 %).

## P3 — Explora una API REST pública (T5)
**Objetivo:** consumir una API y documentar sus endpoints. *(CE1.g)*
**Pasos:**
1. Elige una API pública sencilla (p. ej. `https://api.github.com`, `https://pokeapi.co`, `https://jsonplaceholder.typicode.com`).
2. Haz **3 peticiones GET** distintas y observa el **JSON** de respuesta.
3. Documenta cada endpoint: método, URL, parámetros, y 3 campos interesantes de la respuesta.
**Entregable:** ficha de 3 endpoints (tabla) + una captura del JSON de uno de ellos.
**Rúbrica:** endpoints válidos (40 %) · documentación clara (30 %) · identifica estructura JSON (20 %) · presentación (10 %).

## P4 — Desmonta un JWT (T5)
**Objetivo:** entender la estructura y los riesgos de un JWT. *(CE1.g)*
**Pasos:**
1. En [jwt.io](https://jwt.io), pega un JWT de ejemplo (o genera uno).
2. Identifica sus **3 partes** (header, payload, signature) y qué hay en cada una.
3. Cambia un dato del payload y observa qué pasa con la firma.
**Entregable:** documento con las 3 partes explicadas, qué claims ves en el payload, y una respuesta: *"¿por qué NO se debe guardar la contraseña en el payload?"*.
**Rúbrica:** identifica las 3 partes (40 %) · explica el payload (25 %) · entiende la firma (25 %) · riesgo de seguridad (10 %).

## P5 — Monta tu servidor web (T8)
**Objetivo:** servir una web y distinguir servidor web de contenido. *(CE1.f, CE1.e)*
**Pasos (elige una vía):**
- **Rápida (con el JDK):** crea un `index.html` y sírvelo con **`jwebserver -p 8000`** (incluido en el JDK). Abre `http://localhost:8000`.
- **Avanzada:** instala Apache/Nginx, coloca la web en el directorio raíz y (opcional) configura un **VirtualHost**.
**Entregable:** captura del navegador mostrando tu web servida + 3 líneas explicando quién es el "servidor web" aquí y qué diferencia hay con un servidor de aplicaciones. Si hiciste VirtualHost, adjunta el fichero de config.
**Rúbrica:** web servida y accesible (50 %) · explica servidor web vs. aplicaciones (30 %) · extra VirtualHost (20 %).

## P6 — Estática vs. dinámica en vivo (T6)
**Objetivo:** comprobar la diferencia de forma tangible. *(CE1.b)*
**Pasos:**
1. Crea una página **estática** `hora.html` con una hora escrita a mano.
2. Crea un mini endpoint **dinámico** en **Java** que devuelva la **hora actual** del servidor: usa el `Servidor.java` del tema (JDK 25+, se ejecuta con `java Servidor.java`, sin proyecto ni compilación aparte).
3. Recarga varias veces cada una.
**Entregable:** los dos ficheros/URLs + captura mostrando que la estática **no cambia** y la dinámica **sí**. Una frase: *"¿por qué la dinámica cambia?"*.
**Rúbrica:** estática correcta (25 %) · dinámica que cambia (45 %) · explicación (30 %).

## P7 — Dockeriza un "hola mundo" (T8) *(opcional/avanzada)*
**Objetivo:** empaquetar una app en un contenedor. *(CE1.e)*
**Pasos:**
1. Crea un `index.html` o un mini servidor.
2. Escribe un `Dockerfile` (por ejemplo con imagen `nginx`, o `eclipse-temurin` si empaquetas tu `Servidor.java`).
3. `docker build -t holamundo .` y `docker run -p 8080:80 holamundo`. Abre `http://localhost:8080`.
4. (Extra) Un `docker-compose.yml` que levante tu web + una BD (MySQL/Mongo).
**Entregable:** el `Dockerfile` + captura del contenedor corriendo (`docker ps`) y de la web en el navegador.
**Rúbrica:** Dockerfile válido (40 %) · contenedor corriendo (40 %) · extra compose (20 %).

## P8 — Diagrama de arquitectura de un producto real (T3)
**Objetivo:** representar la arquitectura de un sistema conocido. *(CE1.g)*
**Pasos:**
1. Elige un producto (una tienda online, un chat, un banco…).
2. Dibuja en **Mermaid** el flujo cliente → servidor web → back-end → BD, y marca si crees que es monolito o microservicios.
3. Añade una decisión: ¿qué arquitectura elegirías tú si lo construyeras desde cero y por qué?
**Entregable:** el diagrama Mermaid (renderizado) + 3-4 líneas de justificación.
**Rúbrica:** diagrama correcto (40 %) · componentes bien ubicados (30 %) · justificación de arquitectura (30 %).

---

## Relación con las sesiones

Estas prácticas **son el material del bloque *pair work*** de las sesiones (ver el calendario de clase). No hace falta hacer las 8: elige las que encajen con tu ritmo. En esta unidad **no hay examen práctico aparte: la nota es 100 % el test**, y su sección práctica se construye sobre estas prácticas y el [caso HuertoVecino](practica-evaluable.md). Hacerlas ES estudiar.

## Autoevaluación con IA

Estas prácticas de la UT1 producen documentos y diagramas, no código, así que no llevan tests automáticos: se revisan en la puesta en común con la rúbrica delante. Desde la **UT2** cada práctica sí tiene sus tests en el [proyecto base](../comprobar-tu-trabajo.md).
