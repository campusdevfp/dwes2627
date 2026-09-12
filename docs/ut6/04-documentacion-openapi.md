# Documentar la API con OpenAPI

> Una API sin documentar es una API que nadie usa. Y una documentación escrita a mano en un Word es una documentación que a las dos semanas miente.

## 1. El contrato

**OpenAPI** (antes Swagger) es un estándar para describir una API REST en un documento JSON o YAML: qué endpoints hay, qué parámetros aceptan, qué devuelven y qué errores pueden dar. Con ese documento se puede:

- Generar una web interactiva para probar la API (Swagger UI).
- Generar clientes automáticamente en Java, TypeScript, Python…
- Importarla en Bruno o Insomnia de un clic.
- Validar en CI que no has roto el contrato.

Dos formas de trabajar:

| Enfoque | Cómo | Cuándo |
|---|---|---|
| **Code-first** | Escribes el código, la spec se genera sola | Lo habitual, y lo que haremos |
| **Design-first** | Escribes el YAML primero, el código después | Equipos grandes, front y back en paralelo |

## 2. springdoc en tres minutos

```xml title="pom.xml"
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.5</version>
</dependency>
```

Arranca y ya tienes:

- `http://localhost:8080/swagger-ui.html`
    → la interfaz interactiva
- `http://localhost:8080/v3/api-docs`
    → la especificación en JSON
- `http://localhost:8080/v3/api-docs.yaml`
    → en YAML

Sin escribir una línea, springdoc **inspecciona tus controladores** y deduce rutas, verbos, parámetros, tipos y esquemas de los DTOs. Lo que no puede deducir es el **significado**: para eso están las anotaciones.

```yaml
springdoc:
  swagger-ui:
    path: /swagger-ui.html
    operationsSorter: method
    tagsSorter: alpha
    tryItOutEnabled: true
  api-docs:
    path: /v3/api-docs
  show-actuator: false
```

## 3. Información general

``` { .java .numerado }
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI tiendaOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("TiendaAPI")
                .version("1.0.0")
                .description("""
                    API de gestión de productos y pedidos.

                    **Autenticación:** obtén un token en `POST /auth/login`
                    y envíalo en la cabecera `Authorization: Bearer <token>`.
                    """)
                .contact(new Contact().name("2.º DAW").email("daw@iesx.es"))
                .license(new License().name("MIT")))
            .servers(List.of(
                new Server().url("http://localhost:8080").description("Desarrollo"),
                new Server().url("https://api.tienda.com").description("Producción")))
            .components(new Components()
                .addSecuritySchemes("bearerAuth", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")))
            .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }
}
```

Ese `securityScheme` añade el botón **Authorize** en Swagger UI: pegas el token una vez y todas las pruebas van autenticadas. Sin él, no podrás probar los endpoints protegidos desde la web.

## 4. Anotar los endpoints

``` { .java .numerado }
@RestController
@RequestMapping("/api/v1/productos")
@Tag(name = "Productos", description = "Catálogo de productos de la tienda")
public class ProductoControlador {

    @Operation(
        summary = "Obtener un producto por su id",
        description = "Devuelve el detalle completo, incluido el precio con IVA calculado.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Producto encontrado",
            content = @Content(schema = @Schema(implementation = ProductoDto.class))),
        @ApiResponse(responseCode = "404", description = "No existe ese producto",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductoDto> obtener(
            @Parameter(description = "Identificador del producto", example = "42", required = true)
            @PathVariable Integer id) {
        return ResponseEntity.ok(mapper.aDto(servicio.obtener(id)));
    }

    @Operation(summary = "Crear un producto",
               description = "Requiere rol ADMIN. Devuelve 201 con la cabecera `Location`.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Creado"),
        @ApiResponse(responseCode = "400", description = "Datos no válidos"),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "403", description = "Sin permiso"),
        @ApiResponse(responseCode = "409", description = "Ya existe un producto con ese nombre")
    })
    @PostMapping
    public ResponseEntity<ProductoDto> crear(@Valid @RequestBody CrearProductoDto dto) { ... }

    @Operation(summary = "Buscar productos con filtros y paginación")
    @GetMapping
    public PaginaDto<ProductoDto> buscar(
            @Parameter(description = "Texto libre sobre el nombre", example = "patinete")
            @RequestParam(required = false) String q,
            @Parameter(description = "Categoría exacta", example = "movilidad")
            @RequestParam(required = false) String categoria,
            @ParameterObject @PageableDefault(size = 20) Pageable pageable) { ... }
}
```

