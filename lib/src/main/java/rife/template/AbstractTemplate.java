/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.template;

import rife.config.Config;
import rife.config.RifeConfig;
import rife.resources.ResourceFinder;
import rife.template.exceptions.*;
import rife.tools.Localization;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.*;

public abstract class AbstractTemplate implements Template {
    protected TemplateInitializer initializer_ = null;
    protected Map<String, InternalString> fixedValues_ = new HashMap<>();
    protected Map<String, InternalValue> constructedValues_ = new HashMap<>();
    protected BeanHandler beanHandler_ = null;
    protected TemplateEncoder encoder_ = EncoderDummy.instance();
    protected List<ResourceBundle> defaultResourceBundles_ = null;
    protected List<ResourceBundle> resourceBundles_ = null;
    protected Map<String, Object> expressionVars_ = null;
    protected String language_ = null;
    protected Map<String, Object> cache_ = null;
    protected String defaultContentType_ = null;

    public final void appendBlock(String valueId, String blockId)
    throws TemplateException {
        if (null == valueId ||
            0 == valueId.length() ||
            !hasValueId(valueId)) {
            throw new ValueUnknownException(valueId);
        }
        if (null == blockId ||
            0 == blockId.length()) {
            throw new BlockUnknownException(blockId);
        }

        if (fixedValues_.containsKey(valueId)) {
            InternalValue constructed_value = new InternalValue(this);
            constructed_value.appendText(fixedValues_.get(valueId));
            if (!appendBlockInternalForm(blockId, constructed_value)) {
                throw new BlockUnknownException(blockId);
            }
            constructedValues_.put(valueId, constructed_value);
            fixedValues_.remove(valueId);
        } else if (constructedValues_.containsKey(valueId)) {
            if (!appendBlockInternalForm(blockId, constructedValues_.get(valueId))) {
                throw new BlockUnknownException(blockId);
            }
        } else {
            InternalValue constructed_value = new InternalValue(this);
            if (!appendBlockInternalForm(blockId, constructed_value)) {
                throw new BlockUnknownException(blockId);
            }
            constructedValues_.put(valueId, constructed_value);
        }
    }

    public final void setBlock(String valueId, String blockId)
    throws TemplateException {
        if (null == valueId ||
            0 == valueId.length() ||
            !hasValueId(valueId)) {
            throw new ValueUnknownException(valueId);
        }
        if (null == blockId ||
            0 == blockId.length()) {
            throw new BlockUnknownException(blockId);
        }

        fixedValues_.remove(valueId);

        InternalValue constructed_value = new InternalValue(this);
        if (!appendBlockInternalForm(blockId, constructed_value)) {
            throw new BlockUnknownException(blockId);
        }
        constructedValues_.put(valueId, constructed_value);
    }

    public String getBlock(String id)
    throws TemplateException {
        if (null == id ||
            0 == id.length()) {
            throw new BlockUnknownException(id);
        }

        ExternalValue result = new ExternalValue();

        if (!appendBlockExternalForm(id, result)) {
            throw new BlockUnknownException(id);
        }
        return result.toString();
    }

    public final String getContent()
    throws TemplateException {
        List<String> set_values = processLateTags();

        ExternalValue result = new ExternalValue();

        if (!appendBlockExternalForm("", result)) {
            throw new BlockUnknownException("");
        }
        String content = result.toString();
        removeValues(set_values);
        return content;
    }

    public void writeBlock(String id, OutputStream out)
    throws IOException, TemplateException {
        writeBlock(id, out, null);
    }

    public void writeBlock(String id, OutputStream out, String charsetName)
    throws IOException, TemplateException {
        if (null == out) {
            return;
        }
        if (null == id ||
            0 == id.length()) {
            throw new BlockUnknownException(id);
        }

        ExternalValue result = new ExternalValue();

        if (!appendBlockExternalForm(id, result)) {
            throw new BlockUnknownException(id);
        }

        result.write(out, charsetName);
    }

    public final void writeContent(OutputStream out)
    throws IOException, TemplateException {
        writeContent(out, null);
    }

    public final void writeContent(OutputStream out, String charsetName)
    throws IOException, TemplateException {
        if (null == out) {
            return;
        }

        ExternalValue result = new ExternalValue();

        if (!appendBlockExternalForm("", result)) {
            throw new BlockUnknownException("");
        }

        result.write(out, charsetName);
    }

