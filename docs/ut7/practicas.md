# Reto de la unidad — UT7

> **Reto `ut7-seguridad-base`**, en GitHub Classroom. Se te entrega una API abierta y funcionando, y tests en rojo que piden protegerla. Tu trabajo es ponerlos en verde.
>
> Arranca desde una base que funciona: lo que te saliera mal en la unidad anterior no te condiciona.

| Fase | Sesiones | Qué añades |
|---|---|---|
| **F1** | S1–S3 | Endpoints con `HttpSession`: login web, contador y carrito en sesión |
| **F2** | S4–S5 | Cookie de idioma con sus atributos; comparar con `localStorage` |
| **F3** | S6–S8 | Spring Security: cerrar la API, usuarios con BCrypt y roles |
| **F4** | S9–S11 | Login JWT, filtro de validación y matriz de permisos |
| **F5** | S12 | Refresh token revocable y la prueba del despido |

## F1 — Estado con sesión

Monta `POST /demo/login-web`, `GET /demo/quien-soy` y un carrito en sesión. Compruébalo con dos *cookie jars*.

??? success "Comprobación"

    ```bash
    curl -c ana.txt -X POST "localhost:8080/demo/login-web?usuario=ana"
    curl -c luis.txt -X POST "localhost:8080/demo/login-web?usuario=luis"
    curl -b ana.txt localhost:8080/demo/quien-soy     # ana
    curl -b luis.txt localhost:8080/demo/quien-soy    # luis
    ```
    El experimento que hay que hacer sí o sí: mover ese estado a un campo del `@Service` y ver a los dos usuarios compartiendo carrito.


## F2 — Cookies

Endpoint que fija el idioma con `HttpOnly`, `Secure` y `SameSite`, y otro que lo lee.

??? success "Comprobación"

    Con `curl -i` debe verse la cabecera `Set-Cookie` completa. En el navegador, `document.cookie` **no** debe mostrar la que sea `HttpOnly`.


## F3 — Cerrar la API

Añade Spring Security y comprueba que **todo** da 401. Después abre lo justo: `/auth/**` y los `GET` públicos.

??? success "Comprobación"

    El susto inicial es intencionado: el valor por defecto de Spring Security es <b>cerrado</b>. Si alguien pone `.anyRequest().permitAll()` arriba «para que funcione», acaba de abrir la API entera.


## F4 — JWT y permisos

Login que devuelve el token, filtro que lo valida y la matriz completa: GET público 200 · DELETE sin token 401 · DELETE con USER 403 · DELETE con ADMIN 204.

??? success "Comprobación"

    ```bash
    T=$(curl -s -X POST localhost:8080/auth/login -H "Content-Type: application/json" \
        -d '{"username":"admin","password":"admin123"}' | jq -r .token)
    echo $T | cut -d. -f2 | base64 -d 2>/dev/null | jq     # el payload se lee SIN secreto
    ```
    Si el 403 sale como 401, casi siempre es el prefijo `ROLE_`.


## F5 — Refresco y revocación

Token de acceso de 60 segundos + refresco revocable. Haz la prueba del despido: borra el refresco y comprueba que el acceso sigue valiendo hasta caducar.

??? success "Comprobación"

    Ese hueco de 60 segundos <b>es</b> la limitación real de JWT. Verla es el objetivo del ejercicio.