!!! tip "`@ParameterObject` con `Pageable`"
    Sin esa anotación, Swagger UI muestra un campo `pageable` como si fuera un objeto JSON y nadie sabe qué escribir. Con ella, aparecen tres cajitas limpias: `page`, `size` y `sort`.

### Documentar los modelos

```java
@Schema(description = "Datos para crear un producto")
public record CrearProductoDto(

    @Schema(description = "Nombre comercial, único en el catálogo",
            example = "Patinete Urban 350", requiredMode = REQUIRED)
    @NotBlank @Size(min = 3, max = 100) String nombre,

    @Schema(description = "Categoría", example = "movilidad",
            allowableValues = {"movilidad", "seguridad", "accesorios"})
    @NotBlank String categoria,

    @Schema(description = "Precio sin IVA en euros", example = "299.00", minimum = "0.01")
    @Positive Double precio,

    @Schema(description = "Unidades disponibles", example = "12", defaultValue = "0")
    @PositiveOrZero Integer stock
) {}
```

!!! success "Las validaciones se documentan solas"
    springdoc lee las anotaciones de Bean Validation y las refleja en el esquema: `@NotBlank` → `required`, `@Size(max=100)` → `maxLength: 100`, `@Positive` → `exclusiveMinimum: 0`. Documentar y validar dejan de ser dos trabajos.

### Ocultar lo que no debe verse

```
@Hidden                                    // el endpoint entero
@Schema(hidden = true) private String passwordHash;   // un campo
```

```yaml
springdoc:
  paths-to-exclude: /actuator/**, /internal/**
```

## 5. Swagger UI en producción: cuidado

Exponer la documentación completa de una API interna es regalar el mapa a un atacante. Dos opciones:

```
# application-prod.yml — desactivarla
springdoc:
  api-docs:
    enabled: false
  swagger-ui:
    enabled: false
```

O dejarla accesible solo a autenticados:

```
.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").hasRole("ADMIN")
```

En una API **pública** (la que consumen terceros), al contrario: la documentación es tu producto y debe estar visible.

## 6. Del contrato al cliente

```bash
# Guardar la spec
curl -s localhost:8080/v3/api-docs -o openapi.json

# Importarla en Bruno:  Import → File → openapi.json  → colección completa lista

# Generar un cliente TypeScript para el front (Docker, sin instalar nada)
docker run --rm -v "$PWD:/local" openapitools/openapi-generator-cli generate \
  -i /local/openapi.json -g typescript-axios -o /local/cliente-ts
```

Esto es lo que hace que OpenAPI valga la pena: el equipo de front no espera a que le pases un documento; **genera el cliente desde tu API**, tipado y al día.

---

## Pruébalo ahora (30 min)

!!! reto "Trabaja tú ahora"
    Este bloque se hace **en clase, en tu equipo**. No se entrega ni puntúa: es la práctica que hace que el examen te salga.

**Parte 1 — Instala y mira.** Añade la dependencia, arranca y abre `http://localhost:8080/swagger-ui.html`. Sin anotar nada, comprueba cuánto ha deducido solo. Ejecuta un `GET` con **Try it out** y compara la respuesta con la de curl.

**Parte 2 — Info y esquema de seguridad.** Crea `OpenApiConfig` con título, descripción, contacto, los dos servidores y `bearerAuth`. Recarga y localiza el botón **Authorize**.

**Parte 3 — Anota a fondo** el controlador de productos: `@Tag`, y en cada método `@Operation` + `@ApiResponses` con **todos** los códigos posibles. En los DTOs, `@Schema` con `description` y `example` en cada campo.

**Parte 4 — Comprueba el contrato.**

```bash
curl -s localhost:8080/v3/api-docs | jq '.paths | keys'
curl -s \
     localhost:8080/v3/api-docs | jq '.paths."/api/v1/productos/{id}".get.responses | keys'
curl -s localhost:8080/v3/api-docs | jq '.components.schemas.CrearProductoDto.required'
curl -s \
     localhost:8080/v3/api-docs | jq '.components.schemas.CrearProductoDto.properties.nombre'
```

