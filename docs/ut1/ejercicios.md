# Batería de ejercicios — UT1

**34 ejercicios con solución**, agrupados por tema. Son la única práctica de la unidad: aquí están integradas las que antes iban por separado.

!!! info "Alcance: temas 1 a 6"
    Todo lo que se pregunta en el [simulacro](autoevaluacion.md) y en el test de la unidad sale de **los temas 1 a 6 y de estos ejercicios**. Ni más ni menos.

    Los temas [7](07-java-en-2026.md), [8](08-servidores-y-despliegue.md) y [9](09-seguridad-y-logs.md) son **divulgación**: conviene leerlos porque ordenan el panorama, pero **no entran ni en los ejercicios ni en el test**.

| Bloque | Tema | Ejercicios |
|---|---|---|
| 1 · El viaje y los actores | [1](01-el-viaje-de-una-peticion.md) y [2](02-cliente-y-servidor.md) | E1–E6 |
| 2 · Arquitecturas y MVC | [3](03-arquitecturas-y-mvc.md) | E7–E12 |
| 3 · HTTP a fondo | [4](04-http-y-apis.md) | E13–E21 |
| 4 · APIs | [5](05-apis-a-fondo.md) | E22–E29 |
| 5 · Web dinámica y Java | [6](06-web-dinamica-y-lenguajes.md) | E30–E34 |

**Qué hace falta:** un navegador con DevTools (`F12`), una terminal con `curl` y el JDK 25. Nada más.

---

# Bloque 1 · El viaje y los actores

### E1 ● — Lee la conversación

Aquí tienes una petición y su respuesta. Contesta: método, ruta, qué pide el cliente, qué código devuelve el servidor y qué formato.

```http
POST /api/v1/pedidos HTTP/1.1
Host: tienda.example
Content-Type: application/json
Authorization: Bearer eyJhbGciOi...

{"producto": "teclado", "unidades": 2}
```

```http
HTTP/1.1 201 Created
Location: /api/v1/pedidos/8841
Content-Type: application/json

{"id": 8841, "estado": "PENDIENTE"}
```

??? success "Solución"

    | | |
    |---|---|
    | **Método** | `POST` — crea algo nuevo |
    | **Ruta** | `/api/v1/pedidos` |
    | **Qué pide** | Crear un pedido de 2 teclados |
    | **Código** | `201 Created` — se creó, y `Location` dice dónde |
    | **Formato** | JSON, en la petición y en la respuesta |

    Lo que hay que ver de un vistazo: **`201` y no `200`**. Un `POST` que crea un recurso devuelve `201` con la cabecera `Location`. Esa cabecera es la diferencia entre una API correcta y una que «funciona».

    El `Authorization: Bearer ...` dice que va autenticada con un token, no con cookie de sesión.

### E2 ● — Ordena el viaje

Estos siete pasos están desordenados. Ponlos en orden desde que se pulsa Intro.

`El navegador pinta` · `El servidor consulta la base de datos` · `Se resuelve el nombre con DNS` · `El servidor devuelve la respuesta` · `Se abre la conexión TCP y el cifrado TLS` · `El navegador pide los recursos (CSS, JS, imágenes)` · `El servidor recibe la petición y la enruta`

??? success "Solución"

    1. **DNS** — traducir `tienda.example` a una dirección IP.
    2. **TCP + TLS** — abrir la conexión y cifrarla.
    3. **El servidor recibe y enruta** la petición.
    4. **Consulta a la base de datos**.
    5. **Devuelve la respuesta** (el HTML o el JSON).
    6. **El navegador pide los recursos** que menciona ese HTML.
    7. **Pinta**.

    El paso que más se olvida es el **6**: la primera respuesta casi nunca es la página terminada, es un documento que **provoca más peticiones**. Por eso en el E5 vas a contar cuarenta o cincuenta.

### E3 ● — Cliente o servidor

Di dónde ocurre cada cosa y, cuando la respuesta sea «en los dos», explica por qué.

(a) Comprobar que el correo tiene una `@` · (b) Comprobar que la contraseña es correcta · (c) Calcular el total del carrito · (d) Ocultar un botón si no eres admin · (e) Decidir si puedes borrar un pedido · (f) Cambiar el color de un botón al pasar el ratón

??? success "Solución"

    | | Dónde | Por qué |
    |---|---|---|
    | (a) La `@` del correo | **Los dos** | En cliente para avisar rápido; en servidor porque el cliente se puede saltar |
    | (b) Contraseña correcta | **Servidor** | El cliente no tiene con qué compararla, ni debe |
    | (c) Total del carrito | **Servidor** | Si lo calcula el cliente, se puede cambiar el precio |
    | (d) Ocultar un botón | **Cliente** | Es presentación |
    | (e) Si puedes borrar | **Servidor** | Ocultar el botón no impide llamar a la URL |
    | (f) Color al pasar el ratón | **Cliente** | CSS puro |

    La pareja **(d) y (e)** es la que hay que entender. Ocultar el botón es cortesía; **la decisión de verdad es la (e)**, y vive en el servidor. Si solo ocultas el botón, cualquiera con las DevTools abiertas borra el pedido.

    La regla, en una frase: **todo lo que decide el cliente, se puede falsificar**.

### E4 ●● — Autopsia de una web

Abre una web real, `F12 → Red`, y recarga. Cuenta las peticiones y clasifícalas.

1. ¿Cuántas peticiones se lanzan en total?
2. ¿Cuántas son del documento, CSS, JS, imágenes y llamadas a API (`fetch`/`XHR`)?
3. Busca dos con códigos distintos y explica la diferencia.

??? success "Solución"

    No hay un número correcto: lo que importa es lo que descubres.

    - En una web comercial normal salen **entre 40 y 150 peticiones** para *una* página. El documento HTML es **una** de ellas.
    - Las de tipo **Fetch/XHR** son las llamadas a la API. Ahí está el back-end. Si las filtras con el botón `Fetch/XHR`, ves la API que consume la web.
    - Códigos que aparecen casi siempre:

    | Código | Qué significa aquí |
    |---|---|
    | **200** | Se descargó |
    | **304 Not Modified** | Ya lo tenías en caché y sigue valiendo. **No se descargó nada** |
    | **301 / 302** | Redirección: la URL buena es otra |
    | **204** | Todo bien y sin cuerpo (típico de un `DELETE` o de telemetría) |

    Fíjate en la columna **Tamaño**: en las `304` pone «(memoria caché)» o «(disco)». Eso es el E18 visto desde el navegador.

