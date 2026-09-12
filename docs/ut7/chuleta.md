# Chuleta — Estado, sesiones y autenticación

## Sesión

```java
sesion.setAttribute("usuario", u);   sesion.getAttribute("usuario");   sesion.invalidate();
@SessionAttributes("carrito")        // a nivel de @Controller
```
```yaml
server.servlet.session:
  timeout: 30m
  cookie: { http-only: true, secure: true, same-site: lax }
```

## Cookies

```java
ResponseCookie.from("idioma", "es")
    .httpOnly(true).secure(true).sameSite("Lax")
    .path("/").maxAge(Duration.ofDays(365)).build();

@CookieValue(value = "idioma", defaultValue = "es") String idioma
```

| Atributo | Evita |
|---|---|
| `HttpOnly` | robo por XSS |
| `Secure` | interceptación en claro |
| `SameSite=Lax` | CSRF |

## Dónde guardar

cookie → lo necesita el servidor · `localStorage` → solo el navegador · sesión/BD → datos sensibles · **nunca** el precio ni el rol sin firmar.

## Contraseñas

```java
new BCryptPasswordEncoder(12);      // NUNCA SHA-256
encoder.matches(raw, hash);
```

## Spring Security

```java
.csrf(c -> c.disable())                                    // API sin cookies
.sessionManagement(s -> s.sessionCreationPolicy(STATELESS))
.authorizeHttpRequests(a -> a
    .requestMatchers("/auth/**").permitAll()
    .requestMatchers(HttpMethod.DELETE, "/api/v1/**").hasRole("ADMIN")
    .anyRequest().authenticated())                          // ← cerrado por defecto
```
`.roles("ADMIN")` → autoridad `ROLE_ADMIN` · **401** no sé quién eres · **403** no puedes.

## JWT

Tres partes · el payload **se lee sin secreto** · la firma garantiza integridad, no confidencialidad.
Acceso corto (15 min) + **refresco revocable**. Secreto en variable de entorno.

## Sesión o token

Navegador con HTML de servidor → **sesión**. Móvil, SPA u otro servicio → **token**.
Revocación inmediata → sesión. Escalado sin infraestructura → token.
