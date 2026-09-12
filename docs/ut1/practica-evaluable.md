# Práctica de la unidad (formativa) — Informe de arquitectura: caso HuertoVecino

**UT1 · RA1 · Formativa (no puntúa directamente)** · La nota de la UT sale de un único test (100 %), pero **la sección C del test es este caso**: hacer bien esta práctica ES preparar el examen.

## Contexto

Una cooperativa local, **"HuertoVecino"**, quiere una plataforma web para vender cajas de verdura por suscripción. Necesitan:

- Web pública con catálogo de productos que cambia por temporada.
- Zona de cliente (login) para gestionar la suscripción y ver pedidos.
- Una **app móvil** en el futuro que reutilice los mismos datos.
- Empiezan con **poco presupuesto** y 1 desarrollador, pero esperan crecer.

Te contratan como analista para **elegir y justificar** la arquitectura y las tecnologías. **No hay que programar nada**: es un informe técnico.

## Qué tienes que entregar

Un documento (Markdown o PDF, 2–4 páginas) llamado `informe-ra1-APELLIDO.md` con estas secciones **numeradas**:

1. **Componentes del sistema** — diagrama (Mermaid o imagen) del flujo cliente ↔ servidor web ↔ backend ↔ BD y explicación de cada componente. *(CE1.a)*
2. **Estática vs. dinámica** — justifica por qué esta plataforma necesita generación **dinámica** y qué aporta frente a una web estática. *(CE1.b)*
3. **Arquitectura elegida** — elige entre monolito, capas o microservicios para el arranque **y** explica cómo evolucionarías si crecen. Incluye una tabla comparativa con al menos 3 criterios. *(CE1.g)*
4. **Patrón de organización** — explica cómo aplicarías **MVC** (o capas presentación/lógica/datos) e identifica qué iría en cada parte para el caso HuertoVecino. *(CE1.g)*
5. **Lenguaje y stack** — elige lenguaje y framework de servidor y **justifícalo** (scripting vs. bytecode, ecosistema, futura app móvil). Menciona al menos una alternativa descartada y por qué. *(CE1.c, CE1.d)*
6. **Entorno de ejecución y despliegue** — servidor web vs. de aplicaciones, empaquetado (jar/Docker), dónde desplegar y una nota de **seguridad** (HTTPS, auth). *(CE1.e, CE1.f)*
7. **Decisión final** — párrafo de conclusión: en 5 líneas, qué recomiendas y por qué, coherente con el presupuesto y el crecimiento previsto.

## Requisitos de forma

- Vocabulario técnico correcto del RA1.
- Al menos **un diagrama** y **una tabla comparativa**.
- Cada decisión debe ir **justificada**, no solo enunciada.
- Cita las fuentes/tecnologías reales que menciones.

## Cómo prepararte con ella

1. Resuelve el caso completo y escribe tu informe.
2. Contrástalo con la **rúbrica** de más abajo, criterio a criterio, y sé duro contigo mismo.
3. Compáralo en clase con el de otra pareja: las diferencias son lo que más enseña.
4. Las decisiones que hayas tomado aquí son justo lo que pregunta el test de la unidad.


## Rúbrica (100 %)

| Criterio (CE) | Qué se valora | Peso |
|---|---|---|
| 1. Componentes (CE1.a) | Diagrama correcto y componentes bien explicados | 15 % |
| 2. Estática vs dinámica (CE1.b) | Justifica bien la necesidad de generación dinámica | 15 % |
| 3. Arquitectura (CE1.g) | Elección razonada + evolución + tabla comparativa | 20 % |
| 4. Patrón MVC/capas (CE1.g) | Reparto correcto de responsabilidades al caso | 15 % |
| 5. Lenguaje y stack (CE1.c, CE1.d) | Justificación sólida + alternativa descartada | 15 % |
| 6. Ejecución y despliegue (CE1.e, CE1.f) | Servidor web vs. aplicaciones, empaquetado, seguridad | 10 % |
| 7. Decisión final y coherencia | Conclusión clara y coherente con el contexto | 10 % |

**Referencia:** ≥ 80 % = preparado para el test · 60–79 % = repasa lo señalado · < 60 % = rehaz con el feedback.
