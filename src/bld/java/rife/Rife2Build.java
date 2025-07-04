/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import rife.bld.BuildCommand;
import rife.bld.dependencies.VersionNumber;
import rife.bld.operations.JarOperation;
import rife.bld.operations.JavacOptions;
import rife.bld.publish.*;
import rife.tools.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.regex.Pattern;

import static rife.bld.dependencies.Scope.provided;
import static rife.bld.dependencies.Scope.test;
import static rife.bld.operations.TemplateType.*;

public class Rife2Build extends AbstractRife2Build {
    public Rife2Build()
    throws Exception {
        pkg = "rife";
        name = "RIFE2";
        version = VersionNumber.parse(FileUtils.readString(new File(srcMainResourcesDirectory(), "RIFE_VERSION")));

        var imagej_version = version("1.54p");
        var jetty_version = version(12,0,21);
        var jsoup_version = version(1,21,1);
        var tomcat_version = version(11,0,7);

        scope(provided)
            .include(module("org.jsoup", "jsoup", jsoup_version))
            .include(module("org.eclipse.jetty.ee10", "jetty-ee10", jetty_version))
            .include(module("org.eclipse.jetty.ee10", "jetty-ee10-servlet", jetty_version))
            .include(module("org.apache.tomcat.embed", "tomcat-embed-core", tomcat_version).excludeSources())
            .include(module("org.apache.tomcat.embed", "tomcat-embed-jasper", tomcat_version))
            .include(module("net.imagej", "ij", imagej_version).excludeSources());
        scope(test)
            .include(dependency("org.junit-pioneer", "junit-pioneer", version(2,3,0)))
            .include(dependency("org.jsoup", "jsoup", jsoup_version))
            .include(dependency("org.eclipse.jetty.ee10", "jetty-ee10", jetty_version))
            .include(dependency("org.eclipse.jetty.ee10", "jetty-ee10-servlet", jetty_version))
            .include(dependency("net.imagej", "ij", imagej_version).excludeSources());

        var core_directory = new File(workDirectory(), "core");
        var core_src_directory = new File(core_directory, "src");
        var core_src_main_directory = new File(core_src_directory, "main");
        var core_src_main_java_directory = new File(core_src_main_directory, "java");
        var core_src_main_resources_directory = new File(core_src_main_directory, "resources");
        var core_src_test_directory = new File(core_src_directory, "test");
        var core_src_test_java_directory = new File(core_src_test_directory, "java");
        var core_src_test_resources_directory = new File(core_src_test_directory, "resources");
        var core_src_main_resources_templates_directory = new File(core_src_main_resources_directory, "templates");

        antlr4Operation
            .sourceDirectories(List.of(new File(core_src_main_directory, "antlr")));

        precompileOperation()
            .sourceDirectories(core_src_main_resources_templates_directory)
            .templateTypes(HTML, XML, SQL, TXT, JSON);

        compileOperation()
            .mainSourceDirectories(antlr4Operation.outputDirectory(), core_src_main_java_directory)
            .testSourceDirectories(core_src_test_java_directory)
            .compileOptions()
                .debuggingInfo(JavacOptions.DebuggingInfo.ALL)
                .addAll(List.of("-encoding", "UTF-8"));

        jarOperation()
            .sourceDirectories(core_src_main_resources_directory)
            .excluded(Pattern.compile("^\\Q" + core_src_main_resources_templates_directory.getAbsolutePath() + "\\E.*"));

        jarAgentOperation
            .fromProject(this)
            .destinationFileName("rife2-" + version() + "-agent.jar")
            .manifestAttribute(new Attributes.Name("Premain-Class"), "rife.instrument.RifeAgent")
            .included(
                "rife/asm/",
                "rife/instrument/",
                "rife/continuations/ContinuationConfigInstrument",
                "rife/continuations/instrument/",
                "rife/database/querymanagers/generic/instrument/",
                "rife/engine/EngineContinuationConfigInstrument",
                "rife/tools/ClassBytesLoader",
                "rife/tools/FileUtils",
                "rife/tools/InstrumentationUtils",
                "rife/tools/RawFormatter",
                "rife/tools/exceptions/FileUtils",
                "rife/validation/instrument/",
                "rife/validation/MetaDataMerged",
                "rife/validation/MetaDataBeanAware",
                "rife/workflow/config/ContinuationInstrument");

        jarContinuationsOperation
            .fromProject(this)
            .destinationFileName("rife2-" + version() + "-agent-continuations.jar")
            .manifestAttribute(new Attributes.Name("Premain-Class"), "rife.continuations.instrument.ContinuationsAgent")
            .included(
                "rife/asm/",
                "rife/instrument/",
                "rife/continuations/ContinuationConfigInstrument",
                "rife/continuations/instrument/",
                "rife/tools/ClassBytesLoader",
                "rife/tools/FileUtils",
                "rife/tools/InstrumentationUtils",
                "rife/tools/RawFormatter",
                "rife/tools/exceptions/FileUtils");

        testsBadgeOperation
            .classpath(core_src_main_resources_directory.getAbsolutePath())
            .classpath(core_src_test_resources_directory.getAbsolutePath())
            .javaOptions()
                .javaAgent(new File(buildDistDirectory(), jarAgentOperation.destinationFileName()));

        javadocOperation()
            .sourceFiles(FileUtils.getJavaFileList(core_src_main_java_directory))
            .javadocOptions()
                .tag("rife.apiNote", "a", "API Note:")
                .docTitle("<a href=\"https://rife2.com\">RIFE2</a> " + version())
                .overview(new File(srcMainJavaDirectory(), "overview.html"));

        publishOperation()
            .repository(version.isSnapshot() ? repository("rife2-snapshots") : repository("rife2-releases"))
            .repository(version.isSnapshot() ? repository("sonatype-snapshots") : repository("sonatype-releases"))
            .repository(repository("github"))
            .info(new PublishInfo()
                .groupId("com.uwyn.rife2")
                .artifactId("rife2")
                .name("RIFE2")
                .description("Full-stack, no-declaration, framework to quickly and effortlessly create web applications with modern Java.")
                .url("https://github.com/rife2/rife2")
                .developer(new PublishDeveloper()
                    .id("gbevin")
                    .name("Geert Bevin")
                    .email("gbevin@uwyn.com")
                    .url("https://github.com/gbevin"))
                .license(new PublishLicense()
                    .name("The Apache License, Version 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0.txt"))
                .scm(new PublishScm()
                    .connection("scm:git:https://github.com/rife2/rife2.git")
                    .developerConnection("scm:git:git@github.com:rife2/rife2.git")
                    .url("https://github.com/rife2/rife2"))
                .signKey(property("sign.key"))
                .signPassphrase(property("sign.passphrase")))
            .artifacts(
                new PublishArtifact(jarAgentOperation.destinationFile(), "agent", "jar"),
                new PublishArtifact(jarContinuationsOperation.destinationFile(), "agent-continuations", "jar"));

        examples = new ExamplesBuild(this);
    }

