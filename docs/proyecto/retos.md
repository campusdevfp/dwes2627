# Los seis retos

Todos tienen la misma estructura, y conviene entenderla antes de empezar:

| Apartado | Qué es |
|---|---|
| **La situación** | El contexto. Dónde estáis y qué se ha roto o qué falta |
| **La pregunta** | El reto de verdad. **No dice qué programar** |
| **Restricciones** | Lo que no se puede hacer. Aquí es donde vive el contenido de la unidad |
| **Criterios de aceptación** | Cómo se sabe que está hecho. Objetivos y comprobables |
| **La demo** | Lo que hay que enseñar el viernes, en cinco minutos |

!!! reto "La diferencia entre un ejercicio y un reto"
    Un ejercicio dice «implementa un filtro `OncePerRequestFilter` que valide el JWT».

    Un reto dice «**el token de un usuario despedido sigue funcionando tres días después. Arregladlo.**» Las dos cosas acaban en el mismo filtro, pero solo en la segunda has tenido que darte cuenta tú.

---

# UT7 · Estado y seguridad

## R1 — ¿Quién está al otro lado?

**Sesiones S1–S10** · Cierra en la semana 2

### La situación

Tenéis la aplicación de la UT6: una API que devuelve datos de Madrid y **que puede llamar cualquiera**. El Ayuntamiento quiere abrirla al público, pero con matices: el ciudadano ve los datos agregados, el técnico municipal ve también las incidencias sin publicar, y solo un administrador puede lanzar una ingesta manual.

### La pregunta

> **¿Cómo sabe vuestra aplicación quién está haciendo cada petición, y cómo se asegura de que el técnico no puede hacer lo del administrador — ni siquiera si conoce la URL?**

### Restricciones

- **HTTP no tiene memoria.** Cada petición llega sin saber nada de la anterior. Eso no se puede cambiar.
- Las contraseñas **no se guardan.** Lo que se guarda es otra cosa; averiguad qué y por qué.
- Nada de comprobar el rol dentro del controlador con un `if`. Si lo hacéis así, el reto está suspenso aunque funcione.
- La respuesta a «no me conoces» y a «te conozco pero no puedes» **no es el mismo código de estado**.

### Criterios de aceptación

- [ ] Un usuario se registra, inicia sesión y su contraseña **no aparece legible** en la base de datos.
- [ ] Existen al menos **tres perfiles** y cada uno ve un conjunto distinto de endpoints.
- [ ] Sin credenciales → **401**. Con credenciales insuficientes → **403**. Comprobado con `curl`.
- [ ] Hay un test automático por cada combinación perfil × endpoint protegido.
- [ ] La configuración de seguridad está **en un sitio**, no repartida.

### La demo

Abrís tres pestañas con tres usuarios distintos y pulsáis el mismo botón. Que se vea que pasan tres cosas diferentes.

---

## R2 — ¿Sesión o token? Demostradlo

**Sesiones S11–S20** · Cierra en la semana 4

### La situación

Funciona. Pero ahora hay dos clientes muy distintos: **la web** que vais a construir en la UT8, que la usa una persona con un navegador; y **una aplicación móvil** de otro equipo que consume vuestra API. El responsable técnico pregunta si se puede usar el mismo mecanismo para los dos.

### La pregunta

> **¿Sesión de servidor o token? Elegid para cada cliente, montad las dos y traed una razón que no sea «es lo moderno».**

### Restricciones

- Las dos puertas tienen que **convivir** en la misma aplicación sin duplicar la lógica de autorización.
- Hay que **medir**, no opinar: cuánto ocupa una sesión en memoria, qué pasa con 500 sesiones abiertas, qué pasa al reiniciar el servidor.
- El token tiene que poder **caducar y renovarse**. Un token eterno es un agujero.
- Prohibido meter datos sensibles en el JWT. Vuestra demo tiene que incluir el momento en que lo abrís con `base64 -d` delante de todos.

### Criterios de aceptación

