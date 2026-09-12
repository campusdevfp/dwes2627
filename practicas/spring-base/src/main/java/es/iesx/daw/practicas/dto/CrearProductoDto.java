package es.iesx.daw.practicas.dto;

/**
 * UT4 · P10 — Añade TÚ las anotaciones de validación:
 *   nombre     obligatorio, 2 a 100 caracteres
 *   categoria  obligatoria, una de: movilidad, seguridad, accesorios
 *   precio     estrictamente positivo
 *   stock      cero o positivo
 *
 * OJO: no declares 'id'. Lo decide el servidor.
 */
public record CrearProductoDto(String nombre, String categoria, Double precio, Integer stock) {}