    public final void write(OutputStream out)
    throws IOException, TemplateException {
        writeContent(out);
    }

    public final List<CharSequence> getDeferredBlock(String id)
    throws TemplateException {
        if (null == id ||
            0 == id.length()) {
            throw new BlockUnknownException(id);
        }

        ExternalValue result = new ExternalValue();

        if (!appendBlockExternalForm(id, result)) {
            throw new BlockUnknownException(id);
        }

        return result;
    }

    public final List<CharSequence> getDeferredContent()
    throws TemplateException {
        List<String> set_values = processLateTags();

        ExternalValue result = new ExternalValue();

        if (!appendBlockExternalForm("", result)) {
            throw new BlockUnknownException("");
        }

        removeValues(set_values);

        return result;
    }

    private List<String> processLateTags() {
        var set_values = new ArrayList<String>();

        _evaluateL10nTags(set_values);
        _evaluateRenderTags(set_values);
        _evaluateLangTags(set_values, null);
//        _evaluateOgnlTags(set_values, null);
//        _evaluateOgnlConfigTags(set_values, null);
//        _evaluateMvelTags(set_values, null);
//        _evaluateMvelConfigTags(set_values, null);
//        _evaluateGroovyTags(set_values, null);
//        _evaluateGroovyConfigTags(set_values, null);
//        _evaluateJaninoTags(set_values, null);
//        _evaluateJaninoConfigTags(set_values, null);

        return set_values;
    }

    public List<String> evaluateRenderTags()
    throws TemplateException {
        List<String> set_values = new ArrayList<String>();
        _evaluateRenderTags(set_values);
        return set_values;
    }

    private void _evaluateRenderTags(List<String> setValues)
    throws TemplateException {
        if (hasFilteredValues(TemplateFactoryFilters.TAG_RENDER)) {
            List<String[]> render_tags = getFilteredValues(TemplateFactoryFilters.TAG_RENDER);
            for (String[] captured_groups : render_tags) {
                // only execute the renderer if the value hasn't been set in the
                // template yet
                if (!isValueSet(captured_groups[0])) {
                    String classname = captured_groups[1];
                    try {
                        Class klass = Class.forName(classname);
                        if (!ValueRenderer.class.isAssignableFrom(klass)) {
                            throw new RendererWrongTypeException(this, classname);
                        }

                        ValueRenderer renderer = null;
                        try {
                            renderer = (ValueRenderer) klass.getDeclaredConstructor().newInstance();
                        } catch (Exception e) {
                            throw new RendererInstantiationException(this, classname, e);
                        }

                        setValue(captured_groups[0], renderer.render(this, captured_groups[0], captured_groups[2]));
                        if (setValues != null) {
                            setValues.add(captured_groups[0]);
                        }
                    } catch (ClassNotFoundException e) {
                        throw new RendererNotFoundException(this, classname, e);
                    }
                }
            }
        }
    }

    public List<String> evaluateConfigTags() {
        List<String> set_values = new ArrayList<String>();
        _evaluateConfigTags(set_values);
        return set_values;
    }

    private void _evaluateConfigTags(List<String> setValues) {
        // process the config tags
        // TODO
        List<String[]> config_tags = getFilteredValues(TemplateFactoryFilters.TAG_CONFIG);
        if (config_tags != null) {
            String config_key = null;
            String config_value = null;

            for (String[] captured_groups : config_tags) {
                // only set the config value if the value hasn't been set in the
                // template yet
                if (!isValueSet(captured_groups[0])) {
                    config_key = captured_groups[1];

                    // obtain the configuration value
                    config_value = Config.instance().getString(config_key);

                    // don't continue if the config parameter doesn't exist
                    if (config_value != null) {
                        // set the config value in the template
                        setValue(captured_groups[0], getEncoder().encode(config_value));
                        if (setValues != null) {
                            setValues.add(captured_groups[0]);
                        }
                    }
                }
            }
        }
    }

    public List<String> evaluateL10nTags() {
        var set_values = new ArrayList<String>();
        _evaluateL10nTags(set_values);
        return set_values;
    }

