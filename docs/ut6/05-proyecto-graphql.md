# 5. Proyecto paso a paso · GraphQL

Añadir un **esquema GraphQL** al mismo proyecto: consultas, mutaciones y suscripciones, sobre los mismos servicios. Cuatro pasos.

!!! success "Lo que vas a tener al final"
    ```
    POST /graphql       ← un solo endpoint para todo
    GET  /graphiql      ← el playground, solo en dev
    GET  /graphql/schema ← el esquema impreso
    ```

    Y el diff del servicio: **vacío**. Los resolutores solo delegan.

!!! danger "El contenido difícil de este tema es uno: el N+1"
    GraphQL es fácil de montar y fácil de hacer mal. **El cliente decide qué campos pide, así que decide el coste de la consulta.** Pedir la categoría de 100 funkos son 101 consultas si no lo has previsto.

    Y no se arregla con un `JOIN FETCH` fijo como en la UT5, porque la consulta cambia en cada petición. Hace falta **`@BatchMapping`**, y es el ítem que más puntúa en la rúbrica.

    Ten el log con `show-sql=true` abierto durante todo el tema.

---

## Paso 1 · Dependencias y esquema

```xml title="pom.xml"
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-graphql</artifactId>
</dependency>
<dependency>
  <groupId>org.springframework.graphql</groupId>
  <artifactId>spring-graphql-test</artifactId>
  <scope>test</scope>
</dependency>
```

```graphql title="src/main/resources/graphql/schema.graphqls"
# ==== TIPOS ====
type Funko {
    id: ID!
    nombre: String!
    precio: Float!
    cantidad: Int!
    imagen: String
    categoria: Categoria!
    createdAt: String!
}

type Categoria {
    id: ID!
    nombre: String!
    funkos: [Funko!]!
}

type PaginaFunkos {
    contenido: [Funko!]!
    total: Int!
    pagina: Int!
    totalPaginas: Int!
}

# ==== ENTRADAS ====
input FunkoInput {
    nombre: String!
    precio: Float!
    cantidad: Int!
    imagen: String
    categoria: String!
}

input FunkoFiltro {
    categoria: String
    precioMin: Float
    precioMax: Float
    nombre: String
}

# ==== QUERIES ====
type Query {
    funkos(filtro: FunkoFiltro, pagina: Int = 0, tamano: Int = 20): PaginaFunkos!
    funkoById(id: ID!): Funko
    categorias: [Categoria!]!
    categoriaByNombre(nombre: String!): Categoria
}

# ==== MUTACIONES ====
type Mutation {
    crearFunko(input: FunkoInput!): Funko!
    actualizarFunko(id: ID!, input: FunkoInput!): Funko!
    borrarFunko(id: ID!): Boolean!
}

# ==== SUSCRIPCIONES ====
type Subscription {
    funkoCambiado: NotificacionFunko!
}

type NotificacionFunko {
    tipo: String!
    funko: Funko!
    fecha: String!
}
```

!!! success "Los `!` del esquema son el contrato, y se propagan"
    `[Funko!]!` significa: **ni la lista ni sus elementos pueden ser `null`**. Una lista vacía es `[]`, nunca `null`.

    Y el detalle que cuesta entender: si un campo es `!` y tu resolutor devuelve `null`, **GraphQL anula el objeto padre entero** y lo reporta como error. La no-nulabilidad sube hacia arriba, así que `categoria: Categoria!` obliga a que **todos** los funkos tengan categoría.

    Es un contrato verificado en tiempo de ejecución, al revés que OpenAPI, que solo describe.

## Paso 2 · El controlador GraphQL