- [ ] `/api/**` funciona con token; `/**` funciona con sesión; **la misma regla de roles** gobierna las dos.
- [ ] El token caduca y hay un mecanismo de renovación.
- [ ] Un informe de **una página** con las medidas hechas y la decisión justificada por cliente.
- [ ] Explicáis qué pasa con el cierre de sesión en cada mecanismo — y por qué en uno es más difícil.

### La demo

Reiniciad el servidor en directo. Enseñad qué cliente sigue dentro y cuál se ha quedado fuera, y explicad por qué.

---

# UT8 · La cara de la aplicación

## R3 — Esto no lo entiende nadie

**Sesiones S1–S13** · Cierra en la semana 8

### La situación

Lleváis el curso entero devolviendo JSON. Enseñádselo a alguien de otro ciclo: no entenderá nada. La plataforma tiene que poder usarla un vecino de Vallecas que quiere saber si hoy puede salir a correr.

### La pregunta

> **¿Cómo se convierte un JSON en algo que una persona mira y entiende en diez segundos — sin tirar nada de lo que ya está hecho?**

### Restricciones

- **No se empieza de cero y no se toca el servicio.** Se añade un controlador; el servicio, el repositorio y las entidades son los que hay. Si os véis modificando el servicio, parad y preguntad.
- Nada de repetir el HTML. En cuanto haya dos páginas con la misma cabecera, hay una plantilla base.
- Un formulario que se envía y recarga mostrando el mismo mensaje al refrescar **está mal**. Hay un patrón para eso.
- Los errores de validación se ven **junto al campo**, no en una lista al principio.

### Criterios de aceptación

- [ ] La misma aplicación responde en `/api/v1/...` con JSON y en `/...` con HTML.
- [ ] Plantilla base + al menos **tres fragmentos** reutilizados.
- [ ] Un formulario con validación de servidor, mensajes por campo y Post-Redirect-Get.
- [ ] Un listado con **filtro y paginación** que funciona sin JavaScript.
- [ ] Lo que ve el usuario **depende de su rol**, y eso está resuelto en la plantilla, no duplicando páginas.

### La demo

Alguien del equipo navega sin tocar el teclado más que para escribir en el buscador. Si hay que explicar algo mientras se navega, la interfaz no está terminada.

---

## R4 — Que lo use alguien que no seáis vosotros

**Sesiones S14–S26** · Cierra en la semana 12

### La situación

Vuestra aplicación os funciona perfectamente. Como es normal: la habéis hecho vosotros y sabéis dónde hay que pinchar.

### La pregunta

> **Poned vuestra aplicación delante de un equipo del otro grupo, con una tarea concreta y sin ayudarles. ¿Qué pasa, y qué vais a hacer al respecto?**

### Restricciones

- **Prohibido hablar** durante la prueba. Se observa y se apunta. Cada vez que os entren ganas de decir «no, ahí no», eso es un hallazgo: apuntadlo.
- Mínimo **tres personas** probando, con la misma tarea.
- No vale arreglar solo lo fácil. Hay que **priorizar** y justificar qué se arregla y qué no.
- Hay que probar también con el **idioma cambiado** y con la ventana a 400 px de ancho.

### Criterios de aceptación

- [ ] Acta de la prueba: tarea, quién probó, qué falló, cuánto tardaron.
- [ ] Lista priorizada de problemas, con criterio explícito de priorización.
- [ ] Los **tres primeros arreglados**, con el antes y el después.
- [ ] Interfaz en **dos idiomas**, sin cadenas sueltas en las plantillas.
- [ ] Tests de las vistas que cubran lo que se arregló.

### La demo

Enseñad el vídeo o las notas del momento exacto en que alguien se quedó atascado, y después la versión arreglada. Ese contraste es la demo.

---

# UT9 · Datos ajenos y conocimiento

## R5 — Las fuentes mienten y se caen

**Sesiones S1–S17** · Cierra en la semana 16

### La situación

Toca conectarse de verdad. Y las fuentes abiertas son como son: una devuelve XML, otra un CSV con la coma decimal, otra un JSON que cambia de forma sin avisar, y todas se caen los lunes por la mañana.

### La pregunta