    private void _evaluateL10nTags(List<String> setValues) {
        // process the localization keys
        var l10n_tags = getFilteredValues(TemplateFactoryFilters.TAG_L10N);
        if (l10n_tags != null && l10n_tags.size() > 0) {
            String l10n_key;
            String l10n_value;
            String l10n_bundle;

            for (var captured_groups : l10n_tags) {
                // only set the config value if the value hasn't been set in the
                // template yet
                if (!isValueSet(captured_groups[0])) {
                    l10n_value = null;

                    // check if an explicit bundle name was provided
                    // if not go through all the bundles that have been registered
                    // for this template instance
                    if (null == captured_groups[2]) {
                        if (hasResourceBundles()) {
                            l10n_key = captured_groups[1];

                            for (ResourceBundle bundle : resourceBundles_) {
                                // obtain the configuration value
                                try {
                                    l10n_value = bundle.getString(l10n_key);
                                    break;
                                } catch (MissingResourceException e) {
                                    // no-op, go to the next resource bundle
                                }
                            }
                        }
                    } else {
                        l10n_bundle = captured_groups[1];
                        l10n_key = captured_groups[2];

                        var bundle = Localization.getResourceBundle(l10n_bundle);
                        if (bundle != null) {
                            l10n_value = bundle.getString(l10n_key);
                        } else {
                            throw new ResourceBundleNotFoundException(getName(), captured_groups[0], l10n_bundle);
                        }
                    }

                    // don't continue if the config parameter doesn't exist
                    if (l10n_value != null) {
                        // set the config value in the template
                        setValue(captured_groups[0], getEncoder().encodeDefensive(l10n_value));
                        if (setValues != null) {
                            setValues.add(captured_groups[0]);
                        }
                    }
                }
            }
        }
    }

    public List<String> evaluateLangTags(String id) {
        if (null == id) throw new IllegalArgumentException("id can't be null.");

        var set_values = new ArrayList<String>();
        _evaluateLangTags(set_values, TemplateFactoryFilters.PREFIX_LANG + id);
        return set_values;
    }

