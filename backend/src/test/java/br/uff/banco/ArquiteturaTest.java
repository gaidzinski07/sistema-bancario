package br.uff.banco;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Regras de dependência entre camadas (ver docs/plano-migracao.md, seção 3.3).
 * <p>
 * Cada módulo segue {@code api → application → domain}; {@code infra} implementa
 * o acesso a dados. As regras valem para todos os módulos, inclusive os que ainda
 * não existem.
 */
@AnalyzeClasses(packages = "br.uff.banco", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquiteturaTest {

    @ArchTest
    static final ArchRule dominioNaoDependeDeOutrasCamadas = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage("..api..", "..application..", "..infra..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule dominioNaoConheceWebNemSpring = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "org.springframework.web..", "org.springframework.http..", "jakarta.servlet..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule aplicacaoNaoConheceApi = noClasses()
            .that().resideInAPackage("..application..")
            .should().dependOnClassesThat().resideInAPackage("..api..")
            .allowEmptyShould(true);

    @ArchTest
    static final ArchRule apiNaoAcessaInfraDiretamente = noClasses()
            .that().resideInAPackage("..api..")
            .should().dependOnClassesThat().resideInAPackage("..infra..")
            .because("controllers chamam serviços de aplicação, nunca repositórios")
            .allowEmptyShould(true);
}
