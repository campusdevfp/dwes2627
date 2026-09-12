#!/usr/bin/env python3
"""Crea el esqueleto de un ejercicio nuevo con sus tests.

    python3 nuevo-ejercicio.py UT3 P7 "Inventario de una carpeta"
    python3 nuevo-ejercicio.py UT6 P6 "Filtros combinables" --spring
    python3 nuevo-ejercicio.py --estado          ← qué prácticas tienen tests

El proyecto (java-base o spring-base) se deduce de la unidad; --spring y --java
lo fuerzan. Si el fichero ya existe no se toca: hay que borrarlo a mano.
"""
import argparse, re, sys
from pathlib import Path

AQUI = Path(__file__).parent
PROYECTO = {"UT2": "java-base", "UT3": "java-base",
            "UT4": "spring-base", "UT5": "spring-base",
            "UT6": "spring-base", "UT7": "spring-base"}
PAQUETE = "es/iesx/daw/practicas"

CAB = '''package es.iesx.daw.practicas;

{imports}
/**
 * {ut} · {p} — {titulo}
 *
 * QUÉ SE PIDE
 *   TODO profesor: describe aquí en 2-3 líneas qué tiene que implementar el alumno.
 *   Este bloque es el enunciado: el alumno lo lee antes de programar.
 */
@DisplayName("{ut} · {p} · {titulo}")
class {clase} {{
{cuerpo}}}
'''

IMPORTS_JAVA = '''import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

'''

IMPORTS_SPRING = '''import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

'''

CUERPO_JAVA = '''
    @Test
    @DisplayName("el caso normal funciona")
    void casoNormal() {
        // TODO profesor: prepara los datos y comprueba el resultado esperado.
        //   assertThat(sujeto.metodo(entrada)).isEqualTo(esperado);
        fail("test sin escribir");
    }

    @Test
    @DisplayName("el caso límite también")
    void casoLimite() {
        // TODO profesor: el valor justo en la frontera (0, vacío, el último…).
        fail("test sin escribir");
    }

    @Test
    @DisplayName("la entrada inválida lanza la excepción esperada, con un mensaje útil")
    void entradaInvalida() {
        // assertThatThrownBy(() -> sujeto.metodo(malo))
        //         .isInstanceOf(IllegalArgumentException.class)
        //         .hasMessageContaining("el valor recibido");
        fail("test sin escribir");
    }

    @Test
    @DisplayName("no se modifica lo que no se debe modificar")
    void sinEfectosColaterales() {
        // TODO profesor: comprueba que el original queda intacto, que no se
        //   guardó nada tras un fallo, o que la lista devuelta es inmutable.
        fail("test sin escribir");
    }
'''

CUERPO_SPRING = '''
    @Autowired MockMvc mvc;

    @Test
    @DisplayName("el caso correcto devuelve 200 con los datos esperados")
    void casoCorrecto() throws Exception {
        mvc.perform(get("/api/v1/productos"))
           .andExpect(status().isOk());
        // .andExpect(jsonPath("$", hasSize(5)))
        fail("test sin escribir");
    }

    @Test
    @DirtiesContext
    @DisplayName("la operación que escribe deja el estado como toca")
    void operacionQueEscribe() throws Exception {
        // @DirtiesContext porque este test modifica el repositorio en memoria:
        // sin él contaminaría a los siguientes.
        fail("test sin escribir");
    }

    @Test
    @DisplayName("el error devuelve el código correcto, no un 500")
    void error() throws Exception {
        mvc.perform(get("/api/v1/productos/9999"))
           .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("la validación corta antes de llegar al servicio")
    void validacion() throws Exception {
        // mvc.perform(post(...).content(cuerpoInvalido))
        //    .andExpect(status().isBadRequest());
        fail("test sin escribir");
    }
'''


def ruta_test(proyecto, clase):
    return AQUI / proyecto / "src/test/java" / PAQUETE / f"{clase}.java"


def crear(ut, p, titulo, spring=None):
    ut, p = ut.upper(), p.upper()
    if not re.fullmatch(r"UT\d", ut):  sys.exit(f"Unidad no válida: {ut}")
    if not re.fullmatch(r"P\d+", p):   sys.exit(f"Práctica no válida: {p}")

    proyecto = ("spring-base" if spring else "java-base") if spring is not None \
               else PROYECTO.get(ut)
    if proyecto is None:
        sys.exit(f"No sé a qué proyecto va {ut}. Usa --java o --spring.")

    clase = f"{ut}{p}Test"
    destino = ruta_test(proyecto, clase)
    if destino.exists():
        sys.exit(f"Ya existe {destino.relative_to(AQUI)}. Bórralo si quieres rehacerlo.")

    esSpring = proyecto == "spring-base"
    contenido = CAB.format(
        imports=IMPORTS_SPRING if esSpring else IMPORTS_JAVA,
        ut=ut, p=p, titulo=titulo, clase=clase,
        cuerpo=(("@SpringBootTest\n@AutoConfigureMockMvc(addFilters = false)\n" if esSpring else "") and "")
               + (CUERPO_SPRING if esSpring else CUERPO_JAVA))
    if esSpring:
        contenido = contenido.replace(f'@DisplayName("{ut} · {p} · {titulo}")',
            f'@SpringBootTest\n@AutoConfigureMockMvc(addFilters = false)\n@DisplayName("{ut} · {p} · {titulo}")')

    destino.parent.mkdir(parents=True, exist_ok=True)
    destino.write_text(contenido, encoding="utf-8")
    print(f"  creado  {destino.relative_to(AQUI)}")
    print(f"  ejecuta con:  cd {proyecto} && mvn test -Dtest='{clase}'")
    print("\n  Los 4 tests fallan a propósito (`fail(\"test sin escribir\")`).")
    print("  Escríbelos y quita esas líneas; el alumno los recibe ya en rojo.")


def estado():
    print("\n  PRÁCTICAS CON TESTS\n")
    for proyecto in ("java-base", "spring-base"):
        d = AQUI / proyecto / "src/test/java" / PAQUETE
        clases = sorted((f.stem for f in d.glob("UT*Test.java")),
                        key=lambda c: (c[:3], int(c[4:-4]))) if d.is_dir() else []
        print(f"  {proyecto}")
        if not clases:
            print("     (ninguna)")
        for ut in sorted({c[:3] for c in clases}):
            practicas = sorted((c[3:-4] for c in clases if c.startswith(ut)),
                               key=lambda x: int(x[1:]))
            print(f"     {ut}: {', '.join(practicas)}   ({len(practicas)} prácticas)")
        print()
    print("  Los bancos del profesor tienen 15 prácticos por RA (P1–P15).")
    print("  P14 y P15 son de examen: no se publican al alumno.\n")


if __name__ == "__main__":
    ap = argparse.ArgumentParser(description="Crea el esqueleto de un ejercicio con sus tests")
    ap.add_argument("unidad", nargs="?", help="UT2 … UT7")
    ap.add_argument("practica", nargs="?", help="P1, P2, …")
    ap.add_argument("titulo", nargs="?", help="Título corto del ejercicio")
    ap.add_argument("--spring", action="store_true", help="forzar spring-base")
    ap.add_argument("--java", action="store_true", help="forzar java-base")
    ap.add_argument("--estado", action="store_true", help="ver qué prácticas tienen tests")
    a = ap.parse_args()

    if a.estado or not a.unidad:
        estado()
    else:
        if not (a.practica and a.titulo):
            sys.exit("Faltan argumentos: unidad, práctica y título.")
        crear(a.unidad, a.practica, a.titulo,
              spring=True if a.spring else (False if a.java else None))
