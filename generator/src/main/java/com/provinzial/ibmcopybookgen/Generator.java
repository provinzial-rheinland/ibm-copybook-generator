package com.provinzial.ibmcopybookgen;

import com.ibm.recordgen.LibraryUtil;
import com.ibm.recordgen.cobol.JavaNameGenerator;
import com.ibm.recordgen.cobol.RecordClassGenerator;
import com.ibm.recordgen.cobol.RecordSpecTreeBuilder;
import com.ibm.recordgen.cobol.StorageSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spoon.Launcher;
import spoon.processing.AbstractProcessor;
import spoon.reflect.code.CtBlock;
import spoon.reflect.code.CtInvocation;
import spoon.support.sniper.SniperJavaPrettyPrinter;

import java.io.*;
import java.nio.charset.Charset;

public class Generator {

    private static final Logger LOGGER = LoggerFactory.getLogger(Generator.class);

    private final File adataFile;

    private final File outputDir;

    private final String symbol;

    private final String packageName;

    private final String className;

    private final boolean generateSetters;

    private final boolean generateCache;

    private final boolean useBufferOffset;

    private final boolean stringTrim;

    private final Charset stringEncoding;

    private final boolean preInitialize;

    private final boolean generateAccessorJavadoc;

    private final boolean generateProtectedFields;

    private final boolean ignoreOccurs1;

    private final JavaNameGenerator nameGenerator;

    private final boolean removeIncrementOffset;

    @SuppressWarnings("squid:S00107")
    public Generator(File adataFile, File outputDir, String symbol, String packageName, String className,
            boolean generateSetters, boolean generateCache, boolean useBufferOffset, boolean stringTrim, Charset stringEncoding,
            boolean preInitialize, boolean generateAccessorJavadoc, boolean generateProtectedFields, boolean ignoreOccurs1, //NOSONAR
            JavaNameGenerator nameGenerator, boolean removeIncrementOffset) {
        this.adataFile = adataFile;
        this.outputDir = outputDir;
        this.symbol = symbol;
        this.packageName = packageName;
        this.className = className;
        this.generateSetters = generateSetters;
        this.generateCache = generateCache;
        this.useBufferOffset = useBufferOffset;
        this.stringTrim = stringTrim;
        this.stringEncoding = stringEncoding;
        this.preInitialize = preInitialize;
        this.generateAccessorJavadoc = generateAccessorJavadoc;
        this.generateProtectedFields = generateProtectedFields;
        this.ignoreOccurs1 = ignoreOccurs1;
        this.nameGenerator = nameGenerator;
        this.removeIncrementOffset = removeIncrementOffset;
    }

    public void generateJavaClassFromAdata() throws IOException {
        RecordClassGenerator recordClassGenerator = createAndInitializeRecordGenerator();
        RecordSpecTreeBuilder specBuilder = new RecordSpecTreeBuilder();
        specBuilder.setSelectedSymbolName(symbol);
        specBuilder.setIgnoreOccurs1(recordClassGenerator.isIgnoreOccurs1());
        StorageSpec specTree = specBuilder.process(this.adataFile.getAbsolutePath());
        String className = recordClassGenerator.getClassName() != null ? recordClassGenerator.getClassName() :
                recordClassGenerator.getNameGenerator().getJavaClassName(specTree.getSymbol());

        File dir = getPackageDirectory(recordClassGenerator.getPackageName(), outputDir);
        if (!dir.exists() && !dir.mkdirs()) {
            LOGGER.error("Could not create output directory {}", dir.getAbsolutePath());
            return;
        }
        File outputFile = getOutputFile(className, dir);
        if (!outputFile.exists() && !outputFile.createNewFile()) {
            LOGGER.error("Could not create output file {}", outputFile.getAbsolutePath());
            return;
        }
        try (BufferedWriter wtr = new BufferedWriter(new FileWriter(outputFile))) {

            recordClassGenerator.setSpecTree(specTree);

            recordClassGenerator.generateJavaSourceOn(wtr);
            if (removeIncrementOffset) {

                removeIncrementOffsetCallsFromFile(
                        getOutputFile(className, getPackageDirectory(recordClassGenerator.getPackageName(), outputDir)));
            }
        }
    }

    private RecordClassGenerator createAndInitializeRecordGenerator() {
        RecordClassGenerator recordClassGenerator = new RecordClassGenerator();
        recordClassGenerator.setClassName(className);
        recordClassGenerator.setPackageName(packageName);
        recordClassGenerator.setUseBufOffset(useBufferOffset);
        recordClassGenerator.setGenSetters(generateSetters);
        recordClassGenerator.setGenCache(generateCache);
        recordClassGenerator.setStringTrimDefault(stringTrim);
        recordClassGenerator.setStringEncoding(stringEncoding.name());
        recordClassGenerator.setPreInitialize(preInitialize);
        recordClassGenerator.setGenAccessorJavadoc(generateAccessorJavadoc);
        recordClassGenerator.setGenProtectedFields(generateProtectedFields);
        recordClassGenerator.setIgnoreOccurs1(ignoreOccurs1);
        recordClassGenerator.setNameGenerator(nameGenerator);
        return recordClassGenerator;
    }

