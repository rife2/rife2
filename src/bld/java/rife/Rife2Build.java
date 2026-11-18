/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife;

import rife.bld.BuildCommand;
import rife.bld.dependencies.VersionNumber;
import rife.bld.extension.JUnitReporterOperation;
import rife.bld.operations.JarOperation;
import rife.bld.operations.JavacOptions;
import rife.bld.publish.*;
import rife.tools.FileUtils;

import java.io.File;
import java.nio.file.Path;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;
import java.util.StringJoiner;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.regex.Pattern;
import java.util.stream.Stream;

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
        var jetty_version = version(12,1,12);
        var jsoup_version = version(1,23,2);
        var tomcat_version = version(11,0,25);
        var graalvm_sdk_version = version(25,0,5);

        scope(provided)
            .include(module("org.jsoup", "jsoup", jsoup_version))
            .include(module("org.eclipse.jetty.ee10", "jetty-ee10", jetty_version))
            .include(module("org.eclipse.jetty.ee10", "jetty-ee10-servlet", jetty_version))
            .include(module("org.apache.tomcat.embed", "tomcat-embed-core", tomcat_version).excludeSources())
            .include(module("org.apache.tomcat.embed", "tomcat-embed-jasper", tomcat_version))
            .include(module("net.imagej", "ij", imagej_version).excludeSources())
            // only native-image has these, on the JVM the C functions are never loaded
            .include(module("org.graalvm.sdk", "nativeimage", graalvm_sdk_version))
            .include(module("org.graalvm.sdk", "word", graalvm_sdk_version));
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
        coreTemplatesDirectory_ = core_src_main_resources_templates_directory;
        nativeConfigDirectory_ = new File(buildDirectory(), "native-config");

        antlr4Operation
            .sourceDirectories(List.of(new File(core_src_main_directory, "antlr")));

        precompileOperation()
            .sourceDirectories(core_src_main_resources_templates_directory)
            .templateTypes(HTML, XML, SQL, TXT, JSON, SVG);

        compileOperation()
            .mainSourceDirectories(antlr4Operation.outputDirectory(), core_src_main_java_directory)
            .testSourceDirectories(core_src_test_java_directory)
            .compileOptions()
                .debuggingInfo(JavacOptions.DebuggingInfo.ALL)
                .addAll(List.of("-encoding", "UTF-8"));

        jarOperation()
            .sourceDirectories(core_src_main_resources_directory, nativeConfigDirectory_)
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

        jarSourcesOperation()
            .sourceDirectories(core_src_main_java_directory);

        javadocOperation()
            .sourceFiles(FileUtils.getJavaFileList(core_src_main_java_directory))
            .javadocOptions()
                .tag("rife.apiNote", "a", "API Note:")
                .docTitle("<a href=\"https://rife2.com\">RIFE2</a> " + version())
                .overview(new File(srcMainJavaDirectory(), "overview.html"));

        publishOperation()
            .repository(version.isSnapshot() ? "rife2-snapshots" : "rife2-releases")
            .repository(version.isSnapshot() ? "central-snapshots" : "central-releases")
            .repository("github")
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

    final File coreTemplatesDirectory_;
    final File nativeConfigDirectory_;

    @BuildCommand(value = "generate-native-config", summary = "Generates the GraalVM native-image reflection config for the precompiled templates")
    public void generateNativeConfig()
    throws Exception {
        writeTemplateNativeConfig(
            List.of(srcMainResourcesTemplatesDirectory(), coreTemplatesDirectory_),
            new File(nativeConfigDirectory_, "META-INF/native-image/rife-templates"));
    }

    // every precompiled template class is instantiated reflectively at
    // runtime, so each template resource needs a reflection entry for
    // GraalVM native image; generating them from the actual template
    // resources keeps the config from going stale; the types have to
    // match those of the precompile operation
    static void writeTemplateNativeConfig(List<File> templateDirectories, File configDirectory)
    throws Exception {
        var template_pattern = Pattern.compile(".*\\.(html|xml|sql|txt|json|svg)$");
        var entries = new java.util.TreeSet<String>();
        for (var dir : templateDirectories) {
            for (var file : FileUtils.getFileList(dir, template_pattern, null)) {
                var normalized = file.replace(File.separatorChar, '/');
                var extension_index = normalized.lastIndexOf('.');
                var type = normalized.substring(extension_index + 1);
                var name = normalized.substring(0, extension_index).replace('/', '.');
                entries.add("rife.template." + type + "." + name);
            }
        }

        var config = new StringBuilder("[\n");
        var first = true;
        for (var entry : entries) {
            if (!first) {
                config.append(",\n");
            }
            first = false;
            config.append("""
                  {
                    "name": "%s",
                    "methods": [
                      {
                        "name": "<init>",
                        "parameterTypes": []
                      },
                      {
                        "name": "isModified",
                        "parameterTypes": [
                          "rife.resources.ResourceFinder",
                          "java.lang.String"
                        ]
                      }
                    ]
                  }""".formatted(entry));
        }
        config.append("\n]\n");

        configDirectory.mkdirs();
        FileUtils.writeString(config.toString(), new File(configDirectory, "reflect-config.json"));
        System.out.println("Generated native-image reflection config for " + entries.size() + " precompiled templates.");
    }

    @Override
    public void jar()
    throws Exception {
        generateNativeConfig();
        super.jar();
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

    @BuildCommand(value = "test-modular", summary = "Verifies that what the framework ships beside its classes is reachable when it runs as a named module")
    public void testModular()
    throws Exception {
        jar();

        // a resource that sits in an exported package is only provided to
        // the module that holds it, and no test on the class path can tell
        // that apart from a resource that simply isn't there
        var probe_directory = new File(buildDirectory(), "modular");
        var source_directory = new File(probe_directory, "src/rife");
        var classes_directory = new File(probe_directory, "classes");
        source_directory.mkdirs();
        classes_directory.mkdirs();

        var probe = new File(source_directory, "ModularProbe.java");
        FileUtils.writeString("""
            package rife;

            import rife.template.TemplateFactory;

            public class ModularProbe {
                public static void main(String[] args)
                throws Exception {
                    if (!"rife".equals(ModularProbe.class.getModule().getName())) {
                        throw new AssertionError("the probe has to run inside the module of the framework");
                    }

                    var version = Version.getVersion();
                    if (null == version || version.isBlank()) {
                        throw new AssertionError("the version resource didn't come out of the module");
                    }

                    var template = TemplateFactory.HTML.get("errors.rife.engine_error");
                    if (!template.getContent().contains("errors occurred")) {
                        throw new AssertionError("the error template didn't come out of the module");
                    }

                    // a site finds it with its own class loader, which only sees what isn't a package of the module
                    if (null == ModularProbe.class.getClassLoader().getResource(rife.nativeapp.NativeApp.SCRIPT_RESOURCE_BASE + "/rife2-native-app.js")) {
                        throw new AssertionError("the script of the pages inside an app didn't come out of the module");
                    }

                    System.out.println("the module hands out its own resources (version " + version + ")");
                }
            }
            """, probe);

        var module_path = new StringJoiner(File.pathSeparator);
        module_path.add(new File(new File(buildDirectory(), "dist"), jarFileName()).getAbsolutePath());
        for (var jar : compileClasspathJars()) {
            module_path.add(jar.getAbsolutePath());
        }
        for (var jar : providedClasspathJars()) {
            module_path.add(jar.getAbsolutePath());
        }

        run(List.of(compilerTool(), "--module-path", module_path.toString(),
            "--patch-module", "rife=" + new File(probe_directory, "src").getAbsolutePath(),
            "--add-modules", "rife", "-d", classes_directory.getAbsolutePath(), probe.getAbsolutePath()));
        run(List.of(javaTool(), "--module-path", module_path.toString(),
            "--patch-module", "rife=" + classes_directory.getAbsolutePath(),
            "--add-modules", "rife", "-m", "rife/rife.ModularProbe"));
    }

    @BuildCommand(value = "test-native", summary = "Verifies that what the framework ships beside its classes is embedded in a native image, and that a site runner works in one")
    public void testNative()
    throws Exception {
        jar();

        // a resource is only embedded in an image when it was registered for
        // it, which the java version that this targets doesn't work out on its
        // own the way the later ones do
        var probe_directory = new File(buildDirectory(), "native");
        var classes_directory = new File(probe_directory, "classes");
        classes_directory.mkdirs();

        var probe = new File(probe_directory, "NativeResourceProbe.java");
        FileUtils.writeString("""
            public class NativeResourceProbe {
                public static void main(String[] args) {
                    if (null == NativeResourceProbe.class.getResourceAsStream("/RIFE_VERSION")) {
                        System.out.println("the version resource isn't embedded in the native image");
                        System.exit(3);
                    }

                    System.out.println("the native image embeds what the framework ships beside its classes");
                }
            }
            """, probe);

        var jar = new File(new File(buildDirectory(), "dist"), jarFileName()).getAbsolutePath();
        // the probe is built for the version that this targets, since the
        // image is allowed to be made by a toolchain of that version
        run(List.of(compilerTool(), "--release", "17", "-d", classes_directory.getAbsolutePath(), probe.getAbsolutePath()));
        run(List.of(nativeImageTool(), "-cp", classes_directory.getAbsolutePath() + File.pathSeparator + jar,
            "--no-fallback", "-o", new File(probe_directory, "resource-probe").getAbsolutePath(), "NativeResourceProbe"));
        run(List.of(new File(probe_directory, "resource-probe").getAbsolutePath()));

        // a site runner only needs the servlet API beside the framework, so
        // the image is built without a servlet container to prove that
        var site_runner_probe = new File(probe_directory, "NativeSiteRunnerProbe.java");
        FileUtils.writeString("""
            import rife.engine.*;

            public class NativeSiteRunnerProbe {
                public static void main(String[] args) {
                    try (var runner = new SiteRunner().scheme("probe").serverName("app").staticUberJarResourceBase("probe").start(new Site() {
                        public void setup() {
                            post("/greet", c -> c.print("hello " + c.parameter("name") + " from " + c.request().getServerRootUrl(-1)));
                            get("/visit", c -> c.print(c.session().id() + ":" + c.session().isNew()));
                        }
                    })) {
                        var text = runner.handle("/greet?name=native", new SiteRequest(RequestMethod.POST)).getText();
                        if (!text.equals("hello native from probe://app")) {
                            fail("unexpected response: " + text);
                        }
                        if (runner.handle("/missing").isHandled()) {
                            fail("an unrouted path was handled");
                        }
                        var first_visit = runner.handle("/visit").getText();
                        var session_id = first_visit.substring(0, first_visit.indexOf(':'));
                        var next_visit = runner.handle("/visit", new SiteRequest().header("Cookie", "JSESSIONID=" + session_id)).getText();
                        if (!first_visit.endsWith(":true") || !next_visit.equals(session_id + ":false")) {
                            fail("the session wasn't kept: " + first_visit + " " + next_visit);
                        }

                        // the cookies of a jar survive in their file store
                        var cookie_file = new java.io.File(args[0]);
                        try (var remembering = new SiteRunner().cookieJar(new SiteCookieJar(new SiteCookieFileStore(cookie_file))).start(new Site() {
                            public void setup() {
                                get("/remember", c -> c.addCookie(new rife.engine.CookieBuilder("city", "ghent").path("/").maxAge(3600)));
                            }
                        })) {
                            remembering.handle("/remember");
                        }
                        var restored = new SiteCookieJar(new SiteCookieFileStore(cookie_file)).getCookies();
                        if (restored.size() != 1 || !"ghent".equals(restored.get(0).value())) {
                            fail("the cookies weren't restored: " + restored);
                        }

                        // the static files of a native image are its embedded resources
                        var file = runner.handle("/hello.txt");
                        if (200 != file.getStatus() || !file.getText().equals("hello file") || !"10".equals(file.getHeader("Content-Length"))) {
                            fail("unexpected static file: " + file.getStatus() + " " + file.getText());
                        }
                        var etag = file.getHeader("ETag");
                        if (null == etag || 304 != runner.handle("/hello.txt", new SiteRequest().header("If-None-Match", etag)).getStatus()) {
                            fail("the static file isn't versioned: " + etag);
                        }
                        if (runner.handle("/sub").isHandled()) {
                            fail("a resource directory was served");
                        }
                    }

                    System.out.println("the native image runs a site without a servlet container");
                }

                private static void fail(String message) {
                    System.out.println(message);
                    System.exit(3);
                }
            }
            """, site_runner_probe);
        var static_file = new File(classes_directory, "probe/hello.txt");
        var nested_file = new File(classes_directory, "probe/sub/inner.txt");
        var static_config = new File(classes_directory, "META-INF/native-image/probe/resource-config.json");
        nested_file.getParentFile().mkdirs();
        static_config.getParentFile().mkdirs();
        FileUtils.writeString("hello file", static_file);
        FileUtils.writeString("inner file", nested_file);
        FileUtils.writeString("""
            {"resources": {"includes": [{"pattern": "probe/.*"}]}}
            """, static_config);

        var servlet_api = Stream.concat(providedModulePathJars().stream(), providedClasspathJars().stream())
            .filter(f -> f.getName().startsWith("jakarta.servlet-api-"))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("the servlet API jar wasn't found: run the download command first"))
            .getAbsolutePath();
        var site_runner_classpath = classes_directory.getAbsolutePath() + File.pathSeparator + jar + File.pathSeparator + servlet_api;
        run(List.of(compilerTool(), "--release", "17", "-cp", jar + File.pathSeparator + servlet_api, "-d", classes_directory.getAbsolutePath(), site_runner_probe.getAbsolutePath()));
        run(List.of(nativeImageTool(), "-cp", site_runner_classpath,
            "--no-fallback", "-o", new File(probe_directory, "site-runner-probe").getAbsolutePath(), "NativeSiteRunnerProbe"));
        run(List.of(new File(probe_directory, "site-runner-probe").getAbsolutePath(), new File(probe_directory, "cookies.txt").getAbsolutePath()));

        // native-image puts the C functions it finds on the classpath into
        // any image, the ones of an app only belong in a shared library
        var executable = Files.readAllBytes(new File(probe_directory, "site-runner-probe").toPath());
        if (new String(executable, StandardCharsets.ISO_8859_1).contains("rife2_handle")) {
            throw new IllegalStateException("an executable of a site has the C functions of a native app");
        }
    }

    /**
     * Only the name of the tool becomes the compiler, since the path that
     * leads to it is allowed to hold 'java' as well.
     */
    private String compilerTool() {
        var tool = Path.of(javaTool());
        return tool.resolveSibling(tool.getFileName().toString().replaceFirst("java", "javac")).toString();
    }

    private String nativeImageTool() {
        for (var home : new String[]{System.getenv("GRAALVM_HOME"), System.getenv("JAVA_HOME")}) {
            if (home != null) {
                var tool = Path.of(home, "bin", "native-image");
                if (tool.toFile().exists()) {
                    return tool.toString();
                }
            }
        }

        return "native-image";
    }

    private void run(List<String> command)
    throws Exception {
        var process = new ProcessBuilder(command).inheritIO().start();
        if (process.waitFor() != 0) {
            throw new AssertionError("a packaging check failed: " + String.join(" ", command));
        }
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

    @BuildCommand(summary = "Runs the JUnit reporter")
    public void reporter() throws Exception {
        new JUnitReporterOperation()
            .fromProject(this)
            .failOnSummary(true)
            .execute();
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