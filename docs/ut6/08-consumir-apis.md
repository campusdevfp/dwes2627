# Consumir otras APIs

> Tu backend casi nunca está solo: pide el tiempo, cobra con Stripe, envía correos con SendGrid, consulta un microservicio del compañero. Saber **consumir** APIs es tan importante como saber ofrecerlas.

## 1. `RestClient`, el cliente moderno

Spring ha tenido tres clientes HTTP. Conviene saber cuál usar:

| Cliente | Estado |
|---|---|
| `RestTemplate` | En mantenimiento desde Spring 5. Lo verás en código antiguo |
| `WebClient` | Reactivo, no bloqueante. Necesario si trabajas con WebFlux |
| **`RestClient`** | Desde Spring 6.1. **API fluida de `WebClient`, modelo síncrono de `RestTemplate`**. El recomendado |

```java
@Configuration
public class ClientesConfig {

    @Bean
    public RestClient climaClient(RestClient.Builder builder,
                                  @Value("${clima.url}") String url,
                                  @Value("${clima.api-key}") String apiKey) {
        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(5));

        return builder
            .baseUrl(url)
            .defaultHeader("X-API-Key", apiKey)
            .requestFactory(factory)
            .build();
    }
}
```

!!! danger "Sin timeout, un servicio externo lento te tumba a ti"
    Si la API que llamas tarda 60 segundos, tu hilo se queda bloqueado 60 segundos. Con 200 peticiones simultáneas, tu servidor se queda sin hilos y **deja de responder a todo el mundo** por culpa de un tercero. El timeout no es un detalle: es supervivencia. Regla: conexión 2-3 s, lectura 5-10 s.

## 2. Las llamadas

``` { .java .numerado }
@Service
public class ClimaServicio {

    private static final Logger log = LoggerFactory.getLogger(ClimaServicio.class);
    private final RestClient client;

    public ClimaServicio(RestClient climaClient) { this.client = climaClient; }

    // GET simple
    public ClimaDto obtener(String ciudad) {
        return client.get()
            // ← parametrizado, no concatenado
            .uri("/weather?q={ciudad}&units=metric", ciudad)
            .retrieve()
            .body(ClimaDto.class);
    }

    // GET de una lista
    public List<CiudadDto> ciudades() {
        return client.get().uri("/cities").retrieve()
            .body(new ParameterizedTypeReference<List<CiudadDto>>() {});
    }

    // POST con cuerpo
    public PagoDto cobrar(PeticionPago peticion) {
        return client.post()
            .uri("/payments")
            .contentType(MediaType.APPLICATION_JSON)
            .body(peticion)
            .retrieve()
            .body(PagoDto.class);
    }

    // Acceso a estado y cabeceras
    public ResponseEntity<ClimaDto> obtenerCompleto(String ciudad) {
        return client.get().uri("/weather?q={c}", ciudad)
            .retrieve()
            .toEntity(ClimaDto.class);
    }
}
```

!!! warning "Nunca concatenes parámetros en la URI"
    `uri("/weather?q=" + ciudad)` se rompe con espacios, tildes o `&` en el valor, y abre la puerta a inyección de parámetros. Con `uri("/weather?q={c}", ciudad)`, Spring codifica el valor por ti.

## 3. Manejar los errores del otro

Por defecto, `retrieve()` lanza `HttpClientErrorException` (4xx) o `HttpServerErrorException` (5xx). Casi siempre querrás traducirlos a **tus** excepciones:

``` { .java .numerado }
public ClimaDto obtener(String ciudad) {
    try {
        return client.get()
            .uri("/weather?q={c}", ciudad)
            .retrieve()
            .onStatus(status -> status.value() == 404, (req, res) -> {
                throw new CiudadNoEncontradaException(ciudad);
            })
            .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                log.warn("Error 4xx del proveedor: {}", res.getStatusCode());
                throw new ProveedorException("Petición rechazada por el proveedor");
            })
            .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                log.error("El proveedor de clima falla: {}", res.getStatusCode());
                throw new ServicioExternoNoDisponibleException("clima");
            })
            .body(ClimaDto.class);

    } catch (ResourceAccessException e) {          // timeout o DNS caído
        log.error("Timeout llamando al proveedor de clima", e);
        throw new ServicioExternoNoDisponibleException("clima");
    }
}
```

Y ese `ServicioExternoNoDisponibleException` se traduce en **502 Bad Gateway** o **503**, nunca en 500:

```java
@ExceptionHandler(ServicioExternoNoDisponibleException.class)
public ProblemDetail externo(ServicioExternoNoDisponibleException e) {
    var pd = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_GATEWAY,
        "El servicio de " + e.getServicio() + " no está disponible ahora mismo");
    pd.setTitle("Servicio externo no disponible");
    return pd;
}
```