    private void _evaluateLangTags(List<String> setValues, String id) {
        // process the lang keys
        var lang_blocks = getFilteredBlocks(TemplateFactoryFilters.TAG_LANG);
        var language = getLanguage();
        if (lang_blocks != null &&
            language != null) {

            for (var lang_block : lang_blocks) {
                if (id != null &&
                    !id.equals(lang_block[1])) {
                    continue;
                }

                if (null == id &&
                    isValueSet(lang_block[1])) {
                    continue;
                }

                var block_lang = lang_block[lang_block.length - 1];
                if (block_lang.equals(language)) {
                    setBlock(lang_block[1], lang_block[0]);
                    if (setValues != null) {
                        setValues.add(lang_block[1]);
                    }
                }
            }
        }
    }

//    public List<String> evaluateExpressionTags(String id) {
//        if (null == id) throw new IllegalArgumentException("id can't be null.");
//
//        List<String> set_values = new ArrayList<String>();
//        _evaluateOgnlTags(set_values, TemplateFactory.PREFIX_OGNL + id);
//        _evaluateMvelTags(set_values, TemplateFactory.PREFIX_MVEL + id);
//        _evaluateGroovyTags(set_values, TemplateFactory.PREFIX_GROOVY + id);
//        _evaluateJaninoTags(set_values, TemplateFactory.PREFIX_JANINO + id);
//        return set_values;
//    }
//
//    public List<String> evaluateExpressionConfigTags(String id) {
//        if (null == id) throw new IllegalArgumentException("id can't be null.");
//
//        List<String> set_values = new ArrayList<String>();
//        _evaluateOgnlConfigTags(set_values, TemplateFactory.PREFIX_OGNL_CONFIG + id);
//        _evaluateMvelConfigTags(set_values, TemplateFactory.PREFIX_MVEL_CONFIG + id);
//        _evaluateGroovyConfigTags(set_values, TemplateFactory.PREFIX_GROOVY_CONFIG + id);
//        _evaluateJaninoConfigTags(set_values, TemplateFactory.PREFIX_JANINO_CONFIG + id);
//        return set_values;
//    }
//
//    private void _evaluateOgnlTags(List<String> setValues, String id) {
//        if (hasFilteredBlocks(TemplateFactory.TAG_OGNL)) {
//            FilteredTagProcessorOgnl.instance().processTags(setValues, this, getFilteredBlocks(TemplateFactory.TAG_OGNL), id, Template.class, "template", this, null);
//        }
//    }
//
//    private void _evaluateOgnlConfigTags(List<String> setValues, String id) {
//        if (hasFilteredBlocks(TemplateFactory.TAG_OGNL_CONFIG)) {
//            FilteredTagProcessorOgnl.instance().processTags(setValues, this, getFilteredBlocks(TemplateFactory.TAG_OGNL_CONFIG), id, Config.class, "config", Config.getRepInstance(), null);
//        }
//    }
//
//    private void _evaluateMvelTags(List<String> setValues, String id) {
//        if (hasFilteredBlocks(TemplateFactory.TAG_MVEL)) {
//            FilteredTagProcessorMvel.instance().processTags(setValues, this, getFilteredBlocks(TemplateFactory.TAG_MVEL), id, Template.class, "template", this, null);
//        }
//    }
//
//    private void _evaluateMvelConfigTags(List<String> setValues, String id) {
//        if (hasFilteredBlocks(TemplateFactory.TAG_MVEL_CONFIG)) {
//            FilteredTagProcessorMvel.instance().processTags(setValues, this, getFilteredBlocks(TemplateFactory.TAG_MVEL_CONFIG), id, Config.class, "config", Config.getRepInstance(), null);
//        }
//    }
//
//    private void _evaluateGroovyTags(List<String> setValues, String id) {
//        if (hasFilteredBlocks(TemplateFactory.TAG_GROOVY)) {
//            FilteredTagProcessorGroovy.instance().processTags(setValues, this, getFilteredBlocks(TemplateFactory.TAG_GROOVY), id, Template.class, "template", this, null);
//        }
//    }
//
//    private void _evaluateGroovyConfigTags(List<String> setValues, String id) {
//        if (hasFilteredBlocks(TemplateFactory.TAG_GROOVY_CONFIG)) {
//            FilteredTagProcessorGroovy.instance().processTags(setValues, this, getFilteredBlocks(TemplateFactory.TAG_GROOVY_CONFIG), id, Config.class, "config", Config.getRepInstance(), null);
//        }
//    }
//
//    private void _evaluateJaninoTags(List<String> setValues, String id) {
//        if (hasFilteredBlocks(TemplateFactory.TAG_JANINO)) {
//            FilteredTagProcessorJanino.instance().processTags(setValues, this, getFilteredBlocks(TemplateFactory.TAG_JANINO), id, Template.class, "template", this, null);
//        }
//    }
//
//    private void _evaluateJaninoConfigTags(List<String> setValues, String id) {
//        if (hasFilteredBlocks(TemplateFactory.TAG_JANINO_CONFIG)) {
//            FilteredTagProcessorJanino.instance().processTags(setValues, this, getFilteredBlocks(TemplateFactory.TAG_JANINO_CONFIG), id, Config.class, "config", Config.getRepInstance(), null);
//        }
//    }

    public final InternalValue createInternalValue() {
        return new InternalValue(this);
    }

    public final void setValue(String id, List<CharSequence> deferredContent)
    throws TemplateException {
        if (null == id ||
            0 == id.length() ||
            !hasValueId(id)) {
            throw new ValueUnknownException(id);
        }

        fixedValues_.remove(id);

        constructedValues_.put(id, new InternalValue(this, deferredContent));
    }

    public final void setValue(String id, InternalValue internalValue)
    throws TemplateException {
        if (null == id ||
            0 == id.length() ||
            !hasValueId(id)) {
            throw new ValueUnknownException(id);
        }
        if (null == internalValue) {
            internalValue = createInternalValue();
        }

        fixedValues_.remove(id);

        constructedValues_.put(id, internalValue);
    }

    public final void setValue(String id, Template template)
    throws TemplateException {
        if (null == template) {
            setValue(id, "");
        } else {
            setValue(id, template.getContent());
        }
    }

    public final void setValue(String id, Object value)
    throws TemplateException {
        setValue(id, String.valueOf(value));
    }

    public final void setValue(String id, boolean value)
    throws TemplateException {
        setValue(id, String.valueOf(value));
    }

