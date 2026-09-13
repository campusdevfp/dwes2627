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

## Taller con la consola

!!! reto "Del E13 al E24 se hacen en el ordenador"
    Los doce primeros se razonan; **estos se teclean**. Y esa es la idea de toda la unidad: la UT1 no es teoría que hay que pasar, es el mínimo para empezar a tocar.

    Todos usan APIs públicas sin clave. Si no tienes `curl` en Windows, viene con Git Bash. `jq` formatea el JSON: si no lo tienes, quítalo y lo verás en crudo.

    ```bash
    alias api='curl -s -w "\n← %{http_code} en %{time_total}s\n"'
    ```

### E13 ● — Tu primera petición cruda

Pide un recurso y mira **solo las cabeceras**, sin descargar el cuerpo.

??? success "Solución"

    ```bash
    curl -I https://httpbin.org/html
    ```
    ```
    HTTP/2 200
    content-type: text/html; charset=utf-8
    content-length: 3741
    server: gunicorn/19.9.0
    ```

    `-I` hace un **HEAD**: cabeceras sin cuerpo. Sirve para saber si algo existe, cuánto ocupa o si ha cambiado, sin bajarlo.

    Tres cosas que mirar:

    - **`HTTP/2`**, no 1.1. La versión se negocia.
    - **`content-type`** trae el tipo *y* la codificación. Ese `charset=utf-8` es lo que evita los `Ã±` de la UT3.
    - **`server`** delata qué hay detrás. En producción se oculta, precisamente por eso.

    Para ver el diálogo entero: `curl -v https://httpbin.org/html 2>&1 | head -30`

### E14 ● — Los cuatro verbos, uno a uno

Lanza GET, POST, PUT y DELETE y compara lo que devuelve cada uno.

??? success "Solución"

    ```bash
    api https://httpbin.org/get | jq '.args'

    api -X POST https://httpbin.org/post \
        -H "Content-Type: application/json" \
        -d '{"artista":"Amaia","aforo":5000}' | jq '.json'

    api -X PUT https://httpbin.org/put -d "aforo=1200" | jq '.form'
    api -X DELETE https://httpbin.org/delete | jq '.url'
    ```

    - **El GET no lleva cuerpo**: los parámetros van en la URL y salen en `.args`.
    - **El POST sí**, y `httpbin` lo devuelve en `.json` o en `.form` según el `Content-Type`.
    - `-d` ya implica POST, así que el `-X POST` sobra. En PUT y DELETE no.

    Prueba esto y explica por qué devuelve `null`:

    ```bash
    api -X POST https://httpbin.org/post -d '{"a":1}' | jq '.json'
    ```

    Sin la cabecera `Content-Type: application/json`, el servidor trata el cuerpo como un formulario y no lo interpreta como JSON. **La cabecera no es decoración: cambia el significado de lo que mandas.**

### E15 ●● — Provoca los códigos a mano

Genera un 200, 301, 404, 418 y 500, y comprueba cada uno.

??? success "Solución"

    ```bash
    for c in 200 301 404 418 500; do
      printf "%s → " "$c"
      curl -s -o /dev/null -w "%{http_code}\n" https://httpbin.org/status/$c
    done
    ```

    `-o /dev/null` tira el cuerpo, `-w "%{http_code}"` imprime solo el código. **Es la forma de comprobar una API desde un script**, y la que usarás para verificar despliegues.

    Ahora lo interesante:

    ```bash
    curl -s -o /dev/null -w "%{http_code}\n" -L https://httpbin.org/status/301
    ```

    Devuelve **200**, no 301: `-L` sigue la redirección. Por defecto `curl` **no** la sigue y el navegador **sí**. De ahí que un enlace funcione en el navegador y tu script se quede con el 301.

    El 418 es real: *I'm a teapot*, una broma de 1998 que sigue en el estándar.

### E16 ●● — Dónde se va el tiempo

Mide DNS, conexión, TLS y primer byte de una petición.

