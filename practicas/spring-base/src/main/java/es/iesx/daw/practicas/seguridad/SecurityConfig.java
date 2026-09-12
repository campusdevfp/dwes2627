package es.iesx.daw.practicas.seguridad;

import org.springframework.context.annotation.Configuration;

/**
 * UT7 · P8 y P9 — Seguridad.
 *
 * Hasta la UT6 los tests desactivan los filtros. A partir de aquí ya no:
 * los de UT7 comprueban 401, 403 y contraseñas hasheadas.
 *
 * Tienes que declarar:
 *   - PasswordEncoder (BCrypt)
 *   - SecurityFilterChain con la matriz de permisos:
 *       GET  /api/v1/**   público
 *       POST /api/v1/**   autenticado
 *       DELETE /api/v1/** solo ADMIN
 *   - UserDetailsService con admin/ADMIN y user/USER
 */
@Configuration
public class SecurityConfig {
    // TODO UT7·P8 y P9
}
