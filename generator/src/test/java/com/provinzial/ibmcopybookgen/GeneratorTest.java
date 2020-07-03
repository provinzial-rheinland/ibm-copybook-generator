package com.provinzial.ibmcopybookgen;

import org.apache.commons.io.FileDeleteStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import spoon.Launcher;
import spoon.SpoonAPI;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.visitor.filter.AbstractFilter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class GeneratorTest {

    private Generator generator;

    public static final String PATH_TO_OUT_DIR = "target/generated-test-sources/copybook";

    @BeforeEach void setup() throws IOException {
        File outputDirectory = new File(PATH_TO_OUT_DIR);
        if (outputDirectory.exists()) {
            FileDeleteStrategy.FORCE.delete(outputDirectory);
        }
    }

    @Test void generateJavaClassFromAdata() throws IOException {
        String pathToAdataFile = "src/test/resources/SCH009N1.adata";
        Generator generator = Generator.forAdataFile(pathToAdataFile).outputDirectory(PATH_TO_OUT_DIR).build();
        generator.generateJavaClassFromAdata();
        Path generatedJavaSource = Paths.get(PATH_TO_OUT_DIR, "GruppeG.java");
        assertTrue(generatedJavaSource.toFile().exists());
        SpoonAPI spoon = new Launcher();
        spoon.addInputResource(generatedJavaSource.toString());
        spoon.buildModel();
        Optional<? extends CtClass> first =
                spoon.getFactory().Package().getRootPackage().getElements(new AbstractFilter<CtClass>() {
                    @Override public boolean matches(CtClass element) {
                        return element.getSimpleName().equals("GruppeG");
                    }
                }).stream().findFirst();
        assertTrue(first.isPresent());
        CtClass gruppeGClass = first.get();
        CtFieldReference<?> grVar1X = gruppeGClass.getDeclaredField("grVar1X");
        assertNotNull(grVar1X);
        assertTrue(grVar1X.getModifiers().contains(ModifierKind.PROTECTED));
        assertEquals("String", grVar1X.getFieldDeclaration().getType().getSimpleName());
        CtFieldReference<?> grVar2D = gruppeGClass.getDeclaredField("grVar2D");
        assertNotNull(grVar2D);
        assertTrue(grVar2D.getModifiers().contains(ModifierKind.PROTECTED));
        assertEquals("Long", grVar2D.getFieldDeclaration().getType().getSimpleName());
        CtFieldReference<?> grVar3P = gruppeGClass.getDeclaredField("grVar3P");
        assertNotNull(grVar3P);
        assertTrue(grVar3P.getModifiers().contains(ModifierKind.PROTECTED));
        assertEquals("BigDecimal", grVar3P.getFieldDeclaration().getType().getSimpleName());
    }
}