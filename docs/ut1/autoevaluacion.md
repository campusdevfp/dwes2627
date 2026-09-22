# Simulacro de test — UT1

**72 preguntas con solución.** Mismo formato que el test de la unidad: opción múltiple, **una sola correcta**, sin penalización por fallo.

!!! info "De dónde sale todo lo que se pregunta"
    De **los temas 1 a 6** y de la **[batería de ejercicios](ejercicios.md)**. Nada más.

    Los temas [7](07-java-en-2026.md), [8](08-servidores-y-despliegue.md) y [9](09-seguridad-y-logs.md) son divulgación: **no entran**.

| Bloque | De dónde sale | Preguntas |
|---|---|:-:|
| **A** | Temas 1–2 · el viaje y los actores | 10 |
| **B** | Tema 3 · arquitecturas y MVC | 10 |
| **C** | Tema 4 · HTTP | 16 |
| **D** | Tema 5 · APIs | 14 |
| **E** | Tema 6 · web dinámica y Java | 8 |
| **F** | Directamente sobre los ejercicios | 14 |

**El test oficial son 30 preguntas en 55 minutos.** Se aprueba con 15. Al final de esta página tienes la selección exacta para hacer un simulacro cronometrado.

---

## Bloque A · El viaje y los actores

**1.** El código que se ejecuta en el navegador del usuario es…
a) el back-end · b) el front-end · c) la base de datos · d) el proxy

**2.** ¿Dónde tiene que hacerse **siempre** la validación de seguridad?
a) En el cliente · b) En el servidor · c) En los dos, indistintamente · d) En el DNS

**3.** Validar en el cliente sirve sobre todo para…
a) Seguridad · b) Mejorar la experiencia de usuario · c) Cifrar los datos · d) Repartir la carga

**4.** En el viaje de una petición, ¿qué ocurre **justo antes** de abrir la conexión TCP?
a) Se pinta la página · b) Se resuelve el nombre por DNS · c) Se consulta la base de datos · d) Se descargan las imágenes

**5.** Al cargar una página cualquiera, el navegador lanza…
a) Exactamente una petición · b) Una por cada imagen, nada más · c) Muchas: el documento y todos sus recursos · d) Ninguna si hay caché

**6.** Ocultar con CSS el botón «Borrar» a quien no es admin…
a) Basta para proteger la operación · b) No protege nada: la comprobación va en el servidor · c) Es la forma recomendada · d) Equivale a un 403

**7.** «Calcular el total del carrito» debe hacerse en el servidor porque…
a) El cliente no sabe multiplicar · b) Si lo hace el cliente, el precio se puede manipular · c) Es más rápido · d) Lo exige HTTP

**8.** El término **despliegue** designa…
a) Escribir el código · b) El paso de desarrollo a producción · c) Diseñar la base de datos · d) Ejecutar los tests

**9.** En un correo web, «recuperar los mensajes del almacén» es tarea…
a) Del navegador · b) Del servidor · c) Del CSS · d) Del DNS

**10.** Comprobar mientras el usuario escribe que la contraseña tiene 8 caracteres conviene hacerlo…
a) Solo en el servidor · b) En el cliente, **y también** en el servidor · c) Solo en el cliente · d) En el balanceador

> **Soluciones A:** 1b · 2b · 3b · 4b · 5c · 6b · 7b · 8b · 9b · 10b

---

## Bloque B · Arquitecturas y MVC

**11.** La razón **principal** para adoptar microservicios en una empresa grande es…
a) Que el código va más rápido · b) Poder desplegar cada parte por separado · c) Ahorrar memoria · d) Evitar usar base de datos

**12.** Seis «microservicios» que comparten base de datos y se despliegan juntos son…
a) Microservicios correctos · b) Un monolito distribuido · c) Arquitectura serverless · d) Una arquitectura hexagonal

**13.** Para un proyecto de dos personas y seis semanas, lo razonable es…
a) Microservicios · b) Un monolito en capas · c) Serverless con veinte funciones · d) Una arquitectura orientada a eventos

**14.** Un formulario de contacto que se usa diez veces al día encaja bien en…
a) Serverless · b) Un clúster de microservicios · c) Un mainframe · d) Una arquitectura peer-to-peer

**15.** En una arquitectura en capas, el `SELECT` vive en…
a) La capa de presentación · b) La capa de negocio · c) La capa de datos · d) La vista

