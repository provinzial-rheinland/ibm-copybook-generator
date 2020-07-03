package com.provinzial.ibmcopybookgen;

import org.apache.maven.model.Model;
import org.apache.maven.plugin.Mojo;
import org.apache.maven.plugin.testing.AbstractMojoTestCase;
import org.apache.maven.plugin.testing.MojoRule;
import org.apache.maven.plugin.testing.SilentLog;
import org.apache.maven.project.MavenProject;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import spoon.Launcher;
import spoon.SpoonAPI;
import spoon.reflect.declaration.CtClass;
import spoon.reflect.declaration.ModifierKind;
import spoon.reflect.reference.CtFieldReference;
import spoon.reflect.visitor.filter.AbstractFilter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Optional;

import static org.codehaus.plexus.PlexusTestCase.getTestFile;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/**
 * @author pd05843 (Bastian Hellmann)
 */
public class IBMCopybookGeneratorMojoTest {
    private static final String FOO = "foo", BAR = "bar", QUX = "qux", TEMP = "tmp", JAR = "jar";

    @Rule public MojoRule rule = new MojoRule();

    private File project;

    @Before public void setUp() throws Exception {
        project = File.createTempFile(FOO, TEMP);
        assertThat(project.delete(), is(true));
        assertThat(project.mkdir(), is(true));
    }

    @After public void tearDown() throws Exception {
        assertThat(project.delete(), is(true));
    }

    /**
     * @throws Exception if any
     */
    @Test public void testExecution() throws Exception {
        File sourcePath = Paths.get("src/test/resources/ibm-copybook-generator-test/src/copybook/").toAbsolutePath().toFile();
        File outputDir = Paths.get("target/test-harness/ibm-copybook-generator-test/").toAbsolutePath().toFile();
        InputStream in = IBMCopybookGeneratorMojoTest.class.getResourceAsStream("/ibm-copybook-generator-test/pom.xml");
        if (in == null) {
            throw new AssertionError("Cannot find resource for: " + "ibm-copybook-generator-test/pom.xml");
        }
        try {
            File pom = File.createTempFile("maven", ".pom");
            OutputStream out = new FileOutputStream(pom);
            try {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = in.read(buffer)) != -1) {
                    out.write(buffer, 0, length);
                }
            } finally {
                out.close();
            }
            IBMCopybookGeneratorMojo myMojo = (IBMCopybookGeneratorMojo)rule.lookupConfiguredMojo(new File("src/test/resources/ibm-copybook-generator-test"),"generate");
            assertNotNull(myMojo);
            myMojo.setNameGenerator("com.ibm.recordgen.cobol.JavaNameGenerator");
        myMojo.setOutputDir(outputDir.toString());
            myMojo.execute();
        } finally {
            in.close();
        }
        Path generatedJavaSource = Paths.get(outputDir.toString(), "com", "provinzial", "copystrecke", "GruppeG.java");
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
