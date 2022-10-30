/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.tree.*;
import rife.config.RifeConfig;
import rife.datastructures.DocumentPosition;
import rife.resources.ResourceFinder;
import rife.resources.exceptions.ResourceFinderErrorException;
import rife.template.antlr.*;
import rife.template.exceptions.*;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parser implements Cloneable {

    public static final String DEFAULT_TEMPLATES_PATH = "templates/";

    public static final String TEMPLATE_PACKAGE = "rife.template.";

    private TemplateFactory templateFactory_;

    private String identifier_ = null;
    private String packageName_ = null;

    private String ambiguousName_ = null;
    private String extension_ = null;
    private int extensionLength_ = -1;

    private Pattern[] blockFilters_ = null;
    private Pattern[] valueFilters_ = null;

    Parser(TemplateFactory templateFactory, String identifier, String extension, Pattern[] blockFilters, Pattern[] valueFilters) {
        init(templateFactory, identifier, extension, blockFilters, valueFilters);
    }

    String getExtension() {
        return extension_;
    }

    Pattern[] getBlockFilters() {
        return blockFilters_;
    }

    Pattern[] getValueFilters() {
        return valueFilters_;
    }

    TemplateFactory getTemplateFactory() {
        return templateFactory_;
    }

    private void init(TemplateFactory templateFactory, String identifier, String extension, Pattern[] blockFilters, Pattern[] valueFilters) {
        assert templateFactory != null;
        assert identifier != null;
        assert extension != null;

        templateFactory_ = templateFactory;

        identifier_ = identifier;
        packageName_ = TEMPLATE_PACKAGE + identifier_ + ".";

        extension_ = extension;
        extensionLength_ = extension_.length();
        ambiguousName_ = (extension_ + extension_).substring(1);

        blockFilters_ = blockFilters;
        valueFilters_ = valueFilters;

        assert extensionLength_ > 0;
    }

    public Parser clone() {
        Parser new_parser = null;
        try {
            new_parser = (Parser) super.clone();
        } catch (CloneNotSupportedException ignored) {
        }

        assert new_parser != null;

        Pattern[] new_block_filters = null;
        if (blockFilters_ != null) {
            new_block_filters = blockFilters_.clone();
        }
        Pattern[] new_value_filters = null;
        if (valueFilters_ != null) {
            new_value_filters = valueFilters_.clone();
        }

        new_parser.init(templateFactory_, identifier_, extension_, new_block_filters, new_value_filters);

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

        if (null == other_parser.blockFilters_ && this.blockFilters_ != null ||
            other_parser.blockFilters_ != null && null == this.blockFilters_) {
            return false;
        }
        if (other_parser.blockFilters_ != null) {
            if (other_parser.blockFilters_.length != this.blockFilters_.length) {
                return false;
            }

            for (var i = 0; i < other_parser.blockFilters_.length; i++) {
                if (!other_parser.blockFilters_[i].pattern().equals(this.blockFilters_[i].pattern())) {
                    return false;
                }
            }
        }

        if (null == other_parser.valueFilters_ && this.valueFilters_ != null ||
            other_parser.valueFilters_ != null && null == this.valueFilters_) {
            return false;
        }
        if (other_parser.valueFilters_ != null) {
            if (other_parser.valueFilters_.length != this.valueFilters_.length) {
                return false;
            }

            for (var i = 0; i < other_parser.valueFilters_.length; i++) {
                if (!other_parser.valueFilters_[i].pattern().equals(this.valueFilters_[i].pattern())) {
                    return false;
                }
            }
        }

        return true;
    }

    public Parsed parse(String name, String encoding, TemplateTransformer transformer)
    throws TemplateException {
        if (null == name) throw new IllegalArgumentException("name can't be null.");

        var resource = resolve(name);
        if (null == resource) {
            throw new TemplateNotFoundException(name, null);
        }

        return parse(prepare(name, resource), encoding, transformer);
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
        for (var i = 0; i < name_chars.length; i++) {
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
        var package_separator = template_name.lastIndexOf(".");
        if (package_separator != -1) {
            subpackage = "." + template_name.substring(0, package_separator);
            class_name = template_name.substring(package_separator + 1);
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
        var resource = parsed.getResource();

        // obtain the content of the template file
        var content = getContent(parsed.getTemplateName(), parsed, resource, encoding, transformer);

        // replace the included templates
        var previous_includes = new Stack<String>();
        previous_includes.push(parsed.getFullClassName());
        content = replaceIncludeTags(parsed, content, previous_includes, encoding, transformer);
        previous_includes.pop();

        // process the blocks and values
        parseBlocksAndValues(parsed, content);

        // process block and value filters
        parsed.setFilteredBlocks(filterTags(blockFilters_, parsed.getBlockIds()));
        parsed.setFilteredValues(filterTags(valueFilters_, parsed.getValueIds()));

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
            encoding = RifeConfig.template().getDefaultEncoding();
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
            encoding = RifeConfig.template().getDefaultEncoding();
        }

        var result = new ByteArrayOutputStream();

        // transform the content
        transformer.setResourceFinder(templateFactory_.getResourceFinder());
        var dependencies = transformer.transform(templateName, resource, result, encoding);
        // get the dependencies and their modification times
        if (dependencies != null &&
            dependencies.size() > 0) {
            long modification_time = 0;
            for (var dependency_resource : dependencies) {
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

    private String replaceIncludeTags(Parsed parsed, String content, Stack<String> previousIncludes, String encoding, TemplateTransformer transformer)
    throws TemplateException {
        assert parsed != null;
        assert content != null;

        var input = CharStreams.fromString(content);
        var lexer = new TemplatePreLexer(input);
        var tokens = new CommonTokenStream(lexer);
        var parser = new TemplatePreParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new AntlrParserErrorListener(parsed));
        parser.setBuildParseTree(true);

        var walker = new ParseTreeWalker();
        var listener = new AntlrIncludeListener(parsed, content, previousIncludes, encoding, transformer);
        walker.walk(listener, parser.document());

        return listener.getContent();
    }

    private void parseBlocksAndValues(Parsed parsed, String content) {
        assert parsed != null;
        assert content != null;

        var input = CharStreams.fromString(content);
        var lexer = new TemplateMainLexer(input);
        lexer.tc = templateFactory_.getConfig();
        var tokens = new CommonTokenStream(lexer);
        var parser = new TemplateMainParser(tokens);
        parser.setBuildParseTree(true);
        parser.removeErrorListeners();
        parser.addErrorListener(new AntlrParserErrorListener(parsed));

        var walker = new ParseTreeWalker();
        var listener = new AntlrParserListener(parsed);
        walker.walk(listener, parser.document());

        assert parsed.getBlocks().size() >= 1;
    }

    class AntlrIncludeListener extends TemplatePreParserBaseListener {
        final Parsed parsed_;
        final String content_;
        final Stack<String> previousIncludes_;
        final String encoding_;
        final TemplateTransformer transformer_;
        final StringBuilder result_ = new StringBuilder();

        AntlrIncludeListener(Parsed parsed, String content, Stack<String> previousIncludes, String encoding, TemplateTransformer transformer) {
            parsed_ = parsed;
            content_ = content;
            previousIncludes_ = previousIncludes;
            encoding_ = encoding;
            transformer_ = transformer;
        }

        String getContent() {
            return result_.toString();
        }

        @Override
        public void exitDocData(TemplatePreParser.DocDataContext ctx) {
            result_.append(ctx.getText());
        }

        @Override
        public void enterTagI(TemplatePreParser.TagIContext ctx) {
            var name = ctx.TTagName();
            if (name == null) {
                name = ctx.CTagName();
            }

            var included_template_name = name.getText();
            // obtain the parser that will be used to get the included content
            var include_parser = Parser.this;

            // check if the included template references another template type
            var double_colon_index = included_template_name.indexOf(':');
            if (double_colon_index != -1) {
                var template_type = included_template_name.substring(0, double_colon_index);
                if (!template_type.equals(templateFactory_.toString())) {
                    var factory = TemplateFactory.getFactory(template_type);
                    include_parser = factory.getParser();
                    included_template_name = included_template_name.substring(double_colon_index + 1);
                }
            }

            var included_template_resource = include_parser.resolve(included_template_name);

            if (null == included_template_resource) {
                throw new IncludeNotFoundException(parsed_.getClassName(), getDocumentPositionForToken(ctx.getStart()), included_template_name);
            }
            var included_template_parsed = include_parser.prepare(included_template_name, included_template_resource);

            // check for circular references
            if (previousIncludes_.contains(included_template_parsed.getFullClassName())) {
                throw new CircularIncludesException(parsed_.getClassName(), getDocumentPositionForToken(ctx.getStart()), included_template_name, previousIncludes_);
            }

            // parse the included template's include tags too
            var included_template_content = include_parser.getContent(included_template_name, parsed_, included_template_parsed.getResource(), encoding_, transformer_);
            previousIncludes_.push(included_template_parsed.getFullClassName());
            var replaced_content = replaceIncludeTags(included_template_parsed, included_template_content, previousIncludes_, encoding_, transformer_);
            previousIncludes_.pop();

            // retain the link to this include file for optional later modification time checking
            parsed_.addDependency(included_template_parsed);

            // add the dependencies of the included template too
            var included_dependencies = included_template_parsed.getDependencies();
            for (var included_dependency : included_dependencies.entrySet()) {
                parsed_.addDependency(included_dependency.getKey(), included_dependency.getValue());
            }

            // add the replaced content
            result_.append(replaced_content);
        }
    }

    static DocumentPosition getDocumentPositionForToken(Token token) {
        final var input = token.getTokenSource().getInputStream().toString();
        final var lines = input.split("\n");
        final var error_line = lines[token.getLine() - 1];
        final var length = Math.max(1, token.getStopIndex() - token.getStartIndex() + 1);
        return new DocumentPosition(error_line, token.getLine(), token.getCharPositionInLine(), length);
    }

    static class AntlrParserErrorListener extends BaseErrorListener {
        private final Parsed parsed_;

        public AntlrParserErrorListener(Parsed parsed) {
            parsed_ = parsed;
        }

        public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol,
                                int line, int charPositionInLine, String msg, RecognitionException e) {
            final var tokens = (CommonTokenStream) recognizer.getInputStream();
            final var input = tokens.getTokenSource().getInputStream().toString();
            final var lines = input.split("\n");
            final var error_line = lines[line - 1];
            final var token = (Token)offendingSymbol;
            final var length = Math.max(1, token.getStopIndex() - token.getStartIndex() + 1);
            final var position = new DocumentPosition(error_line, line, charPositionInLine, length);
            final String message;
            if (e instanceof InputMismatchException) {
                message = "Mismatched template tags";
            } else {
                message = "Template syntax error";
            }
            throw new SyntaxErrorException(parsed_.getTemplateName(), position, message, null);
        }
    }

    static class AntlrParserListener extends TemplateMainParserBaseListener {
        final Parsed parsed_;

        final Map<String, ParsedBlockData> blocks_ = new LinkedHashMap<>();
        final Stack<String> blockIds_ = new Stack<>();
        StringBuilder currentValueData_ = null;

        AntlrParserListener(Parsed parsed) {
            parsed_ = parsed;
        }

        @Override
        public void enterDocument(TemplateMainParser.DocumentContext ctx) {
            blocks_.put("", new ParsedBlockData());
            blockIds_.push("");
        }

        @Override
        public void exitDocument(TemplateMainParser.DocumentContext ctx) {
            blockIds_.pop();

            for (var block : blocks_.entrySet()) {
                parsed_.setBlock(block.getKey(), block.getValue());
            }
        }

        @Override
        public void exitTagV(TemplateMainParser.TagVContext ctx) {
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

            var block_id = blockIds_.peek();
            if (block_id != null) {
                parsed_.addValue(value_id);
                blocks_.get(block_id).add(new ParsedBlockValue(value_id, tag));
            }
        }

        @Override
        public void enterTagVDefault(TemplateMainParser.TagVDefaultContext ctx) {
            currentValueData_ = new StringBuilder();
        }

        @Override
        public void exitTagVDefault(TemplateMainParser.TagVDefaultContext ctx) {
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

            var block_id = blockIds_.peek();
            if (block_id != null) {
                parsed_.addValue(value_id);
                parsed_.setDefaultValue(value_id, currentValueData_.toString());
                blocks_.get(block_id).add(new ParsedBlockValue(value_id, tag));
            }

            currentValueData_ = null;
        }

        @Override
        public void enterTagB(TemplateMainParser.TagBContext ctx) {
            var name = ctx.TTagName();
            if (name == null) {
                name = ctx.CTagName();
            }
            final var block_id = name.getText();
            blockIds_.push(block_id);
            blocks_.put(block_id, new ParsedBlockData());
        }

        @Override
        public void exitTagB(TemplateMainParser.TagBContext ctx) {
            blockIds_.pop();
        }

        @Override
        public void enterTagBV(TemplateMainParser.TagBVContext ctx) {
            var name = ctx.TTagName();
            if (name == null) {
                name = ctx.CTagName();
            }
            final var block_id = name.getText();
            parsed_.setBlockvalue(block_id);
            blockIds_.push(block_id);
            blocks_.put(block_id, new ParsedBlockData());
        }

        @Override
        public void exitTagBV(TemplateMainParser.TagBVContext ctx) {
            blockIds_.pop();
        }

        @Override
        public void enterTagBA(TemplateMainParser.TagBAContext ctx) {
            var name = ctx.TTagName();
            if (name == null) {
                name = ctx.CTagName();
            }
            final var block_id = name.getText();
            parsed_.setBlockvalue(block_id);
            blockIds_.push(block_id);

            var current_block = blocks_.get(block_id);
            if (null == current_block) {
                current_block = new ParsedBlockData();
                blocks_.put(block_id, current_block);
            }
        }

        @Override
        public void exitTagBA(TemplateMainParser.TagBAContext ctx) {
            blockIds_.pop();
        }

        @Override
        public void exitBlockData(TemplateMainParser.BlockDataContext ctx) {
            var block_id = blockIds_.peek();
            if (block_id != null) {
                var data = blocks_.get(block_id);
                var last = data.getLastPart();
                if (last != null && last.getType() == ParsedBlockPart.Type.TEXT) {
                    var text_part = (ParsedBlockText) last;
                    text_part.setData(text_part.getData() + ctx.getText());
                } else {
                    data.add(new ParsedBlockText(ctx.getText()));
                }
            }
        }

        @Override
        public void exitValueData(TemplateMainParser.ValueDataContext ctx) {
            if (currentValueData_ != null) {
                currentValueData_.append(ctx.getText());
            }
        }
    }

    private FilteredTagsMap filterTags(Pattern[] filters, Collection<String> tags) {
        FilteredTagsMap result = null;

        if (filters != null) {
            result = new FilteredTagsMap();

            Matcher filter_matcher = null;
            ArrayList<String> captured_groups = null;
            String pattern = null;
            String[] captured_groups_array = null;

            var filtered_tags = new ArrayList<String>();

            // iterate over the tag filters
            for (var filter_pattern : filters) {
                // go over all the tags and try to match them against the current filter
                for (var tag : tags) {
                    // skip over tags that have already been filtered
                    if (filtered_tags.contains(tag)) {
                        continue;
                    }

                    // create the filter matcher
                    filter_matcher = filter_pattern.matcher(tag);

                    // if the filter matches, and it returned capturing groups,
                    // add the returned groups to the filtered tag mapping
                    while (filter_matcher.find()) {
                        if (null == captured_groups) {
                            captured_groups = new ArrayList<String>();
                            captured_groups.add(tag);
                        }

                        if (filter_matcher.groupCount() > 0) {
                            // store the captured groups
                            for (var j = 1; j <= filter_matcher.groupCount(); j++) {
                                captured_groups.add(filter_matcher.group(j));
                            }
                        }
                    }

                    if (captured_groups != null) {
                        pattern = filter_pattern.pattern();

                        captured_groups_array = new String[captured_groups.size()];
                        captured_groups.toArray(captured_groups_array);

                        result.addFilteredTag(pattern, captured_groups_array);
                        filtered_tags.add(tag);

                        captured_groups_array = null;
                        captured_groups = null;
                    }
                }
            }
        }

        return result;
    }

}
