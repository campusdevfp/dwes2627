# 3. CORS y documentación de APIs

!!! tip "Tip del Examinador"
    En el examen valoran saber qué es CORS y cómo configurarlo. Es un tema frecuente.

---

## 3.1. CORS

Los navegadores web implementan una política de seguridad llamada [Cross-Origin Resource Sharing](https://developer.mozilla.org/es/docs/Web/Security/Same-origin_policy), que impide que una página web pueda acceder a recursos de otro dominio. Por ejemplo, si tenemos una página web en el dominio `https://www.midominio.com`, no podremos acceder a recursos de otro dominio, como por ejemplo `https://www.otrodominio.com`. Esto es así por seguridad, para evitar que una página web pueda acceder a recursos de otro dominio sin nuestro consentimiento.

CORS (Cross-Origin Resource Sharing) es un mecanismo de seguridad implementado en los navegadores web para controlar las solicitudes HTTP entre diferentes dominios o recursos de origen cruzado. En términos más sencillos, CORS permite que un servidor especifique a qué dominios o recursos externos se les permite acceder a sus recursos.

Aquí tienes algunos ejemplos que ilustran la importancia de CORS:

- Protección contra ataques CSRF (Cross-Site Request Forgery): Supongamos que tienes un sitio web donde los usuarios inician sesión y realizan acciones sensibles, como cambiar la contraseña o realizar transacciones financieras. Si no se implementa CORS correctamente, un atacante podría crear un sitio web malicioso y engañar a los usuarios para que visiten ese sitio mientras están autenticados en tu sitio legítimo. El sitio malicioso podría enviar solicitudes falsificadas a tu servidor en nombre del usuario, realizando acciones no deseadas. Al habilitar CORS y configurarlo adecuadamente, puedes evitar que los sitios no autorizados realicen solicitudes a tus recursos, protegiendo así a tus usuarios contra ataques CSRF.
- Compartir recursos entre dominios: CORS también es útil cuando deseas compartir recursos entre diferentes dominios. Por ejemplo, si tienes una API REST en un dominio y deseas permitir que una aplicación web en otro dominio acceda a esos recursos, puedes habilitar CORS para permitir solicitudes desde el dominio de la aplicación web. Esto permite que la aplicación web consuma datos o servicios de tu API de manera segura y controlada.
- Acceso a recursos de terceros: CORS también es relevante cuando tu sitio web necesita acceder a recursos de terceros, como una API de redes sociales o un servicio de mapas. Sin CORS, el navegador bloquearía las solicitudes a esos recursos debido a las políticas de seguridad del mismo origen. Al configurar CORS correctamente, puedes permitir que tu sitio web acceda a esos recursos externos y proporcione una experiencia rica e integrada para tus usuarios.

```java
@Configuration
public class CorsConfig {
    /**
     * CORS: Configuración más ajustada.
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {

            @Override
            // Ajustamos una configuración específica para cada serie de métodos
            // Así por cada fuente podemos permitir lo que queremos
            // Por ejemplo ene esta configuración solo permitirmos el dominio producto
            // Permitimos solo un dominio
            // e indicamos los verbos que queremos usar
            // Debes probar con uncliente desde ese puerto
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/rest/producto/**")
                        //.allowedOrigins("http://localhost:6980")
                        .allowedMethods("GET", "POST", "PUT", "DELETE")
                        .maxAge(3600);
            }

        };
    }
}
```

## 3.2. Documentación con Swagger y OpenAPI
OpenAPI es una especificación para describir APIs REST. Swagger es una herramienta que nos permite generar documentación de APIs REST a partir de la especificación OpenAPI. 
- [OpenAPI](https://www.openapis.org/): OpenAPI Specification (anteriormente conocido como Swagger Specification) es una especificación de lenguaje agnóstico y basado en JSON o YAML que describe una API RESTful. Proporciona una forma estándar de describir la estructura de las solicitudes y respuestas, los parámetros, los esquemas de datos, las operaciones disponibles y otra información relevante de una API.
La especificación OpenAPI permite a los desarrolladores y equipos de desarrollo documentar, diseñar y construir APIs de manera consistente y colaborativa. Además, facilita la generación automática de documentación interactiva, la creación de clientes y servidores de API, y la validación y prueba de la API.
- [Swagger](https://swagger.io/): Swagger es una suite de herramientas de código abierto que se utiliza para diseñar, construir, documentar y consumir APIs RESTful basadas en la especificación OpenAPI. Swagger proporciona un conjunto de bibliotecas y herramientas que permiten a los desarrolladores generar automáticamente documentación interactiva, generar clientes de API en varios lenguajes de programación y realizar pruebas de API.
Swagger incluye componentes clave, como Swagger UI (una interfaz de usuario interactiva para visualizar y probar API), Swagger Editor (un editor en línea para escribir y validar la especificación OpenAPI) y Swagger Codegen (una herramienta para generar clientes y servidores de API a partir de la especificación OpenAPI).

Para ello, debemos añadir la [dependencia de Swagger en nuestro proyecto](https://springdoc.org/#Introduction):

=== "Maven (lo que usamos)"

    ```xml
    <dependency>
      <groupId>org.springdoc</groupId>
      <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
      <version>2.7.0</version>
    </dependency>
    ```

    Una sola dependencia, y ya tienes:

    - El JSON de la especificación en **`/v3/api-docs`**
    - La interfaz web de Swagger en **`/swagger-ui/index.html`**

=== "Gradle (el original)"

    ```kotlin
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.2.0")
    ```

El primer paso es configurar Swagger en nuestra aplicación. Para ello, debemos crear una clase de configuración, añadirle los metadatos de Swagger e indicar los endpoints que queremos documentar. Por ejemplo:

```java
@Configuration
class SwaggerConfig {
    @Bean
    OpenAPI apiInfo() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("API REST de Funkos — DWES 2.º DAW")
                                .version("1.0.0")
                                .description("API de ejemplo del módulo Desarrollo Web en Entorno Servidor")
                                .termsOfService("https://iesx.es/terminos")
                                .license(
                                        new License()
                                                .name("CC BY-NC-SA 4.0")
                                                .url("https://creativecommons.org/licenses/by-nc-sa/4.0/")
                                )
                                .contact(
                                        new Contact()
                                                .name("2.º DAW · IES Ejemplo")
                                                .email("dwes@iesx.es")
                                                .url("https://iesx.es")
                                )

                )
                .externalDocs(
                        new ExternalDocumentation()
                                .description("Repositorio y Documentación del Proyecto y API")
                                .url("https://github.com/iesx-daw/funkos-api")
                );
    }

    @Bean
    GroupedOpenApi httpApi() {
        return GroupedOpenApi.builder()
                .group("http")
                //.pathsToMatch("/api/**") // Todas las rutas
                .pathsToMatch("/api/v1/funkos/**") 
                //.pathsToMatch("/api/test/**")
                .displayName("API de Funkos")
                .build();
    }
}
```

Finalmente podemos acceder a la ruta en: http://localhost:XXXX/swagger-ui/index.html (XXXX es el puerto de nuestra aplicación). Por ejemplo: http://localhost:3000/swagger-ui/index.html

***NOTA***: No olvides abrir el endpoint de Swagger en el fichero de configuración de Spring Security.

```java
 // Abrimos a Swagger
.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
```

#### 3.2.1. Documentando nuestros endpoints
Para ello usaremos las anotaciones
- `@Operation`: Para documentar un método
- `@Parameter`: Para documentar un parámetro o `@Parameters` para documentar varios parámetros
- `@RequestBody`: Para documentar el cuerpo de una petición
- `@ApiResponse`: Para documentar una respuesta o `@ApiResponses` para documentar varias respuestas

Un ejemplo de documentación de un método:

```java
/**
     * Actualizar un producto
     *
     * @param id                    del producto a actualizar, se pasa como parámetro de la URL /{id}
     * @param productoUpdateRequest a actualizar
     * @return Producto actualizado
     * @throws ProductoNotFound                    si no existe el producto (404)
     * @throws HttpClientErrorException.BadRequest si el producto no es correcto (400)
     */
    @Operation(summary = "Actualiza un producto", description = "Actualiza un producto")
    @Parameters({
            @Parameter(name = "id", description = "Identificador del producto", example = "1", required = true)
    })
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Producto a actualizar", required = true)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Producto actualizado"),
            @ApiResponse(responseCode = "400", description = "Producto no válido"),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado"),
    })
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Solo los administradores pueden acceder
    public ResponseEntity<ProductoResponse> updateProduct(@PathVariable Long id, @Valid @RequestBody ProductoUpdateRequest productoUpdateRequest) {
        log.info("Actualizando producto por id: " + id + " con producto: " + productoUpdateRequest);
        return ResponseEntity.ok(productosService.update(id, productoUpdateRequest));
    } );
}
```

#### 3.2.2. Documentando nuestras entidades, modelos o DTOs
Para ello usaremos la anotación `@Schema`:

```java
@Schema(description = "Datos de un producto")
public record ProductoResponse(

        @Schema(description = "Identificador", example = "1")
        Long id,

        @Schema(description = "Nombre del producto", example = "Teclado mecánico")
        String nombre,

        @Schema(description = "Precio en euros", example = "79.90")
        BigDecimal precio,

        @Schema(description = "Unidades en stock", example = "10")
        Integer stock,

        @Schema(description = "Nombre de la categoría", example = "INFORMATICA")
        String categoria,

        @Schema(description = "Fecha de creación, en ISO-8601", example = "2026-06-12T10:31:02")
        LocalDateTime createdAt
) {}
```

Finalmente entrando en la ruta de Swagger podemos ver la documentación de nuestros endpoints.

## Casos especiales

Los sitios donde esto se rompe, y qué hacer.

### Quién bloquea

Tu API funciona con `curl` y falla desde el navegador con un error de CORS. ¿Quién bloquea y por qué?

**El navegador**, no tu servidor.

La petición **sí llega** al servidor, se procesa y se responde con un 200. Lo que hace el navegador es **negarle el resultado al JavaScript** que la pidió, porque la respuesta no trae `Access-Control-Allow-Origin` con su origen.

`curl` no implementa la política del mismo origen, así que no le afecta. Y eso explica el síntoma: *«pero si funciona en Postman»*.

**Consecuencia práctica:** CORS **no es un mecanismo de seguridad del servidor**. No protege tu API de nada: cualquiera puede llamarla desde fuera de un navegador. Lo que hace es proteger **al usuario** de que una web maliciosa use su sesión en otro sitio.

### La petición `OPTIONS`

En el log aparece un `OPTIONS /api/v1/funkos` antes de cada POST. ¿Qué es?

Es el ***preflight***: antes de una petición «no simple», el navegador **pregunta primero** si tiene permiso.

Dispara *preflight* cualquier petición que:

- Use un verbo que no sea `GET`, `HEAD` o `POST`.
- Lleve `Content-Type: application/json` (sí: **esto solo ya lo dispara**).
- Lleve cabeceras propias como `Authorization`.

O sea: **prácticamente cualquier petición de una API REST**.

```
OPTIONS /api/v1/funkos
Origin: http://localhost:5173
Access-Control-Request-Method: POST
Access-Control-Request-Headers: content-type

→ 200
Access-Control-Allow-Origin: http://localhost:5173
Access-Control-Allow-Methods: POST
Access-Control-Allow-Headers: content-type
Access-Control-Max-Age: 3600
```

El `Max-Age` es el que importa en rendimiento: sin él, **cada** POST lleva dos viajes de ida y vuelta.

### Configurar CORS

Tu frontend está en `http://localhost:5173` en desarrollo y en `https://miapp.es` en producción. Configúralo.

```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${cors.origenes-permitidos}")
    private String[] origenes;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(origenes)
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .exposedHeaders("Location", "X-Total-Count")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

```properties title="application-dev.properties"
cors.origenes-permitidos=http://localhost:5173,http://localhost:3000
```

```properties title="application-prod.properties"
cors.origenes-permitidos=https://miapp.es
```

**Tres cosas que importan:**

1. **Por configuración, no en el código.** Es el tema 4 de la UT4 aplicado: el mismo `.jar` sirve en los dos entornos.
2. **`exposedHeaders`.** Por defecto, el JavaScript **solo puede leer seis cabeceras**. Si tu 201 devuelve `Location` y el cliente no lo ve, es por esto.
3. **`@CrossOrigin` en el controlador vale para pruebas**, pero reparte la política por todo el código y hay que acordarse en cada clase nueva.

### El comodín peligroso

```java
.allowedOrigins("*")
.allowCredentials(true)
```

**No funciona**: el navegador rechaza la combinación y verás

```
The value of the 'Access-Control-Allow-Origin' header must not be the
wildcard '*' when the request's credentials mode is 'include'.
```

Y está prohibido **a propósito**: `allowCredentials(true)` hace que el navegador mande cookies y cabeceras de autenticación. Con `*`, cualquier web de internet podría hacer peticiones autenticadas **con la sesión de tu usuario**. Es exactamente el ataque CSRF que CORS existe para impedir.

Si necesitas comodín con credenciales, usa `allowedOriginPatterns("https://*.miapp.es")`, que Spring resuelve al origen concreto en cada respuesta.

**La regla:** `allowedOrigins("*")` solo para una API pública **sin autenticación**. En cualquier otro caso, lista explícita.

### Documentar un endpoint

Añade a `GET /funkos/{id}` la descripción y las tres respuestas posibles.

```java
@Operation(
    summary = "Obtiene un funko por su id",
    description = "Devuelve el funko con el id indicado. Los borrados lógicamente "
                + "no se devuelven y se tratan como inexistentes.")
@ApiResponses({
    @ApiResponse(responseCode = "200", description = "Funko encontrado",
        content = @Content(mediaType = "application/json",
                           schema = @Schema(implementation = FunkoResponse.class))),
    @ApiResponse(responseCode = "400", description = "El id no es un número",
        content = @Content),
    @ApiResponse(responseCode = "404", description = "No existe un funko con ese id",
        content = @Content)
})
@GetMapping("/{id}")
public ResponseEntity<FunkoResponse> findById(
        @Parameter(description = "Id del funko", example = "1", required = true)
        @PathVariable Long id) {
    return ResponseEntity.ok(servicio.findById(id));
}
```

Y en el DTO:

```java
@Schema(description = "Datos de un funko")
public record FunkoResponse(
        @Schema(description = "Identificador", example = "1")
        Long id,
        @Schema(description = "Nombre del funko", example = "Mickey Mouse")
        String nombre,
        @Schema(description = "Precio en euros", example = "15.99")
        BigDecimal precio) {}
```

**Lo que `springdoc` no puede adivinar y hay que escribir:**

| Lo deduce solo | Hay que escribirlo |
|---|---|
| Ruta, verbo, parámetros | **Para qué sirve** el endpoint |
| Tipos y obligatoriedad (de las anotaciones de validación) | **Qué errores** devuelve y cuándo |
| Esquema de los DTOs | **Ejemplos** de valores reales |

Y la columna de la derecha es justo la que convierte «una lista de endpoints» en documentación usable.

### Documentar la API y apagarla en producción

Configura OpenAPI con metadatos y deja Swagger apagado en `prod`.

```java title="config/OpenApiConfig.java"
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI apiInfo(@Value("${api.version:v1}") String version) {
        return new OpenAPI()
            .info(new Info()
                .title("API REST de Funkos — DWES 2.º DAW")
                .version(version)
                .description("""
                    API REST de gestión de funkos y categorías.

                    Construida en la UT4 (capas), la UT5 (persistencia con JPA)
                    y la UT6 (WebSockets, GraphQL y documentación).
                    """)
                .contact(new Contact()
                    .name("IES Ejemplo · 2.º DAW")
                    .email("dwes@iesx.es"))
                .license(new License()
                    .name("CC BY-NC-SA 4.0")
                    .url("https://creativecommons.org/licenses/by-nc-sa/4.0/")))
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Desarrollo"),
                new Server().url("https://api.miapp.es").description("Producción")))
            .tags(List.of(
                new Tag().name("Funkos").description("CRUD de funkos"),
                new Tag().name("Categorías").description("CRUD de categorías")));
    }
}
```

```properties title="application-dev.properties"
springdoc.api-docs.enabled=true
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.enabled=true
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=method
springdoc.swagger-ui.tagsSorter=alpha
```

```properties title="application-prod.properties"
springdoc.api-docs.enabled=false
springdoc.swagger-ui.enabled=false
```

!!! danger "Por qué hay que apagarlo en producción"
    Swagger UI **no es un visor: es un cliente HTTP completo** con un botón «Try it out» que manda peticiones de verdad.

    Dejarlo abierto publica:

    - La lista completa de tus endpoints, incluidos los que no documentas en ningún sitio.
    - El esquema de tus DTOs, que dice qué campos tienes.
    - Un formulario para llamarlos a todos.

    Es el mismo criterio que la consola de H2 de la UT5 y la introspección de GraphQL del tema 2: **todo lo que es cómodo en desarrollo se apaga en producción**.

!!! tip "Y el `/v3/api-docs` sí puede quedarse, con matices"
    Si tu API es pública y quieres que otros la consuman, la **especificación** (el JSON) tiene sentido publicarla: con ella se generan clientes automáticamente. Lo que no debe quedarse es **la interfaz con el botón de disparar**.

    Alternativa habitual: generar el JSON en el *build* con el plugin de `springdoc` y publicarlo como una página estática, sin exponer nada en el servidor de producción.
