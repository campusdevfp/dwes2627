package es.iesx.daw.practicas;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.*;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

/**
 * Estos tests no miran lo que hace tu código, sino cómo está organizado.
 * Son los que impiden aprobar metiéndolo todo en el controlador.
 */
@DisplayName("Arquitectura · las capas")
class ArquitecturaTest {

    private static final String BASE = "es.iesx.daw.practicas";
    private static JavaClasses clases;

    @BeforeAll
    static void importar() {
        clases = new ClassFileImporter()
                .withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages(BASE);
    }

    @Test @DisplayName("las dependencias van en un solo sentido: controlador → servicio → repositorio")
    void capas() {
        layeredArchitecture().consideringOnlyDependenciesInLayers()
                .layer("Controlador").definedBy(BASE + ".controlador..")
                .layer("Servicio").definedBy(BASE + ".servicio..")
                .layer("Repositorio").definedBy(BASE + ".repositorio..")
                .whereLayer("Controlador").mayNotBeAccessedByAnyLayer()
                .whereLayer("Servicio").mayOnlyBeAccessedByLayers("Controlador")
                .whereLayer("Repositorio").mayOnlyBeAccessedByLayers("Servicio")
                .check(clases);
    }

    @Test @DisplayName("el controlador no usa el repositorio directamente")
    void controladorNoTocaRepositorio() {
        noClasses().that().resideInAPackage(BASE + ".controlador..")
                .should().dependOnClassesThat().resideInAPackage(BASE + ".repositorio..")
                .allowEmptyShould(true).check(clases);
    }

    @Test @DisplayName("inyección por constructor: ningún campo lleva @Autowired")
    void sinAutowiredEnCampos() {
        noFields().should()
                .beAnnotatedWith(org.springframework.beans.factory.annotation.Autowired.class)
                .allowEmptyShould(true).check(clases);
    }

    @Test @DisplayName("las dependencias son final")
    void dependenciasFinales() {
        fields().that().areDeclaredInClassesThat().resideInAPackage(BASE + ".servicio..")
                .or().areDeclaredInClassesThat().resideInAPackage(BASE + ".controlador..")
                .and().areNotStatic()
                .should().beFinal().check(clases);
    }

    @Test @DisplayName("nadie escribe por consola")
    void sinSystemOut() {
        com.tngtech.archunit.library.GeneralCodingRules
                .NO_CLASSES_SHOULD_ACCESS_STANDARD_STREAMS
                .allowEmptyShould(true).check(clases);
    }

    @Test @DisplayName("el DTO de creación no acepta 'id': lo decide el servidor")
    void dtoSinId() {
        var nombres = java.util.Arrays.stream(
                        es.iesx.daw.practicas.dto.CrearProductoDto.class.getRecordComponents())
                .map(java.lang.reflect.RecordComponent::getName).toList();
        org.assertj.core.api.Assertions.assertThat(nombres)
                .as("aceptar el id desde el cliente es 'mass assignment'")
                .doesNotContain("id");
    }
}
