/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.operations;

import rife.bld.*;
import rife.tools.FileUtils;
import rife.tools.StringUtils;

import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.util.regex.Pattern;

public class JarOperation {
    public static class Help implements BuildHelp {
        public String getDescription() {
            return "Creates a jar archive for a RIFE2 application";
        }

        public String getHelp(String topic) {
            return StringUtils.replace("""
                Creates a jar archive for a RIFE2 application.
                The standard jar command will automatically also execute
                the clean, compile and precompile commands beforehand.
                            
                Usage : ${topic}""", "${topic}", topic);
        }
    }

    private Attributes manifestAttributes_ = new Attributes();
    private List<File> sourceDirectories_ = new ArrayList<>();
    private List<NamedFile> sourceFiles_ = new ArrayList<>();
    private File destinationDirectory_;
    private String destinationFileName_;
    private List<Pattern> included_ = new ArrayList<>();
    private List<Pattern> excluded_ = new ArrayList<>();

    private final byte[] buffer_ = new byte[1024];

    public JarOperation() {
    }

    public void execute()
    throws Exception {
        createDestinationDirectory();
        createJarFile();
    }

    public void createDestinationDirectory() {
        destinationDirectory().mkdirs();
    }

    public void createJarFile()
    throws IOException {
        var out_file = new File(destinationDirectory(), destinationFileName());
        try (var jar = new JarOutputStream(new FileOutputStream(out_file), createManifest())) {
            for (var source_dir : sourceDirectories()) {
                for (var file_name : FileUtils.getFileList(source_dir)) {
                    var file = new File(source_dir, file_name);
                    if (StringUtils.filter(file.getAbsolutePath(), included(), excluded())) {
                        addFileToJar(jar, new NamedFile(file_name, file));
                    }
                }
            }
            for (var source_file : sourceFiles()) {
                addFileToJar(jar, source_file);
            }
            jar.flush();
        }
    }

    public Manifest createManifest() {
        var manifest = new Manifest();
        manifest.getMainAttributes().putAll(manifestAttributes());
        return manifest;
    }

    private void addFileToJar(JarOutputStream jar, NamedFile file)
    throws IOException {
        var entry = new JarEntry(file.name());
        entry.setTime(file.file().lastModified());
        jar.putNextEntry(entry);

        try (var in = new BufferedInputStream(new FileInputStream(file.file()))) {
            int count;
            while ((count = in.read(buffer_)) != -1) {
                jar.write(buffer_, 0, count);
            }
            jar.closeEntry();
        }
    }

    public JarOperation fromProject(Project project) {
        manifestAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        return sourceDirectories(List.of(project.buildMainDirectory(), project.srcMainResourcesDirectory()))
            .destinationDirectory(project.buildDistDirectory())
            .destinationFileName(project.jarFileName())
            .excluded(List.of(Pattern.compile("^\\Q" + project.srcMainResourcesTemplatesDirectory().getAbsolutePath() + "\\E.*")));
    }

    public JarOperation manifestAttributes(Attributes attributes) {
        manifestAttributes_ = attributes;
        return this;
    }

    public Attributes manifestAttributes() {
        return manifestAttributes_;
    }

    public JarOperation sourceDirectories(List<File> sources) {
        sourceDirectories_ = new ArrayList<>(sources);
        return this;
    }

    public List<File> sourceDirectories() {
        return sourceDirectories_;
    }

    public JarOperation sourceFiles(List<NamedFile> sources) {
        sourceFiles_ = new ArrayList<>(sources);
        return this;
    }

    public List<NamedFile> sourceFiles() {
        return sourceFiles_;
    }

    public JarOperation destinationDirectory(File directory) {
        destinationDirectory_ = directory;
        return this;
    }

    public File destinationDirectory() {
        return destinationDirectory_;
    }

    public JarOperation destinationFileName(String name) {
        destinationFileName_ = name;
        return this;
    }

    public String destinationFileName() {
        return destinationFileName_;
    }

    public JarOperation included(List<Pattern> included) {
        included_ = new ArrayList<>(included);
        return this;
    }

    public List<Pattern> included() {
        return included_;
    }

    public JarOperation excluded(List<Pattern> excluded) {
        excluded_ = new ArrayList<>(excluded);
        return this;
    }

    public List<Pattern> excluded() {
        return excluded_;
    }
}