??? success "Solución"

    ```bash
    curl -s -o /dev/null -w "\
    DNS:         %{time_namelookup}s
    Conexión:    %{time_connect}s
    TLS listo:   %{time_appconnect}s
    Primer byte: %{time_starttransfer}s
    TOTAL:       %{time_total}s
    " https://www.wikipedia.org
    ```

    Los tiempos son **acumulados**, no sumandos. Para saber qué costó cada fase hay que restar:

    | Fase | Cuenta |
    |---|---|
    | Resolver el nombre | `namelookup` |
    | Abrir el TCP | `connect − namelookup` |
    | Negociar el TLS | `appconnect − connect` |
    | **Pensar la respuesta** | `starttransfer − appconnect` |
    | Descargar | `total − starttransfer` |

    Esa cuarta fila es el **TTFB**, y es lo que te dice si el problema es del servidor o de la red. Repite la orden: la segunda vez el DNS baja casi a cero porque está cacheado.

### E17 ●● — La misma URL, dos formatos

Pide el mismo recurso en JSON y en XML cambiando una sola cabecera.

??? success "Solución"

    ```bash
    curl -s -H "Accept: application/json" https://httpbin.org/anything | jq '.headers.Accept'
    curl -s -H "Accept: application/xml"  https://httpbin.org/anything | jq '.headers.Accept'
    ```

    Eso es **negociación de contenido**: el recurso es uno, las representaciones pueden ser varias. Es una de las cosas que REST hace bien.

    En Spring lo escribirás así, y funciona solo:

    ```java
    @GetMapping(value = "/conciertos", produces = {"application/json", "application/xml"})
    ```

    Y el error típico: pedir algo que el servidor no sabe dar devuelve **406 Not Acceptable**.

    ```bash
    curl -s -o /dev/null -w "%{http_code}\n" -H "Accept: application/pdf" https://httpbin.org/html
    ```

### E18 ●● — La caché, en vivo

Pide un recurso, guarda su `ETag` y vuelve a pedirlo con él.

??? success "Solución"

    ```bash
    curl -s -D cabeceras.txt -o /dev/null https://httpbin.org/etag/abc123
    grep -i etag cabeceras.txt

    curl -s -o /dev/null -w "%{http_code}\n" \
         -H 'If-None-Match: "abc123"' https://httpbin.org/etag/abc123
    ```
    ```
    ETag: "abc123"
    304
    ```

    **304 Not Modified**: el servidor dice *«lo que tienes vale»* y **no manda el cuerpo**. Ahí está el ahorro.

    Con una etiqueta distinta vuelve el 200 y el contenido entero. Esto es lo que hace tu navegador en cada recarga, y por eso en la pestaña Red aparecen tantos 304.

### E19 ●● — Desmonta un JWT sin librerías

Lee las tres partes de un token con `base64`.

??? success "Solución"

    ```bash
    TOKEN='eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhbmEiLCJyb2wiOiJBRE1JTiJ9.firma'

    echo "$TOKEN" | cut -d. -f1 | base64 -d 2>/dev/null; echo
    echo "$TOKEN" | cut -d. -f2 | base64 -d 2>/dev/null; echo
    ```
    ```
    {"alg":"HS256"}
    {"sub":"ana","rol":"ADMIN"}
    ```

    **Y esa es la lección.** Un JWT tiene tres partes y las dos primeras son **Base64, no cifrado**. Cualquiera las lee sin clave ninguna.

    De ahí dos reglas que se repiten todo el curso:

    1. **En el *payload* no va nada sensible.** Ni contraseñas, ni DNI, ni datos de salud. Va quién eres y qué puedes.
    2. **Lo que protege es la firma**, la tercera parte. Puedes leer el token, pero no puedes cambiar `"rol":"USER"` por `"rol":"ADMIN"` sin invalidarla, porque no tienes la clave del servidor.

### E20 ●●● — Sesión con cookies

Simula un login, guarda la cookie y úsala después.

