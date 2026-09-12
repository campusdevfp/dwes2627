# Vistas con seguridad e idiomas

En la UT7 protegiste una API con tokens y `curl`. Ahora el usuario es una persona con un navegador, y eso cambia las piezas: en vez de un JWT en una cabecera, un **formulario de login y una sesión**.

## 1. Login de formulario con Spring Security

```java
@Bean
SecurityFilterChain web(HttpSecurity http) throws Exception {
    return http
        .authorizeHttpRequests(a -> a
            .requestMatchers("/", "/productos", "/productos/*").permitAll()
            .requestMatchers("/css/**", "/js/**", "/img/**").permitAll()
            .requestMatchers("/admin/**").hasRole("ADMIN")
            .anyRequest().authenticated())
        .formLogin(f -> f
            .loginPage("/login")               // tu plantilla
            .defaultSuccessUrl("/", true)
            .failureUrl("/login?error")
            .permitAll())
        .logout(l -> l
            .logoutSuccessUrl("/?salida")
            .permitAll())
        .build();
}
```

Y la plantilla, que es un formulario normal:

```html
<form th:action="@{/login}" method="post">
    <div class="alerta-error" th:if="${param.error}">Usuario o contraseña incorrectos</div>
    <div class="alerta-ok"    th:if="${param.logout}">Sesión cerrada</div>

    <label for="username">Usuario</label>
    <input type="text" id="username" name="username" required autofocus>

    <label for="password">Contraseña</label>
    <input type="password" id="password" name="password" required>

    <button type="submit">Entrar</button>
</form>
```

!!! warning "Los nombres son `username` y `password`"
    No son elegibles salvo que los cambies explícitamente con `.usernameParameter(...)`. Si escribes `name="usuario"`, Spring Security no encuentra nada, y el login falla sin decir por qué.

Y el cierre de sesión **es un POST**, por lo mismo que el borrado del tema 4:

```html
<form th:action="@{/logout}" method="post" class="en-linea">
    <button type="submit" class="enlace">Cerrar sesión</button>
</form>
```

!!! danger "El mensaje de error no debe distinguir"
    «Usuario o contraseña incorrectos», nunca «ese usuario no existe». La segunda versión permite averiguar qué cuentas existen probando correos, que es el primer paso de un ataque dirigido.

    Es un criterio de la rúbrica y cuesta cero escribirlo bien.

## 2. Pintar según quién mira

Con el dialecto de seguridad de Thymeleaf:

```xml title="pom.xml"
<dependency>
    <groupId>org.thymeleaf.extras</groupId>
    <artifactId>thymeleaf-extras-springsecurity6</artifactId>
</dependency>
```

```html
<html xmlns:sec="http://www.thymeleaf.org/extras/spring-security">

<div sec:authorize="isAnonymous()">
    <a th:href="@{/login}">Entrar</a>
</div>

<div sec:authorize="isAuthenticated()">
    Hola, <span sec:authentication="name">usuario</span>
    <form th:action="@{/logout}" method="post"><button>Salir</button></form>
</div>

<a sec:authorize="hasRole('ADMIN')" th:href="@{/admin}">Administración</a>

<!-- por dato, no solo por rol -->
<a sec:authorize="${#authentication.name == pedido.cliente}"
   th:href="@{/pedidos/{id}/editar(id=${pedido.id})}">Editar</a>
```

!!! danger "Ocultar un botón NO es proteger un endpoint"
    `sec:authorize` decide **qué se pinta**. Nada más. Quien conozca la URL puede escribirla en la barra de direcciones.

    La protección real está en la cadena de filtros o en `@PreAuthorize` sobre el método. El `sec:authorize` es para que la interfaz no muestre botones que van a dar 403.

    En el examen se comprueba pidiendo la URL protegida directamente con `curl`. Si responde 200, el criterio es cero por muy oculto que estuviera el enlace.

Sin el dialecto, desde el controlador, también vale:

```java
@GetMapping("/perfil")
public String perfil(Authentication auth, Model modelo) {
    modelo.addAttribute("usuario", servicio.porNombre(auth.getName()));
    return "perfil";
}
```

## 3. La página 403 propia

Cuando alguien autenticado llega a donde no debe, la pantalla por defecto es fea y no explica nada:

```java
.exceptionHandling(e -> e.accessDeniedPage("/sin-permiso"))
```

