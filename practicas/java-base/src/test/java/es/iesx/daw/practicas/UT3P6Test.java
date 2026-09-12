package es.iesx.daw.practicas;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/** UT3 · P6 — Escritura segura: temporal + renombrado atómico. */
@DisplayName("UT3 · P6 · Escritura de CSV")
class UT3P6Test {

    private final List<Producto> datos = List.of(
            new Producto(1, "Patinete", "movilidad", 299.00, 4),
            new Producto(2, "Casco", "seguridad", 35.50, 20));

    @Test
    @DisplayName("escribe cabecera y una línea por producto")
    void escribe(@TempDir Path dir) throws Exception {
        Path destino = dir.resolve("salida.csv");
        new CatalogoCsv().escribir(destino, datos);

        List<String> lineas = Files.readAllLines(destino);
        assertThat(lineas).hasSize(3);
        assertThat(lineas.get(0)).isEqualTo("id,nombre,categoria,precio,stock");
        assertThat(lineas.get(1)).startsWith("1,Patinete,movilidad,");
    }

    @Test
    @DisplayName("lo escrito se puede volver a leer y da los mismos productos (ida y vuelta)")
    void idaYVuelta(@TempDir Path dir) throws Exception {
        Path destino = dir.resolve("salida.csv");
        var catalogo = new CatalogoCsv();
        catalogo.escribir(destino, datos);

        assertThat(catalogo.leer(destino)).isEqualTo(datos);
    }

    @Test
    @DisplayName("no deja ningún fichero temporal detrás")
    void sinTemporales(@TempDir Path dir) throws Exception {
        Path destino = dir.resolve("salida.csv");
        new CatalogoCsv().escribir(destino, datos);

        try (var ficheros = Files.list(dir)) {
            assertThat(ficheros.map(p -> p.getFileName().toString()))
                    .as("el .tmp tiene que haberse renombrado, no quedarse")
                    .containsExactly("salida.csv");
        }
    }

    @Test
    @DisplayName("crea la carpeta de destino si no existe")
    void creaCarpeta(@TempDir Path dir) throws Exception {
        Path destino = dir.resolve("datos").resolve("salida.csv");
        new CatalogoCsv().escribir(destino, datos);
        assertThat(Files.exists(destino)).isTrue();
    }
}