```java title="funkos/controllers/FunkosGraphQlController.java"
package es.iesx.funkos.funkos.controllers;

import es.iesx.funkos.funkos.dto.*;
import es.iesx.funkos.funkos.services.FunkosService;
import es.iesx.funkos.categorias.services.CategoriasService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.data.method.annotation.*;
import org.springframework.stereotype.Controller;

import java.util.*;
import java.util.stream.Collectors;

@Controller
public class FunkosGraphQlController {

    private final FunkosService servicio;                 // ← EL MISMO que usa REST
    private final CategoriasService categoriasService;

    public FunkosGraphQlController(FunkosService servicio,
                                   CategoriasService categoriasService) {
        this.servicio = servicio;
        this.categoriasService = categoriasService;
    }

    @QueryMapping
    public PaginaFunkosResponse funkos(@Argument Optional<FunkoFiltro> filtro,
                                       @Argument int pagina,
                                       @Argument int tamano) {
        var f = filtro.orElse(FunkoFiltro.vacio());
        var page = servicio.findAll(
                Optional.ofNullable(f.categoria()),
                Optional.ofNullable(f.precioMin()),
                Optional.ofNullable(f.precioMax()),
                Optional.ofNullable(f.nombre()),
                PageRequest.of(pagina, Math.min(tamano, 100)));

        return new PaginaFunkosResponse(page.getContent(), (int) page.getTotalElements(),
                                        page.getNumber(), page.getTotalPages());
    }

    @QueryMapping
    public FunkoResponse funkoById(@Argument Long id) {
        return servicio.findById(id);          // lanza FunkoNotFoundException
    }

    @MutationMapping
    public FunkoResponse crearFunko(@Argument("input") @Valid FunkoCreateRequest input) {
        return servicio.save(input);
    }

    @MutationMapping
    public FunkoResponse actualizarFunko(@Argument Long id,
                                         @Argument("input") @Valid FunkoCreateRequest input) {
        return servicio.replace(id, input);
    }

    @MutationMapping
    public Boolean borrarFunko(@Argument Long id) {
        servicio.deleteById(id);
        return true;
    }

    /** Resuelve el campo "categoria" de Funko para TODOS los funkos de golpe. */
    @BatchMapping(typeName = "Funko")
    public Map<FunkoResponse, CategoriaResponse> categoria(List<FunkoResponse> funkos) {
        var nombres = funkos.stream().map(FunkoResponse::categoria).distinct().toList();
        var porNombre = categoriasService.findByNombres(nombres).stream()
                .collect(Collectors.toMap(CategoriaResponse::nombre, c -> c));
        return funkos.stream().collect(Collectors.toMap(f -> f,
                                       f -> porNombre.get(f.categoria())));
    }
}
```

!!! danger "El `@BatchMapping` es la diferencia entre 2 y 101 consultas"
    Sin él, esta consulta:

    ```graphql
    { funkos { contenido { nombre categoria { nombre } } } }
    ```

    lanza **una consulta por cada funko** para su categoría. Es el N+1 de la [UT5](../ut5/04-proyectos.md), pero aquí **no lo puedes arreglar con un `JOIN FETCH` fijo**, porque la consulta depende de qué campos pida el cliente.

    `@BatchMapping` recibe **la lista entera** de funkos de golpe, hace una sola consulta con todos los nombres y devuelve el mapa. De 101 a **2**, independientemente de lo que se pida.

    **Es el problema característico de GraphQL**, y la razón de que haya que medirlo con `show-sql=true` en vez de suponer.

## Paso 3 · Las excepciones y la suscripción

```java title="config/GraphQlExceptionHandler.java"
@Component
public class GraphQlExceptionHandler extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        return switch (ex) {
            case FunkoNotFoundException e -> error(env, ErrorType.NOT_FOUND, e);
            case FunkoConflictException e -> error(env, ErrorType.BAD_REQUEST, e);
            case FunkoBadRequestException e -> error(env, ErrorType.BAD_REQUEST, e);
            default -> null;                     // que lo trate el por defecto
        };
    }

    private GraphQLError error(DataFetchingEnvironment env, ErrorType tipo, Throwable e) {
        return GraphqlErrorBuilder.newError(env)
                .errorType(tipo)
                .message(e.getMessage())
                .build();
    }
}
```

```java title="funkos/controllers/FunkosSubscriptionController.java"
@Controller
public class FunkosSubscriptionController {

    private final Sinks.Many<NotificacionFunko> emisor =
            Sinks.many().multicast().onBackpressureBuffer();

    @SubscriptionMapping
    public Flux<NotificacionFunko> funkoCambiado() {
        return emisor.asFlux();
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alCambiar(FunkoCambiadoEvent e) {
        emisor.tryEmitNext(new NotificacionFunko(
                e.tipo().name(), e.funko(), LocalDateTime.now().toString()));
    }
}
```

!!! success "Fíjate en lo que ha pasado con las excepciones"
    `FunkoNotFoundException` —escrita en la UT4, sin tocar desde entonces— se traduce a:

    | Transporte | Resultado |
    |---|---|
    | REST | **404 Not Found**, por su `@ResponseStatus` |
    | GraphQL | **200** con `errors[0].extensions.classification = "NOT_FOUND"` |
    | WebSocket | No aplica: no se notifica nada |

    **Si el servicio lanzara `ResponseStatusException` con `HttpStatus`, aquí no habría nada que traducir**, porque en GraphQL un código HTTP no significa nada. Es la decisión del [tema 3 de la UT4](../ut4/03-repositorios-servicios-dtos.md) cobrando intereses.

    Y el `switch` con patrones es Java 25, el de la UT2.