```html title="templates/sin-permiso.html"
<h1>No tienes permiso</h1>
<p>Tu cuenta no puede acceder a esta sección. Si crees que es un error, avisa al administrador.</p>
<a th:href="@{/}">Volver al inicio</a>
```

Junto con las páginas `error/404.html` y `error/500.html` del tema 3, esto completa el trato decente al usuario cuando algo va mal. Es un criterio pequeño de la rúbrica y casi nadie lo hace.

## 4. Textos en varios idiomas

El criterio del RA8 habla de generar páginas *«según especificaciones»*, y la internacionalización es la especificación más común en una empresa con clientes fuera.

El mecanismo es sencillo: los textos salen de ficheros de propiedades, no del HTML.

``` { .text .sinajuste }
src/main/resources/
├── messages.properties        (por defecto)
├── messages_es.properties
└── messages_en.properties
```

```properties title="messages_es.properties"
producto.titulo=Catálogo de productos
producto.vacio=No hay productos
producto.precio=Precio
carrito.total=Total del carrito: {0} €
```

```properties title="messages_en.properties"
producto.titulo=Product catalogue
producto.vacio=No products found
producto.precio=Price
carrito.total=Cart total: {0} €
```

```html
<h1 th:text="#{producto.titulo}">Catálogo</h1>
<th th:text="#{producto.precio}">Precio</th>
<p  th:text="#{carrito.total(${total})}">Total: 0 €</p>
```

Ese `{0}` es un parámetro posicional: los textos con datos dentro **no se concatenan**, se parametrizan. Concatenar rompe los idiomas cuyo orden de palabras es distinto.

Para permitir el cambio de idioma:

```java
@Bean LocaleResolver localeResolver() {
    var r = new SessionLocaleResolver();
    r.setDefaultLocale(Locale.forLanguageTag("es"));
    return r;
}

@Bean LocaleChangeInterceptor cambioIdioma() {
    var i = new LocaleChangeInterceptor();
    i.setParamName("lang");                 // ?lang=en
    return i;
}
```

```html
<a th:href="@{''(lang='es')}">ES</a> · <a th:href="@{''(lang='en')}">EN</a>
```

Y los mensajes de validación también se traducen, usando la clave en la anotación:

```java
@NotBlank(message = "{producto.nombre.obligatorio}")
private String nombre;
```

!!! tip "Empieza por aquí aunque solo tengas un idioma"
    Sacar los textos a `messages.properties` desde el principio cuesta lo mismo y te ahorra una tarde de buscar y reemplazar el día que aparezca el segundo idioma. Además obliga a que los textos sean coherentes, porque están todos juntos y se ven las repeticiones.

## 5. Accesibilidad: lo mínimo exigible

No es un capricho: la normativa de accesibilidad web es obligatoria para las administraciones públicas españolas, y en el examen se mira.

- Cada `<input>` con su `<label for="...">`. No vale el `placeholder` como etiqueta: desaparece al escribir.
- `alt` en todas las imágenes con contenido; `alt=""` en las decorativas.
- Encabezados en orden: un `<h1>` por página, sin saltar de `<h2>` a `<h4>`.
- Los errores de validación, asociados al campo con `aria-describedby`.
- Contraste suficiente y foco visible: no elimines el borde de foco sin sustituirlo.

```html
<label for="precio">Precio (€)</label>
<input type="number" id="precio" th:field="*{precio}"
       aria-describedby="err-precio"
       th:attrappend="aria-invalid=${#fields.hasErrors('precio')} ? 'true'">
<p id="err-precio" class="msg-error" th:errors="*{precio}"></p>
```

## Pruébalo ahora (15 min)

!!! reto "Trabaja tú ahora"
    Este bloque se hace **en clase, en tu equipo**. No se entrega ni puntúa: es la práctica que hace que el examen te salga.

Login, roles e idiomas en un proyecto mínimo.

``` { .java .numerado title="SecurityConfig.java" }
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain filtros(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(a -> a
                .requestMatchers("/", "/css/**", "/login").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .formLogin(f -> f.loginPage("/login").defaultSuccessUrl("/", true).permitAll())
            .logout(l -> l.logoutSuccessUrl("/").permitAll())
            .build();
    }

    @Bean
    UserDetailsService usuarios(PasswordEncoder codificador) {
        return new InMemoryUserDetailsManager(
            User.withUsername("ana").password(codificador.encode("ana")).roles("USER").build(),
            User.withUsername("jefe").password(codificador.encode("jefe")).roles("ADMIN").build());
    }

    @Bean
    PasswordEncoder codificador() { return new BCryptPasswordEncoder(); }
}
```

