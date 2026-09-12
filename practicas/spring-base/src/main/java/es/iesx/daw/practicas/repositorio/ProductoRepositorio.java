package es.iesx.daw.practicas.repositorio;

import es.iesx.daw.practicas.modelo.Producto;
import java.util.List;
import java.util.Optional;

/** Contrato. NO cambies las firmas: los tests dependen de ellas. */
public interface ProductoRepositorio {
    List<Producto> buscarTodos();
    Optional<Producto> buscarPorId(Integer id);
    Optional<Producto> buscarPorNombre(String nombre);
    Producto guardar(Producto p);
    boolean borrar(Integer id);
}