### E5 ●● — Dos webs, dos estrategias

Repite el E4 en dos sitios muy distintos: un periódico y una aplicación tipo panel (un correo web, un gestor de tareas). Compara.

??? success "Solución"

    Lo que sale, casi siempre:

    | | Periódico | Aplicación tipo panel |
    |---|---|---|
    | El primer HTML | **Grande y lleno de texto** | **Casi vacío**, un `<div id="root">` |
    | Peticiones `Fetch/XHR` | Pocas | **Muchas**, y son el contenido |
    | JavaScript | Moderado | **Mucho**, y pesado |
    | Si desactivas JS | Se lee | **Página en blanco** |

    El periódico hace **SSR**: el servidor manda el texto ya montado, porque necesita que Google lo lea y que cargue rápido.

    El panel hace **CSR**: manda una cáscara y la rellena JavaScript, porque una vez dentro se navega sin recargar y da igual el SEO.

    La forma rápida de distinguirlos: **ver el código fuente (`Ctrl+U`)**. Si el texto que ves en pantalla está ahí, es SSR; si no está, lo puso JavaScript.

### E6 ●● — Tres capas, tres responsabilidades

Para una tienda online, di qué hace cada capa y pon un ejemplo de algo que **no** le toca.

??? success "Solución"

    | Capa | Su trabajo | Lo que NO le toca |
    |---|---|---|
    | **Presentación** | Recibir la petición, validar el formato, devolver la respuesta | Decidir si hay stock |
    | **Lógica de negocio** | Las reglas: hay stock, se aplica el descuento, se cobra | Saber que los datos vienen de PostgreSQL |
    | **Datos** | Guardar y recuperar | Decidir si el descuento es válido |

    El error clásico es meter una regla de negocio en la capa de presentación: `if (usuario.getEdad() < 18) ...` dentro del controlador. Funciona, y el día que esa regla también haga falta en una tarea programada, hay que copiarla.

    **La prueba del algodón:** si cambias de PostgreSQL a MongoDB, ¿cuántas capas tocas? Debería ser **una**. Si tocas tres, las capas no están separadas de verdad.

---

# Bloque 2 · Arquitecturas y MVC

### E7 ●● — Elige la arquitectura

Para cada caso, elige entre **monolito**, **capas**, **microservicios** o **serverless**, y justifica en una frase.

(a) TFG de dos personas, seis semanas · (b) Banco con 300 desarrolladores y equipos por producto · (c) Un formulario de contacto que envía un correo, diez veces al día · (d) Una tienda que en rebajas multiplica el tráfico por cincuenta solo en el buscador

??? success "Solución"

    | | Elección | Por qué |
    |---|---|---|
    | (a) TFG | **Monolito en capas** | Con dos personas, la coordinación entre servicios cuesta más que el problema que resuelve |
    | (b) Banco | **Microservicios** | El motivo real es **organizativo**: 300 personas no pueden desplegar el mismo artefacto |
    | (c) Formulario | **Serverless** | Diez ejecuciones al día no justifican un servidor encendido |
    | (d) Tienda en rebajas | **Microservicios**, o al menos el buscador aparte | Se escala **solo la pieza que lo necesita** |

    La trampa está en (b). Casi todo el mundo responde «porque escala mejor», y la razón principal de los microservicios es **poder desplegar por separado**. La escalabilidad es un efecto secundario.

    Y el aviso de siempre: **microservicios en un equipo pequeño es pagar todo el coste sin ninguna ventaja**.

### E8 ●● — El monolito que no era

Un equipo dice: «tenemos microservicios, seis servicios». Pero: comparten la misma base de datos, se despliegan a la vez y si uno cae fallan todos.

¿Qué tienen en realidad, y qué problema causa?

??? success "Solución"

    Tienen un **monolito distribuido**: lo peor de los dos mundos.

    | Coste de microservicios | Ventaja de microservicios |
    |---|---|
    | Latencia de red entre servicios | ~~Despliegue independiente~~ — se despliegan juntos |
    | Errores parciales y reintentos | ~~Fallo aislado~~ — comparten base de datos |
    | Seis despliegues que coordinar | ~~Escalar por separado~~ — la BD es el cuello de botella |
    | Trazas repartidas | |

    **La señal que lo delata es la base de datos compartida.** Si dos servicios escriben en la misma tabla, no son independientes: un cambio de esquema los rompe a la vez.

    Un monolito bien hecho en capas sería más rápido, más fácil de depurar y más barato.

### E9 ●● — Reparte en MVC

Para «un usuario pide ver su pedido 8841», di qué hace el **controlador**, el **modelo** y la **vista**, y dónde va cada una de estas líneas:

(a) `SELECT * FROM pedido WHERE id = ?` · (b) comprobar que el pedido es de ese usuario · (c) devolver `404` si no existe · (d) formatear la fecha como `14/03/2026`

??? success "Solución"

    | | Dónde | Por qué |
    |---|---|---|
    | (a) El `SELECT` | **Modelo** (capa de datos) | El controlador no sabe que hay SQL |
    | (b) ¿Es suyo el pedido? | **Modelo** (lógica de negocio) | Es una **regla**, y debe valer también fuera de la web |
    | (c) Devolver `404` | **Controlador** | El código de estado es HTTP, y HTTP es cosa del controlador |
    | (d) Formatear la fecha | **Vista** | Es presentación: en una API sería ISO, en la web `dd/mm/aaaa` |

    La (b) y la (c) juntas son la pregunta buena. El **modelo** decide *«este pedido no es tuyo»*; el **controlador** traduce eso a *`403 Forbidden`*. Si el modelo devuelve un `ResponseEntity`, las capas están rotas.

    Y fíjate en la (d): la misma fecha se pinta distinta según quién mire. Por eso el formato **no** se guarda en el modelo.

