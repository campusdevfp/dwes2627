# Batería de ejercicios — UT7

> 8 ejercicios sobre estado y seguridad, del mismo estilo que el examen. Con solución.

### E1 — ¿Dónde va cada dato?
Token de sesión · idioma · carrito · rol · orden de una tabla · total a pagar.

??? success "Solución"

    Cookie <code>HttpOnly</code> · cookie · sesión o BD · token firmado o sesión · <code>localStorage</code> · <b>en ningún sitio del cliente</b>: se recalcula en el servidor.


### E2 — Contador de sesión
Endpoint que cuente visitas por usuario. Demuestra con dos *cookie jars* que cada uno lleva la suya, y después rompe el ejercicio moviendo el contador a un campo del `@Service`.

??? success "Solución"

    ```java
    @GetMapping("/demo/contador")
    public Map<String,Object> contador(HttpSession sesion) {
        int n = (int) Optional.ofNullable(sesion.getAttribute("n")).orElse(0) + 1;
        sesion.setAttribute("n", n);
        return Map.of("visitas", n, "sesion", sesion.getId());
    }
    ```
    Con el campo en el `@Service`, los dos usuarios comparten número: el bean es único.


### E3 — La cookie del banco
Escribe la cookie de sesión de una aplicación bancaria justificando cada atributo.

??? success "Solución"

    <code>httpOnly(true)</code> contra XSS · <code>secure(true)</code> para que no viaje en claro · <code>sameSite("Strict")</code> contra CSRF · <code>maxAge</code> de 15 minutos. En banca se elige <code>Strict</code> aunque incomode; en una tienda, <code>Lax</code>.


### E4 — Tres fallos en el login
```java
if (u.getPassword().equals(pass))
    return Jwts.builder().subject(user).claim("password", pass).signWith(clave).compact();
throw new RuntimeException("Contraseña incorrecta para " + user);
```

??? success "Solución"

    (1) compara la contraseña <b>en claro</b>: deben estar hasheadas y comprobarse con <code>matches</code>; (2) mete la <b>contraseña dentro del JWT</b>, que es legible por cualquiera; (3) el token <b>no caduca</b>. Extra: el mensaje revela si el usuario existe.


### E5 — Matriz de permisos
Catálogo público en lectura; alta y modificación para GESTOR o ADMIN; borrado solo ADMIN; pedidos, cualquier autenticado; y un usuario solo ve **sus** pedidos.

??? success "Solución"

    ```java
    .requestMatchers(HttpMethod.GET, "/api/v1/productos/**").permitAll()
    .requestMatchers(HttpMethod.POST, "/api/v1/productos/**").hasAnyRole("GESTOR","ADMIN")
    .requestMatchers(HttpMethod.DELETE, "/api/v1/productos/**").hasRole("ADMIN")
    .requestMatchers("/api/v1/pedidos/**").authenticated()
    .anyRequest().authenticated()
    ```
    La última regla depende del <b>dato</b>, no de la ruta, así que va en el método:
    <pre><code>@PreAuthorize("hasRole('ADMIN') or #username == authentication.name")</code></pre>


### E6 — El despido
Hay que cortar el acceso a un empleado ahora mismo. ¿Qué pasa con sesión y qué con JWT?

??? success "Solución"

    Con <b>sesión</b>, inmediato: se borra del almacén. Con <b>JWT puro</b>, su token vale hasta caducar. Solución real: acceso de 15 minutos + refresco revocable; se borra el refresco y está fuera en 15 minutos como mucho. Si el requisito es cero segundos, hay que asumir una lista negra consultada en cada petición.


### E7 — CSRF y XSS
Explica los dos ataques y a qué mecanismo afecta cada uno.

??? success "Solución"

    <b>CSRF</b>: la cookie viaja sola, así que una web ajena puede provocar una petición autenticada. Afecta a la sesión con cookie; se mitiga con <code>SameSite</code> y token anti-CSRF. <b>XSS</b>: el atacante ejecuta JS y se lleva lo que el JS pueda leer — un token en <code>localStorage</code> sí, una cookie <code>HttpOnly</code> no. Cada mecanismo es vulnerable a uno de los dos.


### E8 — Simulacro de examen
Protege una API abierta: usuarios con BCrypt, tres roles, login JWT, matriz de permisos, refresco revocable, y un endpoint de preferencias que use cookie con sus tres atributos. Cinco tests, incluidos 401 y 403.

??? success "Solución"

    Es el proyecto de la <a href="../practicas/">F3 a la F5</a> completo. Puntos de corrección: contraseñas hasheadas (si no, el criterio de seguridad es 0), <code>.anyRequest().authenticated()</code> al final, mensaje de error genérico en el login, secreto en variable de entorno, y la cookie con <code>HttpOnly</code>, <code>Secure</code> y <code>SameSite</code>.