Ese último debe mostrar el `maxLength: 100` que no escribiste en el `@Schema`: viene del `@Size`.

**Parte 5 — Importa en Bruno.** Guarda `openapi.json`, impórtalo y comprueba que aparece la colección con todos los endpoints y los ejemplos que definiste.

---

## Ejercicios (con solución)

### Ejercicio 1 — Las tres URLs

¿Qué hay en `/swagger-ui.html`, `/v3/api-docs` y `/v3/api-docs.yaml`?

??? success "Solución"

    `/swagger-ui.html`: la interfaz web interactiva, para humanos; permite ejecutar peticiones reales. `/v3/api-docs`: la especificación OpenAPI en JSON, para máquinas (Bruno, generadores de clientes, validadores de CI). `/v3/api-docs.yaml`: lo mismo en YAML, más legible para revisarlo en un pull request. Swagger UI no es más que un visor de ese JSON.


### Ejercicio 2 — Anota el DELETE

Documenta `DELETE /api/v1/productos/{id}`: solo ADMIN, 204 si va bien, 404 si no existe, 409 si el producto tiene pedidos asociados.

??? success "Solución"

    ```
    @Operation(summary = "Eliminar un producto",
        description = "Borra el producto del catálogo. Requiere rol ADMIN. "
                    + "No se puede borrar si tiene pedidos asociados.",
        security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Eliminado correctamente",
                     content = @Content),
        @ApiResponse(responseCode = "401", description = "No autenticado"),
        @ApiResponse(responseCode = "403", description = "Requiere rol ADMIN"),
        @ApiResponse(responseCode = "404", description = "No existe ese producto"),
        @ApiResponse(responseCode = "409", description = "Tiene pedidos asociados")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> borrar(
            @Parameter(description = "Id del producto", example = "42") @PathVariable Integer id) { ... }
    ```

    El

    content = @Content

    vacío del 204 evita que Swagger UI muestre un cuerpo de respuesta inexistente.


### Ejercicio 3 — Encuentra el problema

```java
@Schema(description = "Usuario del sistema")
public record UsuarioDto(Integer id, String email, String passwordHash, String rol) {}
```

??? success "Solución"

    Se está publicando el hash de la contraseña en la documentación pública y, casi seguro, también en las respuestas de la API. Doble error: de seguridad y de diseño. La solución no es `@Schema(hidden = true)` — eso solo lo oculta en la documentación, pero el campo sigue viajando en el JSON. Hay que quitarlo del DTO: un DTO de respuesta no debe contener nunca credenciales, ni siquiera cifradas.


### Ejercicio 4 — Code-first o design-first

Un equipo de front y otro de back tienen que empezar a la vez sobre un producto nuevo. ¿Qué enfoque eliges?

??? success "Solución"

    Design-first: se acuerda primero el YAML de OpenAPI y se convierte en el contrato. El front genera su cliente y monta un servidor simulado (mock) a partir de la spec, y el back implementa contra ella. Así los dos equipos trabajan en paralelo desde el día uno y las discusiones de diseño ocurren antes de escribir código, cuando cambiar algo es barato. El coste es la disciplina de mantener la spec sincronizada. En un proyecto de una sola persona o en un backend que evoluciona rápido, `code-first` es más práctico.


### Ejercicio 5 — Documentación mínima aceptable

Enumera lo que debe tener sí o sí la documentación de un endpoint para considerarse profesional.

??? success "Solución"

    (1) Verbo y ruta. (2) Resumen de una línea de qué hace. (3) Todos los parámetros con descripción, tipo, si son obligatorios y un ejemplo. (4) Esquema del cuerpo de la petición con restricciones de validación. (5) Todos los códigos de respuesta posibles, no solo el 200, cada uno con su esquema — incluidos los de error como `ProblemDetail`. (6) Requisitos de autenticación y rol. (7) Un ejemplo completo de petición y respuesta. Lo que más se olvida y más se echa en falta al consumir una API ajena: los códigos de error y sus formatos.