### E10 ●● — Un diagrama que se sostenga

Dibuja en Mermaid la arquitectura de una tienda online: navegador, servidor web, aplicación, base de datos y caché. Marca qué parte es tuya como desarrollador de servidor.

??? success "Solución"

    ```mermaid
    flowchart LR
        N[Navegador] -->|HTTPS| SW[Servidor web<br/>nginx]
        SW -->|reenvía| APP[Aplicación<br/>Spring Boot]
        APP --> BD[(Base de datos)]
        APP --> C[(Caché)]
        APP -->|API externa| P[Pasarela de pago]
    ```

    **Tuyo es el recuadro `APP`** y las decisiones de qué guarda en la base de datos, qué cachea y cómo llama al pago.

    Tres cosas que se equivocan al dibujarlo:

    1. **Poner el navegador hablando con la base de datos.** No pasa nunca: la base de datos no está expuesta a Internet.
    2. **Confundir servidor web y aplicación.** `nginx` reparte y sirve ficheros estáticos; la aplicación es la que piensa.
    3. **Olvidar la caché.** Existe justo para que la base de datos no reciba la misma pregunta mil veces.

### E11 ●●● — SOLID en cinco frases

Empareja cada principio con el problema que evita.

`S` · `O` · `L` · `I` · `D` ↔ (1) una clase que hace cinco cosas y cambia por cinco motivos · (2) un `if` gigante que hay que tocar cada vez que aparece un caso nuevo · (3) una subclase que rompe lo que la clase padre prometía · (4) implementar métodos vacíos porque la interfaz pide de más · (5) una clase que no se puede probar porque crea ella misma lo que necesita

??? success "Solución"

    | | Principio | Problema | Cómo se ve en el código |
    |---|---|---|---|
    | **S** | Responsabilidad única | (1) | `UsuarioService` que valida, guarda, manda correo y genera el PDF |
    | **O** | Abierto/cerrado | (2) | `switch (tipoPago)` que crece con cada pasarela nueva |
    | **L** | Sustitución de Liskov | (3) | `Cuadrado extends Rectangulo` y `setAncho` rompe el área |
    | **I** | Segregación de interfaces | (4) | Implementar `Repositorio` y dejar la mitad con `throw new UnsupportedOperation` |
    | **D** | Inversión de dependencias | (5) | `new PostgresRepositorio()` dentro del servicio |

    El que más se nota en este curso es la **D**: es literalmente la razón de que exista la inyección de dependencias de Spring, que se ve en la UT4. Si el servicio hace el `new`, no puedes sustituirlo por un doble en un test.

### E12 ●●● — El caso completo

Una aplicación de reserva de pistas deportivas. Contesta las cinco preguntas.

1. ¿Qué arquitectura elegirías para un polideportivo municipal y por qué?
2. ¿Qué capas tendría, y qué hace cada una?
3. ¿Qué endpoints HTTP harían falta para reservar y anular?
4. ¿Qué validas en el cliente y qué en el servidor?
5. ¿Qué pasa si dos personas reservan la misma pista a la vez?

??? success "Solución"

    1. **Monolito en capas.** Un polideportivo es un dominio pequeño, con un equipo pequeño y tráfico previsible. Microservicios aquí es un coste sin retorno.

    2. | Capa | Qué hace |
       |---|---|
       | Presentación | Recibe la petición, valida el formato, devuelve el código |
       | Negocio | «La pista está libre», «no puedes reservar dos veces el mismo día», «hay que anular con 24 h» |
       | Datos | Pistas, reservas, socios |

    3. | Acción | Método y ruta | Respuesta |
       |---|---|---|
       | Ver huecos | `GET /api/v1/pistas/7/huecos?dia=2026-09-30` | `200` + lista |
       | Reservar | `POST /api/v1/reservas` | `201` + `Location` |
       | Ver reserva | `GET /api/v1/reservas/{id}` | `200` o `404` |
       | Anular | `DELETE /api/v1/reservas/{id}` | `204` |

    4. **Cliente:** que la fecha no sea pasada, que los campos estén rellenos. Es comodidad.
       **Servidor:** *todo lo anterior otra vez*, más que la pista exista, que esté libre, que el socio esté al corriente y que se respeten las 24 h.

    5. Esta es la pregunta interesante: **dos reservas a la vez**. Si el código hace «mirar si está libre» y después «guardar», hay un hueco entre las dos operaciones en el que caben las dos reservas.

       Se resuelve en la base de datos, no en Java: una **restricción de unicidad** sobre `(pista, dia, hora)`. La segunda inserción falla, se captura el error y se devuelve `409 Conflict`. Esto vuelve, con nombre y apellidos, en la UT5.

---

# Bloque 3 · HTTP a fondo

!!! tip "A partir de aquí, con la terminal abierta"
    Del E13 al E21 todo se teclea. Si en Windows `curl` te da problemas con las comillas, usa PowerShell y escribe `curl.exe` (con la extensión) para que no coja el alias.

### E13 ● — Tu primera petición cruda

```bash
curl -i https://httpbin.org/get
```

Identifica en la salida: la línea de estado, tres cabeceras y dónde empieza el cuerpo.

??? success "Solución"

    ```
    HTTP/2 200                          ← línea de estado: versión + código
    date: Tue, 22 Sep 2026 09:14:22 GMT ← cabeceras
    content-type: application/json
    content-length: 312
                                        ← LÍNEA EN BLANCO
    {                                   ← aquí empieza el cuerpo
      "headers": { ... }
    }
    ```

    Lo que hay que quedarse: **una línea en blanco separa las cabeceras del cuerpo**. Siempre. Es la regla que hace que el protocolo se pueda leer.

    | Opción | Qué hace |
    |---|---|
    | `-i` | Muestra cabeceras **y** cuerpo |
    | `-I` | **Solo** cabeceras (hace un `HEAD`) |
    | `-v` | Enseña también lo que *envías* (las líneas con `>`) |

