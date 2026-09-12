package es.iesx.daw.practicas;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

/** UT3 · P3 y P5 — lectura y escritura de CSV. */
public class CatalogoCsv {

    private int descartadas = 0;

    /** Filas descartadas en la última lectura. */
    public int descartadas() { return descartadas; }

    /**
     * Lee el CSV a una lista de productos.
     * Salta la cabecera, ignora líneas en blanco y descarta —contándolas—
     * las filas con precio <= 0 o stock < 0.
     */
    public List<Producto> leer(Path ruta) throws IOException {
        return null;   // TODO UT3·P3
    }

    /** Escribe de forma segura: temporal + renombrado atómico. */
    public void escribir(Path ruta, List<Producto> productos) throws IOException {
        // TODO UT3·P6
    }
}