**16.** En MVC, decidir que se devuelve un `404` es responsabilidad…
a) Del modelo · b) Del controlador · c) De la vista · d) De la base de datos

**17.** Formatear una fecha como `14/03/2026` corresponde a…
a) El modelo · b) El controlador · c) La vista · d) El repositorio

**18.** «Este pedido no es de este usuario» es una decisión…
a) Del controlador, porque devuelve 403 · b) Del modelo: es una regla de negocio · c) De la vista · d) Del servidor web

**19.** Un `switch (tipoPago)` que hay que tocar cada vez que se añade una pasarela incumple el principio…
a) De responsabilidad única · b) Abierto/cerrado · c) De sustitución de Liskov · d) De segregación de interfaces

**20.** Hacer `new PostgresRepositorio()` dentro de un servicio incumple…
a) La inversión de dependencias · b) La responsabilidad única · c) Liskov · d) Ninguno: es correcto

> **Soluciones B:** 11b · 12b · 13b · 14a · 15c · 16b · 17c · 18b · 19b · 20a

---

## Bloque C · HTTP

**21.** En un mensaje HTTP, cabeceras y cuerpo se separan por…
a) Una coma · b) Una línea en blanco · c) La etiqueta `<body>` · d) Un punto y coma

**22.** Un `POST` que crea un recurso debería responder…
a) `200 OK` · b) `201 Created` con `Location` · c) `204 No Content` · d) `302 Found`

**23.** Un `DELETE` que borra correctamente suele responder…
a) `200` con el recurso borrado · b) `204 No Content` · c) `404` · d) `201`

**24.** `401` frente a `403`: el `401` significa…
a) No sé quién eres · b) Sé quién eres y no puedes · c) No existe · d) Formato no admitido

**25.** Mandar `Content-Type: text/plain` a una API que solo acepta JSON produce…
a) `400` · b) `406` · c) `415` · d) `422`

**26.** Un JSON bien formado con `{"edad": -5}` en una API que exige edad positiva produce…
a) `415` · b) `422` (o `400`) · c) `409` · d) `500`

**27.** Intentar registrar un correo que ya existe encaja con…
a) `400` · b) `404` · c) `409` · d) `403`

**28.** ¿Qué métodos son **seguros**?
a) `GET` y `POST` · b) Solo `GET` (y `HEAD`/`OPTIONS`) · c) `GET`, `PUT` y `DELETE` · d) Todos

**29.** ¿Cuál **no** es idempotente?
a) `GET` · b) `PUT` · c) `DELETE` · d) `POST`

**30.** `DELETE` se considera idempotente aunque el segundo intento devuelva `404` porque…
a) El `404` también es correcto · b) La idempotencia habla del estado final, no del código · c) `curl` lo reintenta solo · d) Es un convenio sin base

**31.** La cabecera que envía **el cliente** para decir qué formatos entiende es…
a) `Content-Type` · b) `Accept` · c) `Allow` · d) `Vary`

**32.** Si el servidor no sabe producir ninguno de los formatos pedidos responde…
a) `415` · b) `406` · c) `400` · d) `501`

**33.** Un `304 Not Modified` significa que…
a) El recurso se borró · b) Lo que hay en caché sigue valiendo y no se envía cuerpo · c) Hay que autenticarse · d) El servidor falló

**34.** `If-None-Match` se usa junto a…
a) `Location` · b) `ETag` · c) `Set-Cookie` · d) `Authorization`

**35.** El atributo de cookie que impide que JavaScript la lea es…
a) `Secure` · b) `SameSite` · c) `HttpOnly` · d) `Max-Age`

**36.** Las cookies existen porque…
a) HTTP es lento · b) HTTP no mantiene estado entre peticiones · c) Lo exige TLS · d) Sustituyen al DNS

> **Soluciones C:** 21b · 22b · 23b · 24a · 25c · 26b · 27c · 28b · 29d · 30b · 31b · 32b · 33b · 34b · 35c · 36b

---

## Bloque D · APIs

**37.** En una API REST bien diseñada, listar libros es…
a) `GET /obtenerLibros` · b) `GET /api/v1/libros` · c) `POST /libros/listar` · d) `GET /libro?accion=listar`

**38.** Filtrar por autor debe expresarse…
a) `/libros/autor/Saramago` · b) `/libros?autor=Saramago` · c) `/buscarPorAutor/Saramago` · d) En una cabecera

**39.** Pedir un recurso concreto, `/users/1`, debe devolver…
a) Un array de un elemento · b) Un objeto · c) Una cadena · d) Un `204`