### E14 ● — Los cuatro verbos, uno a uno

```bash
curl -i -X GET    https://httpbin.org/get
curl -i -X POST   https://httpbin.org/post   -d '{"a":1}' -H "Content-Type: application/json"
curl -i -X PUT    https://httpbin.org/put    -d '{"a":2}' -H "Content-Type: application/json"
curl -i -X DELETE https://httpbin.org/delete
```

Apunta el código de cada uno y contesta: ¿cuáles llevan cuerpo?

??? success "Solución"

    `httpbin` devuelve `200` en los cuatro porque es un espejo, no una API real. En una API de verdad:

    | Verbo | Para qué | Código normal | ¿Cuerpo? |
    |---|---|---|:-:|
    | `GET` | Leer | `200` | No |
    | `POST` | Crear | **`201`** + `Location` | Sí |
    | `PUT` | Reemplazar entero | `200` o `204` | Sí |
    | `PATCH` | Modificar un trozo | `200` | Sí |
    | `DELETE` | Borrar | `204` | No |

    Sin `-H "Content-Type: application/json"`, `curl` manda `application/x-www-form-urlencoded` y el servidor responde **`415 Unsupported Media Type`**. Pruébalo quitando la cabecera: ese 415 cae en el test.

### E15 ●● — Provoca los códigos a mano

```bash
for c in 200 201 204 301 400 401 403 404 409 415 418 500 503; do
  printf "%s → " $c
  curl -s -o /dev/null -w "%{http_code}\n" https://httpbin.org/status/$c
done
```

Agrúpalos por familia y di cuál es «culpa tuya» y cuál «culpa del servidor».

??? success "Solución"

    | Familia | Significado | ¿De quién es el problema? |
    |---|---|---|
    | **2xx** | Salió bien | De nadie |
    | **3xx** | Está en otro sitio | De nadie; sigue la redirección |
    | **4xx** | **El cliente se equivocó** | Tuya |
    | **5xx** | **El servidor se rompió** | Del servidor |

    Los que hay que saber sin pensar:

    | | |
    |---|---|
    | **401** Unauthorized | **No sé quién eres.** El nombre está mal puesto: debería ser «Unauthenticated» |
    | **403** Forbidden | **Sé quién eres y no puedes.** Identificarte otra vez no arregla nada |
    | **404** Not Found | No existe |
    | **409** Conflict | Existe y choca: correo repetido, pista ya reservada |
    | **415** Unsupported Media Type | Mandaste un formato que no acepta |
    | **422** Unprocessable | El formato es válido pero el contenido no (edad negativa) |

    **401 frente a 403 es la pregunta que más se falla de toda la unidad.**

### E16 ●● — Dónde se va el tiempo

```bash
curl -s -o /dev/null -w "dns:      %{time_namelookup}s\nconexión: %{time_connect}s\ntls:      %{time_appconnect}s\nprimer byte: %{time_starttransfer}s\ntotal:    %{time_total}s\n" https://www.example.com
```

¿Qué tramo se lleva más tiempo? Repite la orden inmediatamente y compara.

??? success "Solución"

    Una salida típica, la primera vez:

    ```
    dns:         0.028s   ← resolver el nombre
    conexión:    0.061s   ← TCP
    tls:         0.132s   ← el cifrado, el tramo más caro
    primer byte: 0.198s   ← aquí el servidor ya pensó
    total:       0.199s
    ```

    Al repetirla, `dns` baja casi a **0**: el sistema lo tiene cacheado. Eso ya es una lección sobre cachés.

    Para leerlo hay que **restar**, porque los tiempos son acumulados:

    | Tramo | Cuenta |
    |---|---|
    | DNS | `time_namelookup` |
    | TCP | `time_connect − time_namelookup` |
    | TLS | `time_appconnect − time_connect` |
    | **Pensar el servidor** | `time_starttransfer − time_appconnect` |
    | Descargar | `time_total − time_starttransfer` |

    Y de aquí sale el criterio profesional: **«va lento» no es un diagnóstico**. Lento ¿dónde? Si el tramo grande es el de pensar, el problema es tu código o la base de datos. Si es TLS, es red.

### E17 ●● — La misma URL, dos formatos

```bash
curl -s -H "Accept: application/json" https://httpbin.org/headers
curl -s -H "Accept: application/xml"  https://httpbin.org/headers
```

¿Cómo sabe el servidor qué devolver? ¿Y si pido un formato que no sabe?

??? success "Solución"

    Se llama **negociación de contenido**. El cliente dice lo que sabe leer con `Accept:` y el servidor elige y lo anuncia en `Content-Type:`.

    ```
    Accept: application/json, text/html;q=0.8, */*;q=0.1
    ```

    El `q` es la preferencia, de 0 a 1. Aquí: JSON primero; HTML si no hay; cualquier cosa antes que nada.

    Si el servidor no sabe dar ninguno de los formatos pedidos, responde **`406 Not Acceptable`**.

    No hay que confundir las dos cabeceras, y se confunden mucho:

    | Cabecera | Quién la manda | Qué dice |
    |---|---|---|
    | `Accept` | El **cliente** | «Sé leer esto» |
    | `Content-Type` | Quien **envía un cuerpo** | «Lo que va aquí dentro es esto» |

### E18 ●● — La caché, en vivo

```bash
curl -i https://httpbin.org/etag/abc123
curl -i -H 'If-None-Match: "abc123"' https://httpbin.org/etag/abc123
```

Compara los dos códigos y el tamaño de la respuesta.

??? success "Solución"

    La primera devuelve **`200`** con el cuerpo y una cabecera `ETag: "abc123"`, que es la huella del contenido.

    La segunda devuelve **`304 Not Modified`** y **sin cuerpo**. El servidor dice «lo que tienes sigue valiendo».

    Lo que se ahorra no es el viaje —la petición se hace igual— sino **la descarga**. Con imágenes y JavaScript eso es la mayor parte del peso de una web.

    | Cabecera | De quién | Para qué |
    |---|---|---|
    | `ETag` | Respuesta | Huella de esta versión |
    | `If-None-Match` | Petición | «Solo mándamelo si ha cambiado» |
    | `Last-Modified` | Respuesta | Fecha de la última modificación |
    | `If-Modified-Since` | Petición | Lo mismo, por fecha |
    | `Cache-Control: max-age=3600` | Respuesta | «Ni preguntes durante una hora» |

    Esto reaparece en la UT6 como buena práctica de API.

