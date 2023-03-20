/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

import rife.config.RifeConfig;
import rife.resources.ResourceFinderClasspath;
import rife.resources.ResourceFinderDirectories;
import rife.resources.ResourceFinderGroup;
import rife.template.exceptions.TemplateException;
import rife.tools.FileUtils;
import rife.tools.StringUtils;

import java.io.File;
import java.util.*;
import java.util.regex.Pattern;

public class TemplateDeployer {
    private boolean verbose_ = false;
    private List<File> directories_ = Collections.emptyList();
    private List<TemplateFactory> templateFactories_ = Collections.emptyList();
    private String generationPath_ = null;
    private String encoding_ = null;
    private Pattern include_ = null;
    private Pattern exclude_ = null;

    public TemplateDeployer() {
    }

    public TemplateDeployer verbose(boolean verbose) {
        verbose_ = verbose;
        return this;
    }

    public TemplateDeployer directoryPaths(List<String> directoryPaths) {
        var directories = new ArrayList<File>();

        File directory_file;

        if (directoryPaths != null) {
            for (var directory_path : directoryPaths) {
                directory_file = new File(directory_path);
                if (!directory_file.exists()) {
                    System.err.println("The path '" + directory_path + "' doesn't exist.");
                    System.exit(1);
                }
                if (!directory_file.isDirectory()) {
                    System.err.println("The path '" + directory_path + "' is not a directory.");
                    System.exit(1);
                }
                if (!directory_file.canRead()) {
                    System.err.println("The directory '" + directory_path + "' is not readable.");
                    System.exit(1);
                }

                directories.add(directory_file);
            }
        }

        directories_ = directories;

        return this;
    }

    public TemplateDeployer generationPath(String generationPath) {
        generationPath_ = generationPath;
        return this;
    }

    public TemplateDeployer encoding(String encoding) {
        encoding_ = encoding;
        return this;
    }

    public TemplateDeployer include(Pattern include) {
        include_ = include;
        return this;
    }

    public TemplateDeployer exclude(Pattern exclude) {
        exclude_ = exclude;
        return this;
    }

    public TemplateDeployer templateFactories(List<TemplateFactory> templateFactories) {
        templateFactories_ = templateFactories;
        return this;
    }

    public void execute()
    throws TemplateException {
        var previous_generation_path = RifeConfig.template().getGenerationPath();
        var previous_encoding = RifeConfig.template().getDefaultEncoding();
        var previous_generate_classes = RifeConfig.template().getGenerateClasses();
        try {
            RifeConfig.template().setGenerationPath(generationPath_);
            RifeConfig.template().setDefaultEncoding(encoding_);
            RifeConfig.template().setGenerateClasses(true);

            List<String> files;
            String classname;

            for (var template_factory : templateFactories_) {
                for (var directory : directories_) {
                    var group = new ResourceFinderGroup()
                        .add(new ResourceFinderDirectories(new File[]{directory}))
                        .add(ResourceFinderClasspath.instance());
                    template_factory.setResourceFinder(group);
                    files = FileUtils.getFileList(directory,
                        Pattern.compile(".*\\" + template_factory.getParser().getExtension() + "$"),
                        Pattern.compile(".*(SCCS|CVS|\\.svn|\\.git).*"));

                    for (var file : files) {
                        if (!StringUtils.filter(file, include_, exclude_)) {
                            continue;
                        }

                        if (verbose_) {
                            System.out.print(directory.getPath() + " : " + file + " ... ");
                        }
                        classname = file.replace(File.separatorChar, '.');
                        classname = classname.substring(0, classname.length() - template_factory.getParser().getExtension().length());
                        template_factory.parse(classname, null);
                        if (verbose_) {
                            System.out.println("done.");
                        }
                    }
                }
            }
        } finally {
            RifeConfig.template().setGenerationPath(previous_generation_path);
            RifeConfig.template().setDefaultEncoding(previous_encoding);
            RifeConfig.template().setGenerateClasses(previous_generate_classes);
        }
    }

