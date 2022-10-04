/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

import rife.config.RifeConfig;
import rife.resources.ResourceFinder;
import rife.template.exceptions.TemplateException;
import rife.tools.FileUtils;
import rife.tools.exceptions.FileUtilsErrorException;

class TemplateClassLoader extends ClassLoader {
    private final TemplateFactory templateFactory_;

    TemplateClassLoader(TemplateFactory templateFactory, ClassLoader initiating) {
        super(initiating);

        assert templateFactory != null;
        assert initiating != null;

        templateFactory_ = templateFactory;
    }

    protected Class loadClass(String classname, boolean resolve, String encoding, TemplateTransformer transformer)
    throws ClassNotFoundException {
        assert classname != null;

        // see if this classloader has cached the class with the provided name
        Class c = findLoadedClass(classname);

        // if an already loaded version was found, check whether it's outdated or not
        if (c != null) {
            // if an already loaded version was found, check whether it's outdated or not
            // this can only be Template classes since those are the only ones that are
            // handled by this classloader
            if (RifeConfig.template().autoReload()) {
                // if the template was modified, don't use the cached class
                // otherwise, just take the previous template class
                if (isTemplateModified(c, transformer)) {
                    TemplateClassLoader new_classloader = new TemplateClassLoader(templateFactory_, this.getParent());
                    // register the new classloader as the default templatefactory's
                    // classloader
                    templateFactory_.setClassLoader(new_classloader);
                    return new_classloader.loadClass(classname, resolve, encoding, transformer);
                }
            }
        }
        // try to obtain the class in another way
        else {
            // try to obtain it from the parent classloader or from the system classloader
            ClassLoader parent = getParent();
            if (parent != null) {
                try {
                    // the parent is never a TemplateClassLoader, it's always the
                    // class that instantiated the initial TemplateClassLoader
                    // thus, the encoding doesn't need to be passed on further
                    c = parent.loadClass(classname);

                    // if templates are reloaded automatically, check if a corresponding
                    // class was found in the parent classloader. If that class came from a jar
                    // file, make sure it's returned immediately and don't try to recompile it
                    if (c != null &&
                        RifeConfig.template().autoReload()) {
                        URL resource = parent.getResource(classname.replace('.', '/') + ".class");
                        if (resource != null &&
                            resource.getPath().indexOf('!') != -1) {
                            // resolve the class if it's needed
                            if (resolve) {
                                resolveClass(c);
                            }

                            return c;
                        }
                    }
                } catch (ClassNotFoundException e) {
                    c = null;
                }
            }

            if (null == c) {
                try {
                    c = findSystemClass(classname);
                } catch (ClassNotFoundException e) {
                    c = null;
                }
            }

            // load template class files in class path
            if (c != null &&
                !classname.startsWith("java.") &&
                !classname.startsWith("javax.") &&
                !classname.startsWith("sun.") &&
                Template.class.isAssignableFrom(c)) {
                // verify if the template in the classpath has been updated
                if (RifeConfig.template().autoReload() &&
                    isTemplateModified(c, transformer)) {
                    c = null;
                }
            }

            if (null == c) {
                // intern the classname to get a synchronization lock monitor
                // that is specific for the current class that is loaded and
                // that will not lock up the classloading of all other classes
                // by for instance synchronizing on this classloader
                classname = classname.intern();
                synchronized (classname) {
                    // make sure that the class has not been defined in the
                    // meantime, otherwise defining it again will trigger an
                    // exception;
                    // reuse the existing class if it has already been defined
                    c = findLoadedClass(classname);
                    if (null == c) {
                        byte[] raw = compileTemplate(classname, encoding, transformer);

                        // define the bytes of the class for this classloader
                        c = defineClass(classname, raw, 0, raw.length);
                    }
                }
            }
        }

        // resolve the class if it's needed
        if (resolve) {
            resolveClass(c);
        }

        assert c != null;

        return c;
    }

    private byte[] compileTemplate(String classname, String encoding, TemplateTransformer transformer)
    throws ClassNotFoundException {
        assert classname != null;

        // try to resolve the classname as a template name by resolving the template
        URL template_url = templateFactory_.getParser().resolve(classname);
        if (null == template_url) {
            throw new ClassNotFoundException("Couldn't resolve template: '" + classname + "'.");
        }

        // prepare the template with all the information that's needed to be able to identify
        // this template uniquely
        Parsed template_parsed = templateFactory_.getParser().prepare(classname, template_url);

        // parse the template
        try {
            templateFactory_.getParser().parse(template_parsed, encoding, transformer);
        } catch (TemplateException e) {
            throw new ClassNotFoundException("Error while parsing template: '" + classname + "'.", e);
        }

        byte[] byte_code = template_parsed.getByteCode();
        if (RifeConfig.template().generateClasses()) {
            // get the package and the short classname of the template
            String template_package = template_parsed.getPackage();
            template_package = template_package.replace('.', File.separatorChar);
            String template_classname = template_parsed.getClassName();

            // setup everything to perform the conversion of the template to java sources
            // and to compile it into a java class
            String generation_path = RifeConfig.template().generationPath() + File.separatorChar;
            String packagedir = generation_path + template_package;
            String filename_class = packagedir + File.separator + template_classname + ".class";
            File file_packagedir = new File(packagedir);
            File file_class = new File(filename_class);

            // prepare the package directory
            if (!file_packagedir.exists()) {
                if (!file_packagedir.mkdirs()) {
                    throw new ClassNotFoundException("Couldn't create the template package directory : '" + packagedir + "'.");
                }
            } else if (!file_packagedir.isDirectory()) {
                throw new ClassNotFoundException("The template package directory '" + packagedir + "' exists but is not a directory.");
            } else if (!file_packagedir.canWrite()) {
                throw new ClassNotFoundException("The template package directory '" + packagedir + "' is not writable.");
            }

            try {
                FileUtils.writeBytes(byte_code, file_class);
            } catch (FileUtilsErrorException e) {
                throw new ClassNotFoundException("Error while writing the contents of the template class file '" + classname + "'.", e);
            }
        }
        return byte_code;
    }

    private boolean isTemplateModified(Class c, TemplateTransformer transformer) {
        assert c != null;

        boolean is_modified = true;
        Method is_modified_method = null;
        String modification_state = null;
        if (transformer != null) {
            modification_state = transformer.getState();
        }
        try {
            is_modified_method = c.getMethod("isModified", ResourceFinder.class, String.class);
            is_modified = (Boolean) is_modified_method.invoke(null, new Object[]{templateFactory_.getResourceFinder(), modification_state});
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException |
                 InvocationTargetException e) {
            // do nothing, template will be considered as outdated
        }

        return is_modified;
    }
}