### E19 ●● — Sesión con cookies

```bash
curl -c galletas.txt -s https://httpbin.org/cookies/set?sesion=abc123 > /dev/null
cat galletas.txt
curl -b galletas.txt -s https://httpbin.org/cookies
```

Explica qué hace cada opción y por qué hacen falta las cookies.

??? success "Solución"

    `-c` **guarda** las cookies que manda el servidor; `-b` las **envía** de vuelta. Es exactamente lo que hace un navegador.

    Hacen falta porque **HTTP no tiene memoria**: cada petición llega sin saber nada de la anterior. La cookie es el hilo que las une.

    ```
    Set-Cookie: sesion=abc123; HttpOnly; Secure; SameSite=Lax; Max-Age=3600
    ```

    | Atributo | Qué protege |
    |---|---|
    | `HttpOnly` | JavaScript **no** puede leerla — corta el robo por XSS |
    | `Secure` | Solo viaja por HTTPS |
    | `SameSite=Lax` | No se manda desde otros sitios — corta el CSRF |
    | `Max-Age` | Caduca sola |

    Sin `HttpOnly`, un `<script>` inyectado en la página se lleva la sesión de todos los usuarios. Es una línea de configuración.

### E20 ●● — Sigue la redirección

```bash
curl -i  http://httpbin.org/redirect-to?url=https://example.com
curl -iL http://httpbin.org/redirect-to?url=https://example.com
```

¿Qué cambia con `-L`? ¿Y qué diferencia hay entre `301` y `302`?

??? success "Solución"

    Sin `-L`, `curl` enseña la respuesta `302` con la cabecera `Location:` y para. Con `-L`, **sigue** la redirección y te da la página final.

    Un navegador siempre hace lo que hace `-L`; por eso normalmente no ves las redirecciones.

    | Código | Significa | Efecto práctico |
    |---|---|---|
    | **301** Moved Permanently | Se mudó para siempre | El navegador **lo cachea**. Si te equivocas, es muy difícil de deshacer |
    | **302** Found | Temporal | No se cachea |
    | **307 / 308** | Igual que 302 / 301 | Pero **conservan el método**: un `POST` sigue siendo `POST` |

    El detalle de los `307/308` importa: con un `301`, muchos clientes convierten un `POST` en `GET` al redirigir, y el cuerpo se pierde por el camino.

### E21 ●●● — ¿Seguro? ¿Idempotente?

Rellena la tabla y explica por qué un `POST` repetido es un problema real.

| Método | ¿Seguro? | ¿Idempotente? |
|---|:-:|:-:|
| GET · POST · PUT · PATCH · DELETE | | |

??? success "Solución"

    | Método | Seguro | Idempotente |
    |---|:-:|:-:|
    | `GET` | ✅ | ✅ |
    | `POST` | ❌ | ❌ |
    | `PUT` | ❌ | ✅ |
    | `PATCH` | ❌ | ❌ |
    | `DELETE` | ❌ | ✅ |

    - **Seguro** = no cambia nada. Solo `GET` (y `HEAD`, `OPTIONS`).
    - **Idempotente** = repetirlo deja el mismo estado que hacerlo una vez.

    `DELETE` **es** idempotente aunque el segundo intento devuelva `404`: el estado final —no existe— es el mismo. Idempotencia habla del **estado**, no del código de respuesta.

    `PATCH` no lo es porque puede ser relativo: `{"op":"incrementar","valor":1}` aplicado dos veces suma dos.

    **Por qué importa de verdad:** si una petición da error de red, el cliente no sabe si llegó. Puede reintentar sin miedo un `GET`, un `PUT` o un `DELETE`. Con un `POST`, reintentar puede generar **dos pedidos**. Por eso existe el botón «no pulse dos veces» — y por eso en la UT6 se ve cómo hacer un `POST` idempotente con una clave.

---

# Bloque 4 · APIs

### E22 ● — Explora una API pública

Elige una API abierta y documenta **tres** endpoints distintos.

```bash
curl -s https://jsonplaceholder.typicode.com/users/1
curl -s https://jsonplaceholder.typicode.com/users/1/posts
curl -s "https://jsonplaceholder.typicode.com/posts?userId=1&_limit=3"
```

Para cada uno: método, URL, parámetros y tres campos de la respuesta.

??? success "Solución"

    | Endpoint | Qué devuelve | Forma |
    |---|---|---|
    | `GET /users/1` | Un usuario | **Objeto** `{}` |
    | `GET /users/1/posts` | Sus publicaciones | **Array** `[]` |
    | `GET /posts?userId=1&_limit=3` | Lo mismo, filtrado | **Array** `[]` |

    Tres cosas que enseña este ejercicio:

    1. **Un recurso concreto devuelve un objeto; una colección devuelve un array.** Si `/users/1` te devolviera un array de un elemento, la API estaría mal diseñada.
    2. Hay **dos formas de expresar lo mismo**: `/users/1/posts` (anidado) y `/posts?userId=1` (filtro). El anidado se lee mejor; el filtro escala mejor cuando hay muchos criterios.
    3. Los **parámetros de consulta** van después de `?`, separados por `&`. Filtrar, ordenar y paginar van ahí — nunca en la ruta.

    Truco: añade `| python3 -m json.tool` al final para verlo formateado.

### E23 ●● — Asigna el código

Para cada situación, di qué código devolverías.

(a) Un `POST /usuarios` que crea bien · (b) `GET /usuarios/999` y no existe · (c) `DELETE /usuarios/5` que borra bien · (d) `POST /usuarios` con un correo ya registrado · (e) `GET /usuarios` sin token · (f) `GET /admin/logs` con token de usuario normal · (g) la base de datos está caída · (h) `POST` con `Content-Type: text/plain` en una API JSON · (i) `POST /usuarios` con `{"edad": -5}`

