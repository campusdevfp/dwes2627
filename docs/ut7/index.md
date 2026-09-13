# UT7 — Estado, sesiones y autenticación

**20 h · 20 sesiones · Trimestre 2.º** · Evaluación: :material-laptop: **examen práctico (100 %)**

> **RA4:** Desarrolla aplicaciones web embebidas en lenguajes de marcas, analizando e incorporando funcionalidades según especificaciones.

Hasta aquí tu API funciona… y **la puede usar cualquiera**. En esta unidad aprendes a saber quién está al otro lado y a recordarlo entre peticiones. Es el bloque de **estado y seguridad**, y toca los dos mundos: la sesión clásica del navegador y el token de las APIs.

!!! info "Por qué aquí y no antes"
    La autenticación se entiende cuando ya tienes algo que proteger. Con la API de la UT6 montada, «este endpoint solo para admins» significa algo. Y lo que aprendas aquí se reutiliza en la UT8, cuando el login sea un formulario en vez de un `curl`.


!!! reto "Esta unidad se da con el proyecto del trimestre"
    Desde enero no hay prácticas sueltas: hay **un producto**, [Pulso de Madrid](../proyecto/index.md), que se construye en equipos de tres. Esta unidad son los dos primeros retos: **¿quién está al otro lado?** y **¿sesión o token?**.

    Lee primero **[el proyecto](../proyecto/index.md)** y **[los seis retos](../proyecto/retos.md)**: el calendario de abajo son las sesiones de R1 y R2.

## Al terminar sabrás hacer

Marca cada casilla cuando puedas hacerlo **sin mirar los apuntes**. Lo que quede sin marcar la semana del examen es exactamente lo que hay que repasar.

- [ ] Explicar por qué **HTTP no tiene estado** y qué consecuencias tiene eso.
- [ ] Usar **cookies** y almacenamiento en el cliente sabiendo qué se puede guardar en cada sitio.
- [ ] Implementar **autenticación y autorización** con Spring Security y **JWT**.
- [ ] Elegir entre **sesión y token** justificando la decisión, y aplicar bien 401 frente a 403.

## Calendario

| Sesión (55') | En clase | Lectura previa |
|---|---|---|
| **S1** | HTTP no tiene memoria: el problema del estado | [1. El estado en la web](01-estado-en-la-web.md) §1–2 |
| **S2** | Sesión de servidor: `HttpSession` y ciclo de vida | [1. El estado en la web](01-estado-en-la-web.md) §3 |
| **S3** | El problema de escalar con sesiones | [1. El estado en la web](01-estado-en-la-web.md) §4 |
| **S4** | Laboratorio: montar y romper una sesión | [Batería de ejercicios](ejercicios.md) |
| **S5** | Cookies: atributos, seguridad y RGPD | [2. Cookies y cliente](02-cookies.md) §1–2 |
| **S6** | Almacenamiento en el cliente y cuándo usarlo | [2. Cookies y cliente](02-cookies.md) §3–4 |
| **S7** | Contraseñas: por qué BCrypt y no SHA-256 | [3. Autenticación](03-autenticacion.md) §1–2 |
| **S8** | Spring Security: la cadena de filtros | [3. Autenticación](03-autenticacion.md) §3 |
| **S9** | Usuarios, perfiles y roles | [3. Autenticación](03-autenticacion.md) §4 |
| **S10** | Laboratorio: dar de alta usuarios y cifrar contraseñas | [Batería de ejercicios](ejercicios.md) |
| **S11** | JWT: qué es y qué no es | [3. Autenticación](03-autenticacion.md) §5 |
| **S12** | Login y filtro de validación del token | [3. Autenticación](03-autenticacion.md) §6 |
| **S13** | Autorización por roles y por dato | [3. Autenticación](03-autenticacion.md) §7 |
| **S14** | La configuración que lo une todo: `SecurityFilterChain` | [3. Autenticación](03-autenticacion.md) §8 |
| **S15** | Sesión frente a token: elegir con criterio | [4. Sesión o token](04-sesion-o-token.md) |
| **S16** | Laboratorio: proteger la API de otro dominio | [Batería de ejercicios](ejercicios.md) |
| **S17** | Proyecto integrador: seguridad de punta a punta | [Reto con tests](practicas.md) |
| **S18** | Repaso: 401 frente a 403 y los fallos de siempre | [Preparar el examen](examen.md) |
| **S19** | :material-laptop: **Examen práctico de RA4 (100 %)** · 1.ª sesión | [Preparar el examen](examen.md) |
| **S20** | :material-laptop: **Examen práctico de RA4 (100 %)** · 2.ª sesión | — |

## Cómo se evalúa

:material-laptop: **Examen práctico (100 %)**: proteger una API existente y añadirle gestión de estado. Se valora que la autenticación funcione, que las contraseñas estén hasheadas, que la matriz de permisos sea correcta y que la sesión y las cookies se usen con sus atributos de seguridad.

## Material

- [Prácticas guiadas](practicas.md) — de la API abierta a la API protegida.
- [Batería de ejercicios](ejercicios.md) — con solución.
- [Chuleta](chuleta.md) — sesión, cookies, Spring Security y JWT en una página.
