/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class TemplateDeployer {
    private final boolean verbose_;
    private final File[] directories_;
    private final TemplateFactory templateFactory_;
    private final Pattern include_;
    private final Pattern exclude_;

    private TemplateDeployer(boolean verbose, ArrayList<String> directoryPaths, TemplateFactory templateFactory, Pattern include, Pattern exclude) {
        assert directoryPaths != null;
        assert directoryPaths.size() > 0;

        verbose_ = verbose;
        templateFactory_ = templateFactory;
        include_ = include;
        exclude_ = exclude;
        var directories = new ArrayList<File>();

        File directory_file;

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

        var array = new File[directories.size()];
        directories_ = directories.toArray(array);
    }

    private void execute()
    throws TemplateException {
        ArrayList<String> files;
        String classname;

        for (var directory : directories_) {
            var group = new ResourceFinderGroup()
                .add(new ResourceFinderDirectories(new File[]{directory}))
                .add(ResourceFinderClasspath.instance());
            templateFactory_.setResourceFinder(group);
            files = FileUtils.getFileList(directory,
                Pattern.compile(".*\\" + templateFactory_.getParser().getExtension() + "$"),
                Pattern.compile(".*(SCCS|CVS|\\.svn|\\.git).*"));

            for (var file : files) {
                if (!StringUtils.filter(file, include_, exclude_)) {
                    continue;
                }

                if (verbose_) {
                    System.out.print(directory.getPath() + " : " + file + " ... ");
                }
                classname = file.replace(File.separatorChar, '.');
                classname = classname.substring(0, classname.length() - templateFactory_.getParser().getExtension().length());
                templateFactory_.parse(classname, null, null);
                if (verbose_) {
                    System.out.println("done.");
                }
            }
        }
    }

    private static void listTemplateTypes() {
        var types = new ArrayList<>(TemplateFactory.getFactoryTypes());

        Collections.sort(types);
        for (Object type : types) {
            System.err.println("  " + type);
        }
    }

    public static void main(String[] arguments) {
        var valid_arguments = true;
        var verbose = false;
        var directory_paths = new ArrayList<String>();
        var template_type = "html";
        Pattern include = null;
        Pattern exclude = null;

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
                        verbose = true;
                    } else if (arguments[i].equals("-d")) {
                        i++;
                        if (arguments[i].startsWith("-")) {
                            valid_arguments = false;
                        } else {
                            RifeConfig.template().setGenerationPath(arguments[i]);
                        }
                    } else if (arguments[i].equals("-encoding")) {
                        i++;
                        if (arguments[i].startsWith("-")) {
                            valid_arguments = false;
                        } else {
                            RifeConfig.template().setDefaultEncoding(arguments[i]);
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
                            include = Pattern.compile(arguments[i]);
                        }
                    } else if (arguments[i].equals("-e")) {
                        i++;
                        if (arguments[i].startsWith("-")) {
                            valid_arguments = false;
                        } else {
                            exclude = Pattern.compile(arguments[i]);
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
            System.err.println("Usage : java " + TemplateDeployer.class.getName() + " <options> <directories>");
            System.err.println("Compiles RIFE templates to class files.");
            System.err.println("All the files of the active template type that are found in the provided");
            System.err.println("directories will be parsed and compiled to java bytecode into the");
            System.err.println("destination directory.");
            System.err.println("  -t <type>             Specify which template type to use (default html)");
            System.err.println("  -l                    List the known template types");
            System.err.println("  -verbose              Output messages about what the parser is doing");
            System.err.println("  -d <directory>        Specify where to place generated class files");
            System.err.println("  -encoding <encoding>  Specify character encoding used by template files");
            System.err.println("  -preload <classes>    Colon seperated list of classes to preload");
            System.err.println("  -i <regexp>           Regexp to include certain files");
            System.err.println("  -e <regexp>           Regexp to exclude certain files");
            System.err.println("  -help                 Print a synopsis of standard options");
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

        RifeConfig.template().setGenerateClasses(true);
        var deployer = new TemplateDeployer(verbose, directory_paths, factory, include, exclude);
        deployer.execute();
    }
}