??? success "Solución"

    ```bash
    curl -s -c galletas.txt https://httpbin.org/cookies/set/sesion/abc123 > /dev/null
    curl -s -b galletas.txt https://httpbin.org/cookies | jq
    curl -s https://httpbin.org/cookies | jq
    ```
    ```json
    { "cookies": { "sesion": "abc123" } }
    { "cookies": {} }
    ```

    Esto es **todo el mecanismo de sesión de la UT7**, en tres órdenes. `-c` guarda las cookies que llegan, `-b` las envía.

    Y aquí se ve por qué HTTP no tiene memoria: el servidor no recuerda nada. Lo único que hace que te reconozca es **que el cliente devuelve el identificador en cada petición**. Borra `galletas.txt` y eres alguien nuevo.

### E21 ●● — Estático o dinámico, comprobado

Demuestra con la consola si una página se genera en el servidor o la pinta JavaScript.

??? success "Solución"

    ```bash
    curl -s https://es.wikipedia.org/wiki/Java | grep -c "Oracle"
    curl -s https://angular.dev | grep -c "app-root"
    ```

    En la primera, el contenido **venía en el HTML**: renderizado en servidor, y es lo que ve Google. En la segunda sale un `<app-root></app-root>` vacío: los datos los pinta el navegador después.

    La prueba equivalente sin consola: **botón derecho → Ver código fuente**. Eso es lo que llegó por la red. `F12 → Elements` es otra cosa: es el DOM **después** de que JavaScript lo haya tocado.

    Que los dos no coincidan es exactamente la diferencia entre SSR y CSR, y es toda la UT8.

### E22 ●●● — El servidor más pequeño posible

Levanta un servidor web con una orden y observa sus peticiones.

??? success "Solución"

    ```bash
    mkdir -p /tmp/web && cd /tmp/web
    cat > index.html <<'HTML'
    <!DOCTYPE html>
    <html lang="es">
    <head><meta charset="UTF-8"><title>Hola</title></head>
    <body><h1>Servidor estático</h1><img src="logo.svg" alt="logo"></body>
    </html>
    HTML

    python3 -m http.server 8000
    ```

    Abre `http://localhost:8000` y mira **la consola del servidor**:

    ```
    "GET / HTTP/1.1" 200 -
    "GET /logo.svg HTTP/1.1" 404 -
    "GET /favicon.ico HTTP/1.1" 404 -
    ```

    1. **Una página son varias peticiones.** Pediste una y salieron tres.
    2. **El 404 del `logo.svg`**: el HTML lo referenciaba y no existe. La página se sirve igual.
    3. **El `favicon.ico` lo pide el navegador solo**, sin que nadie se lo diga.

    Y lo importante: esto es un **servidor web**. Sirve ficheros que existen, no ejecuta código. Pide `/conciertos` y da 404. Eso es lo que hará distinto Tomcat desde la UT4.

### E23 ●● — Un contenedor en dos órdenes

Levanta nginx en Docker sirviendo tu página.

??? success "Solución"

    ```bash
    cd /tmp/web
    docker run --rm -d --name miweb -p 8080:80 \
           -v "$PWD:/usr/share/nginx/html:ro" nginx:alpine

    curl -s http://localhost:8080 | head -3
    docker logs miweb
    docker stop miweb
    ```

    | Trozo | Qué significa |
    |---|---|
    | `--rm` | Borra el contenedor al pararlo |
    | `-d` | En segundo plano |
    | `-p 8080:80` | **Puerto tuyo : puerto de dentro**. Ese orden se falla siempre |
    | `-v "$PWD:...:ro"` | Monta tu carpeta dentro, en solo lectura |

    La prueba que lo explica todo: **edita `index.html` y recarga**. El cambio aparece sin reconstruir nada, porque el fichero vive en tu disco y el contenedor solo lo lee.

    Quita el `-v` y verás la portada por defecto de nginx. Ahí se entiende qué es una imagen y qué es un volumen.

### E24 ●●● — Diagnostica un despliegue roto

Una aplicación devuelve 502 desde que se desplegó. Escribe las órdenes que lanzarías, en orden.