??? success "Solución"

    | | Código | Por qué |
    |---|---|---|
    | (a) Creado | **201** + `Location` | `200` también «funciona» y pierde información |
    | (b) No existe | **404** | |
    | (c) Borrado | **204** | Sin cuerpo: no hay nada que devolver |
    | (d) Correo repetido | **409** | Conflicto con el estado actual |
    | (e) Sin token | **401** | No sé quién eres |
    | (f) Sin permiso | **403** | Sé quién eres y no puedes |
    | (g) BD caída | **503** | `500` vale; `503` dice «vuelve luego» |
    | (h) Formato no aceptado | **415** | |
    | (i) Edad negativa | **422** (o **400**) | El JSON es válido; el contenido no |

    La pareja **(e)/(f)** y la pareja **(h)/(i)** son las dos que caen siempre.

### E24 ●● — Diseña los endpoints

Diseña la API de una biblioteca: listar libros, ver uno, crear, actualizar, borrar, buscar por autor y listar los préstamos de un libro.

??? success "Solución"

    | Acción | Método y ruta |
    |---|---|
    | Listar | `GET /api/v1/libros` |
    | Ver uno | `GET /api/v1/libros/{id}` |
    | Crear | `POST /api/v1/libros` |
    | Actualizar | `PUT /api/v1/libros/{id}` |
    | Borrar | `DELETE /api/v1/libros/{id}` |
    | Buscar por autor | `GET /api/v1/libros?autor=Saramago` |
    | Préstamos de un libro | `GET /api/v1/libros/{id}/prestamos` |

    Las reglas que se comprueban:

    - **Sustantivos en plural, nunca verbos.** `/obtenerLibros` está mal: el verbo ya lo pone el método HTTP.
    - **El filtro va en la consulta**, no en la ruta. `/libros/autor/Saramago` se rompe en cuanto quieras filtrar por dos cosas.
    - **La versión en la ruta**, `/api/v1/`, desde el primer día. Añadirla después obliga a romper a todos los clientes.
    - **Minúsculas y guiones** para nombres compuestos: `/libros-agotados`, no `/librosAgotados`.

### E25 ●● — Desmonta un JWT

Este token es real y está sin cifrar. Ábrelo.

```bash
TOKEN="eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiJhbmFAaWVzeC5lcyIsInJvbCI6IkFMVU1OTyIsImV4cCI6MTc5MDAwMDAwMH0.firma"
echo "$TOKEN" | cut -d. -f1 | base64 -d 2>/dev/null; echo
echo "$TOKEN" | cut -d. -f2 | base64 -d 2>/dev/null; echo
```

¿Qué has podido leer? ¿Qué impide entonces que alguien se cambie el rol a `ADMIN`?

??? success "Solución"

    Sale esto:

    ```json
    {"alg":"HS256","typ":"JWT"}
    {"sub":"ana@iesx.es","rol":"ALUMNO","exp":1790000000}
    ```

    **Un JWT no está cifrado: está firmado.** Lo lee cualquiera que lo tenga, sin contraseña ni herramientas.

    Un token tiene tres partes separadas por puntos: **cabecera.contenido.firma**. Las dos primeras son Base64 —que es codificación, no cifrado— y la tercera es una firma hecha con una clave que solo tiene el servidor.

    Si alguien cambia `"rol":"ADMIN"` y vuelve a montar el token, **la firma deja de cuadrar** y el servidor lo rechaza. Falsificar la firma exige la clave secreta.

    De ahí las dos reglas:

    1. **Nunca metas datos sensibles en un JWT.** Van a la vista: contraseñas, DNI, saldo, nada.
    2. **Comprueba siempre la firma y la caducidad `exp`** en el servidor. Un token sin caducidad es un agujero permanente.

### E26 ●● — Autenticación o autorización

Clasifica: (a) iniciar sesión con usuario y contraseña · (b) que solo los admin borren usuarios · (c) validar un token en cada petición · (d) que un profesor vea solo sus grupos · (e) el segundo factor por SMS

??? success "Solución"

    | | Cuál | Pregunta que responde |
    |---|---|---|
    | (a) Usuario y contraseña | **Autenticación** | ¿Quién eres? |
    | (b) Solo admin borra | **Autorización** | ¿Qué puedes hacer? |
    | (c) Validar el token | **Autenticación** | ¿Sigues siendo tú? |
    | (d) Solo sus grupos | **Autorización** | Y de la difícil: depende del dato, no solo del rol |
    | (e) SMS | **Autenticación** | Segundo factor |

    En HTTP: fallar la autenticación es **401**; fallar la autorización es **403**.

    La (d) es la que separa el ejercicio del mundo real. «Solo sus grupos» no se resuelve con un rol: hay que comprobar, **para cada grupo concreto**, que es suyo. Eso se llama autorización a nivel de dato, y es donde aparecen los fallos de seguridad de verdad.

### E27 ●●● — Qué tipo de API

Elige entre REST, GraphQL, gRPC y WebSocket, y justifica.

(a) Una API pública para que terceros integren tu catálogo · (b) Una app móvil que se queja de pedir cinco endpoints para pintar una pantalla · (c) Dos microservicios internos que se llaman miles de veces por segundo · (d) Un panel que muestra pedidos según van entrando

??? success "Solución"

    | | Elección | Por qué |
    |---|---|---|
    | (a) API pública | **REST** | Lo entiende todo el mundo, se cachea con HTTP y se documenta con OpenAPI |
    | (b) App móvil | **GraphQL** | El cliente pide **exactamente** los campos que pinta, en una sola llamada |
    | (c) Entre microservicios | **gRPC** | Binario y con HTTP/2: mucho menos que JSON por cable |
    | (d) Panel en vivo | **WebSocket** | El **servidor** empuja; con REST habría que preguntar cada segundo |

    El caso (b) tiene nombre: **sobrefetching** (traes campos que no usas) e **infrafetching** (te faltan y haces otra llamada). GraphQL ataca las dos cosas, y a cambio pierdes la caché de HTTP, que no es poco.

    El (d) también admite **SSE** (Server-Sent Events) si la comunicación es en un solo sentido: es más simple que WebSocket y va sobre HTTP normal.

