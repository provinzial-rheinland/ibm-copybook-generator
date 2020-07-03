package com.provinzial.ibmcopybookgen;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;

@Mojo(name = "generate", defaultPhase = LifecyclePhase.GENERATE_SOURCES) public class IBMCopybookGeneratorMojo
        extends AbstractMojo {

    private static final Logger LOGGER = LoggerFactory.getLogger(IBMCopybookGeneratorMojo.class);

    @Parameter(required = true) private String adataFile;

    @Parameter(required = true, defaultValue = "${project.build.directory}/generated-sources/copybook") private String
            outputDir;

    @Parameter() private String symbol;

    @Parameter(defaultValue = "mypackage") private String packageName;

    @Parameter() private String className;

    @Parameter(defaultValue = "true") private boolean generateSetters;

    @Parameter(defaultValue = "true") private boolean generateCache;

    @Parameter(defaultValue = "false") private boolean useBufferOffset;

    @Parameter() private boolean stringTrim;

    @Parameter(defaultValue = "IBM-1047") private String stringEncoding;

    @Parameter(defaultValue = "false") private boolean preInitialize;

    @Parameter(defaultValue = "false") private boolean generateAccessorJavadoc;

    @Parameter(defaultValue = "true") private boolean generateProtectedFields;

    @Parameter(defaultValue = "true") private boolean ignoreOccurs1;

    @Parameter(defaultValue = "com.ibm.recordgen.cobol.JavaNameGenerator") private String nameGenerator;

    @Parameter(defaultValue = "true") private boolean removeIncrementOffset;

    @Parameter(defaultValue = "${project}") private MavenProject project;

    @Override public void execute() throws MojoExecutionException, MojoFailureException {
        try {
            Generator generator = Generator.forAdataFile(adataFile)
                    .className(className)
                    .stringEncoding(stringEncoding)
                    .nameGenerator(nameGenerator)
                    .outputDirectory(outputDir)
                    .generateAccessorJavadoc(generateAccessorJavadoc)
                    .generateCache(generateCache)
                    .generateProtectedFields(generateProtectedFields)
                    .generateSetters(generateSetters)
                    .ignoreOccurs1(ignoreOccurs1)
                    .packaName(packageName)
                    .preInitialize(preInitialize)
                    .stringTrim(stringTrim)
                    .symbol(symbol)
                    .useBufferOffset(useBufferOffset)
                    .removeIncrementOffset(removeIncrementOffset)
                    .build();
            generator.generateJavaClassFromAdata();
            this.project.addCompileSourceRoot(Paths.get(outputDir).toAbsolutePath().toString());
        } catch (IOException e) {
            LOGGER.error("Java class could not be generated", e);
            throw new MojoExecutionException("Error generating Class from adatafile.", e);
        }
    }

    public String getAdataFile() {
        return adataFile;
    }

    public void setAdataFile(String adataFile) {
        this.adataFile = adataFile;
    }

    public String getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(String outputDir) {
        this.outputDir = outputDir;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public boolean isGenerateSetters() {
        return generateSetters;
    }

    public void setGenerateSetters(boolean generateSetters) {
        this.generateSetters = generateSetters;
    }

    public boolean isGenerateCache() {
        return generateCache;
    }

    public void setGenerateCache(boolean generateCache) {
        this.generateCache = generateCache;
    }

    public boolean isUseBufferOffset() {
        return useBufferOffset;
    }

    public void setUseBufferOffset(boolean useBufferOffset) {
        this.useBufferOffset = useBufferOffset;
    }

    public boolean isStringTrim() {
        return stringTrim;
    }

    public void setStringTrim(boolean stringTrim) {
        this.stringTrim = stringTrim;
    }

    public String getStringEncoding() {
        return stringEncoding;
    }

    public void setStringEncoding(String stringEncoding) {
        this.stringEncoding = stringEncoding;
    }

    public boolean isPreInitialize() {
        return preInitialize;
    }

    public void setPreInitialize(boolean preInitialize) {
        this.preInitialize = preInitialize;
    }

    public boolean isGenerateAccessorJavadoc() {
        return generateAccessorJavadoc;
    }

    public void setGenerateAccessorJavadoc(boolean generateAccessorJavadoc) {
        this.generateAccessorJavadoc = generateAccessorJavadoc;
    }

    public boolean isGenerateProtectedFields() {
        return generateProtectedFields;
    }

    public void setGenerateProtectedFields(boolean generateProtectedFields) {
        this.generateProtectedFields = generateProtectedFields;
    }

    public boolean isIgnoreOccurs1() {
        return ignoreOccurs1;
    }

    public void setIgnoreOccurs1(boolean ignoreOccurs1) {
        this.ignoreOccurs1 = ignoreOccurs1;
    }

    public String getNameGenerator() {
        return nameGenerator;
    }

    public void setNameGenerator(String nameGenerator) {
        this.nameGenerator = nameGenerator;
    }

    public MavenProject getProject() {
        return project;
    }

    public void setProject(MavenProject project) {
        this.project = project;
    }
}