**40.** La versión de la API conviene ponerla…
a) Nunca · b) En la ruta, desde el principio: `/api/v1/` · c) Solo cuando haya clientes · d) En una cookie

**41.** Un JWT está…
a) Cifrado: nadie puede leerlo · b) Firmado: cualquiera lo lee, pero no se puede alterar · c) Comprimido · d) Hasheado con bcrypt

**42.** Las tres partes de un JWT son…
a) Usuario, contraseña, rol · b) Cabecera, contenido y firma · c) Clave, valor y caducidad · d) Origen, destino y cuerpo

**43.** ¿Por qué no se guardan datos sensibles en el contenido de un JWT?
a) Porque ocupan mucho · b) Porque van en Base64 y los lee cualquiera · c) Porque el navegador los borra · d) Porque rompen la firma

**44.** Si alguien cambia `"rol":"ADMIN"` en un JWT y lo reenvía…
a) Funciona: el servidor confía · b) Falla: la firma deja de cuadrar · c) Devuelve `404` · d) El token se cifra solo

**45.** «Solo los administradores pueden borrar usuarios» es…
a) Autenticación · b) Autorización · c) Cifrado · d) Trazabilidad

**46.** Validar el token en cada petición es…
a) Autenticación · b) Autorización · c) Auditoría · d) Negociación

**47.** Una app móvil que necesita cinco endpoints para pintar una pantalla sufre…
a) Sobrefetching solamente · b) Infrafetching, y encaja bien con GraphQL · c) Un problema de DNS · d) Falta de caché

**48.** Entre dos microservicios internos con muchísimo tráfico, lo más eficiente suele ser…
a) REST con JSON · b) gRPC · c) SOAP · d) GraphQL

**49.** Para un panel que debe mostrar pedidos según entran, lo adecuado es…
a) Preguntar cada segundo con REST · b) WebSocket o SSE · c) gRPC · d) Un `PUT` periódico

**50.** La ventaja de REST que se pierde al pasar a GraphQL es…
a) La seguridad · b) La caché de HTTP · c) El uso de JSON · d) La autenticación

**51.** El formato estándar para devolver errores en HTTP se llama…
a) Problem Details · b) ErrorObject · c) HTTP-Fault · d) RFC-Error

**52.** Nunca se debe mandar al cliente…
a) El código de estado · b) La traza de la excepción · c) Un mensaje de error · d) La cabecera `Content-Type`

**53.** «Un profesor solo ve sus grupos» requiere…
a) Solo comprobar el rol · b) Comprobar, para cada grupo concreto, que es suyo · c) Ocultar el menú · d) Un `401`

**54.** Un `POST` que se reintenta tras un error de red puede…
a) No pasar nada nunca · b) Crear el recurso dos veces · c) Devolver siempre `409` · d) Convertirse en `GET`

> **Soluciones D:** 37b · 38b · 39b · 40b · 41b · 42b · 43b · 44b · 45b · 46a · 47b · 48b · 49b · 50b · 51a · 52b · 53b · 54b

---

## Bloque E · Web dinámica y Java

**55.** Una página **estática** es aquella que…
a) No tiene CSS · b) Existe ya como fichero y se envía tal cual · c) No usa HTTP · d) Carga sin JavaScript

**56.** La portada de un periódico, generada cada pocos minutos y guardada en CDN, es…
a) Estática pura · b) Dinámica, aunque se sirva como estática · c) Un error de diseño · d) Renderizado en cliente

**57.** Para un blog que vive del buscador conviene…
a) CSR · b) SSR · c) Solo JavaScript · d) Una SPA sin servidor

**58.** La forma más rápida de saber si una página es SSR es…
a) Mirar la URL · b) Ver el código fuente y buscar el texto · c) Medir el tiempo de carga · d) Mirar las cookies

**59.** En una aplicación con CSR, el primer HTML suele contener…
a) Todo el texto · b) Prácticamente nada más que un contenedor vacío · c) La base de datos · d) El CSS en línea

**60.** `java Servidor.java` permite…
a) Compilar a `.class` y ejecutarlo después · b) Ejecutar el fuente directamente, sin proyecto · c) Crear un `.war` · d) Arrancar Tomcat

**61.** Spring Boot, respecto al servidor web…
a) Exige instalar Tomcat aparte · b) Lleva un servidor embebido dentro del `.jar` · c) Solo funciona con nginx · d) No necesita servidor

