# Banco de preguntas — UT1 · RA1

Banco amplio organizado **por tema/sesión**. Úsalo para:
- **Autoevaluación** del alumno tras cada sesión (aula invertida).
- Montar **tests** variados (elige N preguntas por tema).
- El **test oficial de cierre** está aparte en el test oficial (se hace en clase).

Formato: opción múltiple (una correcta salvo aviso). **Solucionario al final de cada bloque.** En total, ~80 preguntas.

---

## Tema 1 — El viaje de una petición (S1)

1. El código **front-end** se ejecuta en…
   a) el servidor web · b) el navegador del usuario · c) la base de datos · d) el proxy

2. ¿Dónde debe hacerse SIEMPRE la validación de seguridad?
   a) en el cliente · b) en el servidor · c) en ninguno · d) en el DNS

3. La validación en el cliente sirve sobre todo para…
   a) seguridad · b) mejorar la experiencia de usuario (UX) · c) cifrar datos · d) balancear carga

4. El **despliegue** es…
   a) escribir el código · b) el paso de desarrollo a producción · c) diseñar la BD · d) un test unitario

5. ¿Cuál NO es un objetivo del despliegue?
   a) accesibilidad · b) estabilidad · c) escalabilidad · d) ofuscación del código

6. En un correo web, "obtener los mensajes de la base de datos" es tarea del…
   a) cliente · b) servidor · c) navegador · d) CSS

7. Comprobar la longitud mínima de una contraseña mientras el usuario escribe conviene hacerlo en el…
   a) servidor únicamente · b) cliente (y también validar en servidor) · c) DNS · d) balanceador

> **Soluciones T1:** 1b · 2b · 3b · 4b · 5d · 6b · 7b

---

## Tema 2 — Cliente y servidor (S2)

1. Un **back-end agnóstico/universal** se caracteriza por…
   a) tener una única interfaz web · b) exponer APIs consumibles por cualquier cliente · c) no usar base de datos · d) ejecutarse en el navegador

2. El formato de datos preferido hoy para las APIs es…
   a) HTML · b) JSON · c) PDF · d) TXT

3. ¿Cuál es una **aplicación web** (no una simple página)?
   a) un portafolio estático · b) Google Docs · c) una landing page · d) un PDF online

4. "Front-end" y "cliente" son…
   a) exactamente lo mismo · b) distintos: un cliente puede ser una app móvil sin front web · c) sinónimos de back-end · d) tipos de servidor

5. Una página web **estática**…
   a) siempre requiere servidor · b) puede visualizarse localmente sin servidor · c) necesita base de datos · d) genera HTML al vuelo

6. Un mismo back-end puede servir a la vez a…
   a) solo webs · b) web, móvil, escritorio y otros servicios · c) solo apps móviles · d) solo otro back-end

7. La tabla de tecnologías sitúa en el **cliente**…
   a) Java/JSP · b) PHP · c) HTML+CSS+JavaScript · d) ASP.NET

> **Soluciones T2:** 1b · 2b · 3b · 4b · 5b · 6b · 7c

---

## Tema 3 — Arquitecturas y MVC (S3)

1. La arquitectura **monolítica** se caracteriza por…
   a) servicios pequeños autónomos · b) agrupar UI, lógica y datos en un bloque · c) no tener lógica · d) ejecutarse sin servidor

2. Una ventaja de la arquitectura **por capas** es…
   a) mezclar responsabilidades · b) permitir cambiar una capa sin afectar a las demás · c) eliminar la BD · d) impedir el mantenimiento

3. Los **microservicios** aportan sobre todo…
   a) menor complejidad de gestión · b) escalado y despliegue independientes por servicio · c) un único punto de fallo · d) una sola tecnología obligatoria

4. La arquitectura **serverless** implica que…
   a) no hay servidores en absoluto · b) el proveedor gestiona la infraestructura y tú escribes funciones · c) no se puede escalar · d) siempre es gratis

5. Para un **MVP** con 1 desarrollador y poco presupuesto conviene…
   a) microservicios · b) monolito o capas · c) SOA distribuida · d) 20 funciones serverless

6. En **MVC**, ¿quién accede a la base de datos?
   a) la Vista · b) el Controlador · c) el Modelo · d) el navegador

7. El problema del **Fat Controller** es…
   a) la vista muy pesada · b) meter lógica de negocio en el controlador · c) que el modelo no accede a datos · d) que hay demasiadas vistas

8. El principio **SRP** de SOLID dice que una clase debe…
   a) tener muchas responsabilidades · b) tener una sola razón para cambiar · c) no cambiar nunca · d) depender de clases concretas

9. La arquitectura **EDA** se basa en…
   a) capas · b) eventos · c) un monolito · d) ficheros

10. La comunicación entre microservicios, frente al monolito, tiene…
    a) menos latencia · b) más sobrecarga (va por red) · c) ninguna diferencia · d) menos complejidad

> **Soluciones T3:** 1b · 2b · 3b · 4b · 5b · 6c · 7b · 8b · 9b · 10b

---

## Tema 4 — HTTP a fondo (S4–S5)