    private static void listTemplateTypes() {
        var types = new ArrayList<>(TemplateFactory.getFactoryTypes());

        Collections.sort(types);
        for (Object type : types) {
            System.err.println("  " + type);
        }
    }

    public static String getHelp(String commandName) {
        if (commandName == null) {
            commandName = "";
        }
        commandName = commandName.trim();
        if (!commandName.isEmpty()) {
            commandName = commandName + " ";
        }

        return StringUtils.replace("""
            Compiles RIFE2 templates to class files.
                        
            All the files of the active template type that are found in the provided
            directories will be parsed and compiled to java bytecode into the
            destination directory.
                        
            Usage : ${commandName}[options] [directories]
              -t <type>             Specify which template type to use (default html)
              -l                    List the known template types
              -verbose              Output messages about what the parser is doing
              -d <directory>        Specify where to place generated class files
              -encoding <encoding>  Specify character encoding used by template files
              -preload <classes>    Colon seperated list of classes to preload
              -i <regexp>           Regexp to include certain files
              -e <regexp>           Regexp to exclude certain files""", "${commandName}", commandName);
    }

    public static void main(String[] arguments) {
        var valid_arguments = true;
        var directory_paths = new ArrayList<String>();
        var template_type = "html";

        var deployer = new TemplateDeployer();

        if (arguments.length < 1) {
            valid_arguments = false;
        } else {
            for (var i = 0; i < arguments.length; i++) {
                if (arguments[i].startsWith("-")) {
                    if (arguments[i].equals("-t")) {
                        i++;
                        if (arguments[i].startsWith("-")) {
                            valid_arguments = false;
                        } else {
                            template_type = arguments[i];
                        }
                    } else if (arguments[i].equals("-l")) {
                        System.err.println("The supported template types are:");
                        listTemplateTypes();
                        System.exit(0);
                    } else if (arguments[i].equals("-verbose")) {
                        deployer.verbose(true);
                    } else if (arguments[i].equals("-d")) {
                        i++;
                        if (arguments[i].startsWith("-")) {
                            valid_arguments = false;
                        } else {
                            deployer.generationPath(arguments[i]);
                        }
                    } else if (arguments[i].equals("-encoding")) {
                        i++;
                        if (arguments[i].startsWith("-")) {
                            valid_arguments = false;
                        } else {
                            deployer.encoding(arguments[i]);
                        }
                    } else if (arguments[i].equals("-preload")) {
                        i++;
                        if (arguments[i].startsWith("-")) {
                            valid_arguments = false;
                        } else {
                            var class_names = StringUtils.split(arguments[i], ":");
                            for (var class_name : class_names) {
                                try {
                                    Class.forName(class_name);
                                } catch (ClassNotFoundException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    } else if (arguments[i].equals("-i")) {
                        i++;
                        if (arguments[i].startsWith("-")) {
                            valid_arguments = false;
                        } else {
                            deployer.include(Pattern.compile(arguments[i]));
                        }
                    } else if (arguments[i].equals("-e")) {
                        i++;
                        if (arguments[i].startsWith("-")) {
                            valid_arguments = false;
                        } else {
                            deployer.exclude(Pattern.compile(arguments[i]));
                        }
                    } else {
                        valid_arguments = false;
                    }
                } else {
                    directory_paths.add(arguments[i]);
                }

                if (!valid_arguments) {
                    break;
                }
            }
        }

        if (0 == directory_paths.size()) {
            valid_arguments = false;
        }

        if (!valid_arguments) {
            System.err.println(getHelp("java " + TemplateDeployer.class.getName()));
            System.exit(1);
        }

        TemplateFactory factory = null;
        factory = TemplateFactory.getFactory(template_type);
        if (null == factory) {
            System.err.println("The template type '" + template_type + "' is not supported.");
            System.err.println("The list of valid types is:");
            listTemplateTypes();
            System.exit(1);
        }

        deployer
            .directoryPaths(directory_paths)
            .templateFactories(List.of(factory))
            .execute();
    }
}