**500** significa «me he equivocado yo». **502/503/504** significan «el problema está aguas abajo». La diferencia importa muchísimo cuando alguien está de guardia mirando gráficas a las tres de la mañana.

## 4. Mapea a tu propio DTO

La API externa devuelve 40 campos con nombres raros. No los propagues a tu dominio:

``` { .java .numerado }
// Lo que llega del proveedor: solo declaras lo que te interesa
@JsonIgnoreProperties(ignoreUnknown = true)
public record RespuestaProveedor(
    @JsonProperty("name") String ciudad,
    Main main,
    List<Weather> weather) {

    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Main(double temp, double humidity) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    public record Weather(String description, String icon) {}
}

// Lo que usa TU aplicación
public record ClimaDto(String ciudad, double temperatura, String descripcion) {

    public static ClimaDto de(RespuestaProveedor r) {
        return new ClimaDto(r.ciudad(), r.main().temp(),
            r.weather().isEmpty() ? "desconocido" : r.weather().get(0).description());
    }
}
```

`@JsonIgnoreProperties(ignoreUnknown = true)` es **obligatorio** al consumir APIs ajenas: el día que el proveedor añada un campo nuevo, tu aplicación seguirá funcionando en lugar de reventar.

Esta capa de traducción se llama *anti-corruption layer*: si mañana cambias de proveedor, solo tocas el mapeo; tu dominio no se entera.

## 5. Resiliencia

Tres patrones que debes saber nombrar y explicar:

**Reintentos** — para fallos transitorios (un 503 puntual, un corte de red). Con **espera exponencial** (1 s, 2 s, 4 s) y solo en peticiones **idempotentes**: reintentar un `POST` de pago puede cobrar dos veces.

```java
@Retryable(retryFor = ServicioExternoNoDisponibleException.class,
           maxAttempts = 3, backoff = @Backoff(delay = 1000, multiplier = 2))
public ClimaDto obtener(String ciudad) { ... }

@Recover
public ClimaDto siFalla(ServicioExternoNoDisponibleException e, String ciudad) {
    return new ClimaDto(ciudad, Double.NaN, "no disponible");  // degradación elegante
}
```

**Circuit breaker** — si el servicio lleva 20 fallos seguidos, deja de llamarlo durante un minuto y responde al instante con el plan B. Evita que tu aplicación se atasque esperando a algo que sabes que está caído. Se implementa con Resilience4j.

**Caché** — la forma más barata de no depender de nadie:

```
@Cacheable(value = "clima", key = "#ciudad")
public ClimaDto obtener(String ciudad) { ... }
```

Con `@EnableCaching`, la segunda llamada con la misma ciudad no sale a la red. Si la API tiene límite de peticiones, esto es lo que impide que lo agotes.

## 6. Gestión de la clave

```
clima:
  url: https://api.openweathermap.org/data/2.5
  api-key: ${CLIMA_API_KEY}       # sin valor por defecto: si falta, no arranca
```

```bash
export CLIMA_API_KEY=abc123
mvn spring-boot:run
```

Nunca en el código, nunca en el repositorio, nunca en el log. Y si una clave se filtra alguna vez: **revócala y genera otra**, no basta con borrarla del commit.

---

## Pruébalo ahora (35 min)

!!! reto "Trabaja tú ahora"
    Este bloque se hace **en clase, en tu equipo**. No se entrega ni puntúa: es la práctica que hace que el examen te salga.

Usaremos APIs públicas sin registro.

**Parte 1 — Primera llamada.** Consume `https://api.chucknorris.io/jokes/random`:

```java
@Bean RestClient chistesClient(RestClient.Builder b) {
    var f = new SimpleClientHttpRequestFactory();
    f.setConnectTimeout(Duration.ofSeconds(3));
    f.setReadTimeout(Duration.ofSeconds(5));
    return b.baseUrl("https://api.chucknorris.io").requestFactory(f).build();
}

@JsonIgnoreProperties(ignoreUnknown = true)
record Chiste(String id, String value) {}

@GetMapping("/api/v1/chiste")
public Chiste chiste() {
    return chistesClient.get().uri("/jokes/random").retrieve().body(Chiste.class);
}
```

```bash
curl -s localhost:8080/api/v1/chiste | jq
```

**Parte 2 — Mapea y limpia.** Consume `https://jsonplaceholder.typicode.com/users`, quédate solo con `id`, `name` y `email`, y expón `GET /api/v1/usuarios-externos`. Comprueba que ignorando el resto de campos (`address`, `company`…) todo funciona.

**Parte 3 — Provoca los errores.**

