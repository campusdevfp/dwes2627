package es.iesx.daw.practicas.excepcion;

public class ProductoDuplicadoException extends RuntimeException {
    public ProductoDuplicadoException(String nombre) {
        super("Ya existe un producto llamado '" + nombre + "'");
    }
}
