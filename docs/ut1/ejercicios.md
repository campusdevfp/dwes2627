# Batería de ejercicios — UT1

Doce ejercicios **de los que salen las preguntas del test**. No son teóricos: cada uno parte de algo real —una traza, un código de estado, una decisión de diseño— y se resuelve razonando.

Las [prácticas guiadas](practicas.md) son para *tocar* la tecnología; esto es para **entrenar la respuesta**.

---

### E1 ● — Lee la conversación

```
> GET /api/v1/conciertos/9999 HTTP/1.1
> Host: festival.es
> Accept: application/json
<
< HTTP/1.1 404 Not Found
< Content-Type: application/problem+json
< {"title":"Concierto no encontrado","status":404}
```

¿Qué pidió el cliente, qué contestó el servidor y de quién es la culpa?

??? success "Solución"

    El cliente pidió **leer** el concierto 9999 en formato JSON. El servidor responde **404**: la ruta es correcta pero el recurso no existe.

    La culpa es **del cliente** —los 4xx siempre lo son—, aunque aquí «culpa» significa solo que pidió algo que no está. Un 404 no es un error de la aplicación: es una respuesta correcta a una pregunta sobre algo inexistente.

    Detalle que suele pasar desapercibido: `application/problem+json` es el formato estándar de errores (RFC 7807) que usarás en la UT4.


### E2 ● — Asigna el código

Di qué código HTTP corresponde a cada situación:

1. Se crea un concierto correctamente.
2. Se borra un concierto correctamente.
3. Falta el campo `artista` en el cuerpo.
4. No se ha enviado ningún token.
5. Hay token, pero el usuario no es administrador.
6. Se intenta crear un concierto con un nombre que ya existe.
7. Se cae la base de datos.

??? success "Solución"

    | # | Código | |
    |---|---|---|
    | 1 | **201** Created | y con cabecera `Location` apuntando al recurso nuevo |
    | 2 | **204** No Content | se borró; no hay nada que devolver |
    | 3 | **400** Bad Request | la petición está mal formada |
    | 4 | **401** Unauthorized | *no sé quién eres* |
    | 5 | **403** Forbidden | *sé quién eres y no puedes* |
    | 6 | **409** Conflict | choca con el estado actual del servidor |
    | 7 | **500** Internal Server Error | culpa del servidor |

    El 4 y el 5 son **la pregunta que más se falla del examen**. El nombre no ayuda: «Unauthorized» debería llamarse «Unauthenticated».


### E3 ●● — ¿Seguro? ¿Idempotente?

Clasifica: `GET /productos`, `POST /pedidos`, `PUT /productos/7`, `DELETE /productos/7`, `PATCH /productos/7`.

??? success "Solución"

    | Método | Seguro | Idempotente | Repetirlo… |
    |---|:-:|:-:|---|
    | `GET` | | | no cambia nada |
    | `POST` | :material-close: | :material-close: | **crea otro pedido** |
    | `PUT` | :material-close: | | deja el mismo resultado |
    | `DELETE` | :material-close: | | el segundo da 404, pero el estado final es el mismo |
    | `PATCH` | :material-close: | :material-close: | depende (`stock -= 1` acumula) |

    **Seguro** = no modifica nada. **Idempotente** = repetirlo deja el mismo estado final.

    Consecuencia práctica que verás en la UT8: el borrado **no puede ser un enlace**, porque los `GET` deben ser seguros y el precargador del navegador podría dispararlo.


### E4 ●● — Diseña los endpoints

Diseña la API de un festival: listar conciertos, ver uno, crearlo, ver sus entradas y comprar una. Sin verbos en la URL.

??? success "Solución"

    ```
    GET    /api/v1/conciertos                 200
    GET    /api/v1/conciertos/{id}            200 · 404
    POST   /api/v1/conciertos                 201 + Location · 400 · 409
    GET    /api/v1/conciertos/{id}/entradas   200 · 404
    POST   /api/v1/entradas                   201 + Location · 400 · 404 · 409
    ```

    Los errores típicos que se corrigen aquí: `/getConciertos` (verbo en la URL), `/concierto` en singular, `/comprarEntrada` (acción en vez de recurso) y `/api/conciertos/borrar/7` (las tres cosas mal a la vez).

    El verbo lo pone **HTTP**; la URL nombra **cosas**.


