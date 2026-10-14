# 4. Proyecto paso a paso · WebSockets

Añadir **notificaciones en tiempo real** al proyecto de la UT5. Seis pasos, y en el servicio solo cambian dos líneas.

!!! success "Lo que vas a tener al final"
    ```
    WS   /ws/v1/funkos          ← cada CREATE, UPDATE y DELETE llega al instante
    GET  /ws-test.html          ← página de prueba con el log en pantalla
    GET  /api/v1/ws/conectados  ← cuántos clientes escuchan
    ```

    Y el diff del servicio:

    ```diff
    + private final ApplicationEventPublisher eventos;
    + eventos.publishEvent(new FunkoCambiadoEvent(Tipo.CREATE, respuesta));
    ```

!!! danger "Antes de empezar: comprueba que tu servicio está limpio"
    ```bash
    grep -rn "ResponseEntity\|HttpStatus\|@PathVariable" src/main/java/**/services/
    ```

    **Si eso devuelve algo, este tema no se puede hacer** sin refactorizar primero. Un servicio que habla HTTP no se puede reutilizar desde un WebSocket, donde no hay códigos de estado.

    Es la factura de la UT4, y llega aquí.

---

## Paso 1 · La dependencia y el evento

```xml title="pom.xml"
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

```java title="funkos/events/FunkoCambiadoEvent.java"
package es.iesx.funkos.funkos.events;

import es.iesx.funkos.funkos.dto.FunkoResponse;

/** Lo que ha pasado con un funko. Se publica DENTRO de la transacción
 *  y se consume DESPUÉS de confirmarla. */
public record FunkoCambiadoEvent(Tipo tipo, FunkoResponse funko) {
    public enum Tipo { CREATE, UPDATE, DELETE }
}
```

!!! success "Por qué un evento y no una llamada directa"
    Podrías inyectar el notificador en el servicio y llamarlo. Con un evento ganas dos cosas:

    1. **El servicio no conoce al notificador.** Publica «ha pasado esto» y se desentiende. Mañana puedes añadir un segundo oyente (un log de auditoría, un correo) sin tocar el servicio.
    2. **Se puede escuchar `AFTER_COMMIT`**, que es la pieza que arregla el bug del paso 3.

    Es la interfaz `Notificador` de la [UT2](../ut2/03-poo-en-java.md) con el acoplamiento aún más bajo: ahora el servicio no conoce ni la interfaz.

## Paso 2 · El `handler`

```java title="websockets/FunkosWebSocketHandler.java"
package es.iesx.funkos.websockets;

import org.slf4j.*;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class FunkosWebSocketHandler extends TextWebSocketHandler {

    private static final Logger log = LoggerFactory.getLogger(FunkosWebSocketHandler.class);

    /** Concurrente a propósito: se recorre y se modifica desde hilos distintos. */
    private final Set<WebSocketSession> sesiones = new CopyOnWriteArraySet<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sesiones.add(session);
        log.info("WS conectado {} · total: {}", session.getId(), sesiones.size());
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sesiones.remove(session);
        log.info("WS desconectado {} ({}) · total: {}",
                 session.getId(), status.getCode(), sesiones.size());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable e) {
        log.warn("WS error en {}: {}", session.getId(), e.getMessage());
        sesiones.remove(session);
    }

    /** Un cliente roto no debe dejar sin mensaje a los demás. */
    public void enviarATodos(String mensaje) {
        for (var s : sesiones) {
            try {
                if (s.isOpen()) s.sendMessage(new TextMessage(mensaje));
                else sesiones.remove(s);
            } catch (IOException e) {
                log.warn("WS no se pudo enviar a {}: {}", s.getId(), e.getMessage());
                sesiones.remove(s);
            }
        }
    }

    public int conectados() { return sesiones.size(); }
}
```

!!! danger "Las tres cosas que hacen que esto no se rompa en producción"
    **1. `CopyOnWriteArraySet`, no `HashSet`.** Cada conexión entra por un hilo distinto, y el *broadcast* recorre la colección mientras alguien se conecta. Con un `HashSet` normal: `ConcurrentModificationException`, el mismo de la [UT2, E18](../ut2/ejercicios.md).

    **2. Quitar la sesión en los tres sitios**: al cerrarse, al dar error de transporte y al fallar el envío. Sin eso, la colección crece para siempre y cada mensaje intenta escribir en sesiones muertas.

    **3. El `try` dentro del bucle.** Sin él, un cliente desconectado lanza `IllegalStateException`, **corta el bucle** y los clientes que vienen después **no reciben nada**. Y `isOpen()` no basta: entre la comprobación y el envío la conexión puede caerse.

## Paso 3 · El notificador, después de confirmar

```java title="websockets/FunkosNotificador.java"
package es.iesx.funkos.websockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import es.iesx.funkos.funkos.dto.FunkoResponse;
import es.iesx.funkos.funkos.events.FunkoCambiadoEvent;
import org.slf4j.*;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.*;