1. HTTP es un protocolo…
   a) con estado · b) sin estado (stateless) · c) cifrado por defecto · d) orientado a conexión permanente

2. Para **crear** un recurso en una API REST se usa…
   a) GET · b) POST · c) DELETE · d) HEAD

3. El código **404** significa…
   a) todo OK · b) creado · c) no encontrado · d) error del servidor

4. El código **500** indica…
   a) éxito · b) redirección · c) error del cliente · d) error del servidor

5. El código **401** significa…
   a) prohibido · b) no autenticado (¿quién eres?) · c) no encontrado · d) creado

6. La cabecera que indica el formato del contenido devuelto es…
   a) Host · b) User-Agent · c) Content-Type · d) Accept-Language

7. **HTTPS** aporta frente a HTTP…
   a) más contenido dinámico · b) cifrado (confidencialidad e integridad) con TLS · c) eliminar la autenticación · d) más velocidad siempre

8. Para que un servidor "recuerde" quién eres entre peticiones necesitas…
   a) nada, HTTP lo recuerda · b) enviar cookie o token en cada petición · c) usar solo GET · d) un código 200

9. El método que pide solo las cabeceras (sin cuerpo) es…
   a) GET · b) POST · c) HEAD · d) PUT

10. La confianza en un certificado HTTPS la da…
    a) el navegador solo · b) una Autoridad de Certificación (AC) · c) el usuario · d) el servidor DNS

> **Soluciones T4:** 1b · 2b · 3c · 4d · 5b · 6c · 7b · 8b · 9c · 10b

---

## Tema 5 — APIs a fondo (S6–S7)

1. Un servicio web (API) está diseñado para…
   a) mostrar HTML a un usuario · b) exponer datos/funcionalidad a otras aplicaciones · c) sustituir a la BD · d) renderizar CSS

2. El estilo de API más usado hoy, sobre HTTP y con JSON, es…
   a) SOAP · b) REST · c) gRPC · d) MQTT

3. **GraphQL** destaca por…
   a) usar XML · b) permitir pedir exactamente los datos necesarios · c) ser binario · d) no usar HTTP

4. Para comunicación **en tiempo real** (chat) conviene…
   a) SOAP · b) REST · c) WebSocket · d) HEAD

5. **gRPC** usa como formato…
   a) JSON · b) XML · c) Protocol Buffers (binario) · d) YAML

6. Un **JWT** consta de tres partes:
   a) usuario, clave, rol · b) header, payload, signature · c) IP, puerto, host · d) get, post, put

7. Sobre el **payload** de un JWT…
   a) está cifrado y es secreto · b) está en Base64: cualquiera puede leerlo, pero no modificarlo sin romper la firma · c) contiene la contraseña · d) no viaja al cliente

8. La arquitectura de **Netflix** se basa principalmente en…
   a) un monolito · b) microservicios en la nube (AWS) · c) serverless puro · d) SOAP

9. Un **webhook** es…
   a) una base de datos · b) un callback HTTP que notifica ante un evento · c) un tipo de cifrado · d) un navegador

> **Soluciones T5:** 1b · 2b · 3b · 4c · 5c · 6b · 7b · 8b · 9b

---

## Tema 6 — Web dinámica y lenguajes (S8)

1. Una web es **dinámica** cuando…
   a) tiene animaciones CSS · b) el servidor genera la respuesta según datos/usuario · c) usa muchas imágenes · d) está en la nube

2. **SSR** significa…
   a) Simple Server Response · b) Server Side Rendering: el servidor "cocina" el HTML · c) Secure Socket Reply · d) Static Site Render

3. Una página **estática**…
   a) consulta la BD en cada visita · b) se envía tal cual, sin procesamiento · c) cambia por usuario · d) requiere Tomcat

4. Una ventaja de la generación dinámica es…
   a) ser más rápida siempre · b) contenido personalizado y actualizado desde BD · c) no necesitar servidor · d) evitar HTTP

5. Mezclar código lógico y HTML sin control produce…
   a) código limpio · b) "código espagueti" · c) una API REST · d) un microservicio

6. Los frameworks modernos evitan el espagueti con el patrón…
   a) monolito · b) MVC · c) serverless · d) SOAP

7. En una **SPA**, el HTML final lo construye…
   a) el servidor · b) el navegador con JSON · c) la base de datos · d) el DNS

> **Soluciones T6:** 1b · 2b · 3b · 4b · 5b · 6b · 7b

---

## Tema 7 — Java en 2026 (S8)

1. PHP y Python son lenguajes de…
   a) código nativo · b) scripting (interpretado) · c) bytecode · d) ensamblador

2. Java se ejecuta como…
   a) interpretado línea a línea · b) nativo del SO · c) bytecode sobre la JVM · d) no se ejecuta

3. La ventaja del bytecode/JVM es…
   a) el máximo rendimiento absoluto · b) portabilidad ("write once, run anywhere") · c) no necesitar compilar nunca · d) prescindir del SO

4. El stack **LAMP** es…
   a) Linux, Apache, MySQL, PHP · b) Linux, Angular, Mongo, Python · c) Windows, IIS, SQL Server, C# · d) Linux, Apache, Maven, Perl

