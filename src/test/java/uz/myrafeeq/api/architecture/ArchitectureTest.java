package uz.myrafeeq.api.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ArchitectureTest {

  private static JavaClasses classes;

  @BeforeAll
  static void importClasses() {
    classes =
        new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("uz.myrafeeq.api");
  }

  @Test
  void controllers_should_not_access_repositories() {
    noClasses()
        .that()
        .resideInAPackage("..controller..")
        .should()
        .accessClassesThat()
        .resideInAPackage("..repository..")
        .check(classes);
  }

  @Test
  void services_should_not_access_controllers() {
    noClasses()
        .that()
        .resideInAPackage("..service..")
        .should()
        .accessClassesThat()
        .resideInAPackage("..controller..")
        .check(classes);
  }

  @Test
  void no_cyclic_dependencies_between_packages() {
    // configuration package excluded: it naturally cross-cuts (wires up beans from security
    // while security uses properties from configuration)
    JavaClasses classesWithoutConfig =
        new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .withImportOption(location -> !location.contains("/configuration/"))
            .importPackages("uz.myrafeeq.api");

    slices()
        .matching("uz.myrafeeq.api.(*)..")
        .should()
        .beFreeOfCycles()
        .check(classesWithoutConfig);
  }
}
