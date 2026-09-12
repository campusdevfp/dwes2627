package es.iesx.daw.practicas;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** UT6 · P3 — La API y sus códigos de estado. */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)   // sin seguridad: eso es la UT7
@DisplayName("UT6 · P3 · API REST")
class UT6P3Test {

    @Autowired MockMvc mvc;

    @Test @DisplayName("GET del listado devuelve 200 con los 5 productos")
    void listar() throws Exception {
        mvc.perform(get("/api/v1/productos"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$", hasSize(5)));
    }

    @Test @DisplayName("GET por id devuelve 200 con el producto")
    void obtener() throws Exception {
        mvc.perform(get("/api/v1/productos/2"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$.nombre").value("Casco"));
    }

    @Test @DisplayName("GET de un id inexistente devuelve 404, no 500")
    void obtener404() throws Exception {
        mvc.perform(get("/api/v1/productos/9999")).andExpect(status().isNotFound());
    }

    @Test @DirtiesContext @DisplayName("POST válido devuelve 201 con la cabecera Location")
    void crear201() throws Exception {
        mvc.perform(post("/api/v1/productos").contentType(MediaType.APPLICATION_JSON).content("""
                    {"nombre":"Timbre","categoria":"accesorios","precio":8.5,"stock":12}"""))
           .andExpect(status().isCreated())
           .andExpect(header().string("Location", containsString("/api/v1/productos/")));
    }

    @Test @DirtiesContext @DisplayName("POST con nombre repetido devuelve 409")
    void crear409() throws Exception {
        mvc.perform(post("/api/v1/productos").contentType(MediaType.APPLICATION_JSON).content("""
                    {"nombre":"Casco","categoria":"seguridad","precio":10.0,"stock":1}"""))
           .andExpect(status().isConflict());
    }

    @Test @DirtiesContext @DisplayName("DELETE devuelve 204 y sin cuerpo")
    void borrar204() throws Exception {
        mvc.perform(delete("/api/v1/productos/3"))
           .andExpect(status().isNoContent())
           .andExpect(content().string(""));
    }
}