    @Override
    public void download()
    throws Exception {
        super.download();
        sanitizeTomcatEmbedCore();
    }

    private void sanitizeTomcatEmbedCore()
    throws IOException {
        System.out.println("Sanitizing tomcat-embed-core for Java module usage...");
        for (var jar : FileUtils.getFileList(libProvidedModulesDirectory(), Pattern.compile("tomcat-embed-core.*\\.jar"), null)) {
            System.out.println("Stripping jakarta.servlet from " + jar + "...");
            var tmp_dir = Files.createTempDirectory("tomcat-embed").toFile();
            try {
                var source_jar = new File(libProvidedModulesDirectory(), jar);
                FileUtils.unzipFile(source_jar, tmp_dir);
                var servlet_dir = new File(new File(tmp_dir, "jakarta"), "servlet");
                if (!servlet_dir.exists()) {
                    System.out.println("Already sanitized " + jar);
                }
                else {
                    FileUtils.deleteDirectory(servlet_dir);

                    var module_info = new File(tmp_dir,"module-info.class");
                    module_info.delete();

                    var existing_manifest = new File(new File(tmp_dir, "META-INF"), "MANIFEST.MF");
                    existing_manifest.delete();
                    source_jar.delete();
                    new JarOperation()
                        .manifestAttributes(Map.of(
                            Attributes.Name.MANIFEST_VERSION, "1.0"))
                        .sourceDirectories(tmp_dir)
                        .destinationDirectory(libProvidedModulesDirectory())
                        .destinationFileName(jar)
                        .execute();
                }
            }
            finally {
                FileUtils.deleteDirectory(tmp_dir);
            }
        }
    }

    final JarOperation jarAgentOperation = new JarOperation();
    @BuildCommand(value = "jar-agent", summary = "Creates the agent jar archive")
    public void jarAgent()
    throws Exception {
        compile();
        jarAgentOperation.executeOnce();
    }

    final JarOperation jarContinuationsOperation = new JarOperation();
    @BuildCommand(value = "jar-continuations", summary = "Creates the continuations agent jar archive")
    public void jarContinuations()
    throws Exception {
        compile();
        jarContinuationsOperation.executeOnce();
    }

    public void test()
    throws Exception {
        jarAgent();
        super.test();
    }

    @BuildCommand(summary = "Creates all the distribution artifacts")
    public void all()
    throws Exception {
        jar();
        jarSources();
        jarJavadoc();
        jarAgent();
        jarContinuations();
    }

    public void publish()
    throws Exception {
        all();
        super.publish();
    }

    public void publishLocal()
    throws Exception {
        all();
        super.publishLocal();
    }

    final ExamplesBuild examples;

    @BuildCommand(value = "compile-examples", summary = "Compiles the RIFE2 examples")
    public void compileExamples()
    throws Exception {
        compile();
        precompile();
        examples.compile();
    }

    @BuildCommand(value = "test-examples", summary = "Tests the RIFE2 examples")
    public void testExamples()
    throws Exception {
        jarAgent();
        examples.test();
    }

    @BuildCommand(value = "run-examples", summary = "Run the RIFE2 examples")
    public void runExamples()
    throws Exception {
        jarAgent();
        examples.run();
    }

    public static void main(String[] args)
    throws Exception {
        new Rife2Build().start(args);
    }
}