**62.** La pieza que traduce entre objetos Java y tablas es…
a) Maven · b) JPA / Hibernate · c) Thymeleaf · d) JUnit

> **Soluciones E:** 55b · 56b · 57b · 58b · 59b · 60b · 61b · 62b

---

## Bloque F · Sobre los ejercicios

Estas preguntas salen directamente de lo que hiciste en la [batería](ejercicios.md).

**63.** Dada esta respuesta, el cliente debe buscar el recurso creado en…

```http
HTTP/1.1 201 Created
Location: /api/v1/pedidos/8841
```

a) El cuerpo, siempre · b) La cabecera `Location` · c) Una cookie · d) La URL original

**64.** Ejecutas esto y sale `415`. La causa es…

```bash
curl -i -X POST https://api.example/pedidos -d '{"a":1}'
```

a) Falta el token · b) Falta `-H "Content-Type: application/json"` · c) La URL está mal · d) `POST` no admite cuerpo

**65.** En la salida de `curl -w`, el tramo `time_starttransfer − time_appconnect` mide…
a) El DNS · b) El cifrado TLS · c) Lo que tardó el servidor en pensar · d) La descarga

**66.** Repites la misma orden `curl -w` dos veces seguidas y `time_namelookup` baja casi a cero. Es porque…
a) El servidor va más rápido · b) El DNS quedó cacheado · c) `curl` reutiliza la conexión TLS · d) Se activó HTTP/3

**67.** Lanzas esto. La segunda orden devuelve…

```bash
curl -i https://httpbin.org/etag/abc123
curl -i -H 'If-None-Match: "abc123"' https://httpbin.org/etag/abc123
```

a) `200` con cuerpo · b) `304` sin cuerpo · c) `404` · d) `412`

**68.** Al decodificar la segunda parte de un JWT con `base64 -d` obtienes `{"sub":"ana@iesx.es","rol":"ALUMNO"}`. Eso demuestra que…
a) El token está roto · b) El contenido de un JWT es legible por cualquiera · c) La firma es inválida · d) Falta cifrarlo

**69.** En `curl`, `-c galletas.txt` sirve para…
a) Enviar cookies · b) Guardar las cookies que manda el servidor · c) Borrar la caché · d) Comprimir la respuesta

**70.** `curl -i` sin `-L` ante una redirección muestra…
a) La página final · b) La respuesta `302` con su `Location`, y para ahí · c) Un error · d) Un `404`

**71.** En el ejercicio del `Servidor.java`, `/hora` cambia en cada recarga y `hora.html` no. La razón es…
a) El navegador cachea el HTML · b) El código de `/hora` se ejecuta en cada petición · c) El `.html` está mal escrito · d) El servidor no sirve ficheros

**72.** Dos personas reservan la misma pista a la vez. La solución correcta es…
a) Un `synchronized` en el servicio · b) Una restricción de unicidad en la base de datos y devolver `409` · c) Preguntar antes si está libre · d) Guardar las dos y avisar por correo

> **Soluciones F:** 63b · 64b · 65c · 66b · 67b · 68b · 69b · 70b · 71b · 72b

---

## Simulacro cronometrado

Cuando lleves la unidad hecha, siéntate **55 minutos con un reloj** y contesta estas 30, sin mirar nada:

> **2 · 6 · 7 · 11 · 12 · 15 · 16 · 18 · 20 · 22 · 24 · 25 · 26 · 28 · 29 · 31 · 33 · 35 · 37 · 41 · 43 · 45 · 47 · 51 · 53 · 56 · 61 · 64 · 68 · 72**

| Aciertos | Lectura |
|:-:|---|
| **24 o más** | Vas sobrado. Repasa solo lo que fallaste |
| **18 a 23** | Aprobado holgado. Vuelve a los ejercicios del bloque que peor te fue |
| **15 a 17** | Justo. Rehaz la batería entera antes del test |
| **menos de 15** | Suspenso. El problema casi nunca es de memoria: es que faltan los ejercicios de consola |

!!! tip "Cómo sacarle partido de verdad"
    Contestar y mirar la solución sirve de poco. Lo que rinde es, **por cada fallo**, escribir en una línea *por qué* la tuya estaba mal. Si no sabes escribirla, ahí tienes lo que hay que releer.

    Las respuestas de este banco siguen un orden fijo para poder corregir rápido; en el test real **el orden de las opciones está mezclado**, así que no memorices letras.