!!! info "La suscripción usa el mismo evento que el WebSocket"
    Un solo `FunkoCambiadoEvent` alimenta **dos** canales de tiempo real: el WebSocket crudo y la suscripción GraphQL. Si mañana añades Server-Sent Events, es un tercer oyente y cero cambios.

## Paso 4 · Probarlo

```properties title="application-dev.properties"
spring.graphql.graphiql.enabled=true
spring.graphql.graphiql.path=/graphiql
spring.graphql.schema.printer.enabled=true
spring.graphql.schema.introspection.enabled=true
```

```properties title="application-prod.properties"
spring.graphql.graphiql.enabled=false
spring.graphql.schema.introspection.enabled=false
```

=== "Consulta mínima"

    ```graphql
    { funkos { contenido { nombre } total } }
    ```

    ```json
    { "data": { "funkos": {
        "contenido": [{"nombre":"Mickey Mouse"},{"nombre":"Stitch"}],
        "total": 6 } } }
    ```

    **Solo el nombre.** Compara con el `GET /api/v1/funkos`, que devuelve los ocho campos de cada funko quiera el cliente o no.

=== "Con la relación"

    ```graphql
    { funkos(filtro: {categoria: "ANIME"}, tamano: 5) {
        contenido { nombre precio categoria { nombre } }
        total totalPaginas } }
    ```

    En el log: **2 consultas** gracias al `@BatchMapping`. Sin él serían 1 + número de funkos.

=== "Mutación"

    ```graphql
    mutation {
      crearFunko(input: {
        nombre: "Pikachu", precio: 20.0, cantidad: 7, categoria: "ANIME"
      }) { id nombre categoria { nombre } }
    }
    ```

    Y si el nombre está repetido:

    ```json
    { "data": { "crearFunko": null },
      "errors": [{ "message": "Ya existe un funko llamado Pikachu",
                   "extensions": { "classification": "BAD_REQUEST" } }] }
    ```

    **Con código HTTP 200.** Es la trampa del tema 2.

=== "Desde `curl`"

    ```bash
    curl -s localhost:8080/graphql \
      -H "Content-Type: application/json" \
      -d '{"query":"{ funkos { contenido { nombre } total } }"}' | jq
    ```

    Un solo endpoint, siempre `POST`, siempre con `{"query": "…"}`. Por eso la caché HTTP no sirve.

=== "Suscripción"

    En GraphiQL:

    ```graphql
    subscription { funkoCambiado { tipo funko { nombre } fecha } }
    ```

    Déjala abierta y manda un POST por REST. **El mensaje llega a la suscripción GraphQL y al WebSocket a la vez**, desde el mismo evento.

---

---

## Paso 5 · Los tests

```java
@SpringBootTest
@AutoConfigureGraphQlTester
@ActiveProfiles("test")
class FunkosGraphQlTest {

    @Autowired GraphQlTester tester;

    @Test
    void consultaDevuelveSoloLosCamposPedidos() {
        tester.document("{ funkos { contenido { nombre } total } }")
              .execute()
              .path("funkos.total").entity(Integer.class).isEqualTo(6)
              .path("funkos.contenido[0].nombre").entity(String.class).isNotNull();
    }

    @Test
    void idQueNoExisteDevuelveErrorNotFound() {
        tester.document("{ funkoById(id: 9999) { nombre } }")
              .execute()
              .errors()
              .satisfy(errores -> assertThat(errores)
                      .anyMatch(e -> e.getErrorType() == ErrorType.NOT_FOUND));
    }

    @Test
    void mutacionConNombreRepetidoFalla() {
        tester.document("""
                mutation {
                  crearFunko(input: {nombre: "Goku", precio: 10.0,
                                     cantidad: 1, categoria: "ANIME"}) { id }
                }""")
              .execute()
              .errors().expect(e -> e.getMessage().contains("Ya existe"));
    }
}
```

---

## Checklist

- [ ] `{ funkos { contenido { nombre } } }` devuelve **solo** el nombre
- [ ] Pedir la categoría de 100 funkos son **2 consultas**, no 101 *(contadas en el log)*
- [ ] Un id inexistente devuelve `errors` con `classification: NOT_FOUND`
- [ ] Las excepciones de la UT4 funcionan **sin tocarlas**
- [ ] Una mutación inválida devuelve `errors`, no un 500
- [ ] `MaxQueryDepthInstrumentation` y `MaxQueryComplexityInstrumentation` configurados
- [ ] `introspection.enabled=false` en el perfil `prod`
- [ ] `graphiql.enabled=false` en el perfil `prod`
- [ ] La suscripción usa **el mismo evento** que el WebSocket del tema 4
- [ ] `git diff` del servicio respecto al tema 4: **vacío**