    public final void setValue(String id, char value)
    throws TemplateException {
        setValue(id, String.valueOf(value));
    }

    public final void setValue(String id, char[] value)
    throws TemplateException {
        setValue(id, String.valueOf(value));
    }

    public final void setValue(String id, char[] value, int offset, int count)
    throws TemplateException {
        setValue(id, String.valueOf(value, offset, count));
    }

    public final void setValue(String id, double value)
    throws TemplateException {
        setValue(id, String.valueOf(value));
    }

    public final void setValue(String id, float value)
    throws TemplateException {
        setValue(id, String.valueOf(value));
    }

    public final void setValue(String id, int value)
    throws TemplateException {
        setValue(id, String.valueOf(value));
    }

    public final void setValue(String id, long value)
    throws TemplateException {
        setValue(id, String.valueOf(value));
    }

    public final void setValue(String id, String value)
    throws TemplateException {
        setValue(id, (CharSequence) value);
    }

    public final void setValue(String id, CharSequence value)
    throws TemplateException {
        if (null == id ||
            0 == id.length() ||
            !hasValueId(id)) {
            throw new ValueUnknownException(id);
        }
        if (null == value) {
            value = "";
        }

        fixedValues_.remove(id);
        constructedValues_.remove(id);
        fixedValues_.put(id, new InternalString(value));
    }

    public void setBean(Object bean)
    throws TemplateException {
        setBean(bean, null, true);
    }

    public void setBean(Object bean, String prefix)
    throws TemplateException {
        setBean(bean, prefix, true);
    }

    public void setBean(Object bean, String prefix, boolean encode)
    throws TemplateException {
        if (null == beanHandler_) {
            throw new BeanHandlerUnsupportedException(this, bean);
        }

        beanHandler_.setBean(this, bean, prefix, encode);
    }

    public void removeBean(Object bean)
    throws TemplateException {
        removeBean(bean, null);
    }

    public void removeBean(Object bean, String prefix)
    throws TemplateException {
        if (null == beanHandler_) {
            throw new BeanHandlerUnsupportedException(this, bean);
        }

        beanHandler_.removeBean(this, bean, prefix);
    }

    public final void appendValue(String id, Object value)
    throws TemplateException {
        appendValue(id, String.valueOf(value));
    }

    public final void appendValue(String id, boolean value)
    throws TemplateException {
        appendValue(id, String.valueOf(value));
    }

    public final void appendValue(String id, char value)
    throws TemplateException {
        appendValue(id, String.valueOf(value));
    }

    public final void appendValue(String id, char[] value)
    throws TemplateException {
        appendValue(id, String.valueOf(value));
    }

    public final void appendValue(String id, char[] value, int offset, int count)
    throws TemplateException {
        appendValue(id, String.valueOf(value, offset, count));
    }

    public final void appendValue(String id, double value)
    throws TemplateException {
        appendValue(id, String.valueOf(value));
    }

    public final void appendValue(String id, float value)
    throws TemplateException {
        appendValue(id, String.valueOf(value));
    }

    public final void appendValue(String id, int value)
    throws TemplateException {
        appendValue(id, String.valueOf(value));
    }

    public final void appendValue(String id, long value)
    throws TemplateException {
        appendValue(id, String.valueOf(value));
    }

    public final void appendValue(String id, String value)
    throws TemplateException {
        if (null == id ||
            0 == id.length() ||
            !hasValueId(id)) {
            throw new ValueUnknownException(id);
        }
        if (null == value) {
            return;
        }

        if (fixedValues_.containsKey(id)) {
            fixedValues_.get(id).append(value);
        } else if (constructedValues_.containsKey(id)) {
            constructedValues_.get(id).appendText(value);
        } else {
            fixedValues_.put(id, new InternalString(value));
        }
    }

    public final String getValue(String id)
    throws TemplateException {
        if (null == id ||
            0 == id.length() ||
            !hasValueId(id)) {
            throw new ValueUnknownException(id);
        }

        if (fixedValues_.containsKey(id)) {
            return fixedValues_.get(id).toString();
        }
        if (constructedValues_.containsKey(id)) {
            ExternalValue result = new ExternalValue();
            constructedValues_.get(id).appendExternalForm(result);
            return result.toString();
        }

        return getDefaultValue(id);
    }