### E9 — La contraseña que se puede leer

Un compañero guarda las contraseñas con `MessageDigest.getInstance("SHA-256")`. Argumenta por qué está mal y qué se usa en su lugar.

??? success "Solución"

    SHA-256 está diseñado para ser <b>rápido</b>, que es justo lo contrario de lo que interesa aquí: una GPU prueba miles de millones por segundo, así que un diccionario revienta las contraseñas comunes en minutos. Además, sin sal, dos usuarios con la misma contraseña tienen el mismo hash y se ve a simple vista en la tabla.
    <br><br>
    Se usa <b>BCrypt</b> (o Argon2), que es deliberadamente lento, lleva la sal incorporada en el propio hash y tiene un <i>factor de coste</i> ajustable: cuando el hardware mejora, se sube el coste y sigue siendo caro atacarlo.
    <pre><code>@Bean PasswordEncoder encoder() { return new BCryptPasswordEncoder(12); }</code></pre>
    Comprobación en el examen: <code>grep -r "SHA-256\|MD5" src/</code> sobre el código de contraseñas es un cero directo en el criterio.


### E10 — 401 o 403, con tres peticiones

Tienes `/api/v1/productos` (público en GET), `/api/v1/productos` (POST, rol GESTOR) y `/admin` (rol ADMIN). Escribe las tres órdenes `curl` que demuestran que la matriz de permisos funciona, y di qué código debe devolver cada una.

??? success "Solución"

    <pre><code>curl -o /dev/null -w "%{http_code}\n" localhost:8080/api/v1/productos
    # 200 · es público

    curl -o /dev/null -w "%{http_code}\n" -X POST localhost:8080/api/v1/productos \
         -H "Content-Type: application/json" -d '{"nombre":"X"}'
    # 401 · no sé quién eres

    curl -o /dev/null -w "%{http_code}\n" localhost:8080/admin \
         -H "Authorization: Bearer $TOKEN_DE_USUARIO_NORMAL"
    # 403 · sé quién eres y no puedes</code></pre>
    La confusión clásica es devolver 403 en la segunda. Regla: <b>sin credenciales → 401; con credenciales insuficientes → 403</b>.
    <br><br>
    Si el 403 te sale como 401, casi siempre es que el filtro de JWT no está poblando el <code>SecurityContext</code> y Spring cree que sigues siendo anónimo.


### E11 — El token que no caduca nunca

Encuentras en un proyecto `.setExpiration(new Date(System.currentTimeMillis() + 31536000000L))`. Explica el problema y propón la alternativa.

??? success "Solución"

    Son <b>365 días</b>. Un token JWT no se puede revocar: mientras la firma sea válida y no haya caducado, el servidor lo acepta. Con un año de vida, quien robe ese token tiene acceso durante un año, aunque el usuario cambie la contraseña o lo echen de la empresa.
    <br><br>
    La alternativa es la pareja <b>acceso corto + refresco revocable</b>:
    <ul>
    <li><b>Token de acceso</b>: 15 minutos. Va en cada petición. Si se roba, la ventana es pequeña.</li>
    <li><b>Token de refresco</b>: días o semanas, <b>guardado en base de datos</b>. Sirve solo para pedir un token de acceso nuevo, y como está almacenado se puede borrar.</li>
    </ul>
    Cerrar sesión, cambiar la contraseña o dar de baja a alguien = borrar su refresco. En 15 minutos como mucho está fuera.


### E12 — El carrito que se pierde

Un carrito de la compra guardado en `HttpSession` funciona en desarrollo y se vacía solo en producción. La aplicación corre en tres instancias detrás de un balanceador. Diagnostica y da dos soluciones.

??? success "Solución"

    La sesión vive <b>en la memoria de una instancia</b>. Si la petición siguiente cae en otra, esa instancia no conoce esa sesión y el carrito aparece vacío. En desarrollo hay una sola instancia y por eso no se ve.
    <br><br>
    Dos salidas correctas:
    <ol>
    <li><b>Sesión compartida</b>: guardarla fuera del proceso, con Spring Session y Redis. Una línea de dependencia y sigue funcionando <code>HttpSession</code> tal cual.</li>
    <li><b>Sesiones pegajosas</b> (<i>sticky sessions</i>): el balanceador manda siempre al mismo usuario a la misma instancia. Funciona, pero si esa instancia cae se pierden las sesiones, y estorba al escalar.</li>
    </ol>
    La tercera opción —no guardar estado en el servidor y usar token— es la que eligen las APIs, y es exactamente el debate del <a href="../04-sesion-o-token/">tema 4</a>.