> **¿Cómo se construye una plataforma que dependa de cuatro fuentes ajenas y aun así siga en pie cuando las cuatro fallan a la vez?**

### Restricciones

- **Las cuatro bases de datos en marcha**, con su `compose.yaml`, sus `healthcheck` y arranque ordenado. Cada una con el papel que le toca — si acabáis usando solo Postgres, el reto está mal resuelto.
- El JSON crudo se guarda **tal como llegó**, con su fecha. Nunca se pisa.
- Ninguna llamada externa sin **tiempo de espera**. Una llamada sin timeout es una caída esperando a pasar.
- Las claves de API **no pueden estar en el repositorio**. Si aparece una en el historial de Git, el reto está suspenso.
- Segunda llamada idéntica en menos de un minuto → **no sale a Internet**.

### Criterios de aceptación

- [ ] `docker compose up` levanta las cuatro bases y la aplicación, en orden, sin intervención.
- [ ] Ingesta de **al menos tres fuentes** con formatos distintos, contando los registros descartados y por qué.
- [ ] Con todas las fuentes apagadas, la aplicación **arranca y responde** con los últimos datos buenos y un aviso visible.
- [ ] La caché se demuestra midiendo: primera llamada frente a segunda.
- [ ] Un registro de ingesta consultable: cuándo, de dónde, cuántos, cuántos fallaron.

### La demo

Apagad una fuente en directo, con `docker compose stop`. La aplicación tiene que seguir respondiendo. Después apagad Redis y enseñad qué se degrada y qué no.

---

## R6 — ¿Y esto qué nos dice?

**Sesiones S18–S34** · Cierra la última semana

### La situación

Tenéis datos de cuatro fuentes, limpios, actualizados y consultables. Y ahora la pregunta de siempre: ¿y esto para qué sirve?

### La pregunta

> **Encontrad tres cosas ciertas sobre Madrid que no se puedan saber mirando ninguna de las fuentes por separado, demostradlas con vuestros datos y presentadlas para que las entienda un concejal.**

### Restricciones

- Las tres conclusiones tienen que **cruzar al menos dos fuentes**. Una conclusión de una sola fuente no es vuestra: es de la fuente.
- Hay que decir **cuánto os fiáis** y por qué: cuántos datos, de qué periodo, qué se descartó.
- Prohibido confundir correlación con causa. Si vuestro cuadro de mando insinúa una causa que no habéis demostrado, hay que quitarlo.
- Todo lo del trimestre tiene que seguir funcionando: login, roles, vistas, ingesta. **No se entrega una demo aparte.**
- El proyecto se despliega y se documenta: alguien ajeno tiene que poder levantarlo leyendo el `README`.

### Criterios de aceptación

- [ ] Cuadro de mando con las tres conclusiones, cada una con su dato de respaldo.
- [ ] Cada conclusión **cruza dos o más fuentes**, y se explica el cruce.
- [ ] Un apartado de **limitaciones** honesto.
- [ ] API documentada con OpenAPI y navegable.
- [ ] `README` con el que un tercero levanta el proyecto entero, probado por **otro equipo**.
- [ ] Suite de tests en verde, incluyendo los de las unidades anteriores.

### La demo

Diez minutos, no cinco. Presentáis las tres conclusiones como si tuvierais delante a quien decide el presupuesto — y después enseñáis, por debajo, de dónde sale cada número.

---

## Cómo se cierra cada reto

| | |
|---|---|
| **La demo** | Viernes, 5 min, aplicación en marcha, sin diapositivas |
| **La retro** | 10 min. Tres preguntas: qué nos frenó · qué haríamos distinto · qué le pedimos al profe |
| **El registro** | Cada miembro anota en su diario qué hizo y qué aprendió. Es lo que sostiene la [FFE](../index.md) |

!!! tip "Si vais atascados el miércoles"
    Reducid el alcance, no la calidad. Es mejor demostrar **dos cosas terminadas** que cinco a medias — y en la vida profesional también.

    Lo que no vale es llegar al viernes sin nada que enseñar porque estabais «a punto». En este proyecto, «a punto» y «nada» son lo mismo.
