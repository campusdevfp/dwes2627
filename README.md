# DWES · Curso 26/27 · Material del alumno

**Desarrollo Web en Entorno Servidor — Módulo 0613 · 2.º DAW · 160 h · 8 h semanales**

Sitio público con **todo el material de estudio** del módulo: temario, prácticas, ejercicios resueltos, chuletas y preparación de exámenes.

**Las prácticas se comprueban con tests**, no con correctores automáticos ni con IA: en `practicas/` hay dos proyectos Maven donde cada práctica tiene sus tests. Ver [Comprobar tu trabajo](docs/comprobar-tu-trabajo.md).

```bash
pip install -r requirements.txt
mkdocs serve      # http://localhost:8000
mkdocs gh-deploy  # publicar en GitHub Pages
```

## Qué hay dentro

```
docs/
├── index.md          presentación del módulo: unidades, evaluación y calificación
├── ut1/ … ut6/       una carpeta por unidad
│   ├── index.md          calendario de sesiones y cómo se evalúa
│   ├── 01…NN-*.md        el temario
│   ├── practicas.md      prácticas guiadas con solución
│   ├── ejercicios.md     batería de ejercicios tipo examen, resueltos
│   ├── chuleta.md        resumen de una página
│   ├── examen.md         formato, rúbrica y autochequeo
│   └── autoevaluacion.md preguntas de repaso (UT1–UT3)
├── comprobar-tu-trabajo.md   cómo se usan los tests
├── images/           ilustraciones
├── stylesheets/      tema propio (claro y oscuro) y hoja de impresión
└── javascripts/      utilidades de impresión
```

## Unidades

| UT | RA | Contenido | Estado |
|---|---|---|:---:|
| UT1 | RA1 | Arquitecturas y tecnologías web en servidor | :material-check: |
| UT2 | RA2 | Java en el servidor (JDK 25 LTS) | :material-check: |
| UT3 | RA3 | Estructuras, ficheros y JSON | :material-check: |
| UT4 | RA5 | Aplicación por capas con Spring Boot | :material-check: |
| UT6 | RA7 | Servicios web: REST, GraphQL y WebSockets | :material-check: |
| UT7 | RA4 | Estado, sesiones y autenticación | :material-check: |
| UT5 | RA6 | Acceso a datos con JPA | :material-cancel: |
| UT8 | RA8 | Páginas dinámicas en servidor (Thymeleaf) | :material-cancel: |
| UT9 | RA9 | Aplicaciones híbridas y tiempo real | :material-cancel: |

## Prácticas con tests

```
practicas/
├── java-base/     UT2 y UT3 — Java puro, JUnit 5 y AssertJ
└── spring-base/   UT4, UT6 y UT7 — Spring Boot, MockMvc y ArchUnit
```

```bash
cd practicas/java-base
mvn test -Dtest='UT3*'
./verificar.sh UT3
```

## Descargar en PDF

En `/curso-completo/` está todo el temario en una sola página, con índice y las soluciones desplegadas. <kbd>Ctrl</kbd>+<kbd>P</kbd> → Guardar como PDF.

---

*Este repositorio contiene solo material de estudio. Guiones de sesión, solucionarios de examen y calificaciones están en el repositorio privado del profesor.*
