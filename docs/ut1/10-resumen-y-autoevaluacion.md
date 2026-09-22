# Resumen de la unidad

!!! info "Qué de esto entra en el test"
    Entra lo de los **temas 1 a 6**. Las ideas 9 y 10, el glosario de despliegue y las dos últimas casillas de la comprobación vienen de los temas 7, 8 y 9, que son **divulgación**: ordenan el panorama y no se preguntan.

    Para entrenar de verdad: la [batería de ejercicios](ejercicios.md) y el [simulacro](autoevaluacion.md).

### Las 10 ideas del RA1

1. Una web es un baile de componentes: navegador → DNS → servidor web → backend → BD, y vuelta.
2. Cliente = presentación; servidor = lógica, datos y seguridad. **Nunca confíes en el cliente.**
3. No hay arquitectura "mejor": monolito para empezar, microservicios cuando la escala obliga.
4. **MVC** organiza la app; la lógica va en Modelo/Servicios, no en el controlador.
5. **HTTP** es sin estado; cookies y tokens le dan memoria. Métodos = CRUD; 4xx tú, 5xx yo.
6. **HTTPS** cifra con TLS y certificados. No es opcional.
7. El backend moderno es **universal**: una API JSON para web, móvil y lo que venga.
8. Dinámica = generada en el servidor según datos (SSR); la SPA lo genera en el cliente.
9. Java (JDK 25 LTS)/Spring: bytecode en la JVM, estándar de industria. El lenguaje se elige por el problema.
10. Desplegar es parte del trabajo: Docker, nube, CI/CD… y **mirar los logs**.

### Mini-glosario

| Término | En una frase |
|---------|--------------|
| API | Contrato por el que dos programas se hablan (HTTP + JSON normalmente) |
| Backend | Código que corre en el servidor: lógica, datos, seguridad |
| CDN | Copias de tus estáticos repartidas por el mundo |
| CI/CD | Tests y despliegue automáticos en cada push |
| Contenedor (Docker) | Tu app empaquetada con todas sus dependencias |
| DNS | Traduce dominios a IPs |
| JWT | Token firmado que acredita tu identidad en cada petición |
| MVC | Patrón Modelo (datos) – Vista (presenta) – Controlador (orquesta) |
| Proxy inverso | Servidor web delante del backend: TLS, estáticos, balanceo |
| SSR / SPA | HTML generado en el servidor / en el navegador |
| Stateless | Sin estado: cada petición HTTP es independiente |
| VirtualHost | Varias webs en un mismo servidor/IP |

### Autocomprobación final

**Checklist** — deberías poder explicar con tus palabras:

- [ ] El viaje completo de una petición, componente a componente. *(§1)*
- [ ] Dónde va cada funcionalidad (cliente/servidor) y la regla de oro. *(§2)*
- [ ] Monolito vs. capas vs. microservicios vs. serverless, con un caso de uso de cada. *(§3)*
- [ ] El flujo MVC y qué es un Fat Controller. *(§3)*
- [ ] Métodos y códigos HTTP; qué añade HTTPS; qué es un JWT y su trampa. *(§4)*
- [ ] Estática vs. dinámica; SSR vs. SPA; los 3 tipos de ejecución de lenguajes. *(§5)*
- [ ] Autenticación vs. autorización (401 vs. 403) y qué lleva dentro un JWT. *(§5)*
- [ ] ~~Servidor web vs. de aplicaciones, Docker, logs~~ — divulgación, no entra en el test.

**Diez preguntas** (soluciones abajo):

1. Tu compañero: "la validación ya está en JavaScript, no la repito en el servidor". ¿Qué le dices?
2. Ordena: base de datos, DNS, navegador, backend, servidor web.
3. ¿Por qué la segunda visita a una web carga más rápido?
4. Una startup con 1 dev y 3 semanas de plazo: ¿monolito o microservicios? ¿Y un banco con 12 equipos?
5. En MVC, ¿dónde va "calcular el descuento del carrito"? ¿Y "pintar el precio en rojo si hay oferta"?
6. ¿Qué diferencia hay entre un 401 y un 403?
7. ¿Puede cualquiera **leer** el payload de un JWT? ¿Puede **modificarlo**?
8. Web con animaciones espectaculares pero igual para todos los visitantes: ¿estática o dinámica?
9. Diseña las rutas para listar libros, ver uno y filtrarlos por autor.
10. Un `POST` falla por un corte de red. ¿Puede el cliente reintentarlo sin riesgo? ¿Y un `PUT`?

??? success "Soluciones"

    1. La validación JS se salta con F12 o `curl`; es solo UX. El servidor **debe** revalidar: es la única fiable.
    2. Navegador → DNS → servidor web → backend → base de datos (y la respuesta deshace el camino).
    3. Por la **caché**: recursos reutilizados o respondidos con `304 Not Modified`.
    4. Startup: **monolito** (velocidad, simplicidad). Banco: **microservicios** (equipos autónomos, despliegue independiente, fallos aislados).
    5. El descuento: **Modelo/Servicio** (lógica de negocio). El color rojo: **Vista** (presentación).
    6. **401** = no autenticado (no sé quién eres). **403** = autenticado pero sin permiso (sé quién eres y no puedes).
    7. Leerlo **sí** (Base64 no es cifrado). Modificarlo **no** sin invalidar la firma.
    8. **Estática.** Dinámica = generada en el servidor según datos, no "que se mueva".
    9. `GET /api/v1/libros` · `GET /api/v1/libros/{id}` · `GET /api/v1/libros?autor=Saramago`. Sustantivos en plural, el filtro en la consulta y la versión en la ruta.
    10. El `PUT` **sí**: es idempotente, repetirlo deja el mismo estado. El `POST` **no**: puede crear el pedido dos veces. Por eso existe el aviso de «no pulse dos veces».


---

*Material original del módulo DWES (0613) · 2.º DAW · Madrid · Curso 2026-2027. Ilustraciones propias (SVG). Como segunda lectura opcional puede usarse el [repo de J.L. González](https://github.com/joseluisgs/DesarrolloWebEntornosServidor-01-2025-2026) (CC BY-NC-SA).*
