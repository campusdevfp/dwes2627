# Errores, configuración y observabilidad

> Lo que distingue una aplicación de clase de una de producción: **qué responde cuando falla**, **cómo se adapta a cada entorno** y **cómo sabes qué está pasando dentro**.

## 1. Jerarquía de excepciones de dominio

No crees una excepción suelta por cada error: monta una **jerarquía**.

``` { .java .numerado }
// excepcion/TiendaException.java — raíz común
public abstract class TiendaException extends RuntimeException {
    protected TiendaException(String mensaje) { super(mensaje); }
}

// 404
public class RecursoNoEncontradoException extends TiendaException {
    public RecursoNoEncontradoException(String recurso, Object id) {
        super("No existe %s con id %s".formatted(recurso, id));
    }
}
public class ProductoNoEncontradoException extends RecursoNoEncontradoException {
    public ProductoNoEncontradoException(Integer id) { super("producto", id); }
}

// 409
public class ConflictoException extends TiendaException {
    public ConflictoException(String mensaje) { super(mensaje); }
}
public class ProductoDuplicadoException extends ConflictoException {
    public ProductoDuplicadoException(String nombre) {
        super("Ya existe un producto llamado '" + nombre + "'");
    }
}
public class StockInsuficienteException extends ConflictoException {
    private final int disponible;
    public StockInsuficienteException(Integer id, int disponible, int solicitado) {
        super("Producto %d: hay %d unidades y se piden %d".formatted(id, disponible, solicitado));
        this.disponible = disponible;
    }
    public int getDisponible() { return disponible; }
}

// 400
public class DatosInvalidosException extends TiendaException {
    public DatosInvalidosException(String mensaje) { super(mensaje); }
}
```

La ventaja de la jerarquía: **un solo manejador por familia**. `RecursoNoEncontradoException` cubre productos, pedidos y clientes con un único `@ExceptionHandler`.

Y el servicio las lanza sin saber nada de HTTP:

```java
public Producto obtener(Integer id) {
    return repositorio.buscarPorId(id)
        .orElseThrow(() -> new ProductoNoEncontradoException(id));
}
```

## 2. El manejador global

``` { .java .numerado }
@RestControllerAdvice
public class ManejadorGlobalErrores {

    private static final Logger log = LoggerFactory.getLogger(ManejadorGlobalErrores.class);

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ProblemDetail noEncontrado(RecursoNoEncontradoException e, HttpServletRequest req) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
        pd.setTitle("Recurso no encontrado");
        pd.setType(URI.create("https://api.tienda.com/errores/no-encontrado"));
        pd.setInstance(URI.create(req.getRequestURI()));
        pd.setProperty("timestamp", Instant.now());
        return pd;                                                    // 404
    }

    @ExceptionHandler(StockInsuficienteException.class)
    public ProblemDetail sinStock(StockInsuficienteException e, HttpServletRequest req) {
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
        pd.setTitle("Stock insuficiente");
        pd.setInstance(URI.create(req.getRequestURI()));
        // dato útil para el cliente
        pd.setProperty("stockDisponible", e.getDisponible());
        return pd;                                                    // 409
    }

    @ExceptionHandler(ConflictoException.class)
    public ProblemDetail conflicto(ConflictoException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(DatosInvalidosException.class)
    public ProblemDetail datosInvalidos(DatosInvalidosException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    // --- Errores del framework ---

    @ExceptionHandler(MethodArgumentNotValidException.class)          // falla @Valid
    public ProblemDetail validacion(MethodArgumentNotValidException e) {
        var errores = e.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.toMap(FieldError::getField,
                     f -> Objects.toString(f.getDefaultMessage(), "inválido"),
                     (a, b) -> a + "; " + b));
        var pd = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        pd.setTitle("Datos no válidos");
        pd.setDetail("La petición contiene %d campo(s) incorrecto(s)".formatted(errores.size()));
        pd.setProperty("errores", errores);
        return pd;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)      // /productos/abc
    public ProblemDetail tipoIncorrecto(MethodArgumentTypeMismatchException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
            "El parámetro '%s' debe ser de tipo %s".formatted(
                e.getName(), e.getRequiredType().getSimpleName()));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)          // JSON malformado
    public ProblemDetail jsonIlegible(HttpMessageNotReadableException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST,
            "El cuerpo de la petición no es un JSON válido");
    }

    @ExceptionHandler(Exception.class)  // red de seguridad
    public ProblemDetail generico(Exception e, HttpServletRequest req) {
        log.error("Error no controlado en {}", req.getRequestURI(), e);   // al LOG
        var pd = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR,
            "Se ha producido un error interno. Inténtalo más tarde.");     // al CLIENTE
        pd.setTitle("Error interno");
        return pd;
    }
}
```

