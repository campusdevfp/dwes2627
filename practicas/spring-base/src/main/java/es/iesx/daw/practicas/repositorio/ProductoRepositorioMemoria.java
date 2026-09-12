package es.iesx.daw.practicas.repositorio;

import es.iesx.daw.practicas.modelo.Producto;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/** UT4 · P6 — Impleméntalo. La precarga ya está: no la toques. */
@Repository
public class ProductoRepositorioMemoria implements ProductoRepositorio {

    private final Map<Integer, Producto> datos = new ConcurrentHashMap<>();
    private final AtomicInteger secuencia = new AtomicInteger(0);

    @PostConstruct
    void precargar() {
        guardar(new Producto(null, "Patinete", "movilidad",  299.00, 4));
        guardar(new Producto(null, "Casco",    "seguridad",   35.50, 20));
        guardar(new Producto(null, "Candado",  "accesorios",  19.95, 15));
        guardar(new Producto(null, "Bici",     "movilidad",  899.00, 2));
        guardar(new Producto(null, "Luz LED",  "accesorios",  12.00, 30));
    }

    @Override public List<Producto> buscarTodos() { return null; }              // TODO
    @Override public Optional<Producto> buscarPorId(Integer id) { return Optional.empty(); }   // TODO
    @Override public Optional<Producto> buscarPorNombre(String n) { return Optional.empty(); } // TODO
    @Override public Producto guardar(Producto p) { return null; }              // TODO
    @Override public boolean borrar(Integer id) { return false; }               // TODO
}
