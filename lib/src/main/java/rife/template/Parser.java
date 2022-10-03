/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTreeWalker;
import org.antlr.v4.runtime.tree.TerminalNode;
import rife.config.RifeConfig;
import rife.resources.ResourceFinder;
import rife.resources.exceptions.ResourceFinderErrorException;
import rife.template.antlr.TemplateHtmlLexer;
import rife.template.antlr.TemplateHtmlParser;
import rife.template.antlr.TemplateHtmlParserListener;
import rife.template.exceptions.*;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

public class Parser implements Cloneable {

    public static final String DEFAULT_TEMPLATES_PATH = "templates/";

    public static final String TEMPLATE_PACKAGE = "rife.template.";

    private TemplateFactory templateFactory_;

    private String identifier_ = null;
    private String packageName_ = null;

    private String ambiguousName_ = null;
    private String extension_ = null;
    private int extensionLength_ = -1;

    Parser(TemplateFactory templateFactory, String identifier, String extension) {
        init(templateFactory, identifier, extension);
    }

    String getExtension() {
        return extension_;
    }

    TemplateFactory getTemplateFactory() {
        return templateFactory_;
    }

    private void init(TemplateFactory templateFactory, String identifier, String extension) {
        assert templateFactory != null;
        assert identifier != null;
        assert extension != null;

        templateFactory_ = templateFactory;

        identifier_ = identifier;
        packageName_ = TEMPLATE_PACKAGE + identifier_ + ".";

        extension_ = extension;
        extensionLength_ = extension_.length();
        ambiguousName_ = (extension_ + extension_).substring(1);

        assert extensionLength_ > 0;
    }

