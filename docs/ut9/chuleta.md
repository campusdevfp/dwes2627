# Chuleta — Aplicaciones híbridas

## Decidir cómo aprovechar una fuente

| | Consumir en vivo | Ingerir | Incorporar librería |
|---|---|---|---|
| El dato cambia | cada minuto | cada semana o mes | — |
| Si el tercero cae | fallas | sigues | no te afecta |
| `JOIN` con tus datos | no | **sí** | — |

## Cliente HTTP

```java
RestClient.builder()
    .baseUrl(url)
    .defaultHeader("api_key", clave)      // clave desde ${VARIABLE_ENTORNO}
    .requestFactory(f)                    // connectTimeout 3s, readTimeout 5s
    .build();
```
```java
@JsonIgnoreProperties(ignoreUnknown = true)   // el tercero añadirá campos
public record RespuestaAjena(...) {}
```
:material-check: DTO propio · :material-check: `Optional` en vez de propagar la excepción · :material-check: *timeouts* siempre

## Resiliencia

```java
@Retryable(retryFor = RestClientException.class, maxAttempts = 3,
           backoff = @Backoff(delay = 500, multiplier = 2))
@Recover  public T siFalla(Exception e, ...) { return T.sinDatos(); }

@CircuitBreaker(name = "aemet", fallbackMethod = "siFalla")

@Cacheable(value = "tiempo", key = "#codigo")
```
:material-alert: Reintentar solo lo **idempotente**. Nunca un POST que cobra.

| Estado del cortacircuitos | Significa |
|---|---|
| Cerrado | todo normal |
| Abierto | ni lo intento |
| Medio abierto | dejo pasar una de prueba |

## Ingesta (ETL)

```
EXTRAER → TRANSFORMAR → CARGAR → REGISTRAR
```

```java
Files.lines(ruta, Charset.forName("ISO-8859-1"))   // CSV español
linea.split(";", -1)                               // -1 conserva vacíos finales
s.trim().replaceAll("\\s+", " ")                   // limpiar
s.replace(',', '.')                                // decimales
```

**Idempotencia** = ejecutarla dos veces deja la BD igual que una:
```java
repo.findByCodigoOrigen(n.getCodigoOrigen())
    .ifPresentOrElse(e -> e.actualizarDesde(n), () -> repo.save(n));
```
+ `@UniqueConstraint(columnNames = "codigo_origen")` en la tabla.

```java
@Scheduled(cron = "0 30 3 * * MON")    // lunes 3:30, de madrugada
```
:material-alert: Con varias instancias se ejecuta varias veces → ShedLock.

## Librerías

Antes de añadir una: **¿viva? ¿la usa gente? ¿licencia? ¿qué arrastra? ¿CVE?**

| Licencia | Producto cerrado |
|---|:-:|
| MIT · Apache 2.0 · BSD | :material-check: |
| LGPL | :material-check: |
| GPL · **AGPL** | :material-close: |
| sin `LICENSE` | :material-close: |

```bash
mvn dependency:tree
mvn dependency-check:check
mvn versions:display-dependency-updates
```

`MAYOR.MENOR.PARCHE` → mayor **rompe**.

Aísla siempre detrás de una interfaz tuya:
```java
public interface GeneradorPdf { byte[] ficha(X x); }
```

## Analítica

```java
// MAL  findAll() + stream()      BIEN group by en la consulta
@Query("select c.barrio as barrio, count(c) as total from Comercio c group by c.barrio")
List<ResumenBarrio> resumen();      // proyección por interfaz
```

```sql
date_trunc('month', fecha)                        -- serie temporal
rank()  over (partition by barrio order by x desc) -- ranking
lag(total) over (order by mes)                     -- comparar con el anterior
```

| | OLTP | OLAP |
|---|---|---|
| Para | operar | analizar |
| Consultas | muchas y pequeñas | pocas y enormes |

Panel: **pocos indicadores · siempre una comparación · el gráfico adecuado · exportable**

:material-alert: La media miente; usa la mediana. :material-alert: Nada de agregados con menos de 5 individuos.

## Probar

```java
@RestClientTest(TiempoServicio.class)
servidor.expect(requestTo(...)).andRespond(withSuccess(json, APPLICATION_JSON));
servidor.expect(requestTo(...)).andRespond(withServerError());
```
Los tres casos: **éxito · error · tiempo agotado**.

```java
@Test void ejecutarDosVecesNoDuplica() { … }   // el test más valioso
```

:material-check: `mvn test` debe pasar **sin internet**.

## Antes de entregar

- [ ] La aplicación se navega con las fuentes externas apagadas
- [ ] Ninguna clave en el repositorio
- [ ] Ingesta ejecutada dos veces = mismos datos
- [ ] README con tabla de fuentes, licencias y variables de entorno
- [ ] `docker compose up` levanta todo
