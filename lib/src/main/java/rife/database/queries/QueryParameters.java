/*
 * Copyright 2001-2022 Geert Bevin <gbevin[remove] at uwyn dot com>
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database.queries;

import java.util.*;

import rife.database.exceptions.DatabaseException;
import rife.tools.ArrayUtils;

public class QueryParameters implements Cloneable {
    private AbstractParametrizedQuery query_ = null;
    private Map<QueryParameterType, Object> parameters_ = null;

    private List<String> combinedParameters_ = null;
    private String[] combinedParametersArray_ = null;

    public QueryParameters(AbstractParametrizedQuery query) {
        if (null == query) throw new IllegalArgumentException("query can't be null");

        query_ = query;
    }

    public QueryParameters getNewInstance() {
        return new QueryParameters(query_);
    }

    public int getNumberOfTypes() {
        if (null == parameters_) {
            return 0;
        }

        return parameters_.size();
    }

    public void clear() {
        parameters_ = null;
        combinedParameters_ = null;
        combinedParametersArray_ = null;
    }

    public boolean hasParameter(QueryParameterType type, String value) {
        if (null == type ||
            null == value ||
            null == parameters_) {
            return false;
        }

        if (!parameters_.containsKey(type)) {
            return false;
        }

        if (type.isSingular()) {
            return value.equals(parameters_.get(type));
        } else {
            List<String> list = (List<String>) parameters_.get(type);
            if (null == list) {
                return false;
            }

            return list.contains(value);
        }
    }

    public Set<String> getDistinctNames() {
        if (null == parameters_ ||
            0 == parameters_.size()) {
            return null;
        }

        HashSet<String> names = new HashSet<String>();
        for (Map.Entry<QueryParameterType, Object> entry : parameters_.entrySet()) {
            if (entry.getKey().isSingular()) {
                names.add((String) entry.getValue());
            } else {
                names.addAll((List<String>) entry.getValue());
            }
        }

        return names;
    }

    public List<String> getOrderedNames() {
        if (null == combinedParameters_) {
            ArrayList<String> combined_parameters = null;

            if (parameters_ != null &&
                parameters_.size() > 0) {
                if (parameters_.containsKey(QueryParameterType.FIELD)) {
                    combined_parameters = new ArrayList<String>();

                    for (String parameter : (List<String>) parameters_.get(QueryParameterType.FIELD)) {
                        // add the parameter to the combined list
                        combined_parameters.add(parameter);
                    }
                }

                if (parameters_.containsKey(QueryParameterType.TABLE)) {
                    if (null == combined_parameters) {
                        combined_parameters = new ArrayList<String>();
                    }

                    for (String parameter : (List<String>) parameters_.get(QueryParameterType.TABLE)) {
                        // add the parameter to the combined list
                        combined_parameters.add(parameter);
                    }
                }

                if (parameters_.containsKey(QueryParameterType.WHERE)) {
                    if (null == combined_parameters) {
                        combined_parameters = new ArrayList<String>();
                    }

                    for (String parameter : (List<String>) parameters_.get(QueryParameterType.WHERE)) {
                        // add the parameter to the combined list
                        combined_parameters.add(parameter);
                    }
                }

                if (parameters_.containsKey(QueryParameterType.UNION)) {
                    if (null == combined_parameters) {
                        combined_parameters = new ArrayList<String>();
                    }

                    for (String parameter : (List<String>) parameters_.get(QueryParameterType.UNION)) {
                        // add the parameter to the combined list
                        combined_parameters.add(parameter);
                    }
                }

                if (parameters_.containsKey(QueryParameterType.LIMIT) ||
                    parameters_.containsKey(QueryParameterType.OFFSET)) {
                    if (query_.isLimitBeforeOffset()) {
                        if (parameters_.containsKey(QueryParameterType.LIMIT)) {
                            if (null == combined_parameters) {
                                combined_parameters = new ArrayList<String>();
                            }

                            // get the parameter value
                            String value = (String) parameters_.get(QueryParameterType.LIMIT);
                            // add the parameter to the combined list
                            combined_parameters.add(value);
                        }

                        if (parameters_.containsKey(QueryParameterType.OFFSET)) {
                            if (null == combined_parameters) {
                                combined_parameters = new ArrayList<String>();
                            }

                            // get the parameter value
                            String value = (String) parameters_.get(QueryParameterType.OFFSET);
                            // add the parameter to the combined list
                            combined_parameters.add(value);
                        }
                    } else {
                        if (parameters_.containsKey(QueryParameterType.OFFSET)) {
                            if (null == combined_parameters) {
                                combined_parameters = new ArrayList<String>();
                            }

                            // get the parameter value
                            String value = (String) parameters_.get(QueryParameterType.OFFSET);
                            // add the parameter to the combined list
                            combined_parameters.add(value);
                        }

                        if (parameters_.containsKey(QueryParameterType.LIMIT)) {
                            if (null == combined_parameters) {
                                combined_parameters = new ArrayList<String>();
                            }

                            // get the parameter value
                            String value = (String) parameters_.get(QueryParameterType.LIMIT);
                            // add the parameter to the combined list
                            combined_parameters.add(value);
                        }
                    }
                }
            }

            combinedParameters_ = combined_parameters;
            combinedParametersArray_ = null;
        }

        return combinedParameters_;
    }

    private void addVirtualIndexMapping(QueryParameters virtualParameters, Map<Integer, Integer> map, int[] parameterIndex, int[] realIndex, QueryParameterType type, String parameter) {
        if (virtualParameters.hasParameter(type, parameter)) {
            map.put(parameterIndex[0], -1);
        } else {
            map.put(parameterIndex[0], realIndex[0]);
            realIndex[0]++;
        }

        parameterIndex[0]++;
    }

    public Map<Integer, Integer> getVirtualIndexMapping(QueryParameters virtualParameters) {
        Map<Integer, Integer> map = null;

        if (parameters_ != null &&
            parameters_.size() > 0 &&
            virtualParameters != null &&
            virtualParameters.getNumberOfTypes() > 0) {
            map = new HashMap<Integer, Integer>();

            int[] parameter_index = new int[]{1};
            int[] real_index = new int[]{1};

            if (parameters_.containsKey(QueryParameterType.FIELD)) {
                for (String parameter : (List<String>) parameters_.get(QueryParameterType.FIELD)) {
                    addVirtualIndexMapping(virtualParameters, map, parameter_index, real_index, QueryParameterType.FIELD, parameter);
                }
            }

            if (parameters_.containsKey(QueryParameterType.TABLE)) {
                for (String parameter : (List<String>) parameters_.get(QueryParameterType.TABLE)) {
                    addVirtualIndexMapping(virtualParameters, map, parameter_index, real_index, QueryParameterType.TABLE, parameter);
                }
            }

            if (parameters_.containsKey(QueryParameterType.WHERE)) {
                for (String parameter : (List<String>) parameters_.get(QueryParameterType.WHERE)) {
                    addVirtualIndexMapping(virtualParameters, map, parameter_index, real_index, QueryParameterType.WHERE, parameter);
                }
            }

            if (parameters_.containsKey(QueryParameterType.UNION)) {
                for (String parameter : (List<String>) parameters_.get(QueryParameterType.UNION)) {
                    addVirtualIndexMapping(virtualParameters, map, parameter_index, real_index, QueryParameterType.UNION, parameter);
                }
            }

            if (parameters_.containsKey(QueryParameterType.LIMIT) ||
                parameters_.containsKey(QueryParameterType.OFFSET)) {
                if (query_.isLimitBeforeOffset()) {
                    if (parameters_.containsKey(QueryParameterType.LIMIT)) {
                        String parameter = (String) parameters_.get(QueryParameterType.LIMIT);
                        addVirtualIndexMapping(virtualParameters, map, parameter_index, real_index, QueryParameterType.LIMIT, parameter);
                    }

                    if (parameters_.containsKey(QueryParameterType.OFFSET)) {
                        String parameter = (String) parameters_.get(QueryParameterType.OFFSET);
                        addVirtualIndexMapping(virtualParameters, map, parameter_index, real_index, QueryParameterType.OFFSET, parameter);
                    }
                } else {
                    if (parameters_.containsKey(QueryParameterType.OFFSET)) {
                        String parameter = (String) parameters_.get(QueryParameterType.OFFSET);
                        addVirtualIndexMapping(virtualParameters, map, parameter_index, real_index, QueryParameterType.OFFSET, parameter);
                    }

                    if (parameters_.containsKey(QueryParameterType.LIMIT)) {
                        String parameter = (String) parameters_.get(QueryParameterType.LIMIT);
                        addVirtualIndexMapping(virtualParameters, map, parameter_index, real_index, QueryParameterType.LIMIT, parameter);
                    }
                }
            }
        }

        return map;
    }

    public String[] getOrderedNamesArray() {
        if (null == parameters_ ||
            0 == parameters_.size()) {
            return null;
        }

        if (null == combinedParametersArray_) {
            String[] array = new String[0];

            for (String parameter_name : getOrderedNames()) {
                array = ArrayUtils.join(array, parameter_name);
            }

            combinedParametersArray_ = array;
        }

        return combinedParametersArray_;
    }

    private void clearCombinedParameters() {
        combinedParameters_ = null;
        combinedParametersArray_ = null;
    }

    public void clearTypedParameters(QueryParameterType type) {
        if (null == type) throw new IllegalArgumentException("the parameter type can't be null");

        if (null == parameters_) {
            return;
        }

        parameters_.remove(type);
        clearCombinedParameters();
    }

    public <T> T getTypedParameters(QueryParameterType type) {
        if (null == type) throw new IllegalArgumentException("the parameter type can't be null");

        if (null == parameters_) {
            return null;
        }

        return (T) parameters_.get(type);
    }

    public void addTypedParameters(QueryParameterType type, List<String> parameters) {
        if (null == type) throw new IllegalArgumentException("the parameter type can't be null");
        if (type.isSingular())
            throw new IllegalArgumentException("the parameter type '" + type + "' only supports a singular value");

        // don't add empty parameters
        if (null == parameters ||
            0 == parameters.size()) {
            return;
        }

        // obtain the existing typed parameters
        List<String> typed_parameters = null;
        if (null == parameters_) {
            parameters_ = new HashMap<QueryParameterType, Object>();
        } else {
            typed_parameters = (List<String>) parameters_.get(type);
        }

        // initialize the typed parameters collection if it didn't exist before
        boolean new_collection = false;
        if (null == typed_parameters) {
            typed_parameters = new ArrayList<String>();
            new_collection = true;
        }

        // add the new parameters
        typed_parameters.addAll(parameters);

        if (new_collection) {
            parameters_.put(type, typed_parameters);
        }

        // clear the already calculated combined parameters
        clearCombinedParameters();
    }

    public void addTypedParameter(QueryParameterType type, String value) {
        if (null == type) throw new IllegalArgumentException("the parameter type can't be null");

        if (value != null) {
            // initialize the parameters map if it doesn't exist yet
            if (null == parameters_) {
                parameters_ = new HashMap<QueryParameterType, Object>();
            }

            // remove the table-field separator dot
            if (value.contains(".")) {
                value = value.substring(value.lastIndexOf(".") + 1);
            }
        }

        // check if the parameter is singular
        if (type.isSingular()) {
            // empty singular parameters clear out the key
            if (null == value) {
                if (null == parameters_) {
                    return;
                }

                parameters_.remove(type);
            }
            // store the singular parameter
            else {
                parameters_.put(type, value);
            }
        } else {
            // don't add empty parameters
            if (null == value) {
                return;
            }

            // obtain the existing typed parameters
            List<String> typed_parameters = (List<String>) parameters_.get(type);

            // initialize the typed parameters collection if it didn't exist before
            boolean new_collection = false;
            if (null == typed_parameters) {
                typed_parameters = new ArrayList<String>();
                new_collection = true;
            }

            // add the new parameters
            typed_parameters.add(value);

            // store the new collection if it has been allocated
            if (new_collection) {
                parameters_.put(type, typed_parameters);
            }
        }

        // clear the already calculated combined parameters
        clearCombinedParameters();
    }

    public QueryParameters clone() {
        QueryParameters new_instance = null;
        try {
            new_instance = (QueryParameters) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new DatabaseException(e);
        }

        if (new_instance != null) {
            if (combinedParameters_ != null) {
                new_instance.combinedParameters_ = new ArrayList<String>();
                new_instance.combinedParameters_.addAll(combinedParameters_);
            }

            if (parameters_ != null) {
                new_instance.parameters_ = new HashMap<QueryParameterType, Object>();
                for (Map.Entry<QueryParameterType, Object> entry : parameters_.entrySet()) {
                    if (entry.getKey().isSingular()) {
                        new_instance.parameters_.put(entry.getKey(), entry.getValue());
                    } else {
                        List<String> values = new ArrayList<String>();
                        values.addAll((List<String>) entry.getValue());
                        new_instance.parameters_.put(entry.getKey(), values);
                    }
                }
            }
        }

        return new_instance;
    }
}