### E28 ●●● — Lee una respuesta de error

```json
{
  "type": "https://api.tienda.example/errores/stock-insuficiente",
  "title": "Stock insuficiente",
  "status": 409,
  "detail": "Quedan 2 unidades de «teclado» y se pidieron 5",
  "instance": "/api/v1/pedidos"
}
```

¿Qué formato es? ¿Por qué es mejor que devolver `{"error": "fallo"}`?

??? success "Solución"

    Es **Problem Details**, el formato estándar de errores de HTTP (RFC 7807/9457). Se ve a fondo en la UT6.

    | Campo | Para qué |
    |---|---|
    | `type` | Identificador estable del tipo de error. **Una máquina puede compararlo** |
    | `title` | Resumen corto y fijo |
    | `status` | El código HTTP, repetido dentro |
    | `detail` | Lo concreto de **este** caso |
    | `instance` | Dónde pasó |

    Frente a `{"error": "fallo"}`: aquí el cliente puede **programar** contra el error —si `type` es stock-insuficiente, enseña el selector de unidades— en vez de comparar cadenas de texto que cambian cuando alguien corrige una tilde.

    Y `detail` dice lo que hace falta **sin filtrar nada interno**. Nunca se manda la traza de Java al cliente: eso regala nombres de clases y versiones a quien esté mirando.

### E29 ●●● — Diseña la API completa

Un sistema de incidencias de un instituto: crear, listar, asignar a un técnico, cerrar, comentar. Diseña las rutas, los códigos y di qué protegerías.

??? success "Solución"

    | Acción | Método y ruta | Respuesta |
    |---|---|---|
    | Crear | `POST /api/v1/incidencias` | `201` + `Location` |
    | Listar | `GET /api/v1/incidencias?estado=ABIERTA&_page=1` | `200` |
    | Ver una | `GET /api/v1/incidencias/{id}` | `200` / `404` |
    | Asignar | `PUT /api/v1/incidencias/{id}/tecnico` | `200` / `409` si ya está cerrada |
    | Cerrar | `POST /api/v1/incidencias/{id}/cierre` | `200` / `409` |
    | Comentar | `POST /api/v1/incidencias/{id}/comentarios` | `201` |
    | Ver comentarios | `GET /api/v1/incidencias/{id}/comentarios` | `200` |

    **Sobre «cerrar»:** no es un recurso, es una transición de estado. Hay dos formas defendibles: `PATCH /incidencias/{id}` con `{"estado":"CERRADA"}`, o un subrecurso `/cierre` como aquí. La segunda se lee mejor cuando cerrar tiene reglas propias.

    **Qué se protege:**

    | Quién | Qué puede |
    |---|---|
    | Cualquier usuario autenticado | Crear y ver **las suyas** |
    | Técnico | Ver todas, asignarse, comentar, cerrar |
    | Admin | Todo, incluido reabrir |

    Y lo importante: **«ver las suyas» no se resuelve ocultando el listado**. Hay que comprobar en `GET /incidencias/{id}` que esa incidencia es del que pregunta, o cualquiera cambia el número en la URL y lee las de los demás.

---

# Bloque 5 · Web dinámica y Java

### E30 ● — Estática o dinámica

Clasifica: (a) el `index.html` de un portfolio · (b) el perfil de un usuario con su nombre · (c) un PDF en el servidor · (d) el listado de productos de una tienda · (e) la portada de un periódico

??? success "Solución"

    | | Cuál | Matiz |
    |---|---|---|
    | (a) Portfolio | **Estática** | El mismo fichero para todos |
    | (b) Perfil | **Dinámica** | Cambia según quién mira |
    | (c) PDF | **Estático** | El fichero no se genera |
    | (d) Listado | **Dinámica** | Sale de la base de datos |
    | (e) Portada | **Dinámica…** pero servida como estática |

    La (e) tiene truco y por eso está aquí. Las portadas se generan cada pocos minutos y se guardan **ya montadas** en una caché o CDN. Para el usuario es estática —rapidísima— y para la redacción es dinámica.

    La definición que vale: **estático** = el fichero existe tal cual en el disco; **dinámico** = se construye en el momento.

### E31 ●● — Estática frente a dinámica, comprobado

Crea `hora.html` con una hora escrita a mano. Después este servidor en Java, que se ejecuta **sin compilar ni crear proyecto**:

```java
// Servidor.java · se lanza con: java Servidor.java
import com.sun.net.httpserver.HttpServer;
import java.net.InetSocketAddress;
import java.time.LocalTime;

void main() throws Exception {
    var servidor = HttpServer.create(new InetSocketAddress(8000), 0);
    servidor.createContext("/hora", intercambio -> {
        var cuerpo = "<h1>Son las " + LocalTime.now().withNano(0) + "</h1>";
        var bytes = cuerpo.getBytes();
        intercambio.getResponseHeaders().add("Content-Type", "text/html; charset=utf-8");
        intercambio.sendResponseHeaders(200, bytes.length);
        try (var salida = intercambio.getResponseBody()) { salida.write(bytes); }
    });
    servidor.start();
    IO.println("En http://localhost:8000/hora");
}
```

Recarga las dos varias veces. ¿Por qué una cambia y la otra no?

??? success "Solución"

    El `.html` es un fichero: el servidor lo lee y lo manda **tal cual**. Da igual cuántas veces recargues.

    En `/hora`, el código se ejecuta **en cada petición**: `LocalTime.now()` se evalúa cada vez y el HTML se construye en ese momento.

    Eso es toda la diferencia entre estático y dinámico, y es el trabajo de este módulo: **el HTML —o el JSON— se fabrica cuando alguien lo pide**.

    Dos cosas que conviene notar:

    - `java Servidor.java` **ejecuta el fuente directamente**, sin `javac` y sin proyecto. Es lo que hace posible que los ejercicios de la UT2 y la UT3 sean ficheros sueltos.
    - Esto es un juguete: sin capas, sin rutas, sin errores, sin seguridad. Lo que hace Spring Boot es exactamente esto, con todo lo que le falta.

