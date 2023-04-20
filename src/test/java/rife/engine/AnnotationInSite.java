/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.engine.annotations.*;
import rife.tools.FileUtils;
import rife.tools.StringUtils;

import java.io.File;
import java.util.List;

public class AnnotationInSite extends Site {
    public static class ParentElement implements Element {
        @ActiveSite AnnotationInSite site;
        @ActiveSite Site baseSite;
        @ActiveSite Number notSite;
        @Body int intBody = -1;
        @Cookie("cookie2") String stringCookie2 = "defaultCookie2";
        @FileUpload String fileString;
        @Parameter int intParam = -4;
        @PathInfo int intPathInfo = -6;
        @RequestAttribute int intRequestAttribute = -7;
        @SessionAttribute int intSessionAttribute = -9;
        @Header("header2") String stringHeader2 = "defaultHeader2";
        @Property String prop1 = "defaultProp1";
        @Property String prop2 = "defaultProp2";
        @Property("prop1") String prop3 = "defaultProp3";
        @Config String config1 = "defaultConfig1";
        @Config String config2 = "defaultConfig2";
        @Config("config1") String config3 = "defaultConfig3";
        @Config List list1 = List.of("one", "two", "false");
        @Config List<Boolean> list2 = List.of(true, false, false);
        @Config List<String> list3 = List.of("one", "two", "false");
        @Config("listint") List<Integer> list4 = List.of(1, 2, 3);

        public void process(Context c)
        throws Exception {
            if (site == null) throw new RuntimeException("site should be filled");
            if (baseSite == null) throw new RuntimeException("baseSite should be filled");
            if (notSite != null) throw new RuntimeException("notSite should not be filled");
        }
    }

    public static class AnnotatedElement extends ParentElement {
        @ActiveSite AnnotationInSite childSite;
        @Body String stringBody = "defaultBody";

        @Cookie String stringCookie = "defaultCookie";
        @Cookie int intCookie = -2;
        @Cookie("cookie3") int intCookie2 = -3;

        @FileUpload UploadedFile uploadedFile;
        @FileUpload File file;
        @FileUpload("file2") UploadedFile uploadedFile2;
        @FileUpload("file3") File file2;
        @FileUpload("file4") String fileString2;

        @Parameter String stringParam = "defaultParam";
        @Parameter("param2") String stringParam2 = "defaultParam2";
        @Parameter("param3") int intParam2 = -5;

        @PathInfo String stringPathInfo = "defaultPathInfo";

        @RequestAttribute String stringRequestAttribute = "defaultRequestAttribute";
        @RequestAttribute("requestAttr2") String stringRequestAttribute2 = "defaultRequestAttribute2";
        @RequestAttribute("requestAttr3") int intRequestAttribute2 = -8;

        @SessionAttribute String stringSessionAttribute = "defaultSessionAttribute";
        @SessionAttribute("sessionAttr2") String stringSessionAttribute2 = "defaultSessionAttribute2";
        @SessionAttribute("sessionAttr3") int intSessionAttribute2 = -10;

        @Header String stringHeader = "defaultHeader";
        @Header int intHeader = -11;
        @Header("header3") int intHeader2 = -12;

        @Property String prop1 = "defaultProp1";
        @Property String prop2 = "defaultProp2";

        @Config String config2 = "defaultConfig2";
        @Config("config1") String config3 = "defaultConfig3";

        @Config List<Boolean> list2 = List.of(true, false, false);
        @Config List<String> list3 = List.of("one", "two", "false");
        @Config("listint") List<Integer> list4 = List.of(1, 2, 3);

        @ParametersBean BeanImpl beanParams = null;

