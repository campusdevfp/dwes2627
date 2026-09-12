# Preparar el examen — UT1

**:material-form-select: Test aplicado · 100 % de la nota · 55 min · S10**

## Formato

**30 preguntas** de opción múltiple, una sola respuesta correcta y **sin penalización por fallo**. Se aprueba con **15 aciertos**.

No es un test de memoria. Está construido sobre lo que hiciste en clase: salidas de `curl`, tu `Servidor.java`, el JWT que desmontaste en jwt.io, el diagrama que dibujaste.

## Cómo se reparten las preguntas

| Bloque | Preguntas | De dónde salen |
|---|:-:|---|
| **A · Cliente, servidor y arquitecturas** | 7 | Temas 1–3 |
| **B · HTTP** | 8 | Tema 4 + práctica P1 |
| **C · APIs** | 7 | Tema 5 + prácticas P3 y P4 |
| **D · Web dinámica, lenguajes y Java** | 4 | Temas 6–7 + práctica P6 |
| **E · Servidores, despliegue y seguridad** | 4 | Temas 8–9 + prácticas P5 y P7 |

Los bloques **B y C suman la mitad del examen**. Si tienes que priorizar el repaso, es ahí.

## Qué se pregunta de verdad

Tres formas, y conviene reconocerlas:

**1. Conceptual con caso.** No *«¿qué es un método idempotente?»*, sino *«un usuario pulsa F5 tras enviar un formulario y se crea un pedido duplicado. ¿Qué método HTTP se usó y cuál debió usarse?»*.

**2. Lectura de una salida real.** Se te da un intercambio HTTP o un JSON y hay que interpretarlo:

```
< HTTP/1.1 201 Created
< Location: /api/v1/conciertos/7
```

*«¿Qué acaba de pasar y dónde está el recurso nuevo?»*

**3. Decisión justificada.** *«Una web de un ayuntamiento con 200 visitas al día y mucho contenido de texto: ¿SSR o SPA?»*. Se valora la respuesta que atiende a indexación y primera pantalla.

## Cómo prepararte

En este orden, y empezando una semana antes:

1. **Rehaz las prácticas P1, P3 y P4** sin mirar los apuntes. Son la fuente directa del 40 % de las preguntas.
2. **Haz la [autoevaluación](autoevaluacion.md) entera** — unas 80 preguntas con solución. Apunta las que fallas.
3. **Repasa la [chuleta](chuleta.md)**: códigos HTTP, métodos y vocabulario de arquitecturas.
4. **Dibuja el mapa mental de memoria**, con los apuntes cerrados. Lo que no salga solo es lo que falta.

!!! tip "La prueba de fuego"
    Explícale a alguien que no sepa informática qué pasa desde que escribe una dirección hasta que ve la página. Si puedes hacerlo sin trabarte y nombrando DNS, HTTP, servidor y base de datos, tienes el examen aprobado.

## Errores que más cuestan

1. **Confundir 401 con 403.** Sin identificar → **401**. Identificado pero sin permiso → **403**. Cae todos los años.
2. **Confundir autenticación con autorización.** El pasaporte te identifica; la pulsera dice a qué tienes derecho.
3. **Creer que Apache y Tomcat compiten.** Uno sirve ficheros, el otro ejecuta tu código. En producción van juntos.
4. **Decir que `GET` y `POST` son intercambiables.** El `GET` debe ser seguro e idempotente; si lo usas para borrar, un rastreador te vacía la base de datos.
5. **Pensar que HTTPS cifra la URL entera.** Cifra la ruta y el cuerpo, pero el **dominio viaja visible**.
6. **Decir que un JWT está cifrado.** Está **firmado**, no cifrado: cualquiera lo lee en jwt.io. La firma garantiza que no se ha manipulado, no que sea secreto.
7. **Confundir REST con «devolver JSON».** REST son recursos, verbos y códigos; el formato es otra cosa.
8. **Olvidar que HTTP no tiene memoria.** Es el origen de las cookies, de la sesión y de todo lo que verás en la UT7.

## El día del examen

- 30 preguntas en 55 minutos: **menos de dos minutos por pregunta**. No te atasques; marca y sigue.
- Sin penalización: **contesta todas**. Una respuesta en blanco es un cero seguro.
- Descarta primero las dos opciones imposibles y decide entre las dos que quedan.