Respuesta que ve el cliente ante datos inválidos:

```json
{
  "type": "about:blank",
  "title": "Datos no válidos",
  "status": 400,
  "detail": "La petición contiene 2 campo(s) incorrecto(s)",
  "errores": {
    "nombre": "El nombre es obligatorio",
    "precio": "El precio debe ser mayor que cero"
  }
}
```

!!! danger "La traza va al log, nunca al cliente"
    Un *stack trace* revela tu arquitectura, versiones de librerías y rutas del servidor: información valiosísima para un atacante. Fíjate en el manejador genérico: `log.error(...)` con el detalle completo, y al cliente un mensaje neutro.

**Orden de resolución:** Spring elige el manejador **más específico** disponible. Por eso puedes tener `ConflictoException` genérico y `StockInsuficienteException` particular: para el stock se usa el segundo.

## 3. Configuración externa

Spring Boot admite **dos formatos** para el mismo fichero de configuración. Son equivalentes: puedes usar el que prefieras, pero **no los dos a la vez** en el mismo proyecto. Compara la misma configuración escrita de las dos formas:

=== "YAML (`application.yml`)"

    ``` yaml title="src/main/resources/application.yml"
    server:
      port: 8080

    spring:
      application:
        name: tienda
      jackson:
        default-property-inclusion: non_null      # no serializar nulls
        serialization:
          indent-output: true

    tienda:
      iva: 0.21
      descuento-volumen: 0.10
      unidades-minimas-descuento: 6
      categorias-validas: [movilidad, seguridad, accesorios]
      contacto:
        email: soporte@tienda.com
        telefono: "900123456"

    logging:
      level:
        es.iesx.daw.tienda: DEBUG
        org.springframework.web: INFO
    ```

    **A favor:** la jerarquía se ve de un vistazo, no repites prefijos, las listas son naturales y admite varios perfiles en un solo fichero (separados por `---`).

    **En contra:** la indentación es significativa. Un espacio de más y la clave cuelga de otro sitio. **Tabuladores prohibidos**: YAML no los acepta.

=== "Properties (`application.properties`)"

    ``` properties title="src/main/resources/application.properties"
    server.port=8080

    spring.application.name=tienda
    spring.jackson.default-property-inclusion=non_null
    spring.jackson.serialization.indent-output=true

    tienda.iva=0.21
    tienda.descuento-volumen=0.10
    tienda.unidades-minimas-descuento=6
    tienda.categorias-validas[0]=movilidad
    tienda.categorias-validas[1]=seguridad
    tienda.categorias-validas[2]=accesorios
    tienda.contacto.email=soporte@tienda.com
    tienda.contacto.telefono=900123456

    logging.level.es.iesx.daw.tienda=DEBUG
    logging.level.org.springframework.web=INFO
    ```

    **A favor:** imposible equivocarse con la indentación —no hay—, cada línea se entiende aislada y es lo que sale por defecto al crear el proyecto.

    **En contra:** repites el prefijo en cada línea, las listas necesitan índices `[0]`, `[1]`… y con configuraciones grandes se vuelve difícil de leer.

!!! tip "Cuál usamos en el módulo"
    **`application.yml`**, por las listas y los perfiles. Pero debes saber leer los dos: la mitad de la documentación de Spring y de las respuestas de Stack Overflow están en `.properties`.

!!! bug "Si tienes los dos ficheros a la vez"
    Spring Boot carga los dos y **`.properties` gana** sobre `.yml` para las claves repetidas. Es una fuente de desconciertos absurda: cambias un valor en el `.yml`, reinicias, y no pasa nada. Comprueba siempre que no te has dejado el otro fichero en `src/main/resources`.

**Tres formas de leerlo**, de peor a mejor:

``` { .java .numerado }
// (a) @Value — para valores sueltos
@Value("${tienda.iva}") private double iva;
@Value("${tienda.iva:0.21}") private double ivaConDefecto;      // valor por defecto

// (b) Environment — programático, poco habitual
private final Environment env;
double iva = env.getProperty("tienda.iva", Double.class, 0.21);

// (c) @ConfigurationProperties — BIEN la profesional
@ConfigurationProperties(prefix = "tienda")
@Validated
public record TiendaConfig(
    @DecimalMin("0.0") @DecimalMax("1.0") double iva,
    double descuentoVolumen,
    int unidadesMinimasDescuento,
    List<String> categoriasValidas,
    Contacto contacto
) {
    public record Contacto(String email, String telefono) {}
}
```

```java
@SpringBootApplication
@EnableConfigurationProperties(TiendaConfig.class)
public class TiendaApplication { }
```

Ventajas de (c): **tipado**, agrupado, **validable** (si el IVA fuera 1.5, la aplicación no arranca), autocompletado en el IDE y se inyecta como cualquier bean.

**Precedencia** (de mayor a menor prioridad): argumentos de línea de comandos → variables de entorno → `application-{perfil}.yml` → `application.yml`. Por eso funciona `java -jar app.jar --server.port=9090`.

## 4. Perfiles

```
# application.yml
spring:
  profiles:
    active: dev

---
spring:
  config:
    activate:
      on-profile: dev
tienda:
  iva: 0.21
logging:
  level:
    es.iesx.daw.tienda: DEBUG

---
spring:
  config:
    activate:
      on-profile: prod
tienda:
  iva: 0.21
logging:
  level:
    es.iesx.daw.tienda: WARN
server:
  error:
    include-stacktrace: never
```

```bash
java -jar tienda.jar --spring.profiles.active=prod
# o con variable de entorno:
SPRING_PROFILES_ACTIVE=prod java -jar tienda.jar
```

Combinado con `@Profile` en los beans: repositorio en memoria en desarrollo, base de datos en producción, **sin tocar el código**.

!!! warning "Secretos fuera del repositorio"
    Contraseñas, claves de API y tokens **nunca** en el `.yml` que va a Git:

    ```
    spring:
      datasource:
        password: ${DB_PASSWORD}          # de variable de entorno
    jwt:
      secreto: ${JWT_SECRET:claveSoloParaDesarrolloLocal}
    ```

    Un secreto subido a un repositorio queda en el

    historial de Git

    aunque lo borres después.

## 5. Logging bien hecho

```java
private static final Logger log = LoggerFactory.getLogger(ProductoServicio.class);

log.trace("Detalle finísimo");
log.debug("Stock de {}: {} → {}", id, antes, despues);     // diagnóstico
log.info("Producto creado: id={}", creado.id());           // hechos de negocio
log.warn("Stock bajo en {}: quedan {}", id, stock);        // atención
log.error("Fallo al guardar {}", id, excepcion);  // errores (con la excepción al final)
```

Cuatro reglas:

1. Nunca `System.out.println`
    : no se puede filtrar por nivel, ni redirigir, ni desactivar en producción.
2. Usa `{}` en vez de concatenar: `log.debug("x={}", x)` no construye el String si DEBUG está desactivado.
3. Nunca registres datos sensibles
    : contraseñas, tokens, tarjetas, datos personales completos.
4. La excepción va como último argumento (sin `{}`): así se imprime la traza completa.

## 6. Actuator: ver dentro de la aplicación

```xml title="pom.xml"
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,env,beans
  endpoint:
    health:
      show-details: when-authorized
```

| Endpoint | Para qué |
|---|---|
| `/actuator/health` | ¿La aplicación está viva? Lo consultan Kubernetes y los balanceadores |
| `/actuator/metrics` | Peticiones, memoria, hilos, tiempos |
| `/actuator/beans` | **Todos** los beans del contenedor (útil para depurar inyecciones) |
| `/actuator/env` | Configuración efectiva y de dónde sale cada valor |

```bash
curl -s localhost:8080/actuator/health | jq
curl -s localhost:8080/actuator/metrics/http.server.requests | jq
```

:material-alert: En producción se exponen **solo** `health` e `info`, y protegidos: `/env` y `/beans` filtran información interna.

---

## Pruébalo ahora (30 min)

!!! reto "Trabaja tú ahora"
    Este bloque se hace **en clase, en tu equipo**. No se entrega ni puntúa: es la práctica que hace que el examen te salga.

**Parte 1 — Errores.** Crea la jerarquía y el manejador, y comprueba los cinco casos:

```bash
curl -s localhost:8080/api/productos/999 | jq  # 404 con type/instance/timestamp
curl -s -X POST localhost:8080/api/productos -H "Content-Type: application/json" \
  # 400 con mapa de errores
  -d '{"nombre":"","categoria":"nope","precio":-5,"stock":-1}' | jq
# 409 + stockDisponible
curl -s -X PATCH "localhost:8080/api/productos/1/stock?unidades=-9999" | jq
curl -i localhost:8080/api/productos/abc                          # 400 tipo incorrecto
curl -i -X POST localhost:8080/api/productos -H "Content-Type: application/json" \
     -d '{esto no es json}'  # 400
```

**Parte 2 — Configuración.** Saca el IVA a `application.yml` con `@ConfigurationProperties`, cámbialo a `0.10`, reinicia y comprueba que `precioConIva` cambia **sin tocar el código**. Después arranca con `--tienda.iva=0.05` y verifica que el argumento gana al fichero.

**Parte 3 — Perfiles.** Crea los bloques `dev` y `prod`, y arranca con cada uno observando el nivel de log:

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

**Parte 4 — Actuator.** Añade la dependencia y explora:

```bash
curl -s localhost:8080/actuator/health | jq
curl -s localhost:8080/actuator/beans | jq '.contexts.application.beans | keys | length'
curl -s "localhost:8080/actuator/metrics/http.server.requests" | jq '.measurements'
```

Ese último te dice cuántas peticiones ha atendido tu API y cuánto han tardado. Bienvenido a la observabilidad.

---

## Ejercicios (con solución)

### Ejercicio 1 — Asigna el código

(a) producto inexistente · (b) precio negativo en el body · (c) nombre duplicado · (d) `/productos/abc` · (e) JSON malformado · (f) NullPointerException en tu código · (g) DELETE a la colección.

??? success "Solución"

    (a) `404` · (b) `400` (validación) · (c) `409` · (d) `400` (tipo incorrecto) · (e) `400` · (f) `500` — con la traza en el log y mensaje genérico al cliente · (g) `405`, que Spring devuelve solo.


### Ejercicio 2 — Encuentra los tres fallos

```java
@ExceptionHandler(Exception.class)
public String error(Exception e) {
    return "Error: " + e.getMessage() + " " + Arrays.toString(e.getStackTrace());
}
```

??? success "Solución"

    (1) Devuelve la traza al cliente: fuga de información. (2) No fija código de estado → responde 200 OK ante un error, el peor pecado de una API. (3) No registra nada en el log: tú nunca te enteras del fallo. Correcto: `log.error(...)` + `ProblemDetail` con 500 y mensaje neutro.


### Ejercicio 3 — Configuración tipada

Pasa esta configuración a un `@ConfigurationProperties` validado:

```yaml
envios:
  gratis-desde: 50.0
  coste-estandar: 4.95
  dias-entrega: 3
  zonas: [peninsula, baleares]
```

??? success "Solución"

    ```java
    @ConfigurationProperties(prefix = "envios")
    @Validated
    public record EnviosConfig(
        @PositiveOrZero double gratisDesde,
        @PositiveOrZero double costeEstandar,
        @Min(1) @Max(30) int diasEntrega,
        @NotEmpty List<String> zonas) {}
    ```

    Registrarla con `@EnableConfigurationProperties(EnviosConfig.class)`. Fíjate en el paso de `gratis-desde` (kebab-case en YAML) a `gratisDesde` (camelCase en Java): Spring lo hace solo.


### Ejercicio 4 — Logging

Corrige: `System.out.println("Guardando producto " + p.getNombre() + " con password " + p.getPassword());`

??? success "Solución"

    ```java
    log.info("Guardando producto id={} nombre={}", p.id(), p.nombre());
    ```

    Tres correcciones: usar el logger en vez de `System.out`, usar placeholders `{}` en vez de concatenar, y jamás registrar contraseñas ni datos sensibles.


### Ejercicio 5 — Perfiles

Necesitas: en desarrollo, datos de prueba en memoria y logs DEBUG; en producción, base de datos real, logs WARN y sin traza en las respuestas de error. Describe la solución completa.

??? success "Solución"

    Dos bloques de perfil en `application.yml` (o dos ficheros `application-dev.yml` / `application-prod.yml`) con los niveles de log y server.error.include-stacktrace: never en prod; dos implementaciones del repositorio anotadas con `@Profile("dev")` y `@Profile("prod")`; y arranque con `--spring.profiles.active=prod`. Los servicios y controladores no cambian ni una línea.