    public abstract String getDefaultValue(String id);

    public boolean hasDefaultValue(String id) {
        if (null == getDefaultValue(id)) {
            return false;
        }

        return true;
    }

    public abstract List<String[]> getFilteredBlocks(String filter);

    public abstract boolean hasFilteredBlocks(String filter);

    public abstract List<String[]> getFilteredValues(String filter);

    public abstract boolean hasFilteredValues(String filter);

    public final boolean hasBlock(String id) {
        if (null == id ||
            0 == id.length()) {
            return false;
        }

        ExternalValue temp_value = new ExternalValue();

        return appendBlockExternalForm(id, temp_value);
    }

    public final boolean isValueSet(String id) {
        if (null == id ||
            0 == id.length()) {
            return false;
        }

        return fixedValues_.containsKey(id) || constructedValues_.containsKey(id);
    }

    public final int countValues() {
        return fixedValues_.size() + constructedValues_.size();
    }

    public final void removeValue(String id) {
        if (null == id ||
            0 == id.length() ||
            !hasValueId(id)) {
            throw new ValueUnknownException(id);
        }

        fixedValues_.remove(id);
        constructedValues_.remove(id);
    }

    public final void removeValues(List<String> ids) {
        if (null == ids ||
            0 == ids.size()) {
            return;
        }

        for (String id : ids) {
            removeValue(id);
        }
    }

    public final void blankValue(String id) {
        setValue(id, "");
    }

    public final void clear() {
        fixedValues_ = new HashMap<>();
        constructedValues_ = new HashMap<>();
        resourceBundles_ = null;
        if (defaultResourceBundles_ != null) {
            resourceBundles_ = new ArrayList<>(defaultResourceBundles_);
        }
        initialize();
    }

    public abstract String[] getAvailableValueIds();

    public abstract Collection<String> getUnsetValueIds();

    public abstract boolean hasValueId(String id);

    public abstract long getModificationTime();

    // make the template only instantiatable from within this package or from derived classes
    protected AbstractTemplate() {
    }

    protected void appendTextInternal(InternalValue value, CharSequence text) {
        value.appendText(text);
    }

    protected void increasePartsCapacityInternal(InternalValue value, int size) {
        value.increasePartsCapacity(size);
    }

    protected void increaseValuesCapacityInternal(InternalValue value, int size) {
        value.increaseValuesCapacity(size);
    }

    protected abstract boolean appendBlockExternalForm(String id, ExternalValue result);

    protected abstract boolean appendBlockInternalForm(String id, InternalValue result);

    protected final void appendValueExternalForm(String id, String tag, ExternalValue result) {
        assert id != null;
        assert id.length() != 0;

        CharSequence fixed_value = fixedValues_.get(id);
        if (fixed_value != null) {
            result.add(fixed_value);
            return;
        }

        InternalValue constructed_value = constructedValues_.get(id);
        if (constructed_value != null) {
            constructed_value.appendExternalForm(result);
            return;
        }

        if (!appendDefaultValueExternalForm(id, result)) {
            result.add(tag);
        }
    }

    protected abstract boolean appendDefaultValueExternalForm(String id, ExternalValue result);

    protected final void appendValueInternalForm(String id, String tag, InternalValue result) {
        CharSequence fixed_value = fixedValues_.get(id);
        if (fixed_value != null) {
            result.appendText(fixed_value);
            return;
        }

        InternalValue constructed_value = constructedValues_.get(id);
        if (constructed_value != null) {
            result.appendConstructedValue(constructed_value);
            return;
        }

        if (!appendDefaultValueInternalForm(id, result)) {
            result.appendValueId(id, tag);
        }
    }

    protected abstract boolean appendDefaultValueInternalForm(String id, InternalValue result);

    public final BeanHandler getBeanHandler() {
        return beanHandler_;
    }

    final void setBeanHandler(BeanHandler beanHandler) {
        beanHandler_ = beanHandler;
    }

    public final TemplateEncoder getEncoder() {
        return encoder_;
    }

    final void setEncoder(TemplateEncoder encoder) {
        if (null == encoder) {
            encoder_ = EncoderDummy.instance();
        } else {
            encoder_ = encoder;
        }
    }