import java.time.LocalDateTime;

@Component
public class FunkosNotificador {

    private static final Logger log = LoggerFactory.getLogger(FunkosNotificador.class);

    private final FunkosWebSocketHandler handler;
    private final ObjectMapper mapper;

    public FunkosNotificador(FunkosWebSocketHandler handler, ObjectMapper mapper) {
        this.handler = handler;
        this.mapper = mapper;
    }

    /** AFTER_COMMIT: solo se notifica lo que se ha guardado de verdad. */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void alCambiar(FunkoCambiadoEvent evento) {
        try {
            handler.enviarATodos(mapper.writeValueAsString(new Notificacion(
                    "FUNKO",
                    evento.tipo().name(),
                    evento.funko(),
                    LocalDateTime.now().toString())));
        } catch (JsonProcessingException e) {
            log.error("No se pudo serializar la notificación", e);
        }
    }

    public record Notificacion(String entidad, String tipo,
                               FunkoResponse datos, String fecha) {}
}
```

!!! danger "`AFTER_COMMIT` arregla un bug que, sin él, es invisible"
    Sin `@TransactionalEventListener(AFTER_COMMIT)`, la notificación sale **antes** de que la transacción se confirme. Si algo falla después, el `rollback` deshace el `INSERT` pero **no deshace el mensaje ya enviado**.

    Resultado: los clientes conectados ven un funko que no existe en la base de datos. Y nadie lo detecta, porque en desarrollo la transacción nunca falla.

!!! tip "Se manda el `FunkoResponse`, no la entidad"
    Dos razones, y las dos son de la UT5:

    1. La entidad **expone campos** que no quieres publicar.
    2. La entidad **arrastra relaciones perezosas**, y aquí estamos **fuera de toda transacción**: serializarla sería un `LazyInitializationException` garantizado dentro del notificador, donde además es más difícil de diagnosticar.

## Paso 4 · Publicar desde el servicio

```java title="funkos/services/FunkosServiceImpl.java (los cambios)"
@Service
@CacheConfig(cacheNames = {"funkos"})
public class FunkosServiceImpl implements FunkosService {

    private final FunkosRepository repositorio;
    private final CategoriasRepository categoriasRepository;
    private final FunkoMapper mapper;
    private final ApplicationEventPublisher eventos;          // ← lo único nuevo

    public FunkosServiceImpl(FunkosRepository repositorio,
                             CategoriasRepository categoriasRepository,
                             FunkoMapper mapper,
                             ApplicationEventPublisher eventos) {
        this.repositorio = repositorio;
        this.categoriasRepository = categoriasRepository;
        this.mapper = mapper;
        this.eventos = eventos;
    }

    @Override
    @Transactional
    @CacheEvict(allEntries = true)
    public FunkoResponse save(FunkoCreateRequest request) {
        if (repositorio.existsByNombreIgnoreCaseAndDeletedFalse(request.nombre()))
            throw new FunkoConflictException("Ya existe un funko llamado " + request.nombre());

        var categoria = categoriasRepository
                .findByNombreIgnoreCaseAndDeletedFalse(request.categoria())
                .orElseThrow(() -> new FunkoBadRequestException(
                        "La categoría " + request.categoria() + " no existe"));

        var respuesta = mapper.toResponse(repositorio.save(mapper.toModel(request, categoria)));
        eventos.publishEvent(new FunkoCambiadoEvent(Tipo.CREATE, respuesta));   // ← nuevo
        return respuesta;
    }

