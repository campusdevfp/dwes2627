# Chuleta — Arquitecturas y tecnologías web

## El viaje de una petición

```
Navegador → DNS → (CDN) → Proxy inverso (Nginx) → Servidor de aplicaciones (Tomcat) → BD
```

## Cliente frente a servidor

| | Cliente | Servidor |
|---|---|---|
| Se ejecuta en | El navegador | Tu máquina |
| Lenguajes | JavaScript | Java, PHP, Python, C#… |
| El usuario puede modificarlo | **Sí** | No |
| Vale para seguridad | **Nunca** | Sí |

## Arquitecturas

| Modelo | En una frase | Cuándo |
|---|---|---|
| Monolito | Todo en un despliegue | Equipo pequeño, dominio claro |
| Monolito modular | Un despliegue, fronteras internas | **Lo más habitual en 2026** |
| Capas | Presentación · negocio · datos | Casi siempre, dentro de lo anterior |
| Microservicios | Servicios autónomos y desplegables | Equipos grandes, escalado desigual |
| EDA | Se comunican por eventos | Procesos asíncronos |
| *Serverless* | Funciones que arrancan por evento | Carga irregular |

**MVC:** modelo (datos y reglas) · vista (lo que se ve) · controlador (recibe y reparte).

**SOLID:** responsabilidad única · abierto/cerrado · sustitución de Liskov · segregación de interfaces · inversión de dependencias.

## HTTP

```
GET /api/v1/productos/42 HTTP/1.1     ← método + ruta + versión
Host: tienda.es                        ← cabeceras
Accept: application/json

HTTP/1.1 200 OK                        ← versión + código + texto
Content-Type: application/json
{...}                                  ← cuerpo
```

| Método | Seguro | Idempotente | Para |
|---|:-:|:-:|---|
| `GET` | :material-check: | :material-check: | Leer |
| `POST` | :material-close: | :material-close: | Crear |
| `PUT` | :material-close: | :material-check: | Reemplazar |
| `PATCH` | :material-close: | :material-close: | Modificar en parte |
| `DELETE` | :material-close: | :material-check: | Borrar |

**Seguro** = no cambia nada · **Idempotente** = repetirlo da el mismo resultado.

| Código | Significa |
|---|---|
| **200** OK · **201** Created (+ `Location`) · **204** No Content | Fue bien |
| **301/302** Moved / Found | Redirección |
| **400** Bad Request · **401** Unauthorized · **403** Forbidden · **404** Not Found · **409** Conflict · **422** Unprocessable | Culpa del cliente |
| **500** Internal · **502** Bad Gateway · **503** Unavailable | Culpa del servidor |

:material-alert: **401** = no sé quién eres · **403** = sé quién eres y no puedes.

**Versiones:** HTTP/1.1 (una petición por conexión) · HTTP/2 (multiplexado, binario) · HTTP/3 (sobre QUIC/UDP).

**HTTPS** cifra ruta, cabeceras y cuerpo. **El dominio viaja visible.**

## APIs

**REST:** recursos en plural (`/productos`), el verbo lo pone HTTP, sin verbos en la URL.

```
GET    /productos          listar        200
GET    /productos/42       uno           200 · 404
POST   /productos          crear         201 + Location
PUT    /productos/42       reemplazar    200 · 404
DELETE /productos/42       borrar        204 · 404
GET    /productos/42/valoraciones   subrecurso
```

Paginación: `?page=0&size=20&sort=precio,desc`

| Tipo | En una frase |
|---|---|
| **REST** | El estándar por defecto |
| **GraphQL** | El cliente pide exactamente los campos que quiere |
| **gRPC** | Binario y rápido, entre servicios |
| **WebSocket** | Conversación permanente en los dos sentidos |
| **Webhook** | El servidor te llama a ti cuando pasa algo |

**JWT:** `cabecera.carga.firma` en Base64. **Firmado, no cifrado** — se lee en jwt.io.

## Web dinámica

| | SSR | SPA (CSR) |
|---|---|---|
| Monta el HTML | El servidor | El navegador |
| Primera pantalla | **Rápida** | Lenta |
| Buscadores | **Bien** | Peor |
| Interacción fina | Torpe | **Natural** |

## Servidores y despliegue

| | Sirve |
|---|---|
| **Nginx**, Apache | Ficheros estáticos, proxy inverso, TLS |
| **Tomcat** (embebido en Spring Boot) | Tu código |

**Despliegue:** `.jar` → contenedor (Docker) → nube. **CI/CD** = construir, probar y desplegar automáticamente.

## Seguridad y logs

- **Autenticación** = quién eres (401) · **Autorización** = qué puedes (403)
- Todo por **HTTPS**. Puertos internos (3306, 5432, 8080) nunca expuestos.
- **Log de acceso**: quién pidió qué y qué recibió. **Log de error**: por qué falló.
- En un log **nunca**: contraseñas, tokens ni datos personales.
- Sin **rotación** de logs, el disco se llena y el servidor cae.