        public void process(Context c)
        throws Exception {
            super.process(c);

            if (childSite == null) throw new RuntimeException("childSite should be filled");

            c.print(stringBody + "\n");
            c.print(intBody + "\n");

            c.print(stringCookie + "\n");
            c.print(intCookie + "\n");
            c.print(stringCookie2 + "\n");
            c.print(intCookie2 + "\n");

            if (uploadedFile != null) {
                c.print(uploadedFile.getName() + ", " + uploadedFile.getContentType() + ", " + FileUtils.readString(uploadedFile.getFile()) + "\n");
            } else {
                c.print(uploadedFile + "\n");
            }
            if (file != null) {
                c.print(FileUtils.readString(file) + "\n");
            } else {
                c.print(file + "\n");
            }
            if (fileString != null) {
                c.print(FileUtils.readString(new File(fileString)) + "\n");
            } else {
                c.print(fileString + "\n");
            }
            if (uploadedFile2 != null) {
                c.print(uploadedFile2.getName() + ", " + uploadedFile2.getContentType() + ", " + FileUtils.readString(uploadedFile2.getFile()) + "\n");
            } else {
                c.print(uploadedFile2 + "\n");
            }
            if (file2 != null) {
                c.print(FileUtils.readString(file2) + "\n");
            } else {
                c.print(file2 + "\n");
            }
            if (fileString2 != null) {
                c.print(FileUtils.readString(new File(fileString2)) + "\n");
            } else {
                c.print(fileString2 + "\n");
            }

            c.print(stringParam + "\n");
            c.print(intParam + "\n");
            c.print(stringParam2 + "\n");
            c.print(intParam2 + "\n");

            c.print(stringPathInfo + "\n");
            c.print(intPathInfo + "\n");

            c.print(stringRequestAttribute + "\n");
            c.print(intRequestAttribute + "\n");
            c.print(stringRequestAttribute2 + "\n");
            c.print(intRequestAttribute2 + "\n");

            c.print(stringSessionAttribute + "\n");
            c.print(intSessionAttribute + "\n");
            c.print(stringSessionAttribute2 + "\n");
            c.print(intSessionAttribute2 + "\n");

            c.print(stringHeader + "\n");
            c.print(intHeader + "\n");
            c.print(stringHeader2 + "\n");
            c.print(intHeader2 + "\n");

            c.print(prop1 + "\n");
            c.print(prop2 + "\n");
            c.print(prop3 + "\n");

            c.print(config1 + "\n");
            c.print(config2 + "\n");
            c.print(config3 + "\n");

            c.print(StringUtils.join(list1, ",") + "\n");
            c.print(StringUtils.join(list2, ",") + "\n");
            c.print(StringUtils.join(list3, ",") + "\n");
            c.print(StringUtils.join(list4, ",") + "\n");

            beanParams.printToContext(c);
        }
    }

    public void setup() {
        properties().put("prop1", "propval1");
        config().put("config1", "configval1");
        config().putItem("list1", "listitem1");
        config().putItem("list1", "listitem2");
        config().putItem("list1", "listitem3");
        config().putItem("list2", false);
        config().putItem("list2", true);
        config().putItem("list3", "listitem4");
        config().putItem("list3", "listitem5");
        config().putItem("listint", 11);
        config().putItem("listint", 22);
        config().putItem("listint", 33);
        before(c -> {
           if (c.parameterBoolean("generate")) {
               c.setAttribute("stringRequestAttribute", "value9");
               c.setAttribute("intRequestAttribute", "10");
               c.setAttribute("requestAttr2", "value11");
               c.setAttribute("requestAttr3", "12");
               c.session().setAttribute("stringSessionAttribute", "value13");
               c.session().setAttribute("intSessionAttribute", "14");
               c.session().setAttribute("sessionAttr2", "value15");
               c.session().setAttribute("sessionAttr3", "16");
           }
        });
        get("/get", AnnotatedElement.class);
        get("/get/info", PathInfoHandling.CAPTURE, AnnotatedElement::new);
        post("/post", AnnotatedElement.class);
        post("/form", AnnotatedElement::new);
        get("/form", c -> c.print("""
<form action="/form" method="post" enctype="multipart/form-data">
    <input type="file" name="uploadedFile">
    <input type="file" name="file">
    <input type="file" name="fileString">
    <input type="file" name="file2">
    <input type="file" name="file3">
    <input type="file" name="file4">
    <input type="file" name="stringFile">
    <input type="file" name="bytesFile">
    <input type="file" name="streamFile">
    <button type="submit" id="submit">SEND</button>
</form>
            """));
    }
}