??? success "Solución"

    Un **502 Bad Gateway** lo devuelve el de delante —nginx— cuando el de detrás —tu aplicación— no contesta. El problema casi nunca está en nginx.

    ```bash
    docker compose ps                 # 1. ¿está vivo el contenedor?
    docker compose logs --tail=50 app # 2. ¿por qué se cae?
    docker compose exec app curl -s -o /dev/null -w "%{http_code}\n" localhost:8080/actuator/health
    docker compose exec proxy grep proxy_pass /etc/nginx/conf.d/default.conf
    docker compose port app 8080
    ```

    Qué buscas en cada una:

    1. Si dice `Exited` o `Restarting`, la aplicación se cae al arrancar.
    2. Aquí sale casi siempre: puerto ocupado, base de datos no disponible, variable vacía.
    3. Si por dentro responde 200 y por fuera da 502, el problema es **de red o puertos**.
    4. El fallo clásico: `proxy_pass http://localhost:8080`. Dentro de un contenedor, `localhost` es **ese** contenedor. Tiene que ser el nombre del servicio: `http://app:8080`.

    **El orden importa**, y es siempre el mismo: *¿vive? → ¿qué dice el log? → ¿responde por dentro? → ¿está bien enrutado?* Ir directo a tocar nginx sin mirar el log es la forma más rápida de perder una tarde.

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
| **E13 · Tu primera petición cruda** | ● | S3 |
| **E14 · Los cuatro verbos** | ● | S4 |
| **E15 · Provoca los códigos** | ●● | S4 |
| **E16 · Dónde se va el tiempo** | ●● | S3 |
| **E17 · La misma URL, dos formatos** | ●● | S5 |
| **E18 · La caché en vivo** | ●● | S5 |
| **E19 · Desmonta un JWT** | ●● | S7 |
| **E20 · Sesión con cookies** | ●●● | S7 |
| **E21 · Estático o dinámico** | ●● | S6 |
| **E22 · El servidor más pequeño** | ●●● | S8 |
| **E23 · Un contenedor en dos órdenes** | ●● | S8 |
| **E24 · Despliegue roto** | ●●● | S9 |

!!! tip "Del E13 en adelante, con el ordenador"
    Son doce ejercicios de teclear. La UT1 dura **seis sesiones** y existe para una cosa: salir de ella sabiendo lo justo para empezar a programar. Cuanto antes se pase, mejor — pero pasando por el teclado, no solo por la pizarra.

---

## Del ejercicio a la pregunta de test

El examen de esta unidad son **30 preguntas de opción múltiple** ([formato aquí](examen.md)). Hacer los ejercicios no prepara para un test **por sí solo**: hay que hacerlos de una forma concreta.

| Ejercicios | Preguntas | Qué se pregunta |
|---|:-:|---|
| **E1–E5 · E13–E16** · HTTP | 10 | Qué verbo, qué código de estado, qué cabecera; 401 frente a 403; dónde se va el tiempo de una petición |
| **E6–E8 · E21–E22** · Arquitecturas | 8 | MVC y sus responsabilidades; SSR frente a SPA; servidor web frente a servidor de aplicaciones |
| **E17–E18 · E23** · Contenido y despliegue | 6 | Negociación de contenido; ETag y 304; qué hace cada orden de Docker |
| **E9 · E19–E20** · Seguridad | 4 | Autenticación frente a autorización; qué lleva un JWT y qué no; sesión con cookies |
| **E10–E12 · E24** · Diagnóstico | 2 | Leer un log y decir qué falló; el 502 y su causa |

!!! reto "Las tres costumbres que transfieren"
    1. **Predice antes de ejecutar.** Antes de lanzar el `curl`, escribe qué código de estado y qué cabeceras esperas. Si aciertas, lo entendiste; si no, acabas de encontrar tu hueco. Esta es la costumbre que más nota vale.

    2. **Rómpelo a propósito.** Cuando un ejercicio te salga, quita la cabecera `Accept`, pide un recurso que no existe, manda el verbo equivocado. Apunta el código que sale. Las preguntas de «¿por qué falla?» son literalmente eso.

    3. **Escribe tú la pregunta.** Coge un ejercicio resuelto e invéntate una pregunta con cuatro opciones. Los tres distractores te obligan a saber por qué alguien se equivocaría — que es justo lo que se pregunta.

    Dos minutos por ejercicio. En toda la unidad son menos de treinta, y valen más que releer los apuntes la víspera.