``` { .html .numerado title="templates/login.html" }
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<body>
    <form th:action="@{/login}" method="post">
        <p th:if="${param.error}" class="error">Usuario o contraseña incorrectos</p>
        <p th:if="${param.logout}" class="ok">Sesión cerrada</p>

        <label for="username">Usuario</label>
        <input id="username" type="text" name="username" required>

        <label for="password">Contraseña</label>
        <input id="password" type="password" name="password" required>

        <button type="submit">Entrar</button>
    </form>
</body>
</html>
```

``` { .html .numerado title="templates/index.html" }
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      xmlns:sec="http://www.thymeleaf.org/extras/spring-security">
<body>
    <p sec:authorize="isAnonymous()">
        <a th:href="@{/login}">Entrar</a>
    </p>

    <div sec:authorize="isAuthenticated()">
        <span th:text="|Hola, ${#authentication.name}|">Hola</span>
        <span sec:authorize="hasRole('ADMIN')"> · eres administrador</span>

        <form th:action="@{/logout}" method="post">
            <button type="submit">Salir</button>
        </form>
    </div>
</body>
</html>
```

Cuatro cosas que probar:

1. Entra como `ana`, luego como `jefe`. El texto de administrador solo sale con el segundo.
2. **Con `ana` dentro, pide `/admin` a mano** en la barra de direcciones. Sale un 403: el `sec:authorize` ocultó el enlace, pero quien protege es `SecurityConfig`.
3. **Quita la línea `.requestMatchers("/admin/**").hasRole("ADMIN")`** y repite. Ahora `ana` entra en `/admin` aunque el enlace siga oculto. Ese es el ejercicio 7, en vivo.
4. Cambia el `<button>Salir</button>` por un `<a th:href="@{/logout}">Salir</a>`. Deja de funcionar: el logout es un POST.

---

## Ejercicios (con solución)

### Ejercicio 1 — Los nombres de los campos

¿Qué nombres deben tener los campos del formulario de login y por qué?

??? success "Solución"

    **`username` y `password`**, exactamente así.

    El motivo es que quien procesa el POST de `/login` no es un controlador tuyo, sino un filtro de Spring Security (`UsernamePasswordAuthenticationFilter`), y ese filtro busca esos dos parámetros por su nombre.

    Si los llamas `usuario` y `clave`, el filtro no encuentra nada, la autenticación falla siempre y **no hay ningún error que te lo diga**: vuelves al login con `?error` y a saber por qué.

    Se pueden cambiar, pero hay que decírselo:

    ```java
    .formLogin(f -> f.usernameParameter("usuario").passwordParameter("clave"))
    ```

    Merece la pena no hacerlo: es una fuente de fallos a cambio de nada.

### Ejercicio 2 — Por qué el logout es POST

??? success "Solución"

    Porque un GET puede dispararlo cualquiera sin que el usuario lo pida. Si `/logout` fuese un enlace, bastaría con que alguien pusiera esto en un foro:

    ```html
    <img src="https://tuweb/logout">
    ```

    y todo el que abriera esa página quedaría desconectado de tu aplicación. Es molesto más que grave, pero es el mismo mecanismo que el borrado por enlace del tema 4: **un GET no debe cambiar estado**.

    Además, al ser POST lleva testigo CSRF, así que solo tu propio formulario puede dispararlo.

    ```html
    <form th:action="@{/logout}" method="post">
        <button type="submit">Salir</button>
    </form>
    ```

    Si de verdad quieres que parezca un enlace, se maquilla con CSS. El método no se toca.

### Ejercicio 3 — `sec:authorize` no protege

Explica por qué `sec:authorize` no protege nada y qué sí lo hace.

??? success "Solución"

    Porque **solo decide qué se pinta**. Cuando la condición es falsa, la etiqueta no llega al HTML; pero la ruta que había detrás sigue existiendo y sigue respondiendo.

    Quitar el botón no cierra la puerta: solo esconde el picaporte. Basta con escribir la URL a mano.

    Lo que protege de verdad es la configuración del servidor:

    ```java
    .requestMatchers("/admin/**").hasRole("ADMIN")
    ```

    o, a nivel de método, en el servicio:

    ```java
    @PreAuthorize("hasRole('ADMIN')")
    public void borrar(Long id) { … }
    ```

    `sec:authorize` sirve para **no enseñar botones que darían 403**, que es una cuestión de buena educación con el usuario. Nada más.