    private void removeIncrementOffsetCallsFromFile(File f) {
        Launcher spoon = new Launcher();
        spoon.getEnvironment().setPrettyPrinterCreator(() -> new SniperJavaPrettyPrinter(spoon.getEnvironment()));
        spoon.getEnvironment().setNoClasspath(true);
        spoon.getEnvironment().setAutoImports(true);
        spoon.getEnvironment().setShouldCompile(false);
        spoon.addInputResource(f.getAbsolutePath());
        spoon.addProcessor(new AbstractProcessor<CtBlock<?>>() {
            @Override public void process(CtBlock<?> block) {
                if (block.getStatements().size() == 1 && block.getStatement(
                        0) instanceof CtInvocation && ((CtInvocation<?>)block.getStatement(
                        0)).getTarget() != null && ((CtInvocation<?>)block.getStatement(0)).getTarget()
                        .toString()
                        .contains("factory") && ((CtInvocation<?>)block.getStatement(
                        0)).getExecutable() != null && ("incrementOffset"
                        .equals(((CtInvocation<?>)block.getStatement(0)).getExecutable()
                        .getSimpleName()))) {
                    block.getParent().delete();
                }

            }
        });
        spoon.setSourceOutputDirectory(outputDir);
        spoon.run();
    }

    private File getOutputFile(String className, File dir) {
        return new File(dir, className + ".java");
    }

    private File getPackageDirectory(String packageName, File dir) {
        packageName = packageName != null ? packageName.replace('.', '/') : "";
        dir = new File(dir, packageName);
        return dir;
    }

    public static Builder forAdataFile(String adataFile) {
        return new Builder(adataFile);
    }

    public File getAdataFile() {
        return adataFile;
    }

    public File getOutputDir() {
        return outputDir;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getPackageName() {
        return packageName;
    }

    public String getClassName() {
        return className;
    }

    public boolean isGenerateSetters() {
        return generateSetters;
    }

    public boolean isGenerateCache() {
        return generateCache;
    }

    public boolean isUseBufferOffset() {
        return useBufferOffset;
    }

    public boolean isStringTrim() {
        return stringTrim;
    }

    public Charset getStringEncoding() {
        return stringEncoding;
    }

    public boolean isPreInitialize() {
        return preInitialize;
    }

    public boolean isGenerateAccessorJavadoc() {
        return generateAccessorJavadoc;
    }

    public boolean isGenerateProtectedFields() {
        return generateProtectedFields;
    }

    public boolean isIgnoreOccurs1() {
        return ignoreOccurs1;
    }

    public JavaNameGenerator getNameGenerator() {
        return nameGenerator;
    }

    static class Builder {
        private final String adataFile;

        private String outputDir;

        private String symbol;

        private String packageName;

        private String className;

        private boolean generateSetters = true;

        private boolean generateCache = true;

        private boolean useBufferOffset = false;

        private boolean stringTrim = false;

        private String stringEncoding = "CP1047";

        private boolean preInitialize = false;

        private boolean generateAccessorJavadoc = false;

        private boolean generateProtectedFields = true;

        private boolean ignoreOccurs1 = false;

        private String nameGenerator = "com.ibm.recordgen.cobol.JavaNameGenerator";

        private boolean removeIncrementOffset = true;

        private Builder(String adataFile) {
            this.adataFile = adataFile;
        }

        public Builder outputDirectory(String pathForOutputDirectory) {
            this.outputDir = pathForOutputDirectory;
            return this;
        }

        public Builder packaName(String packageName) {
            this.packageName = packageName;
            return this;
        }

        public Builder symbol(String symbol) {
            this.symbol = symbol;
            return this;
        }

        public Builder className(String className) {
            this.className = className;
            return this;
        }

        public Builder generateSetters(boolean generateSetters) {
            this.generateSetters = generateSetters;
            return this;
        }

        public Builder generateCache(boolean generateCache) {
            this.generateCache = generateCache;
            return this;
        }

        public Builder useBufferOffset(boolean useBufferOffset) {
            this.useBufferOffset = useBufferOffset;
            return this;
        }

        public Builder stringTrim(boolean stringTrim) {
            this.stringTrim = stringTrim;
            return this;
        }

        public Builder stringEncoding(String stringEncoding) {
            this.stringEncoding = stringEncoding;
            return this;
        }

        public Builder preInitialize(boolean preInitialize) {
            this.preInitialize = preInitialize;
            return this;
        }

        public Builder generateAccessorJavadoc(boolean generateAccessorJavadoc) {
            this.generateAccessorJavadoc = generateAccessorJavadoc;
            return this;
        }

        public Builder generateProtectedFields(boolean generateProtectedFields) {
            this.generateProtectedFields = generateProtectedFields;
            return this;
        }

        public Builder ignoreOccurs1(boolean ignoreOccurs1) {
            this.ignoreOccurs1 = ignoreOccurs1;
            return this;
        }

        public Builder nameGenerator(String nameGenerator) {
            this.nameGenerator = nameGenerator;
            return this;
        }

        public Builder removeIncrementOffset(boolean removeIncrementOffset) {
            this.removeIncrementOffset = removeIncrementOffset;
            return this;
        }

        public Generator build() throws IOException {
            File adataFile = new File(LibraryUtil.resolveLeadingTilde(LibraryUtil.getLibraryMembers(this.adataFile)
                    .stream()
                    .findFirst()
                    .orElseThrow(FileNotFoundException::new)));
            File outputDirecotry = new File(LibraryUtil.resolveLeadingTilde(this.outputDir));
            JavaNameGenerator javaNameGenerator = loadNameGenerator(this.nameGenerator);
            Charset charset = Charset.forName(this.stringEncoding);
            return new Generator(adataFile, outputDirecotry, symbol, packageName, className, generateSetters, generateCache,
                    useBufferOffset, stringTrim, charset, preInitialize, generateAccessorJavadoc, generateProtectedFields,
                    ignoreOccurs1, javaNameGenerator, removeIncrementOffset);
        }

        private JavaNameGenerator loadNameGenerator(String className) {
            try {
                return (JavaNameGenerator)Class.forName(className).newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
                throw new RuntimeException("Could not Load JavaNameGenerator: " + className, e);
            }
        }
    }
}
