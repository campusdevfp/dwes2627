package es.iesx.daw.practicas.controlador;

import es.iesx.daw.practicas.dto.CrearProductoDto;
import es.iesx.daw.practicas.modelo.Producto;
import es.iesx.daw.practicas.servicio.ProductoServicio;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** UT6 · P3 y P4 — API REST. NO cambies las rutas. */
@RestController
@RequestMapping("/api/v1/productos")
public class ProductoControlador {

    private final ProductoServicio servicio;
    public ProductoControlador(ProductoServicio servicio) { this.servicio = servicio; }

    @GetMapping                  public List<Producto> listar() { return null; }          // TODO
    @GetMapping("/{id}")         public Producto obtener(@PathVariable Integer id) { return null; }  // TODO
    @PostMapping                 public ResponseEntity<Producto> crear(@RequestBody CrearProductoDto dto) { return null; }  // TODO 201+Location
    @DeleteMapping("/{id}")      public ResponseEntity<Void> borrar(@PathVariable Integer id) { return null; }  // TODO 204
}
