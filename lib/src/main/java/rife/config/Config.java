/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.config;

import rife.config.exceptions.*;
import rife.tools.*;

import java.util.*;

import rife.tools.exceptions.SerializationUtilsErrorException;
import rife.config.exceptions.ConfigErrorException;
import rife.config.exceptions.MissingPreferencesUserNodeException;

import java.io.Serializable;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Config implements Cloneable {
    public static final String PARAMETER_PREFERENCES_USER = "CONFIG_PREFERENCES_USER";
    public static final String PARAMETER_PREFERENCES_SYSTEM = "CONFIG_PREFERENCES_SYSTEM";

    private HashMap<String, String> parameters_;
    private ArrayList<String> finalParameters_;
    private HashMap<String, ArrayList<String>> lists_;
    private ArrayList<String> finalLists_;

    /**
     * Returns the shared singleton instance of the
     * <code>Config</code> class.
     *
     * @return the singleton <code>Config</code> instance
     * @since 2.0
     */
    public static Config instance() {
        return ConfigSingleton.INSTANCE;
    }

    public Config() {
        parameters_ = new HashMap<>();
        finalParameters_ = new ArrayList<>();
        lists_ = new HashMap<>();
        finalLists_ = new ArrayList<>();
    }

    private boolean isPreferencesParameter(String parameter) {
        if (null == parameter) {
            return false;
        }

        return parameter.equals(PARAMETER_PREFERENCES_SYSTEM) ||
            parameter.equals(PARAMETER_PREFERENCES_USER);

    }

    public void setPreferencesNode(Preferences node) {
        if (null == node) throw new IllegalArgumentException("node can't be null.");

        if (node.isUserNode()) {
            setParameter(PARAMETER_PREFERENCES_USER, node.absolutePath());
            removeParameter(PARAMETER_PREFERENCES_SYSTEM);
        } else {
            removeParameter(PARAMETER_PREFERENCES_USER);
            setParameter(PARAMETER_PREFERENCES_SYSTEM, node.absolutePath());
        }
    }

    public boolean hasPreferencesNode() {
        return parameters_.containsKey(PARAMETER_PREFERENCES_USER) || parameters_.containsKey(PARAMETER_PREFERENCES_SYSTEM);
    }

    public Preferences getPreferencesNode() {
        if (parameters_.containsKey(PARAMETER_PREFERENCES_USER)) {
            return Preferences.userRoot().node(getString(PARAMETER_PREFERENCES_USER));
        }

        if (parameters_.containsKey(PARAMETER_PREFERENCES_SYSTEM)) {
            return Preferences.systemRoot().node(getString(PARAMETER_PREFERENCES_SYSTEM));
        }

        return null;
    }

    public boolean hasParameter(String parameter) {
        if (null == parameter) throw new IllegalArgumentException("parameter can't be null.");
        if (0 == parameter.length()) throw new IllegalArgumentException("parameter can't be empty.");

        if (parameters_.containsKey(parameter)) {
            return true;
        }

        if (!isPreferencesParameter(parameter) &&
            hasPreferencesNode()) {
            Preferences preferences = getPreferencesNode();

            if (preferences != null &&
                preferences.get(parameter, null) != null) {
                return true;
            }
        }

        return false;
    }

    public boolean isFinalParameter(String parameter) {
        if (null == parameter) throw new IllegalArgumentException("parameter can't be null.");
        if (0 == parameter.length()) throw new IllegalArgumentException("parameter can't be empty.");

        return finalParameters_.contains(parameter);
    }

    public int countParameters() {
        int result = parameters_.size();

        if (hasPreferencesNode()) {
            Preferences preferences = getPreferencesNode();

            if (preferences != null) {
                try {
                    String keys[] = preferences.keys();

                    for (String key : keys) {
                        if (!parameters_.containsKey(key)) {
                            result++;
                        }
                    }
                } catch (BackingStoreException e) {
                    // that's ok, don't handle the preferences node
                }
            }
        }

        return result;
    }

    public String getString(String parameter) {
        if (null == parameter) throw new IllegalArgumentException("parameter can't be null.");
        if (0 == parameter.length()) throw new IllegalArgumentException("parameter can't be empty.");

        return getString(parameter, null);
    }

    public String getString(String parameter, String defaultValue) {
        if (null == parameter) throw new IllegalArgumentException("parameter can't be null.");
        if (0 == parameter.length()) throw new IllegalArgumentException("parameter can't be empty.");

        String result = null;

        synchronized (this) {
            if (!finalParameters_.contains(parameter) &&
                !isPreferencesParameter(parameter) &&
                hasPreferencesNode()) {
                Preferences preferences = getPreferencesNode();

                if (preferences != null) {
                    result = preferences.get(parameter, null);
                }
            }

            if (null == result &&
                null != parameters_) {
                result = parameters_.get(parameter);
            }
        }

        if (null == result) {
            result = defaultValue;
        }

        return result;
    }

    public boolean getBool(String parameter) {
        if (null == parameter) throw new IllegalArgumentException("parameter can't be null.");
        if (0 == parameter.length()) throw new IllegalArgumentException("parameter can't be empty.");

        return getBool(parameter, false);
    }

    public boolean getBool(String parameter, boolean defaultValue) {
        if (null == parameter) throw new IllegalArgumentException("parameter can't be null.");
        if (0 == parameter.length()) throw new IllegalArgumentException("parameter can't be empty.");

        String string_parameter = getString(parameter);
        if (null != string_parameter) {
            return StringUtils.convertToBoolean(string_parameter);
        } else {
            return defaultValue;
        }
    }

    public int getChar(String parameter) {
        if (null == parameter) throw new IllegalArgumentException("parameter can't be null.");
        if (0 == parameter.length()) throw new IllegalArgumentException("parameter can't be empty.");

        return getChar(parameter, (char) 0);
    }

    public int getChar(String parameter, char defaultValue) {
        if (null == parameter) throw new IllegalArgumentException("parameter can't be null.");
        if (0 == parameter.length()) throw new IllegalArgumentException("parameter can't be empty.");

        String parameter_string_value = getString(parameter);

        if (null != parameter_string_value &&
            parameter_string_value.length() > 0) {
            return parameter_string_value.charAt(0);
        } else {
            return defaultValue;
        }
    }

    public int getInt(String parameter) {
        if (null == parameter) throw new IllegalArgumentException("parameter can't be null.");
        if (0 == parameter.length()) throw new IllegalArgumentException("parameter can't be empty.");

        return getInt(parameter, 0);
    }

    public int getInt(String parameter, int defaultValue) {
        if (null == parameter) throw new IllegalArgumentException("parameter can't be null.");
        if (0 == parameter.length()) throw new IllegalArgumentException("parameter can't be empty.");

        String string_parameter = getString(parameter);

        if (null != string_parameter) {
            try {
                return Integer.parseInt(string_parameter);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    public long getLong(String parameter) {
        if (null == parameter) throw new IllegalArgumentException("parameter can't be null.");
        if (0 == parameter.length()) throw new IllegalArgumentException("parameter can't be empty.");

        return getLong(parameter, 0L);
    }

    public long getLong(String parameter, long defaultValue) {
        if (null == parameter) throw new IllegalArgumentException("parameter can't be null.");
        if (0 == parameter.length()) throw new IllegalArgumentException("parameter can't be empty.");

        String string_parameter = getString(parameter);

        if (null != string_parameter) {
            try {
                return Long.parseLong(string_parameter);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    public float getFloat(String parameter) {
        if (null == parameter) throw new IllegalArgumentException("parameter can't be null.");
        if (0 == parameter.length()) throw new IllegalArgumentException("parameter can't be empty.");

        return getFloat(parameter, 0F);
    }

    public float getFloat(String parameter, float defaultValue) {
        if (null == parameter) throw new IllegalArgumentException("parameter can't be null.");
        if (0 == parameter.length()) throw new IllegalArgumentException("parameter can't be empty.");

        String string_parameter = getString(parameter);

        if (null != string_parameter) {
            try {
                return Float.parseFloat(string_parameter);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    public double getDouble(String parameter) {
        if (null == parameter) throw new IllegalArgumentException("parameter can't be null.");
        if (0 == parameter.length()) throw new IllegalArgumentException("parameter can't be empty.");

        return getDouble(parameter, 0D);
    }

    public double getDouble(String parameter, double defaultValue) {
        if (null == parameter) throw new IllegalArgumentException("parameter can't be null.");
        if (0 == parameter.length()) throw new IllegalArgumentException("parameter can't be empty.");

        String string_parameter = getString(parameter);

        if (null != string_parameter) {
            try {
                return Double.parseDouble(string_parameter);
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    public <TargetType extends Serializable> TargetType getSerializable(String parameter) {
        if (null == parameter) throw new IllegalArgumentException("parameter can't be null.");
        if (0 == parameter.length()) throw new IllegalArgumentException("parameter can't be empty.");

        return (TargetType) getSerializable(parameter, null);
    }

    public <TargetType extends Serializable> TargetType getSerializable(String parameter, TargetType defaultValue) {
        if (null == parameter) throw new IllegalArgumentException("parameter can't be null.");
        if (0 == parameter.length()) throw new IllegalArgumentException("parameter can't be empty.");

        String value = getString(parameter);
        if (null != value) {
            try {
                return (TargetType) SerializationUtils.deserializeFromString(value);
            } catch (SerializationUtilsErrorException e) {
                return defaultValue;
            }
        } else {
            return defaultValue;
        }
    }

    public void setFinalParameter(String parameter, boolean isFinal) {
        if (null == parameter) throw new IllegalArgumentException("parameter can't be null.");
        if (0 == parameter.length()) throw new IllegalArgumentException("parameter can't be empty.");

        if (isFinal &&
            !finalParameters_.contains(parameter)) {
            finalParameters_.add(parameter);
        } else {
            finalParameters_.remove(parameter);
        }
    }

    public void setParameter(String parameter, String value) {
        if (null == parameter) throw new IllegalArgumentException("parameter can't be null.");
        if (0 == parameter.length()) throw new IllegalArgumentException("parameter can't be empty.");
        if (null == value) throw new IllegalArgumentException("value can't be null.");

        synchronized (this) {
            if (!finalParameters_.contains(parameter)) {
                parameters_.put(parameter, value);

                if (!isPreferencesParameter(parameter) &&
                    hasPreferencesNode()) {
                    Preferences preferences = getPreferencesNode();

                    if (preferences != null &&
                        preferences.get(parameter, null) != null) {
                        preferences.put(parameter, value);
                    }
                }
            }
        }
    }

    public void setParameter(String parameter, boolean value) {
        setParameter(parameter, String.valueOf(value));
    }

    public void setParameter(String parameter, char value) {
        setParameter(parameter, String.valueOf(value));
    }

    public void setParameter(String parameter, int value) {
        setParameter(parameter, String.valueOf(value));
    }

    public void setParameter(String parameter, long value) {
        setParameter(parameter, String.valueOf(value));
    }

    public void setParameter(String parameter, float value) {
        setParameter(parameter, String.valueOf(value));
    }

    public void setParameter(String parameter, double value) {
        setParameter(parameter, String.valueOf(value));
    }

    public void setParameter(String parameter, Serializable value)
    throws ConfigErrorException {
        try {
            setParameter(parameter, SerializationUtils.serializeToString(value));
        } catch (SerializationUtilsErrorException e) {
            throw new ConfigErrorException(e);
        }
    }

    public void removeParameter(String parameter) {
        if (null == parameter) throw new IllegalArgumentException("parameter can't be null.");
        if (0 == parameter.length()) throw new IllegalArgumentException("parameter can't be empty.");

        if (finalParameters_.contains(parameter)) {
            return;
        }

        synchronized (this) {
            parameters_.remove(parameter);

            if (!isPreferencesParameter(parameter) &&
                hasPreferencesNode()) {
                Preferences preferences = getPreferencesNode();

                if (preferences != null) {
                    preferences.remove(parameter);
                }
            }
        }
    }

    public boolean isFinalList(String list) {
        if (null == list) throw new IllegalArgumentException("list can't be null.");
        if (0 == list.length()) throw new IllegalArgumentException("list can't be empty.");

        return finalLists_.contains(list);
    }

    public Collection<String> getStringItems(String list) {
        if (null == list) throw new IllegalArgumentException("list can't be null.");
        if (0 == list.length()) throw new IllegalArgumentException("list can't be empty.");

        synchronized (this) {
            Collection<String> list_items = null;

            if (!finalLists_.contains(list) &&
                hasPreferencesNode()) {
                Preferences preferences = getPreferencesNode();

                if (preferences != null) {
                    Preferences list_preferences = preferences.node(list);

                    if (list_preferences != null) {
                        try {
                            String[] string_array = list_preferences.keys();
                            if (string_array != null &&
                                string_array.length > 0) {
                                int[] int_array = new int[string_array.length];
                                int counter = 0;
                                for (String item_string : string_array) {
                                    int_array[counter++] = Integer.parseInt(item_string);
                                }
                                Arrays.sort(int_array);

                                list_items = new ArrayList<String>(int_array.length);
                                for (int item_int : int_array) {
                                    list_items.add(list_preferences.get(String.valueOf(item_int), null));
                                }
                            }
                        } catch (BackingStoreException e) {
                            // that's ok, don't handle the preferences node
                        }
                    }
                }
            }

            if (null == list_items) {
                list_items = lists_.get(list);
            }

            return list_items;
        }
    }

    public Collection<Boolean> getBoolItems(String list) {
        if (null == list) throw new IllegalArgumentException("list can't be null.");
        if (0 == list.length()) throw new IllegalArgumentException("list can't be empty.");

        Collection<String> list_items = getStringItems(list);
        ArrayList<Boolean> result = new ArrayList<Boolean>(list_items.size());
        for (String item : list_items) {
            if (item.equalsIgnoreCase("true") ||
                item.equalsIgnoreCase("t") ||
                item.equalsIgnoreCase("1")) {
                result.add(Boolean.TRUE);
            } else {
                result.add(Boolean.FALSE);
            }
        }

        return result;
    }

    public Collection<Character> getCharItems(String list) {
        if (null == list) throw new IllegalArgumentException("list can't be null.");
        if (0 == list.length()) throw new IllegalArgumentException("list can't be empty.");

        Collection<String> list_items = getStringItems(list);
        ArrayList<Character> result = new ArrayList<Character>(list_items.size());
        for (String item : list_items) {
            if (null != item &&
                item.length() > 0) {
                result.add(item.charAt(0));
            }
        }

        return result;
    }

    public Collection<Integer> getIntItems(String list) {
        if (null == list) throw new IllegalArgumentException("list can't be null.");
        if (0 == list.length()) throw new IllegalArgumentException("list can't be empty.");

        Collection<String> list_items = getStringItems(list);
        ArrayList<Integer> result = new ArrayList<Integer>(list_items.size());
        for (String item : list_items) {
            if (null != item) {
                try {
                    result.add(Integer.parseInt(item));
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }

        return result;
    }

    public Collection<Long> getLongItems(String list) {
        if (null == list) throw new IllegalArgumentException("list can't be null.");
        if (0 == list.length()) throw new IllegalArgumentException("list can't be empty.");

        Collection<String> list_items = getStringItems(list);
        ArrayList<Long> result = new ArrayList<Long>(list_items.size());
        for (String item : list_items) {
            if (null != item) {
                try {
                    result.add(Long.parseLong(item));
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }

        return result;
    }

    public Collection<Float> getFloatItems(String list) {
        if (null == list) throw new IllegalArgumentException("list can't be null.");
        if (0 == list.length()) throw new IllegalArgumentException("list can't be empty.");

        Collection<String> list_items = getStringItems(list);
        ArrayList<Float> result = new ArrayList<Float>(list_items.size());
        for (String item : list_items) {
            if (null != item) {
                try {
                    result.add(Float.parseFloat(item));
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }

        return result;
    }

    public Collection<Double> getDoubleItems(String list) {
        if (null == list) throw new IllegalArgumentException("list can't be null.");
        if (0 == list.length()) throw new IllegalArgumentException("list can't be empty.");

        Collection<String> list_items = getStringItems(list);
        ArrayList<Double> result = new ArrayList<Double>(list_items.size());
        for (String item : list_items) {
            if (null != item) {
                try {
                    result.add(Double.parseDouble(item));
                } catch (NumberFormatException e) {
                    continue;
                }
            }
        }

        return result;
    }

    public <TargetType extends Serializable> Collection<TargetType> getSerializableItems(String list) {
        if (null == list) throw new IllegalArgumentException("list can't be null.");
        if (0 == list.length()) throw new IllegalArgumentException("list can't be empty.");

        Collection<String> list_items = getStringItems(list);
        ArrayList<TargetType> result = new ArrayList<TargetType>(list_items.size());
        for (String item : list_items) {
            try {
                result.add((TargetType) SerializationUtils.deserializeFromString(item));
            } catch (SerializationUtilsErrorException e) {
                continue;
            }
        }

        return result;
    }

    public boolean hasList(String list) {
        if (null == list) throw new IllegalArgumentException("list can't be null.");
        if (0 == list.length()) throw new IllegalArgumentException("list can't be empty.");

        if (lists_.containsKey(list)) {
            return true;
        }

        if (hasPreferencesNode()) {
            Preferences preferences = getPreferencesNode();

            if (preferences != null) {
                try {
                    String[] list_names_array = preferences.childrenNames();

                    if (list_names_array != null) {
                        List<String> list_names = Arrays.asList(list_names_array);

                        return list_names.contains(list);
                    }
                } catch (BackingStoreException e) {
                    // that's ok, don't handle the preferences node
                }
            }
        }

        return false;
    }

    public int countLists() {
        int result = lists_.size();

        if (hasPreferencesNode()) {
            Preferences preferences = getPreferencesNode();

            if (preferences != null) {
                try {
                    String[] list_names = preferences.childrenNames();

                    if (list_names != null) {
                        for (String list_name : list_names) {
                            if (!lists_.containsKey(list_name)) {
                                result++;
                            }
                        }
                    }
                } catch (BackingStoreException e) {
                    // that's ok, don't handle the preferences node
                }
            }
        }

        return result;
    }

    public void addListItem(String list, String item) {
        if (null == list) throw new IllegalArgumentException("list can't be null.");
        if (0 == list.length()) throw new IllegalArgumentException("list can't be empty.");
        if (null == item) throw new IllegalArgumentException("item can't be null.");

        if (finalLists_.contains(list)) {
            return;
        }

        synchronized (this) {
            ArrayList<String> list_items = null;

            if (hasPreferencesNode()) {
                Preferences preferences = getPreferencesNode();

                if (preferences != null) {
                    Preferences list_preferences = preferences.node(list);

                    if (list_preferences != null) {
                        try {
                            String[] string_array = list_preferences.keys();
                            if (string_array != null) {
                                int[] int_array = new int[string_array.length];
                                int counter = 0;
                                for (String item_string : string_array) {
                                    int_array[counter++] = Integer.parseInt(item_string);
                                }
                                Arrays.sort(int_array);

                                list_items = new ArrayList<String>(int_array.length);
                                for (int item_int : int_array) {
                                    list_items.add(list_preferences.get(String.valueOf(item_int), null));
                                }
                            }

                            list_preferences.put(String.valueOf(string_array.length), item);
                            list_items.add(item);
                        } catch (BackingStoreException e) {
                            // that's ok, don't handle the preferences node
                        }
                    }
                }
            }

            if (list_items != null) {
                lists_.put(list, list_items);
            } else {
                if (lists_.containsKey(list)) {
                    list_items = lists_.get(list);
                    list_items.add(item);
                } else {
                    list_items = new ArrayList<String>();
                    lists_.put(list, list_items);

                    list_items.add(item);
                }
            }
        }
    }

    public void addListItem(String list, boolean item) {
        addListItem(list, String.valueOf(item));
    }

    public void addListItem(String list, char item) {
        addListItem(list, String.valueOf(item));
    }

    public void addListItem(String list, int item) {
        addListItem(list, String.valueOf(item));
    }

    public void addListItem(String list, long item) {
        addListItem(list, String.valueOf(item));
    }

    public void addListItem(String list, float item) {
        addListItem(list, String.valueOf(item));
    }

    public void addListItem(String list, double item) {
        addListItem(list, String.valueOf(item));
    }

    public void addListItem(String list, Serializable item)
    throws ConfigErrorException {
        try {
            addListItem(list, SerializationUtils.serializeToString(item));
        } catch (SerializationUtilsErrorException e) {
            throw new ConfigErrorException(e);
        }
    }

    public void clearList(String list) {
        if (null == list) throw new IllegalArgumentException("list can't be null.");
        if (0 == list.length()) throw new IllegalArgumentException("list can't be empty.");

        if (finalLists_.contains(list)) {
            return;
        }

        synchronized (this) {
            if (hasPreferencesNode()) {
                Preferences preferences = getPreferencesNode();

                if (preferences != null) {
                    Preferences list_preferences = preferences.node(list);

                    if (list_preferences != null) {
                        try {
                            list_preferences.clear();
                        } catch (BackingStoreException e) {
                            // that's ok, don't handle the preferences node
                        }
                    }
                }
            }

            if (lists_.containsKey(list)) {
                lists_.put(list, new ArrayList<String>());
            }
        }
    }

    public void removeList(String list) {
        if (null == list) throw new IllegalArgumentException("list can't be null.");
        if (0 == list.length()) throw new IllegalArgumentException("list can't be empty.");

        if (finalLists_.contains(list)) {
            return;
        }

        synchronized (this) {
            if (hasPreferencesNode()) {
                Preferences preferences = getPreferencesNode();

                if (preferences != null) {
                    Preferences list_preferences = preferences.node(list);

                    if (list_preferences != null) {
                        try {
                            list_preferences.removeNode();
                        } catch (BackingStoreException e) {
                            // that's ok, don't handle the preferences node
                        }
                    }
                }
            }

            lists_.remove(list);
        }
    }

    public void setFinalList(String list, boolean isFinal) {
        if (null == list) throw new IllegalArgumentException("list can't be null.");
        if (0 == list.length()) throw new IllegalArgumentException("list can't be empty.");

        if (isFinal &&
            !finalLists_.contains(list)) {
            finalLists_.add(list);
        } else {
            finalLists_.remove(list);
        }
    }

    Map<String, String> getParameters() {
        return parameters_;
    }

    List<String> getFinalParameters() {
        return finalParameters_;
    }

    Map<String, ArrayList<String>> getLists() {
        return lists_;
    }

    List<String> getFinalLists() {
        return finalLists_;
    }

    public String toXml() {
        StringBuilder xml_output = new StringBuilder();
        xml_output.append("<config>\n");

        ArrayList<String> list_keys_arraylist = new ArrayList<String>();
        for (String list_key : lists_.keySet()) {
            list_keys_arraylist.add(list_key);
        }

        Collections.sort(list_keys_arraylist);

        for (String list_key : list_keys_arraylist) {
            xml_output.append("\t<list name=\"");
            xml_output.append(StringUtils.encodeXml(list_key));
            if (finalLists_.contains(list_key)) {
                xml_output.append("\" final=\"true");
            }
            xml_output.append("\">\n");

            ArrayList<String> list_items = lists_.get(list_key);
            for (String list_item : list_items) {
                xml_output.append("\t\t<item>").append(StringUtils.encodeXml(list_item)).append("</item>\n");
            }

            xml_output.append("\t</list>\n");
        }

        ArrayList<String> parameter_keys_arraylist = new ArrayList<String>();
        for (String parameter_key : parameters_.keySet()) {
            parameter_keys_arraylist.add(parameter_key);
        }

        Collections.sort(parameter_keys_arraylist);

        for (String parameter_key : parameter_keys_arraylist) {
            xml_output.append("\t<param name=\"");
            xml_output.append(StringUtils.encodeXml(parameter_key));
            if (finalParameters_.contains(parameter_key)) {
                xml_output.append("\" final=\"true");
            }
            xml_output.append("\">");
            xml_output.append(StringUtils.encodeXml(parameters_.get(parameter_key)));
            xml_output.append("</param>\n");
        }

        xml_output.append("</config>\n");

        return xml_output.toString();
    }

    public void storeToPreferences()
    throws ConfigErrorException {
        if (!hasPreferencesNode()) {
            throw new MissingPreferencesUserNodeException();
        }

        storeToPreferences(getPreferencesNode());
    }

    public synchronized void storeToPreferences(Preferences preferences)
    throws ConfigErrorException {
        if (null == preferences) throw new IllegalArgumentException("destination can't be null");

        synchronized (preferences) {
            Preferences list_node = null;

            for (String list_key : lists_.keySet()) {
                if (finalLists_.contains(list_key)) {
                    continue;
                }

                list_node = preferences.node(list_key);

                int counter = 0;
                ArrayList<String> list_items = lists_.get(list_key);
                for (String list_item : list_items) {
                    list_node.put(String.valueOf(counter++), list_item);
                }
            }

            for (String parameter_key : parameters_.keySet()) {
                if (parameter_key.equals(PARAMETER_PREFERENCES_SYSTEM) ||
                    parameter_key.equals(PARAMETER_PREFERENCES_USER) ||
                    finalParameters_.contains(parameter_key)) {
                    continue;
                }

                preferences.put(parameter_key, parameters_.get(parameter_key));
            }

            try {
                preferences.flush();
            } catch (BackingStoreException e) {
                throw new StorePreferencesErrorException(preferences, e);
            }
        }
    }

    public Config clone() {
        Config new_config = null;
        try {
            new_config = (Config) super.clone();
        } catch (CloneNotSupportedException e) {
            // this should never happen
            Logger.getLogger("rife.config").severe(ExceptionUtils.getExceptionStackTrace(e));
        }

        try {
            new_config.parameters_ = ObjectUtils.deepClone(parameters_);
            new_config.finalParameters_ = ObjectUtils.deepClone(finalParameters_);
            new_config.lists_ = ObjectUtils.deepClone(lists_);
            new_config.finalLists_ = ObjectUtils.deepClone(finalLists_);
        } catch (CloneNotSupportedException e) {
            ///CLOVER:OFF
            // this should never happen
            Logger.getLogger("rife.config").severe(ExceptionUtils.getExceptionStackTrace(e));
            ///CLOVER:ON
        }

        return new_config;
    }
}
