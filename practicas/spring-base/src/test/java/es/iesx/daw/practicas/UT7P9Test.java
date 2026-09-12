package es.iesx.daw.practicas;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * UT7 · P9 — La matriz de permisos.
 *
 * Ojo: aquí NO se desactivan los filtros. Es el primer test que ejecuta
 * la seguridad de verdad.
 */
@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("UT7 · P9 · Seguridad")
class UT7P9Test {

    @Autowired MockMvc mvc;
    @Autowired PasswordEncoder encoder;

    @Test @DisplayName("las contraseñas se hashean con BCrypt, no se guardan en claro")
    void contrasenaHasheada() {
        String hash = encoder.encode("secreta123");
        assertThat(hash).isNotEqualTo("secreta123");
        assertThat(encoder.matches("secreta123", hash)).isTrue();
        assertThat(encoder.encode("secreta123"))
                .as("BCrypt lleva sal: dos hashes de la misma contraseña son distintos")
                .isNotEqualTo(hash);
    }

    @Test @DisplayName("el catálogo se lee sin estar autenticado")
    void getEsPublico() throws Exception {
        mvc.perform(get("/api/v1/productos")).andExpect(status().isOk());
    }

    @Test @DisplayName("sin token, borrar da 401 (no sé quién eres)")
    void borrarSinAutenticar() throws Exception {
        mvc.perform(delete("/api/v1/productos/1")).andExpect(status().isUnauthorized());
    }

    @Test @WithMockUser(roles = "USER")
    @DisplayName("con rol USER, borrar da 403 (sé quién eres y no puedes)")
    void borrarConUser() throws Exception {
        mvc.perform(delete("/api/v1/productos/1")).andExpect(status().isForbidden());
    }

    @Test @WithMockUser(roles = "ADMIN")
    @DisplayName("con rol ADMIN, borrar funciona")
    void borrarConAdmin() throws Exception {
        mvc.perform(delete("/api/v1/productos/1")).andExpect(status().isNoContent());
    }
}