    void setDefaultResourceBundles(ArrayList<ResourceBundle> bundles) {
        defaultResourceBundles_ = bundles;
        if (bundles != null) {
            resourceBundles_ = new ArrayList<>(bundles);
        }
    }

    public final void addResourceBundle(ResourceBundle resourceBundle) {
        if (null == resourceBundle) {
            return;
        }

        if (null == resourceBundles_) {
            resourceBundles_ = new ArrayList<>();
        }

        resourceBundles_.add(resourceBundle);
    }

    public final Collection<ResourceBundle> getResourceBundles() {
        if (null == resourceBundles_) {
            resourceBundles_ = new ArrayList<>();
        }

        return resourceBundles_;
    }

    public final boolean hasResourceBundles() {
        return resourceBundles_ != null && resourceBundles_.size() > 0;
    }

    public void setLanguage(String lang) {
        language_ = lang;
    }

    public String getLanguage() {
        if (null == language_) {
            return RifeConfig.tools().getDefaultLanguage();
        }

        return language_;
    }

    public void setExpressionVar(String name, Object value) {
        if (null == expressionVars_) {
            expressionVars_ = new HashMap<>();
        }

        expressionVars_.put(name, value);
    }

    public void setExpressionVars(Map<String, Object> map) {
        expressionVars_ = map;
    }

    public Map<String, Object> getExpressionVars() {
        return expressionVars_;
    }

    final void initialize()
    throws TemplateException {
        _evaluateConfigTags(null);
        _evaluateL10nTags(null);

        if (null == initializer_) {
            return;
        }

        initializer_.initialize(this);
    }

    final void setInitializer(TemplateInitializer initializer) {
        initializer_ = initializer;
    }

    public void cacheObject(String key, Object value) {
        if (null == key) {
            return;
        }

        if (null == cache_) {
            cache_ = new HashMap<>();
        }

        cache_.put(key, value);
    }

    public Object getCacheObject(String key) {
        if (null == cache_) {
            return null;
        }

        return cache_.get(key);
    }

    public String getDefaultContentType() {
        return defaultContentType_;
    }

    public void setDefaultContentType(String defaultContentType) {
        defaultContentType_ = defaultContentType;
    }

    protected static boolean isTemplateClassModified(URL templateResource, long templateModificationTime,
                                                     Map templateDependencies, String templateModificationState,
                                                     ResourceFinder resourceFinder, String modificationState) {
        try {
            if (Parser.getModificationTime(resourceFinder, templateResource) > templateModificationTime) {
                return true;
            }

            if (templateDependencies.size() > 0) {
                Iterator url_it = templateDependencies.keySet().iterator();
                URL dependency_resource = null;
                while (url_it.hasNext()) {
                    dependency_resource = (URL) url_it.next();
                    if (Parser.getModificationTime(resourceFinder, dependency_resource) > (Long) templateDependencies.get(dependency_resource)) {
                        return true;
                    }
                }
            }

            if (templateModificationState != null || modificationState != null) {
                if (null == templateModificationState || null == modificationState) {
                    return true;
                }

                if (!templateModificationState.equals(modificationState)) {
                    return true;
                }
            }

        } catch (TemplateException e) {
            return false;
        }

        return false;
    }

    public Template clone() {
        AbstractTemplate new_template;
        try {
            new_template = (AbstractTemplate) super.clone();
        } catch (CloneNotSupportedException e) {
            new_template = null;
        }

        new_template.beanHandler_ = beanHandler_;
        new_template.initializer_ = initializer_;
        new_template.encoder_ = encoder_;
        new_template.language_ = language_;
        new_template.defaultContentType_ = defaultContentType_;

        new_template.fixedValues_ = new HashMap<>();

        for (String value_id : fixedValues_.keySet()) {
            new_template.fixedValues_.put(value_id, fixedValues_.get(value_id));
        }

        new_template.constructedValues_ = new HashMap<>();

        for (String constructed_value_id : constructedValues_.keySet()) {
            new_template.constructedValues_.put(constructed_value_id, constructedValues_.get(constructed_value_id));
        }

        if (expressionVars_ != null) {
            new_template.expressionVars_ = new HashMap<>(expressionVars_);
        }

        return new_template;
    }
}
