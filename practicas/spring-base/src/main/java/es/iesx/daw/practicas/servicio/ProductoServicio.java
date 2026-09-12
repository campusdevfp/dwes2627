package es.iesx.daw.practicas.servicio;

import es.iesx.daw.practicas.modelo.Producto;
import es.iesx.daw.practicas.repositorio.ProductoRepositorio;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * UT4 · P7 — Reglas de negocio. Inyección POR CONSTRUCTOR, campo final.
 * Los tests de arquitectura comprueban que no usas @Autowired sobre campo.
 */
@Service
public class ProductoServicio {

    private final ProductoRepositorio repositorio;

    public ProductoServicio(ProductoRepositorio repositorio) { this.repositorio = repositorio; }

    public List<Producto> listar() { return null; }                    // TODO
    public Producto obtener(Integer id) { return null; }               // TODO · 404 si no existe
    public Producto crear(Producto p) { return null; }                 // TODO · 409 si el nombre ya existe
    public void borrar(Integer id) { }                                 // TODO
    public Producto restarStock(Integer id, int unidades) { return null; }  // TODO · 409 si no hay stock
}
