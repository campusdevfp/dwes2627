package es.iesx.daw.practicas;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * UT3 · P3 — Leer un CSV con criterio.
 *
 * El fichero de prueba (src/test/resources/productos.csv) trae a propósito
 * una línea en blanco y dos filas inválidas. Ábrelo y míralo.
 */
@DisplayName("UT3 · P3 · Lectura de CSV")
class UT3P3Test {

    private static final Path CSV = Path.of("src", "test", "resources", "productos.csv");

    @Test
    @DisplayName("carga solo las filas válidas y salta la cabecera")
    void cargaFilasValidas() throws Exception {
        List<Producto> productos = new CatalogoCsv().leer(CSV);
        assertThat(productos)
                .as("6 filas de datos, 2 inválidas y 1 línea en blanco")
                .hasSize(4)
                .extracting(Producto::nombre)
                .containsExactly("Patinete", "Casco", "Candado", "Bici");
    }

    @Test
    @DisplayName("cuenta cuántas filas ha descartado")
    void cuentaDescartadas() throws Exception {
        var catalogo = new CatalogoCsv();
        catalogo.leer(CSV);
        assertThat(catalogo.descartadas())
                .as("precio negativo y stock negativo")
                .isEqualTo(2);
    }

    @Test
    @DisplayName("convierte bien los tipos")
    void tipos() throws Exception {
        var primero = new CatalogoCsv().leer(CSV).get(0);
        assertThat(primero.id()).isEqualTo(1);
        assertThat(primero.precio()).isEqualTo(299.00);
        assertThat(primero.stock()).isEqualTo(4);
    }

    @Test
    @DisplayName("un fichero que no existe lanza IOException, no NullPointerException")
    void ficheroInexistente() {
        org.assertj.core.api.Assertions.assertThatThrownBy(
                        () -> new CatalogoCsv().leer(Path.of("no-existe.csv")))
                .isInstanceOf(java.io.IOException.class);
    }
}