    public Parser clone() {
        Parser new_parser = null;
        try {
            new_parser = (Parser) super.clone();
        } catch (CloneNotSupportedException ignored) {
        }

        assert new_parser != null;
        new_parser.init(templateFactory_, identifier_, extension_);

        return new_parser;
    }

    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }

        if (null == object) {
            return false;
        }

        if (!(object instanceof Parser other_parser)) {
            return false;
        }

        if (!other_parser.identifier_.equals(identifier_)) {
            return false;
        }
        if (!other_parser.extension_.equals(this.extension_)) {
            return false;
        }

        return true;
    }

    public URL resolve(String name) {
        if (null == name) throw new IllegalArgumentException("name can't be null.");

        if (0 == name.indexOf(getPackage())) {
            name = name.substring(getPackage().length());
        }
        name = name.replace('.', '/') + extension_;

        var resource = templateFactory_.getResourceFinder().getResource(name);
        if (null == resource) {
            resource = templateFactory_.getResourceFinder().getResource(DEFAULT_TEMPLATES_PATH + name);
        }

        return resource;
    }

    public String getPackage() {
        return packageName_;
    }

    String escapeClassname(String name) {
        assert name != null;

        if (name.equals(ambiguousName_)) {
            throw new AmbiguousTemplateNameException(name);
        }

        if (name.endsWith(extension_)) {
            name = name.substring(0, name.length() - extensionLength_);
        }

        var name_chars = name.toCharArray();
        int char_code;
        for (int i = 0; i < name_chars.length; i++) {
            char_code = name_chars[i];
            if ((char_code >= 48 && char_code <= 57) ||
                (char_code >= 65 && char_code <= 90) ||
                (char_code >= 97 && char_code <= 122) ||
                char_code == 46) {
                continue;
            }

            if (char_code == '/' || char_code == '\\') {
                name_chars[i] = '.';
            } else {
                name_chars[i] = '_';
            }
        }

        return new String(name_chars);
    }

    public Parsed prepare(String name, URL resource) {
        if (null == name) throw new IllegalArgumentException("name can't be null.");
        if (null == resource) throw new IllegalArgumentException("resource can't be null.");

        var template_name = name;
        var template_parsed = new Parsed(this);
        if (0 == template_name.indexOf(getPackage())) {
            template_name = name.substring(getPackage().length());
        }

        var class_name = template_name;
        var subpackage = "";
        int package_seperator = template_name.lastIndexOf(".");
        if (package_seperator != -1) {
            subpackage = "." + template_name.substring(0, package_seperator);
            class_name = template_name.substring(package_seperator + 1);
        }
        template_parsed.setTemplateName(template_name);
        template_parsed.setPackage(getPackage().substring(0, getPackage().length() - 1) + subpackage);
        template_parsed.setClassName(escapeClassname(class_name));
        template_parsed.setResource(resource);

        return template_parsed;
    }


    Parsed parse(Parsed parsed, String encoding, TemplateTransformer transformer)
    throws TemplateException {
        assert parsed != null;

        // get the resource of the template file
        URL resource = parsed.getResource();

        // obtain the content of the template file
        String content = getContent(parsed.getTemplateName(), parsed, resource, encoding, transformer);

        var input = CharStreams.fromString(content);
        var lexer = new TemplateHtmlLexer(input);
        var tokens = new CommonTokenStream(lexer);
        var parser = new TemplateHtmlParser(tokens);
        parser.setBuildParseTree(true);

        var walker = new ParseTreeWalker();
        var listener = new AntlrListener(parsed);
        walker.walk(listener, parser.document());

//        // replace the included templates
//        Stack<String> previous_includes = new Stack<String>();
//        previous_includes.push(parsed.getFullClassName());
//        replaceIncludeTags(parsed, content_buffer, previous_includes, encoding, transformer);
//        previous_includes.pop();
//
//        // process the blocks and values
//        String content = content_buffer.toString();
//        parseBlocks(parsed, content);
//        parsed.setFilteredBlocks(filterTags(mBlockFilters, parsed.getBlockIds()));
//        parsed.setFilteredValues(filterTags(mValueFilters, parsed.getValueIds()));
//
//        assert parsed.getBlocks().size() >= 1;

        return parsed;
    }

    private String getContent(String templateName, Parsed parsed, URL resource, String encoding, TemplateTransformer transformer)
    throws TemplateException {
        if (null == transformer) {
            return getFileContent(resource, encoding);
        } else {
            return getTransformedContent(templateName, parsed, resource, encoding, transformer);
        }
    }

    public String getTemplateContent(String name)
    throws TemplateException {
        return getFileContent(resolve(name), null);
    }

    private String getFileContent(URL resource, String encoding)
    throws TemplateException {
        assert resource != null;

        if (null == encoding) {
            encoding = RifeConfig.instance().template.defaultEncoding();
        }

        String content;

        try {
            content = templateFactory_.getResourceFinder().getContent(resource, encoding);
        } catch (ResourceFinderErrorException e) {
            throw new GetContentErrorException(resource.toExternalForm(), e);
        }

        return content;
    }

    private String getTransformedContent(String templateName, Parsed parsed, URL resource, String encoding, TemplateTransformer transformer)
    throws TemplateException {
        assert resource != null;

        if (null == encoding) {
            encoding = RifeConfig.instance().template.defaultEncoding();
        }

        ByteArrayOutputStream result = new ByteArrayOutputStream();

        // transform the content
        transformer.setResourceFinder(templateFactory_.getResourceFinder());
        Collection<URL> dependencies = transformer.transform(templateName, resource, result, encoding);
        // get the dependencies and their modification times
        if (dependencies != null &&
            dependencies.size() > 0) {
            long modification_time = 0;
            for (URL dependency_resource : dependencies) {
                try {
                    modification_time = transformer.getResourceFinder().getModificationTime(dependency_resource);
                } catch (ResourceFinderErrorException e) {
                    // if there was trouble in obtaining the modification time, just set
                    // it to 0 so that the dependency will always be outdated
                    modification_time = 0;
                }

                if (modification_time > 0) {
                    parsed.addDependency(dependency_resource, modification_time);
                }
            }
        }
        // set the modification state so that different filter configurations
        // will reload the template, not only modifications to the dependencies
        parsed.setModificationState(transformer.getState());

        // convert the result to a StringBuilder
        try {
            if (null == encoding) {
                if (null == transformer.getEncoding()) {
                    return result.toString();
                } else {
                    return result.toString(transformer.getEncoding());
                }
            } else {
                return result.toString(encoding);
            }
        } catch (UnsupportedEncodingException e) {
            throw new TransformedResultConversionException(resource.toExternalForm(), e);
        }
    }

    public static long getModificationTime(ResourceFinder resourceFinder, URL resource)
    throws TemplateException {
        if (null == resource) throw new IllegalArgumentException("resource can't be null.");

        long modification_time = -1;

        try {
            modification_time = resourceFinder.getModificationTime(resource);
        } catch (ResourceFinderErrorException e) {
            throw new ModificationTimeErrorException(resource.toExternalForm(), e);
        }

        return modification_time;
    }

    static class AntlrListener implements TemplateHtmlParserListener {
        final Parsed parsed_;

        final Map<String, ParsedBlockData> blocks_ = new LinkedHashMap<>();
        final Stack<String> blockIds_ = new Stack<>();
        StringBuilder currentValueData_ = null;

        AntlrListener(Parsed parsed) {
            parsed_ = parsed;
        }

        @Override
        public void enterDocument(TemplateHtmlParser.DocumentContext ctx) {
            blocks_.put("", new ParsedBlockData());
            blockIds_.push("");
        }

        @Override
        public void exitDocument(TemplateHtmlParser.DocumentContext ctx) {
            blockIds_.pop();

            for (var block : blocks_.entrySet()) {
                parsed_.setBlock(block.getKey(), block.getValue());
            }
        }

        @Override
        public void enterContent(TemplateHtmlParser.ContentContext ctx) {

        }

        @Override
        public void exitContent(TemplateHtmlParser.ContentContext ctx) {

        }

        @Override
        public void enterTagV(TemplateHtmlParser.TagVContext ctx) {
        }

        @Override
        public void exitTagV(TemplateHtmlParser.TagVContext ctx) {
            var name = ctx.TTagName();
            final String value_id;
            final String tag;
            if (name == null) {
                name = ctx.CTagName();
                value_id = String.valueOf(name);
                tag = ctx.CSTART_V() + " " + value_id + ctx.CSTERM();

            } else {
                value_id = String.valueOf(name);
                tag = ctx.TSTART_V() + " " + value_id + ctx.TSTERM();
            }

            String block_id = blockIds_.peek();
            if (block_id != null) {
                parsed_.addValue(value_id);
                blocks_.get(block_id).add(new ParsedBlockValue(value_id, tag));
            }
        }

        @Override
        public void enterTagVDefault(TemplateHtmlParser.TagVDefaultContext ctx) {
            currentValueData_ = new StringBuilder();
        }

        @Override
        public void exitTagVDefault(TemplateHtmlParser.TagVDefaultContext ctx) {
            var name = ctx.TTagName();
            final String value_id;
            final String tag;
            if (name == null) {
                name = ctx.CTagName();
                value_id = String.valueOf(name);
                tag = ctx.CSTART_V() + " " + value_id + ctx.CENDI() + ctx.CCLOSE_V();

            } else {
                value_id = String.valueOf(name);
                tag = ctx.TSTART_V() + " " + value_id + ctx.TENDI() + ctx.TCLOSE_V();
            }

            String block_id = blockIds_.peek();
            if (block_id != null) {
                parsed_.addValue(value_id);
                parsed_.setDefaultValue(value_id, currentValueData_.toString());
                blocks_.get(block_id).add(new ParsedBlockValue(value_id, tag));
            }

            currentValueData_ = null;
        }

        @Override
        public void enterTagB(TemplateHtmlParser.TagBContext ctx) {
            var name = ctx.TTagName();
            if (name == null) {
                name = ctx.CTagName();
            }
            final String block_id = name.getText();
            blockIds_.push(block_id);
            blocks_.put(block_id, new ParsedBlockData());
        }

        @Override
        public void exitTagB(TemplateHtmlParser.TagBContext ctx) {
            blockIds_.pop();
        }

        @Override
        public void enterTagBV(TemplateHtmlParser.TagBVContext ctx) {
            var name = ctx.TTagName();
            if (name == null) {
                name = ctx.CTagName();
            }
            final String block_id = name.getText();
            parsed_.setBlockvalue(block_id);
            blockIds_.push(block_id);
            blocks_.put(block_id, new ParsedBlockData());
        }

        @Override
        public void exitTagBV(TemplateHtmlParser.TagBVContext ctx) {
            blockIds_.pop();
        }

        @Override
        public void enterTagBA(TemplateHtmlParser.TagBAContext ctx) {
            var name = ctx.TTagName();
            if (name == null) {
                name = ctx.CTagName();
            }
            final String block_id = name.getText();
            parsed_.setBlockvalue(block_id);
            blockIds_.push(block_id);

            var current_block = blocks_.get(block_id);
            if (null == current_block)
            {
                current_block = new ParsedBlockData();
                blocks_.put(block_id, current_block);
            }
        }

        @Override
        public void exitTagBA(TemplateHtmlParser.TagBAContext ctx) {
            blockIds_.pop();
        }

        @Override
        public void enterTagI(TemplateHtmlParser.TagIContext ctx) {

        }

        @Override
        public void exitTagI(TemplateHtmlParser.TagIContext ctx) {

        }

        @Override
        public void enterBlockData(TemplateHtmlParser.BlockDataContext ctx) {
        }

        @Override
        public void exitBlockData(TemplateHtmlParser.BlockDataContext ctx) {
            String block_id = blockIds_.peek();
            if (block_id != null) {
                blocks_.get(block_id).add(new ParsedBlockText(ctx.getText()));
            }
        }

        @Override
        public void enterValueData(TemplateHtmlParser.ValueDataContext ctx) {
        }

        @Override
        public void exitValueData(TemplateHtmlParser.ValueDataContext ctx) {
            if (currentValueData_ != null) {
                currentValueData_.append(ctx.getText());
            }
        }

        @Override
        public void visitTerminal(TerminalNode node) {

        }

        @Override
        public void visitErrorNode(ErrorNode node) {

        }

        @Override
        public void enterEveryRule(ParserRuleContext ctx) {

        }

        @Override
        public void exitEveryRule(ParserRuleContext ctx) {

        }
    }
}