### E5 ●● — Desmonta un JWT

Este token está en tres partes separadas por puntos. ¿Qué contiene cada una, cuál puede leer cualquiera y qué garantiza la firma?

??? success "Solución"

    `cabecera.carga.firma`, las dos primeras en **Base64, que no es cifrado**: cualquiera las decodifica en [jwt.io](https://jwt.io).

    - **Cabecera**: el algoritmo de firma (`HS256`, `RS256`).
    - **Carga**: quién es el usuario, sus roles y cuándo caduca (`exp`).
    - **Firma**: calculada con una clave secreta que solo tiene el servidor.

    La firma garantiza que **nadie ha manipulado** el contenido. **No** garantiza que sea secreto.

    De ahí la regla que cae en el examen: **nunca metas datos sensibles en un JWT**. Van a la vista de todos.


### E6 ●● — Elige la arquitectura

Justifica con dos argumentos en cada caso: (a) la web de una panadería con 30 visitas al día, (b) una plataforma de vídeo con picos de audiencia, (c) una aplicación interna de gestión para 40 empleados.

??? success "Solución"

    **(a) Monolito, SSR.** El coste de operar microservicios no se justifica con 30 visitas, y el contenido tiene que indexarse en buscadores.

    **(b) Microservicios o serverless.** La transcodificación de vídeo y el catálogo escalan de forma muy distinta; separarlos permite dimensionar cada pieza y aguantar los picos.

    **(c) Monolito modular.** Un solo despliegue —lo mantiene un equipo pequeño—, con fronteras internas claras por si mañana crece.

    La respuesta que puntúa no es la etiqueta: es **el porqué**. «Microservicios porque son modernos» vale cero.


### E7 ●●● — SSR o SPA

Decide para: (a) un periódico digital, (b) un editor de fotos en línea, (c) la tienda de un ayuntamiento.

??? success "Solución"

    **(a) SSR.** El posicionamiento en buscadores es su negocio y la primera pantalla tiene que llegar rápida.

    **(b) SPA.** Interacción intensa y continua: arrastrar, deshacer, previsualizar. Nadie busca un editor en Google por su contenido.

    **(c) SSR, o híbrido.** Contenido indexable, obligación de accesibilidad y usuarios con conexiones y dispositivos muy dispares.

    Los dos criterios que hay que nombrar siempre: **indexación** y **grado de interactividad**.


### E8 ●● — Servidor web o de aplicaciones

Un compañero dice que usa «Apache para ejecutar su código Java». Corrígelo y explica cómo se colocan las piezas en producción.

??? success "Solución"

    Apache o Nginx son **servidores web**: sirven ficheros estáticos, terminan el TLS y hacen de proxy inverso. **No ejecutan Java.**

    Tomcat es un **servidor de aplicaciones**: ejecuta tu código. En Spring Boot va **dentro del propio `.jar`**.

    En producción se combinan:

    ```
    Internet → Nginx (443, HTTPS, estáticos) → Spring Boot + Tomcat (8080, interno) → BD (5432, interno)
    ```

    Y una regla que cae: **los puertos internos nunca se exponen a internet**.


### E9 ●● — Autenticación o autorización

Clasifica: (a) iniciar sesión con usuario y contraseña, (b) que solo el admin pueda borrar, (c) que un usuario vea únicamente sus pedidos, (d) validar el token de cada petición, (e) el mensaje «tu cuenta no tiene acceso a esta sección».

??? success "Solución"

    | | | Código |
    |---|---|---|
    | (a) | Autenticación | — |
    | (b) | Autorización | 403 |
    | (c) | Autorización **por dato**, no solo por rol | 403 |
    | (d) | Autenticación | 401 si falla |
    | (e) | Autorización | 403 |

    El (c) es el más fino y el que separa notas: no basta con «este rol puede», hay que comprobar **que ese pedido es suyo**. Lo implementarás en la UT7.


### E10 ●●● — Diagnostica por el log

```
192.168.1.40 - - [12/Nov/2025:10:03:11] "POST /login HTTP/1.1" 401 92
192.168.1.40 - - [12/Nov/2025:10:03:12] "POST /login HTTP/1.1" 401 92
192.168.1.40 - - [12/Nov/2025:10:03:12] "POST /login HTTP/1.1" 401 92
192.168.1.40 - - [12/Nov/2025:10:03:13] "POST /login HTTP/1.1" 401 92
```

¿Qué está pasando y qué harías?

??? success "Solución"

    **Un ataque de fuerza bruta**: la misma IP probando contraseñas, cuatro intentos en dos segundos. Un humano no teclea así.

    Medidas, de más simple a más completa: limitar el número de intentos por IP y por cuenta, introducir un retardo creciente tras cada fallo, bloquear temporalmente la cuenta y añadir un segundo factor.

    Y lo que **no** se hace: escribir en el log qué contraseña se probó. Ahí es donde se acaba filtrando la buena.


### E11 ●● — Qué se registra y qué no

De esta lista, di qué puede ir a un log y qué no: IP de origen, contraseña, código de respuesta, número de tarjeta, ruta pedida, token JWT, tiempo de respuesta, DNI del usuario.

??? success "Solución"

    | Va al log | No va nunca |
    |---|---|
    | IP de origen | Contraseñas |
    | Código de respuesta | Números de tarjeta |
    | Ruta pedida | Tokens completos |
    | Tiempo de respuesta | Datos personales (DNI, dirección) |

    La IP es un caso intermedio: es un dato personal según el RGPD, así que se registra con una finalidad concreta y un plazo de conservación.

    Y una que sorprende: **no rotar los logs tumba servidores**. El disco se llena y todo se detiene.


### E12 ●●● — El caso completo

Un ayuntamiento quiere una web para reservar pistas deportivas: consulta pública de horarios, reserva con usuario, y un panel de gestión. Decide arquitectura, forma de renderizar, tipo de API y esquema de despliegue, con una línea de justificación en cada punto.

??? success "Solución"

    | Decisión | Elección | Por qué |
    |---|---|---|
    | Arquitectura | Monolito modular en capas | Lo mantiene un equipo pequeño; las fronteras internas permiten crecer |
    | Renderizado | SSR con retoques dinámicos | Contenido público indexable, accesibilidad obligatoria, usuarios con dispositivos dispares |
    | API | REST | Estándar, cacheable y suficiente; una app móvil futura la reutiliza |
    | Autenticación | Sesión para la web, token si hay app | Sesión es más simple y el navegador la gestiona solo |
    | Despliegue | `.jar` en contenedor, Nginx delante, PostgreSQL | Reproducible y con el TLS terminado antes de la aplicación |

    Este ejercicio es **la sección final del test**, y se corrige por la justificación, no por la elección. Dos alumnos pueden elegir distinto y sacar los dos la máxima nota.


---

## Reparto sugerido

| Ejercicio | Nivel | Sesión |
|---|:-:|:-:|
| E1 · Lee la conversación | ● | S4 |
| E2 · Asigna el código | ● | S4 |
| E3 · Seguro e idempotente | ●● | S5 |
| E4 · Diseña los endpoints | ●● | S6 |
| E5 · Desmonta un JWT | ●● | S7 |
| E6 · Elige la arquitectura | ●● | S3 |
| E7 · SSR o SPA | ●●● | S8 |
| E8 · Servidor web o de aplicaciones | ●● | S9 |
| E9 · Autenticación o autorización | ●● | S9 |
| E10 · Diagnostica por el log | ●●● | S9 |
| E11 · Qué se registra | ●● | S9 |
| E12 · El caso completo | ●●● | S9 |
