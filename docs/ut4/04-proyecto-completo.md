# 4. Proyecto completo paso a paso

Una **API REST de Funkos** entera, de cero a `.jar` ejecutable, con Maven y Spring Boot. Catorce pasos, cada uno con el fichero completo y una comprobación con `curl`.

!!! success "Qué vas a tener al final"
    ```
    GET    /api/v1/funkos                 → lista, con filtro ?categoria=
    GET    /api/v1/funkos/{id}            → uno, o 404
    POST   /api/v1/funkos                 → crea, 201 + Location, o 400 / 409
    PUT    /api/v1/funkos/{id}            → reemplaza, o 404
    PATCH  /api/v1/funkos/{id}            → modifica lo que mandes, o 404
    DELETE /api/v1/funkos/{id}            → 204, o 404
    ```

    Con **capas** (controlador → servicio → repositorio), **DTOs**, **validación**, **excepciones propias**, **caché** y **tests**. Los datos van en memoria: la base de datos llega en la UT5.

!!! info "Antes de empezar"
    Esto no se lee: se teclea. Cada paso deja el proyecto **compilando y arrancando**, así que si algo falla sabes exactamente en qué paso fue.

    Haz `git commit` al final de cada paso. Es la única forma de volver atrás cuando el paso 9 rompe algo del paso 4.

---

## Paso 1 · Crear el proyecto

