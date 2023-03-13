/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.bld.operations;

import rife.bld.BuildHelp;
import rife.bld.Project;
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
                            
                Usage : ${topic}""", "${topic}", topic);
        }
    }

    private Attributes manifestAttributes_ = new Attributes();
    private List<File> sourceDirectories_ = new ArrayList<>();
    private File destinationDirectory_;
    private String jarFileName_;
    private List<Pattern> included_ = new ArrayList<>();
    private List<Pattern> excluded_ = new ArrayList<>();

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
        var out_file = new File(destinationDirectory(), jarFileName());
        try (var jar = new JarOutputStream(new FileOutputStream(out_file), createManifest())) {
            for (var source_dir : sourceDirectories()) {
                for (var file_name : FileUtils.getFileList(source_dir)) {
                    var file = new File(source_dir, file_name);
                    if (StringUtils.filter(file.getAbsolutePath(), included(), excluded())) {
                        addFileToJar(jar, file_name, file);
                    }
                }
            }
            jar.flush();
        }
    }

    public Manifest createManifest() {
        var manifest = new Manifest();
        manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");
        manifest.getMainAttributes().putAll(manifestAttributes());
        return manifest;
    }

    public void addFileToJar(JarOutputStream jar, String fileName, File file)
    throws IOException {
        var entry = new JarEntry(fileName);
        entry.setTime(file.lastModified());
        jar.putNextEntry(entry);

        var in = new BufferedInputStream(new FileInputStream(file));
        var buffer = new byte[1024];
        while (true) {
            var count = in.read(buffer);
            if (count == -1) {
                break;
            }
            jar.write(buffer, 0, count);
        }
        jar.closeEntry();
        in.close();
    }

    public JarOperation fromProject(Project project) {
        manifestAttributes().put(Attributes.Name.MAIN_CLASS, project.mainClass);
        return sourceDirectories(List.of(project.buildMainDirectory(), project.srcMainResourcesDirectory()))
            .destinationDirectory(project.buildDistDirectory())
            .jarFileName(project.jarFileName())
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

    public JarOperation destinationDirectory(File directory) {
        destinationDirectory_ = directory;
        return this;
    }

    public File destinationDirectory() {
        return destinationDirectory_;
    }

    public JarOperation jarFileName(String name) {
        jarFileName_ = name;
        return this;
    }

    public String jarFileName() {
        return jarFileName_;
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
