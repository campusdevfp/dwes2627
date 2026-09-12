package es.iesx.daw.practicas;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/** UT6 · P4 — Validación y errores en formato ProblemDetail. */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@DisplayName("UT6 · P4 · Validación y errores")
class UT6P4Test {

    @Autowired MockMvc mvc;

    @Test @DisplayName("los errores usan application/problem+json")
    void tipoDeContenido() throws Exception {
        mvc.perform(get("/api/v1/productos/9999"))
           .andExpect(content().contentTypeCompatibleWith("application/problem+json"));
    }

    @Test @DisplayName("el 404 trae title, status y detail")
    void camposDelProblema() throws Exception {
        mvc.perform(get("/api/v1/productos/9999"))
           .andExpect(jsonPath("$.title", not(emptyOrNullString())))
           .andExpect(jsonPath("$.status").value(404))
           .andExpect(jsonPath("$.detail", not(emptyOrNullString())));
    }

    @Test @DisplayName("nunca se filtra la traza de la excepción")
    void sinTraza() throws Exception {
        mvc.perform(get("/api/v1/productos/9999"))
           .andExpect(content().string(not(containsString("at es.iesx"))))
           .andExpect(content().string(not(containsString("stackTrace"))));
    }

    @Test @DisplayName("nombre vacío y precio negativo devuelven 400")
    void validacion400() throws Exception {
        mvc.perform(post("/api/v1/productos").contentType(MediaType.APPLICATION_JSON).content("""
                    {"nombre":"","categoria":"seguridad","precio":-5,"stock":1}"""))
           .andExpect(status().isBadRequest());
    }

    @Test @DisplayName("una categoría que no existe devuelve 400")
    void categoriaInvalida() throws Exception {
        mvc.perform(post("/api/v1/productos").contentType(MediaType.APPLICATION_JSON).content("""
                    {"nombre":"Algo","categoria":"inventada","precio":10,"stock":1}"""))
           .andExpect(status().isBadRequest());
    }

    @Test @DisplayName("un JSON malformado devuelve 400, no 500")
    void jsonMalformado() throws Exception {
        mvc.perform(post("/api/v1/productos").contentType(MediaType.APPLICATION_JSON)
                    .content("{esto no es json}"))
           .andExpect(status().isBadRequest());
    }
}