5. El framework estrella de **Java** para servicios es…
   a) Laravel · b) Django · c) Spring Boot · d) Express

6. **MEAN/MERN** es un stack basado en…
   a) Java · b) JavaScript (full stack JS) · c) C# · d) Python

7. La integración "código incrustado en HTML" es típica de…
   a) SPA · b) MVC server-side (JSP, plantillas) · c) gRPC · d) Docker

8. Un lenguaje **compilado a nativo** es…
   a) PHP · b) Python · c) Go · d) JavaScript

> **Soluciones T7:** 1b · 2c · 3b · 4a · 5c · 6b · 7b · 8c

---

## Tema 8 — Servidores web y de aplicaciones (S9)

1. Un **servidor web** (Apache/Nginx) se encarga sobre todo de…
   a) ejecutar la lógica de negocio · b) servir estáticos y hacer de proxy/puerta de entrada · c) almacenar datos · d) compilar código

2. Un **servidor de aplicaciones** (Tomcat) se encarga de…
   a) servir solo imágenes · b) ejecutar la lógica (Servlets/JSP, apps) · c) el DNS · d) el cifrado TLS únicamente

3. Un **VirtualHost** permite…
   a) cifrar datos · b) alojar varias webs en un mismo servidor/IP · c) balancear entre nubes · d) crear una BD

4. Nginx destaca frente a Apache por…
   a) usar .htaccess · b) su arquitectura orientada a eventos (más carga con menos RAM) · c) no servir estáticos · d) ejecutar Java

5. En producción, lo recomendable con Tomcat es…
   a) exponerlo directo a internet en el 8080 · b) poner Nginx/Apache delante como proxy · c) desactivar los logs · d) abrir el puerto de la BD

6. **Redis** es un gestor de datos…
   a) relacional en disco · b) NoSQL clave-valor en memoria (caché/sesiones) · c) documental XML · d) un servidor web

7. Nunca debes exponer directamente a internet…
   a) el puerto 443 · b) el puerto de la base de datos (ej. 3306) · c) el 80 · d) el proxy inverso

> **Soluciones T8:** 1b · 2b · 3b · 4b · 5b · 6b · 7b

---

## Tema 8 (cont.) — Despliegue (S9)

1. La escalabilidad **vertical** consiste en…
   a) poner más máquinas · b) poner una máquina más potente (más CPU/RAM) · c) usar Docker · d) usar un CDN

2. La escalabilidad **horizontal** requiere…
   a) nada especial · b) un balanceador de carga que reparta el tráfico · c) apagar servidores · d) un único servidor gigante

3. **Docker** soluciona sobre todo el problema de…
   a) "en mi máquina funcionaba" (empaqueta dependencias) · b) el cifrado · c) el DNS · d) el diseño de la BD

4. **Kubernetes** sirve para…
   a) crear un contenedor · b) orquestar muchos contenedores (autoescalado, recuperación) · c) compilar Java · d) servir HTML

5. En el modelo **PaaS**…
   a) alquilas máquinas virtuales y lo instalas todo · b) subes tu código y el proveedor pone el servidor · c) usas software final como Gmail · d) compras hardware físico

6. **CI** (Integración Continua) significa…
   a) desplegar a mano · b) ejecutar los tests automáticamente en cada push · c) cifrar el tráfico · d) crear VirtualHosts

7. Frente a una VM, un contenedor Docker es más ligero porque…
   a) incluye su propio SO invitado · b) comparte el núcleo del SO anfitrión · c) no usa CPU · d) no tiene dependencias

> **Soluciones T9:** 1b · 2b · 3a · 4b · 5b · 6b · 7b

---

## Tema 9 — Seguridad y monitorización (S9)

1. **Autenticación** responde a la pregunta…
   a) ¿qué puedes hacer? · b) ¿quién eres? · c) ¿dónde estás? · d) ¿cuánto pesa?

2. **Autorización** responde a…
   a) ¿quién eres? · b) ¿qué puedes hacer? · c) ¿qué hora es? · d) ¿qué IP tienes?

3. **HTTP Basic** es inseguro si…
   a) se usa con HTTPS · b) no va sobre HTTPS (la clave viaja en Base64 legible) · c) se usa JWT · d) se rota el log

4. Los **access logs** registran…
   a) por qué falla el servidor · b) quién entra, qué pide y con qué código · c) la contraseña · d) el código fuente

5. La **rotación de logs** evita que…
   a) el servidor se cifre · b) el disco se llene y el servidor caiga · c) haya autenticación · d) se pierdan cookies

6. El token moderno para autenticar APIs es…
   a) HTTP Basic · b) Digest · c) JWT · d) CLF

> **Soluciones T10:** 1b · 2b · 3b · 4b · 5b · 6c

---

## Cómo usar este banco

- **Autoevaluación por sesión:** el alumno responde el bloque del tema tras leerlo (aula invertida) y se corrige con el solucionario.
- **Test de clase:** elige 2–3 preguntas por tema para un test rápido de 15–20 preguntas.