Desde [start.spring.io](https://start.spring.io) o desde la terminal:

```bash
curl https://start.spring.io/starter.zip \
  -d type=maven-project \
  -d language=java \
  -d bootVersion=3.5.6 \
  -d javaVersion=25 \
  -d groupId=es.iesx \
  -d artifactId=funkos-api \
  -d name=funkos-api \
  -d packageName=es.iesx.funkos \
  -d dependencies=web,validation,cache,devtools \
  -o funkos-api.zip

unzip funkos-api.zip -d funkos-api && cd funkos-api
```

!!! tip "Las cuatro dependencias, y por qué cada una"
    | Starter | Para qué |
    |---|---|
    | `web` | Tomcat embebido, `@RestController`, Jackson |
    | `validation` | `@Valid`, `@NotBlank` y compañía |
    | `cache` | `@Cacheable`, `@CacheEvict` |
    | `devtools` | Reinicio automático al guardar — **solo en desarrollo** |

    `devtools` no se empaqueta en el `.jar` de producción: Spring Boot lo excluye solo.

```bash
./mvnw spring-boot:run
```

```
Tomcat started on port 8080 (http) with context path '/'
Started FunkosApiApplication in 1.412 seconds
```

Ya tienes un servidor. Vacío, pero funcionando.

!!! success "Comprueba"
    ```bash
    curl -i localhost:8080/
    # HTTP/1.1 404
    # {"timestamp":"...","status":404,"error":"Not Found","path":"/"}
    ```

    **Un 404 es buena señal**: significa que Tomcat está escuchando y que el manejador de errores por defecto funciona. Lo que no hay todavía son rutas.

---

## Paso 2 · El `pom.xml` completo

```xml title="pom.xml"
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0
                             https://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.5.6</version>
    <relativePath/>
  </parent>

  <groupId>es.iesx</groupId>
  <artifactId>funkos-api</artifactId>
  <version>1.0.0</version>
  <name>funkos-api</name>
  <description>API REST de Funkos — UT4 DWES</description>

  <properties>
    <java.version>25</java.version>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
  </properties>

  <dependencies>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-cache</artifactId>
    </dependency>

    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-devtools</artifactId>
      <scope>runtime</scope>
      <optional>true</optional>
    </dependency>

    <dependency>
      <groupId>org.springframework.boot</groupId>
      <artifactId>spring-boot-starter-test</artifactId>
      <scope>test</scope>
    </dependency>
  </dependencies>

  <build>
    <plugins>
      <plugin>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-maven-plugin</artifactId>
      </plugin>
    </plugins>
  </build>
</project>
```

!!! danger "Las tres cosas del `pom.xml` que importan"
    1. **El `<parent>` es lo que hace que no haya versiones.** Trae un `dependencyManagement` con la versión compatible de cada librería del ecosistema. Si pones una versión a mano, te sales de ese conjunto probado y puedes acabar con dos versiones de Jackson peleándose.

    2. **`<java.version>25</java.version>`** es la propiedad que el parent traduce a `maven.compiler.release`. Sin ella, los `record` no compilan.

    3. **El `spring-boot-maven-plugin` es el que hace el `.jar` ejecutable.** Sin él, `mvn package` genera un jar normal que no arranca porque no lleva las dependencias. Es el `maven-shade-plugin` de la UT2, pero específico de Spring Boot y mejor: usa un *fat jar* con su propio cargador de clases en vez de aplastar todo junto.

---

## Paso 3 · La estructura de paquetes

```
src/main/java/es/iesx/funkos/
├── FunkosApiApplication.java          ← arranque (paquete raíz)
├── config/
│   └── FunkosConfig.java
├── funkos/
│   ├── controllers/
│   │   └── FunkosRestController.java
│   ├── services/
│   │   ├── FunkosService.java
│   │   └── FunkosServiceImpl.java
│   ├── repositories/
│   │   ├── FunkosRepository.java
│   │   └── FunkosRepositoryImpl.java
│   ├── models/
│   │   └── Funko.java
│   ├── dto/
│   │   ├── FunkoResponse.java
│   │   ├── FunkoCreateRequest.java
│   │   └── FunkoUpdateRequest.java
│   ├── mappers/
│   │   └── FunkoMapper.java
│   └── exceptions/
│       ├── FunkoException.java
│       ├── FunkoNotFoundException.java
│       ├── FunkoBadRequestException.java
│       └── FunkoConflictException.java
└── common/
    └── GlobalExceptionHandler.java
```

!!! tip "Por qué agrupado por feature y no por capa"
    Hay dos formas de organizar esto:

    === "Por feature (la de arriba)"

        ```
        funkos/{controllers,services,repositories,models,dto}
        pedidos/{controllers,services,repositories,models,dto}
        ```

        Todo lo de funkos está junto. Añadir pedidos es **una carpeta nueva** y cero ficheros tocados. Borrar una funcionalidad es borrar una carpeta.

    === "Por capa"

        ```
        controllers/{FunkosController, PedidosController}
        services/{FunkosService, PedidosService}
        ```

        Al añadir pedidos hay que tocar cinco carpetas, y para entender funkos hay que abrir cinco sitios.

    Con dos entidades da igual; con quince, la segunda es inmanejable. En el proyecto del segundo trimestre tendrás más de dos.

    **Lo que no es negociable:** `FunkosApiApplication` va en el **paquete raíz**, porque `@ComponentScan` escanea desde ahí hacia abajo.

---

## Paso 4 · El modelo

```java title="funkos/models/Funko.java"
package es.iesx.funkos.funkos.models;

import java.time.LocalDateTime;

public class Funko {
    private Long id;
    private String nombre;
    private Double precio;
    private Integer cantidad;
    private String imagen;
    private String categoria;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Funko() {}

    public Funko(Long id, String nombre, Double precio, Integer cantidad,
                 String imagen, String categoria,
                 LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.nombre = nombre;
        this.precio = precio;
        this.cantidad = cantidad;
        this.imagen = imagen;
        this.categoria = categoria;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Builder builder() { return new Builder(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNombre() { return nombre; }
    public Double getPrecio() { return precio; }
    public Integer getCantidad() { return cantidad; }
    public String getImagen() { return imagen; }
    public String getCategoria() { return categoria; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public static class Builder {
        private final Funko f = new Funko();
        public Builder id(Long v)                { f.id = v;        return this; }
        public Builder nombre(String v)          { f.nombre = v;    return this; }
        public Builder precio(Double v)          { f.precio = v;    return this; }
        public Builder cantidad(Integer v)       { f.cantidad = v;  return this; }
        public Builder imagen(String v)          { f.imagen = v;    return this; }
        public Builder categoria(String v)       { f.categoria = v; return this; }
        public Builder createdAt(LocalDateTime v){ f.createdAt = v; return this; }
        public Builder updatedAt(LocalDateTime v){ f.updatedAt = v; return this; }
        public Funko build() { return f; }
    }
}
```

!!! info "¿Por qué una clase y no un `record`?"
    Porque en la **UT5** este modelo se convierte en una `@Entity` de JPA, y JPA necesita un constructor sin argumentos y poder asignar los campos. Un `record` no sirve para una entidad.

    Los **DTOs sí son `record`** (paso 6): esos nunca van a ser entidades.

    El `builder` no es decoración: con ocho campos, `new Funko(null, "Casco", 35.0, 1, null, "OTROS", ahora, ahora)` es ilegible y basta con cambiar dos parámetros de sitio para meter un bug que compila.

---

## Paso 5 · El repositorio en memoria

```java title="funkos/repositories/FunkosRepository.java"
package es.iesx.funkos.funkos.repositories;

import es.iesx.funkos.funkos.models.Funko;
import java.util.List;
import java.util.Optional;

public interface FunkosRepository {
    List<Funko> findAll();
    List<Funko> findByCategoria(String categoria);
    Optional<Funko> findById(Long id);
    boolean existsByNombre(String nombre);
    Funko save(Funko funko);
    void deleteById(Long id);
}
```

```java title="funkos/repositories/FunkosRepositoryImpl.java"
package es.iesx.funkos.funkos.repositories;

import es.iesx.funkos.funkos.models.Funko;
import org.springframework.stereotype.Repository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class FunkosRepositoryImpl implements FunkosRepository {

    private final Map<Long, Funko> almacen = new ConcurrentHashMap<>();
    private final AtomicLong siguienteId = new AtomicLong(1);

    @Override
    public List<Funko> findAll() {
        return List.copyOf(almacen.values());
    }

    @Override
    public List<Funko> findByCategoria(String categoria) {
        return almacen.values().stream()
                .filter(f -> f.getCategoria().equalsIgnoreCase(categoria))
                .toList();
    }

    @Override
    public Optional<Funko> findById(Long id) {
        return Optional.ofNullable(almacen.get(id));
    }

    @Override
    public boolean existsByNombre(String nombre) {
        return almacen.values().stream()
                .anyMatch(f -> f.getNombre().equalsIgnoreCase(nombre));
    }

    @Override
    public Funko save(Funko funko) {
        if (funko.getId() == null) funko.setId(siguienteId.getAndIncrement());
        almacen.put(funko.getId(), funko);
        return funko;
    }

    @Override
    public void deleteById(Long id) {
        almacen.remove(id);
    }
}
```

!!! danger "`ConcurrentHashMap` y `AtomicLong`, no `HashMap` e `int`"
    El repositorio es un **singleton** y atiende peticiones **en paralelo**. Con un `HashMap` normal, dos POST simultáneos pueden corromper la estructura interna; con un `int id++`, los dos pueden recibir el mismo id y uno sobrescribe al otro.

    `ConcurrentHashMap` y `getAndIncrement()` resuelven las dos cosas. Es la lección del scope singleton del tema 1, aplicada.

    Y el `List.copyOf(...)` del `findAll`: devuelve una copia inmutable, así que nadie de fuera puede modificar el almacén por accidente. Es el `List.copyOf` de la UT2.

!!! info "El `equalsIgnoreCase` está puesto a propósito"
    El enunciado de la práctica avisaba: *«cuidado con las letras en mayúscula o minúscula»*. `?categoria=disney` y `?categoria=DISNEY` tienen que devolver lo mismo.

---

## Paso 6 · Los DTOs

```java title="funkos/dto/FunkoResponse.java"
package es.iesx.funkos.funkos.dto;

import java.time.LocalDateTime;

public record FunkoResponse(
        Long id,
        String nombre,
        Double precio,
        Integer cantidad,
        String imagen,
        String categoria,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
```

```java title="funkos/dto/FunkoCreateRequest.java"
package es.iesx.funkos.funkos.dto;

import jakarta.validation.constraints.*;

public record FunkoCreateRequest(

        @NotBlank(message = "El nombre no puede estar vacío")
        @Size(max = 100, message = "El nombre no puede tener más de 100 caracteres")
        String nombre,

        @NotNull(message = "El precio es obligatorio")
        @PositiveOrZero(message = "El precio no puede ser negativo")
        Double precio,

        @NotNull(message = "La cantidad es obligatoria")
        @PositiveOrZero(message = "La cantidad no puede ser negativa")
        Integer cantidad,

        String imagen,

        @NotBlank(message = "La categoría no puede estar vacía")
        @Pattern(regexp = "DISNEY|MARVEL|ANIME|OTROS",
                 message = "La categoría debe ser DISNEY, MARVEL, ANIME u OTROS")
        String categoria
) {}
```

```java title="funkos/dto/FunkoUpdateRequest.java"
package es.iesx.funkos.funkos.dto;

import jakarta.validation.constraints.*;

public record FunkoUpdateRequest(

        @Size(max = 100, message = "El nombre no puede tener más de 100 caracteres")
        String nombre,

        @PositiveOrZero(message = "El precio no puede ser negativo")
        Double precio,

        @PositiveOrZero(message = "La cantidad no puede ser negativa")
        Integer cantidad,

        String imagen,

        @Pattern(regexp = "DISNEY|MARVEL|ANIME|OTROS",
                 message = "La categoría debe ser DISNEY, MARVEL, ANIME u OTROS")
        String categoria
) {}
```

!!! success "Compara los dos requests campo a campo"
    | | `CreateRequest` | `UpdateRequest` |
    |---|---|---|
    | `nombre` | `@NotBlank` + `@Size` | solo `@Size` |
    | `precio` | `@NotNull` + `@PositiveOrZero` | solo `@PositiveOrZero` |
    | `categoria` | `@NotBlank` + `@Pattern` | solo `@Pattern` |

    En el de actualización **desaparecen todos los `@NotNull` y `@NotBlank`**, porque en un PATCH `null` significa «este campo no lo cambies». Las reglas de formato (`@Size`, `@Pattern`, `@PositiveOrZero`) **sí se quedan**: si mandas un valor, tiene que ser válido.

    Y esto es la razón de que sean dos records y no uno con los campos opcionales.

!!! info "`@Pattern` con una lista cerrada, de momento"
    Lo correcto sería un `enum Categoria`. Con `@Pattern` se ve el mecanismo de validación por expresión regular (UT3, tema 2) y el mensaje de error sale claro. En el reto 1 lo cambias a `enum`.

---

## Paso 7 · El mapeador

```java title="funkos/mappers/FunkoMapper.java"
package es.iesx.funkos.funkos.mappers;

import es.iesx.funkos.funkos.dto.*;
import es.iesx.funkos.funkos.models.Funko;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class FunkoMapper {

    public FunkoResponse toResponse(Funko f) {
        return new FunkoResponse(
                f.getId(), f.getNombre(), f.getPrecio(), f.getCantidad(),
                f.getImagen(), f.getCategoria(), f.getCreatedAt(), f.getUpdatedAt());
    }

    public List<FunkoResponse> toResponse(List<Funko> funkos) {
        return funkos.stream().map(this::toResponse).toList();
    }

    /** POST: crea uno nuevo. El id y las fechas los pone el servidor. */
    public Funko toModel(FunkoCreateRequest r) {
        var ahora = LocalDateTime.now();
        return Funko.builder()
                .nombre(r.nombre())
                .precio(r.precio())
                .cantidad(r.cantidad())
                .imagen(r.imagen() != null ? r.imagen() : "https://placehold.co/300")
                .categoria(r.categoria().toUpperCase())
                .createdAt(ahora)
                .updatedAt(ahora)
                .build();
    }

    /** PUT: reemplaza todo menos el id y la fecha de creación. */
    public Funko toModelReemplazando(Funko original, FunkoCreateRequest r) {
        return Funko.builder()
                .id(original.getId())
                .nombre(r.nombre())
                .precio(r.precio())
                .cantidad(r.cantidad())
                .imagen(r.imagen())
                .categoria(r.categoria().toUpperCase())
                .createdAt(original.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    /** PATCH: solo cambia lo que llega distinto de null. */
    public Funko toModelParcheando(Funko original, FunkoUpdateRequest r) {
        return Funko.builder()
                .id(original.getId())
                .nombre(r.nombre()       != null ? r.nombre()                   : original.getNombre())
                .precio(r.precio()       != null ? r.precio()                   : original.getPrecio())
                .cantidad(r.cantidad()   != null ? r.cantidad()                 : original.getCantidad())
                .imagen(r.imagen()       != null ? r.imagen()                   : original.getImagen())
                .categoria(r.categoria() != null ? r.categoria().toUpperCase()  : original.getCategoria())
                .createdAt(original.getCreatedAt())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
```

!!! success "Tres métodos porque son tres operaciones distintas"
    | Método | Verbo | Qué hace con lo que no mandas |
    |---|---|---|
    | `toModel` | POST | No aplica: es nuevo |
    | `toModelReemplazando` | PUT | **Lo borra** (queda `null`) |
    | `toModelParcheando` | PATCH | **Lo conserva** |

    Si tu PUT y tu PATCH llaman al mismo método, has implementado dos veces el PATCH. Es el error de diseño más común de la unidad.

    Y `createdAt` **nunca** se toca después del `toModel`: es la fecha en que nació el registro.

---

## Paso 8 · Las excepciones

```java title="funkos/exceptions/FunkoException.java"
package es.iesx.funkos.funkos.exceptions;

public abstract class FunkoException extends RuntimeException {
    protected FunkoException(String mensaje) { super(mensaje); }
}
```

```java title="funkos/exceptions/FunkoNotFoundException.java"
package es.iesx.funkos.funkos.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class FunkoNotFoundException extends FunkoException {
    public FunkoNotFoundException(Long id) {
        super("No se ha encontrado el funko con id: " + id);
    }
}
```

```java title="funkos/exceptions/FunkoConflictException.java"
package es.iesx.funkos.funkos.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class FunkoConflictException extends FunkoException {
    public FunkoConflictException(String mensaje) { super(mensaje); }
}
```

```java title="funkos/exceptions/FunkoBadRequestException.java"
package es.iesx.funkos.funkos.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class FunkoBadRequestException extends FunkoException {
    public FunkoBadRequestException(String mensaje) { super(mensaje); }
}
```

!!! tip "Fíjate en el constructor de `FunkoNotFoundException`"
    Recibe **el id**, no el mensaje ya montado. Así todos los 404 de la API dicen exactamente lo mismo y nadie se inventa la redacción. Es el mismo criterio del E3 de la UT2, tema 5.

---

## Paso 9 · El servicio

```java title="funkos/services/FunkosService.java"
package es.iesx.funkos.funkos.services;

import es.iesx.funkos.funkos.dto.*;
import java.util.List;
import java.util.Optional;

public interface FunkosService {
    List<FunkoResponse> findAll(Optional<String> categoria);
    FunkoResponse findById(Long id);
    FunkoResponse save(FunkoCreateRequest funko);
    FunkoResponse replace(Long id, FunkoCreateRequest funko);
    FunkoResponse update(Long id, FunkoUpdateRequest funko);
    void deleteById(Long id);
}
```

```java title="funkos/services/FunkosServiceImpl.java"
package es.iesx.funkos.funkos.services;

import es.iesx.funkos.funkos.dto.*;
import es.iesx.funkos.funkos.exceptions.*;
import es.iesx.funkos.funkos.mappers.FunkoMapper;
import es.iesx.funkos.funkos.models.Funko;
import es.iesx.funkos.funkos.repositories.FunkosRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@CacheConfig(cacheNames = {"funkos"})
public class FunkosServiceImpl implements FunkosService {

    private static final Logger log = LoggerFactory.getLogger(FunkosServiceImpl.class);

    private final FunkosRepository repositorio;
    private final FunkoMapper mapper;

    public FunkosServiceImpl(FunkosRepository repositorio, FunkoMapper mapper) {
        this.repositorio = repositorio;
        this.mapper = mapper;
    }

    @Override
    public List<FunkoResponse> findAll(Optional<String> categoria) {
        log.info("findAll con categoria: {}", categoria);
        return mapper.toResponse(
                categoria.map(repositorio::findByCategoria)
                         .orElseGet(repositorio::findAll));
    }

    @Override
    @Cacheable(key = "#id")
    public FunkoResponse findById(Long id) {
        log.info("findById: {}", id);
        return repositorio.findById(id)
                .map(mapper::toResponse)
                .orElseThrow(() -> new FunkoNotFoundException(id));
    }

    @Override
    @CacheEvict(allEntries = true)
    public FunkoResponse save(FunkoCreateRequest request) {
        log.info("save: {}", request.nombre());
        if (repositorio.existsByNombre(request.nombre()))
            throw new FunkoConflictException(
                    "Ya existe un funko con el nombre: " + request.nombre());
        return mapper.toResponse(repositorio.save(mapper.toModel(request)));
    }

    @Override
    @CacheEvict(allEntries = true)
    public FunkoResponse replace(Long id, FunkoCreateRequest request) {
        log.info("replace: {}", id);
        var original = repositorio.findById(id)
                .orElseThrow(() -> new FunkoNotFoundException(id));
        return mapper.toResponse(
                repositorio.save(mapper.toModelReemplazando(original, request)));
    }

    @Override
    @CacheEvict(allEntries = true)
    public FunkoResponse update(Long id, FunkoUpdateRequest request) {
        log.info("update: {}", id);
        var original = repositorio.findById(id)
                .orElseThrow(() -> new FunkoNotFoundException(id));
        return mapper.toResponse(
                repositorio.save(mapper.toModelParcheando(original, request)));
    }

    @Override
    @CacheEvict(allEntries = true)
    public void deleteById(Long id) {
        log.info("deleteById: {}", id);
        if (repositorio.findById(id).isEmpty())
            throw new FunkoNotFoundException(id);
        repositorio.deleteById(id);
    }
}
```

!!! success "Lee este fichero entero y mira lo que NO hay"
    No hay **ni una** de estas palabras: `ResponseEntity`, `HttpStatus`, `@PathVariable`, `@RequestParam`, `HttpServletRequest`.

    El servicio no sabe que existe HTTP. Recibe DTOs, devuelve DTOs, y cuando algo va mal **lanza**. Quien traduzca eso a un código de estado es problema de otro.

    Y por eso en la UT6 este mismo servicio podrá atender un WebSocket sin cambiar una línea.

!!! danger "Los tres detalles que marcan la diferencia"
    **1. `@CacheEvict(allEntries = true)` en los cuatro métodos de escritura.** No solo en `save`. Un `update` también deja obsoleta la lista completa.

    **2. El `deleteById` comprueba antes de borrar.** Sin ese `if`, borrar un id que no existe devuelve **204 No Content** y el cliente cree que ha borrado algo. Con él, **404**. `repositorio.deleteById` no se queja de un id inexistente.

    **3. `categoria.map(...).orElseGet(...)`** es el `Optional` de la UT2 haciendo exactamente aquello para lo que se inventó: una rama sin un solo `if`.

---

## Paso 10 · El controlador

```java title="funkos/controllers/FunkosRestController.java"
package es.iesx.funkos.funkos.controllers;

import es.iesx.funkos.funkos.dto.*;
import es.iesx.funkos.funkos.services.FunkosService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("${api.path:/api}/${api.version:v1}/funkos")
public class FunkosRestController {

    private final FunkosService servicio;

    public FunkosRestController(FunkosService servicio) {
        this.servicio = servicio;
    }

    @GetMapping
    public ResponseEntity<List<FunkoResponse>> findAll(
            @RequestParam Optional<String> categoria) {
        return ResponseEntity.ok(servicio.findAll(categoria));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FunkoResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(servicio.findById(id));
    }

    @PostMapping
    public ResponseEntity<FunkoResponse> create(
            @Valid @RequestBody FunkoCreateRequest funko) {
        var creado = servicio.save(funko);
        return ResponseEntity
                .created(URI.create("/api/v1/funkos/" + creado.id()))
                .body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<FunkoResponse> replace(
            @PathVariable Long id,
            @Valid @RequestBody FunkoCreateRequest funko) {
        return ResponseEntity.ok(servicio.replace(id, funko));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<FunkoResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody FunkoUpdateRequest funko) {
        return ResponseEntity.ok(servicio.update(id, funko));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        servicio.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
```

!!! success "Seis métodos, ninguno con un `if`"
    Cada uno hace **tres cosas**: recibe, delega y envuelve en un `ResponseEntity`. Nada más.

    - **No captura excepciones.** `FunkoNotFoundException` sube con su `@ResponseStatus` y Spring responde 404.
    - **No valida.** El `@Valid` dispara las anotaciones del DTO, y el `@RestControllerAdvice` del paso 11 formatea el 400.
    - **No conoce el modelo `Funko`**, solo los DTOs.

    Un controlador con lógica dentro es un controlador que no se puede probar sin servidor.

    Y el **`PUT` usa `FunkoCreateRequest`**, no `FunkoUpdateRequest`: un PUT reemplaza el recurso entero, así que todos los campos obligatorios vuelven a serlo.

---

## Paso 11 · El manejador global de errores

```java title="common/GlobalExceptionHandler.java"
package es.iesx.funkos.common;

import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 400 con los errores campo a campo. */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, Object> validacion(MethodArgumentNotValidException ex) {
        Map<String, String> campos = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
          .forEach(e -> campos.put(e.getField(), e.getDefaultMessage()));
        return Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 400,
                "error", "Datos de entrada inválidos",
                "campos", campos);
    }

    /** 400 cuando el JSON no se puede ni leer. */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public Map<String, Object> jsonIlegible(HttpMessageNotReadableException ex) {
        return Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 400,
                "error", "El cuerpo de la peticion no es un JSON valido");
    }

    /** 400 cuando /funkos/abc intenta ser un Long. */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public Map<String, Object> tipoMalo(MethodArgumentTypeMismatchException ex) {
        return Map.of(
                "timestamp", LocalDateTime.now().toString(),
                "status", 400,
                "error", "El parametro '%s' no tiene el tipo esperado"
                        .formatted(ex.getName()));
    }
}
```

!!! info "Lo que NO está aquí, y es deliberado"
    No hay un handler para `FunkoNotFoundException`. **No hace falta**: la excepción lleva su `@ResponseStatus(HttpStatus.NOT_FOUND)` y Spring la traduce sola.

    Aquí solo están los errores que **no son del dominio**: validación, JSON roto, tipo de parámetro incorrecto. Son fallos del cliente al hablar HTTP, no reglas de negocio.

---

## Paso 12 · La configuración

```properties title="src/main/resources/application.properties"
spring.application.name=funkos-api

### Servidor
server.port=${PORT:8080}

### Rutas de la API
api.path=/api
api.version=v1

### Errores: en desarrollo queremos ver el mensaje
server.error.include-message=always
server.error.include-binding-errors=always
server.error.include-stacktrace=never

### Compresion
server.compression.enabled=true
server.compression.mime-types=application/json,text/html,text/plain
server.compression.min-response-size=1024

### Locale
spring.web.locale=es_ES
spring.web.locale-resolver=fixed

### Logging
logging.level.es.iesx.funkos=DEBUG

### Perfil por defecto
spring.profiles.active=${SPRING_PROFILES_ACTIVE:dev}
```

```java title="config/FunkosConfig.java"
package es.iesx.funkos.config;

import es.iesx.funkos.funkos.dto.FunkoCreateRequest;
import es.iesx.funkos.funkos.services.FunkosService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.*;

@Configuration
public class FunkosConfig {

    /** Carga datos de ejemplo al arrancar, solo en el perfil dev. */
    @Bean
    @Profile("dev")
    public CommandLineRunner datosIniciales(FunkosService servicio) {
        return args -> {
            servicio.save(new FunkoCreateRequest(
                    "Mickey Mouse", 15.99, 10, null, "DISNEY"));
            servicio.save(new FunkoCreateRequest(
                    "Spider-Man", 19.99, 5, null, "MARVEL"));
            servicio.save(new FunkoCreateRequest(
                    "Goku", 22.50, 3, null, "ANIME"));
            servicio.save(new FunkoCreateRequest(
                    "Stitch", 17.00, 8, null, "DISNEY"));
        };
    }
}
```

Y hay que habilitar la caché:

```java title="FunkosApiApplication.java"
package es.iesx.funkos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class FunkosApiApplication {
    public static void main(String[] args) {
        SpringApplication.run(FunkosApiApplication.class, args);
    }
}
```

!!! danger "Sin `@EnableCaching`, las anotaciones de caché no hacen nada"
    Y no avisan. `@Cacheable` queda ahí, decorativa, y el método se ejecuta siempre. Es el mismo tipo de fallo silencioso que el `@Valid` olvidado.

!!! tip "El `@Profile(\"dev\")` del `CommandLineRunner`"
    Los datos de ejemplo **solo** se cargan en desarrollo. En producción, ese bean no se crea. Sin el `@Profile`, tu API de producción arrancaría con cuatro funkos inventados dentro.

---

## Paso 13 · Probarlo

```bash
./mvnw spring-boot:run
```

=== "Los casos que funcionan"

    ```bash
    # Listar los cuatro de ejemplo
    curl -s localhost:8080/api/v1/funkos | jq '.[].nombre'
    # "Mickey Mouse"  "Spider-Man"  "Goku"  "Stitch"

    # Filtrar, en minúsculas
    curl -s "localhost:8080/api/v1/funkos?categoria=disney" | jq 'length'
    # 2

    # Uno
    curl -s localhost:8080/api/v1/funkos/1 | jq '.nombre'
    # "Mickey Mouse"

    # Crear
    curl -i -X POST localhost:8080/api/v1/funkos \
      -H "Content-Type: application/json" \
      -d '{"nombre":"Pikachu","precio":20.0,"cantidad":7,"categoria":"ANIME"}'
    # HTTP/1.1 201
    # Location: /api/v1/funkos/5

    # PATCH: solo el precio
    curl -s -X PATCH localhost:8080/api/v1/funkos/5 \
      -H "Content-Type: application/json" \
      -d '{"precio":25.0}' | jq '{nombre, precio, cantidad}'
    # { "nombre": "Pikachu", "precio": 25, "cantidad": 7 }   ← cantidad intacta

    # Borrar
    curl -i -X DELETE localhost:8080/api/v1/funkos/5
    # HTTP/1.1 204
    ```

=== "Los casos de error"

    ```bash
    # 404: no existe
    curl -i localhost:8080/api/v1/funkos/9999
    # HTTP/1.1 404
    # {"status":404,"message":"No se ha encontrado el funko con id: 9999"}

    # 400: validación, campo a campo
    curl -s -X POST localhost:8080/api/v1/funkos \
      -H "Content-Type: application/json" \
      -d '{"nombre":"","precio":-5,"cantidad":1,"categoria":"PELICULAS"}' | jq
    # {
    #   "status": 400,
    #   "error": "Datos de entrada inválidos",
    #   "campos": {
    #     "nombre": "El nombre no puede estar vacío",
    #     "precio": "El precio no puede ser negativo",
    #     "categoria": "La categoría debe ser DISNEY, MARVEL, ANIME u OTROS"
    #   }
    # }

    # 409: nombre repetido
    curl -i -X POST localhost:8080/api/v1/funkos \
      -H "Content-Type: application/json" \
      -d '{"nombre":"Goku","precio":10.0,"cantidad":1,"categoria":"ANIME"}'
    # HTTP/1.1 409
    # {"message":"Ya existe un funko con el nombre: Goku"}

    # 400: el id no es un número
    curl -i localhost:8080/api/v1/funkos/abc
    # HTTP/1.1 400

    # 415: falta el Content-Type
    curl -i -X POST localhost:8080/api/v1/funkos -d '{"nombre":"X"}'
    # HTTP/1.1 415

    # 404 en DELETE: no existe
    curl -i -X DELETE localhost:8080/api/v1/funkos/9999
    # HTTP/1.1 404
    ```

!!! success "Los tres errores que distinguen una API bien hecha"
    | | Lo que hace una API mal hecha | Lo que hace la tuya |
    |---|---|---|
    | GET de un id inexistente | 500 (`NoSuchElementException`) | **404** |
    | POST con datos inválidos | 201, con basura dentro | **400** con los campos |
    | DELETE de un id inexistente | 204, «borrado» | **404** |

---

## Paso 14 · Los tests y el `.jar`

```java title="src/test/java/.../FunkosServiceImplTest.java"
@ExtendWith(MockitoExtension.class)
class FunkosServiceImplTest {

    @Mock  FunkosRepository repositorio;
    @Spy   FunkoMapper mapper = new FunkoMapper();
    @InjectMocks FunkosServiceImpl servicio;

    @Test
    void findByIdQueNoExisteLanzaNotFound() {
        when(repositorio.findById(99L)).thenReturn(Optional.empty());

        assertThrows(FunkoNotFoundException.class, () -> servicio.findById(99L));
        verify(repositorio, times(1)).findById(99L);
    }

    @Test
    void saveConNombreRepetidoLanzaConflict() {
        when(repositorio.existsByNombre("Goku")).thenReturn(true);

        var request = new FunkoCreateRequest("Goku", 10.0, 1, null, "ANIME");
        assertThrows(FunkoConflictException.class, () -> servicio.save(request));
        verify(repositorio, never()).save(any());
    }
}
```

```java title="src/test/java/.../FunkosRestControllerTest.java"
@SpringBootTest
@AutoConfigureMockMvc
class FunkosRestControllerTest {

    @Autowired MockMvc mockMvc;
    @MockitoBean FunkosService servicio;

    @Test
    void postConNombreVacioDevuelve400() throws Exception {
        mockMvc.perform(post("/api/v1/funkos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                        {"nombre":"","precio":10.0,"cantidad":1,"categoria":"ANIME"}"""))
               .andExpect(status().isBadRequest())
               .andExpect(jsonPath("$.campos.nombre").exists());
    }

    @Test
    void getIdQueNoExisteDevuelve404() throws Exception {
        when(servicio.findById(99L)).thenThrow(new FunkoNotFoundException(99L));

        mockMvc.perform(get("/api/v1/funkos/99"))
               .andExpect(status().isNotFound());
    }
}
```

```bash
./mvnw clean package
java -jar target/funkos-api-1.0.0.jar
```

```
[INFO] Tests run: 12, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
[INFO] Building jar: target/funkos-api-1.0.0.jar
```

!!! success "Los dos tipos de test y para qué sirve cada uno"
    | | `@ExtendWith(MockitoExtension)` | `@SpringBootTest` + `MockMvc` |
    |---|---|---|
    | Qué prueba | La lógica del servicio | La capa HTTP |
    | Levanta Spring | **No** | Sí |
    | Tarda | milisegundos | ~2 segundos |
    | Detecta | reglas de negocio mal | `@Valid` olvidado, rutas, códigos |

    El de arriba no sabe que existe HTTP; el de abajo no sabe qué hace el servicio (está mockeado). Entre los dos cubren la unidad, y ninguno necesita base de datos ni Postman.

    El `@Valid` olvidado del tema 3 **solo** lo caza el segundo. Por eso van los dos.

!!! info "`@MockitoBean`, no `@MockBean`"
    `@MockBean` está obsoleta desde Spring Boot 3.4. Si copias código de un tutorial de 2023 verás la vieja; con Spring Boot 3.5 toca `@MockitoBean`, del paquete `org.springframework.test.context.bean.override.mockito`.

---

## Lo que has construido

```mermaid
graph TB
    Cliente["curl / Postman"] -->|HTTP + JSON| C["FunkosRestController<br/>@RestController"]
    C -->|DTOs| S["FunkosServiceImpl<br/>@Service + @Cacheable"]
    S -->|modelo Funko| R["FunkosRepositoryImpl<br/>@Repository"]
    R --> M[("ConcurrentHashMap<br/>en memoria")]

    C -.->|@Valid falla| G["GlobalExceptionHandler<br/>@RestControllerAdvice"]
    S -.->|FunkoNotFoundException| G
    G -.->|400 / 404 / 409| Cliente

    MAP["FunkoMapper<br/>@Component"] -.-> S
```

!!! success "Checklist antes de dar el proyecto por bueno"
    - [ ] `./mvnw clean package` acaba en `BUILD SUCCESS` con los tests en verde
    - [ ] `java -jar target/*.jar` arranca sin errores
    - [ ] GET de un id inexistente devuelve **404**, no 500
    - [ ] POST con nombre vacío devuelve **400** con el campo señalado
    - [ ] POST con nombre repetido devuelve **409**
    - [ ] DELETE de un id inexistente devuelve **404**, no 204
    - [ ] POST con 201 devuelve cabecera **`Location`**
    - [ ] El PATCH conserva los campos que no mandas; el PUT **no**
    - [ ] El `@Service` no contiene la palabra `ResponseEntity` en ninguna línea
    - [ ] El filtro `?categoria=disney` funciona en minúsculas

!!! info "Y en la UT5 se cambia una sola capa"
    `FunkosRepositoryImpl` y su `ConcurrentHashMap` desaparecen; en su lugar, una interfaz que extiende `JpaRepository`. **El servicio, el controlador, los DTOs, el mapeador y las excepciones no se tocan.**

    Eso es lo que compras con las capas, y solo se nota el día que cambias una.