```bash
# 404 del proveedor → ¿qué devuelve tu API?
curl -s localhost:8080/api/v1/usuarios-externos/99999 | jq
```

Traduce ese 404 externo a un 404 propio con Problem Details. Después, apunta el `baseUrl` a `http://localhost:9999` (nada escuchando) y comprueba que obtienes `502`, no 500 ni una traza en pantalla.

**Parte 4 — Timeout.** Llama a `https://httpbin.org/delay/10` con un `readTimeout` de 2 segundos. Debe fallar rápido y limpio con 502 o 504, capturando `ResourceAccessException`.

**Parte 5 — Caché.** Añade `@EnableCaching` y `@Cacheable`. Mide:

```bash
time curl -s localhost:8080/api/v1/usuarios-externos > /dev/null   # primera: red
time curl -s localhost:8080/api/v1/usuarios-externos > /dev/null   # segunda: caché
```

La diferencia debe ser evidente.

---

## Ejercicios (con solución)

### Ejercicio 1 — Qué cliente

Proyecto Spring MVC nuevo que consume tres APIs REST. ¿`RestTemplate`, `WebClient` o `RestClient`?

??? success "Solución"

    `RestClient`. Es el recomendado desde Spring 6.1: API fluida y moderna con modelo síncrono, que es el que encaja en un proyecto MVC. `RestTemplate` está en mantenimiento y solo se justifica en código heredado. `WebClient` tendría sentido si el proyecto fuese reactivo (WebFlux) o necesitaras miles de llamadas concurrentes no bloqueantes; en un MVC normal añade complejidad sin beneficio.


### Ejercicio 2 — Cuatro errores

```java
public Usuario buscar(String id) {
    return new RestTemplate().getForObject(
        "https://api.ejemplo.com/users?id=" + id + "&key=SECRETO123", Usuario.class);
}
```

??? success "Solución"

    (1) Crea un cliente nuevo en cada llamada: debería ser un bean reutilizado. (2) Sin timeouts: una API lenta bloquea el hilo indefinidamente. (3) La clave está en el código fuente, y acabará en el repositorio: debe ir en variable de entorno. (4) Concatena el parámetro en vez de usar plantilla `{id}`, con problemas de codificación e inyección. (5) De propina: sin manejo de errores, así que un 404 del proveedor se convierte en un 500 tuyo.


### Ejercicio 3 — El código correcto

La API de pagos que consumes responde 503. ¿Qué devuelves tú?

??? success "Solución"

    502 Bad Gateway (o `503` si prefieres indicar que tu servicio está temporalmente degradado, o `504` si fue un timeout). Nunca `500`: eso afirmaría que el fallo es tuyo y despistaría a quien depura. Y nunca propagar el error crudo del proveedor: se registra el detalle en el log y al cliente le llega un ProblemDetail claro del estilo «El servicio de pagos no está disponible ahora mismo; inténtalo en unos minutos».


### Ejercicio 4 — Reintentar o no

¿En cuáles reintentarías? (a) `GET /productos` con timeout · (b) `POST /pagos` sin respuesta · (c) `DELETE /productos/1` con 503 · (d) `POST /pagos` con `Idempotency-Key` sin respuesta.

??? success "Solución"

    (a) Sí: GET es seguro e idempotente. (b) No: podrías cobrar dos veces; hay que consultar el estado del pago antes de decidir. (c) Sí: DELETE es idempotente. (d) Sí: para eso existe la clave de idempotencia — el proveedor detecta la repetición y devuelve la respuesta original sin cobrar de nuevo. Regla: reintenta solo lo idempotente, con espera exponencial y un número máximo de intentos.


### Ejercicio 5 — Diseña la integración

Tu tienda debe cobrar con una pasarela externa. Enumera todo lo que hay que prever.

??? success "Solución"

    (1) `RestClient` como bean con `baseUrl` y timeouts explícitos. (2) Clave en variable de entorno, jamás en el repositorio. (3) DTOs propios con `@JsonIgnoreProperties(ignoreUnknown = true)` y una capa de mapeo que aísle tu dominio del proveedor. (4) Clave de idempotencia por intento de pago para no duplicar cobros. (5) Traducción de errores: 4xx del proveedor → 400/409 tuyos; 5xx y timeouts → 502. (6) Sin reintentos ciegos en el cobro; si no hay respuesta, consultar el estado. (7) Circuit breaker para no atascarte si la pasarela cae. (8) Logs con el identificador de transacción pero sin datos de tarjeta jamás. (9) Webhook para recibir la confirmación asíncrona, validando su firma. (10) Tests con un servidor simulado (WireMock o MockRestServiceServer), nunca contra la pasarela real.