    // igual en update, replace y deleteById, con Tipo.UPDATE y Tipo.DELETE
}
```

!!! success "El diff completo del servicio: 2 líneas"
    Una dependencia en el constructor y un `publishEvent` por cada método de escritura. **La lógica de negocio no se ha tocado.**

    `ApplicationEventPublisher` es un bean que Spring inyecta sin que haya que declararlo en ninguna parte.

## Paso 5 · Registrar el endpoint

```java title="config/WebSocketConfig.java"
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final FunkosWebSocketHandler funkos;
    private final String[] origenes;

    public WebSocketConfig(FunkosWebSocketHandler funkos,
                           @Value("${cors.origenes-permitidos}") String[] origenes) {
        this.funkos = funkos;
        this.origenes = origenes;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(funkos, "/ws/v1/funkos")
                .setAllowedOrigins(origenes);
    }
}
```

!!! danger "`setAllowedOrigins` no es opcional"
    **Los WebSockets no están sujetos a la política del mismo origen** del navegador. Sin esta línea, cualquier página de internet puede abrir una conexión contra tu servidor y escuchar todas las notificaciones.

    Y ojo: `setAllowedOrigins("*")` es lo mismo que no ponerlo.

## Paso 6 · Probarlo

```html title="src/main/resources/static/ws-test.html"
<!doctype html>
<html lang="es">
<head><meta charset="utf-8"><title>Notificaciones de funkos</title></head>
<body>
  <h1>Notificaciones</h1>
  <p id="estado">Conectando…</p>
  <ul id="log"></ul>

  <script>
    const ws = new WebSocket(`ws://${location.host}/ws/v1/funkos`);
    const log = document.getElementById('log');
    const estado = document.getElementById('estado');

    ws.onopen    = () => estado.textContent = 'Conectado';
    ws.onclose   = () => estado.textContent = 'Desconectado';
    ws.onerror   = e  => estado.textContent = 'Error';
    ws.onmessage = e  => {
      const n = JSON.parse(e.data);
      const li = document.createElement('li');
      li.textContent = `${n.fecha} · ${n.tipo} · ${n.datos.nombre}`;
      log.prepend(li);
    };
  </script>
</body>
</html>
```

```bash
# Abre http://localhost:8080/ws-test.html en DOS pestañas, y después:
curl -X POST localhost:8080/api/v1/funkos \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Pikachu","precio":20.0,"cantidad":7,"categoria":"ANIME"}'
```

```
2026-10-07T12:34:56 · CREATE · Pikachu        ← en las dos pestañas, al instante
```

```bash
# Desde la terminal, con wscat
npx wscat -c ws://localhost:8080/ws/v1/funkos
```

!!! success "Comprueba las cuatro cosas"
    - [ ] Las **dos** pestañas reciben la notificación.
    - [ ] Cierras una pestaña, creas otro funko y **el servidor no se queja** (y el log dice `total: 1`).
    - [ ] Un POST que devuelve **409** (nombre repetido) **no genera notificación**.
    - [ ] Un POST que devuelve **400** (validación) tampoco.

    Las dos últimas son `AFTER_COMMIT` haciendo su trabajo.

---


---

## Paso 7 · Los tests

```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class FunkosWebSocketTest {

    @LocalServerPort int puerto;
    @Autowired FunkosService servicio;

    @Test
    void notificaAlCrearUnFunko() throws Exception {
        var recibido = new CompletableFuture<String>();

        var cliente = new StandardWebSocketClient();
        cliente.execute(new TextWebSocketHandler() {
            @Override
            protected void handleTextMessage(WebSocketSession s, TextMessage m) {
                recibido.complete(m.getPayload());
            }
        }, "ws://localhost:" + puerto + "/ws/v1/funkos").get(2, TimeUnit.SECONDS);

        servicio.save(new FunkoCreateRequest(
                "Test WS", new BigDecimal("10.00"), 1, null, "OTROS"));

        var mensaje = recibido.get(3, TimeUnit.SECONDS);
        assertThat(mensaje).contains("CREATE").contains("Test WS");
    }
}
```

!!! tip "Qué prueba este test y qué no"
    Comprueba que **la notificación llega**, que es lo único que un `curl` no te deja automatizar. Lo que **no** prueba es el `AFTER_COMMIT`: para eso hace falta provocar un 409 y comprobar que **no** llega nada.

    ```java
    @Test
    void unPostQueFallaNoNotifica() {
        var recibido = new CompletableFuture<String>();
        // … conectar el cliente …

        assertThrows(FunkoConflictException.class, () -> servicio.save(
                new FunkoCreateRequest("Goku", new BigDecimal("10.00"), 1, null, "ANIME")));

        assertThrows(TimeoutException.class, () -> recibido.get(1, TimeUnit.SECONDS));
    }
    ```

    **Ese `assertThrows(TimeoutException.class)` es el test del `AFTER_COMMIT`:** se espera un segundo y se exige que **no** haya llegado nada.

---

## Checklist

- [ ] Dos pestañas reciben la notificación a la vez
- [ ] Cerrar una pestaña no rompe las notificaciones a la otra, y el log dice `total: 1`
- [ ] Un POST que devuelve **409** (nombre repetido) **no** genera notificación
- [ ] Un POST que devuelve **400** (validación) tampoco
- [ ] `setAllowedOrigins` configurado por perfiles, **sin comodín**
- [ ] Se manda el **DTO**, no la entidad
- [ ] `git diff` del servicio: **solo el `publishEvent`**
- [ ] El test de notificación pasa, y el de `AFTER_COMMIT` también