### Ejercicio 4 — «Ese usuario no existe»

¿Qué problema tiene ese mensaje?

??? success "Solución"

    Que distingue entre *«el usuario no existe»* y *«la contraseña es incorrecta»*, y con eso **confirma qué correos están registrados**.

    Un atacante prueba direcciones una por una y va apuntando cuáles dan «contraseña incorrecta»: acaba de conseguir la lista de usuarios de tu aplicación sin entrar en ninguna cuenta. Con esa lista ya puede ir a por las contraseñas, o simplemente venderla.

    Es peor de lo que parece si la aplicación es sensible: saber que alguien tiene cuenta en un sitio ya es información.

    El mensaje correcto es **el mismo para los dos casos**:

    > Usuario o contraseña incorrectos.

    Spring Security lo hace bien por defecto: lanza `BadCredentialsException` en ambos. El fallo aparece cuando alguien decide «mejorar» el mensaje.

### Ejercicio 5 — El parámetro dentro del mensaje

¿Por qué `#{carrito.total(${total})}` y no concatenar el total al texto?

??? success "Solución"

    Porque **el orden de las palabras cambia con el idioma**, y concatenando lo estás fijando en español.

    ```properties
    # messages_es.properties
    carrito.total=Tu carrito suma {0} euros
    # messages_en.properties
    carrito.total=Your basket adds up to {0} euros
    # messages_de.properties
    carrito.total=Dein Warenkorb enthält Waren im Wert von {0} Euro
    ```

    Con el parámetro dentro del mensaje, cada traductor coloca el hueco donde le pida su idioma. Con `#{carrito.total} + ${total}` el número va siempre al final, y en alemán queda mal.

    Y hay una segunda razón: **el formateo**. Un `BigDecimal` concatenado se pinta como `1234.5`; pasado como parámetro, el motor de mensajes lo formatea según el idioma activo: `1.234,50` en español y `1,234.50` en inglés.

### Ejercicio 6 — Tres requisitos de accesibilidad

Nombra tres que se comprueben en la rúbrica.

??? success "Solución"

    1. **Cada campo con su `<label for="...">`.** No basta con un `placeholder`: desaparece al escribir y muchos lectores de pantalla no lo anuncian.
    2. **Toda imagen con `alt`.** Con texto descriptivo si aporta información, o `alt=""` si es decorativa, para que el lector la salte.
    3. **Encabezados en orden**, un solo `<h1>` por página y sin saltarse niveles. Es el índice con el que un usuario ciego navega la página.

    Otras que también entran: contraste suficiente entre texto y fondo; que todo se pueda usar **solo con el teclado**, con el foco visible; que el idioma esté declarado con `<html lang="es">`; y que los mensajes de error digan qué hacer, no solo que algo falló.

    No es un añadido opcional: en una administración pública es **obligación legal**.

### Ejercicio 7 — «Ya está seguro porque oculté el botón»

Un compañero oculta el botón «Borrar» a los usuarios normales y dice que ya está seguro. Demuéstrale que no.

??? success "Solución"

    Con dos órdenes. Primero entra como usuario normal y guarda la sesión:

    ```bash
    curl -c galletas.txt -X POST http://localhost:8080/login \
         -d "username=ana&password=ana"
    ```

    Y ahora, con esa misma sesión de usuario raso, llama a la ruta que el botón escondía:

    ```bash
    curl -b galletas.txt -X POST http://localhost:8080/productos/7/borrar
    ```

    Si el producto desaparece, queda demostrado: **el botón estaba oculto, la ruta estaba abierta**.

    En el navegador se ve todavía más rápido: F12, y en el inspector el `<form>` sigue ahí… o ni eso, porque con `sec:authorize` ni siquiera está. Da igual: la URL se escribe a mano.

    Lo que hay que añadir es la protección de verdad:

    ```java
    .requestMatchers(HttpMethod.POST, "/productos/*/borrar").hasRole("ADMIN")
    ```

    **Regla del módulo:** ocultar no es proteger. Lo que decide el cliente, lo decide el atacante.