### E32 ●● — SSR o CSR

Para cada caso di qué elegirías y por qué. (a) Un blog que vive de Google · (b) Un panel de administración interno · (c) Una tienda · (d) Un editor de texto colaborativo

??? success "Solución"

    | | Elección | Motivo dominante |
    |---|---|---|
    | (a) Blog | **SSR** | SEO: el buscador tiene que leer el texto |
    | (b) Panel interno | **CSR** | Nadie lo busca en Google y se navega mucho dentro |
    | (c) Tienda | **Mixta** | Fichas de producto en SSR (SEO); carrito y filtros en CSR |
    | (d) Editor colaborativo | **CSR** | Estado muy vivo en el cliente, además de WebSocket |

    La comprobación empírica es la del E5: **`Ctrl+U`** y buscar el texto. Si está, es SSR.

    | | SSR | CSR |
    |---|---|---|
    | Primera carga | Rápida | Lenta (hay que bajar el JS) |
    | Navegación interna | Recarga | Instantánea |
    | SEO | Bueno | Problemático |
    | Carga del servidor | Alta | Baja |

    En este módulo harás **SSR** con Thymeleaf en la UT8, y en la UT6 construyes la API que alimentaría un cliente CSR.

### E33 ●● — Dónde encaja Java

Sitúa cada tecnología: `Spring Boot` · `Thymeleaf` · `JPA / Hibernate` · `Maven` · `JUnit` · `Tomcat`. ¿Cuál se usa en qué unidad?

??? success "Solución"

    | Tecnología | Qué es | Dónde se ve |
    |---|---|---|
    | **Maven** | Construye el proyecto y baja dependencias | UT2, y todo el curso |
    | **JUnit** | Escribe y ejecuta pruebas | UT2, y todo el curso |
    | **Spring Boot** | El marco de trabajo: crea los objetos, enruta, configura | UT4 |
    | **JPA / Hibernate** | Traduce entre objetos y tablas | UT5 |
    | **Tomcat** | El servidor que atiende HTTP. **Va dentro del `.jar`** | UT4 (sin que te enteres) |
    | **Thymeleaf** | Rellena plantillas HTML en el servidor | UT8 |

    Lo que sorprende y cae en el test: **Spring Boot lleva Tomcat dentro**. No hay que instalar un servidor ni copiar un `.war` a ningún sitio; el `.jar` se ejecuta con `java -jar` y ya escucha en el 8080. Eso es lo que cambió Spring Boot respecto a cómo se desplegaba Java hace quince años.

### E34 ●●● — El recorrido entero, de punta a punta

Junta toda la unidad. Alguien pulsa «Comprar» en una tienda hecha con Spring Boot. Describe el recorrido completo nombrando: protocolo, método, código, capas, base de datos y respuesta.

??? success "Solución"

    ```mermaid
    sequenceDiagram
        participant N as Navegador
        participant C as Controlador
        participant S as Servicio
        participant R as Repositorio
        participant BD as Base de datos

        N->>C: POST /api/v1/pedidos (JSON + token)
        C->>C: valida formato y token
        C->>S: crearPedido(datos)
        S->>R: ¿hay stock?
        R->>BD: SELECT
        BD-->>R: 2 unidades
        S->>S: aplica las reglas
        S->>R: guarda el pedido
        R->>BD: INSERT
        S-->>C: pedido 8841
        C-->>N: 201 Created + Location
    ```

    Contado en palabras:

    1. El navegador manda un **`POST`** por **HTTPS** a `/api/v1/pedidos`, con el pedido en **JSON** y un **token** en `Authorization`.
    2. El **controlador** comprueba el formato y quién eres. Si el token falta → **401**; si el JSON está mal → **400**.
    3. Llama al **servicio**, que es donde viven las reglas: hay stock, el precio es el que es, el usuario puede comprar.
    4. El servicio usa el **repositorio**, que es el único que sabe que por debajo hay SQL.
    5. Si no hay stock, el servicio lo dice y el controlador lo traduce a **409 Conflict** con un cuerpo Problem Details.
    6. Si todo va bien: **201 Created** y la cabecera **`Location: /api/v1/pedidos/8841`**.

    Este ejercicio **es** la unidad entera. Si sabes contarlo entero y sin mirar, el test está aprobado.

---

## Cómo trabajarlos

| Momento | Ejercicios |
|---|---|
| Para arrancar la sesión, 10 min | E1 · E7 · E13 · E22 · E30 |
| Taller de la sesión, 25-30 min | E4 · E9 · E15 · E24 · E31 |
| Los que hay que hacer sí o sí | **E3 · E12 · E15 · E21 · E25 · E26 · E34** |
| Para quien va sobrado | E8 · E11 · E16 · E27 · E28 · E29 |
| Repaso antes del test | E12 · E15 · E21 · E25 · E34 + el [simulacro](autoevaluacion.md) |

!!! reto "Las tres costumbres que transfieren al test"
    El test no pregunta definiciones: pone delante una salida de `curl`, un trozo de JSON o un caso, y hay que decidir. Eso se entrena así:

    1. **Predice antes de ejecutar.** Antes de lanzar el `curl`, escribe qué código y qué cabeceras esperas. Acertar confirma; fallar te señala el hueco exacto.
    2. **Rómpelo a propósito.** Quita el `Content-Type`, pide un formato imposible, manda el verbo equivocado. Apunta el código que sale: esas son las preguntas de «¿por qué falla?».
    3. **Escribe tú la pregunta.** Coge un ejercicio e invéntate cuatro opciones. Pensar los tres distractores obliga a saber **por qué** alguien se equivocaría, que es justo lo que se pregunta.

    Dos minutos por ejercicio. En toda la unidad es poco más de una hora, y rinde más que releer los apuntes la víspera.

[:material-arrow-right: Al simulacro de test](autoevaluacion.md){ .md-button .md-button--primary }
