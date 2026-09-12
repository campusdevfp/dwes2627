package es.iesx.daw.practicas.excepcion;

public class ProductoNoEncontradoException extends RuntimeException {
    public ProductoNoEncontradoException(Integer id) { super("No existe el producto " + id); }
}
