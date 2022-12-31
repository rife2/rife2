/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.database;

import org.junit.jupiter.api.extension.*;

import java.lang.reflect.AnnotatedElement;
import java.util.Optional;

import static org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.helpers.AnnotationHelper.findAnnotation;

public class DatasourceEnabledIfExtension implements ExecutionCondition {

    private static final ConditionEvaluationResult ENABLED = ConditionEvaluationResult.enabled(
        "@DatasourceEnabledIf is not present");

    @Override
    public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext context) {
        Optional<AnnotatedElement> element = context.getElement();
        if (element.isPresent()) {
            DatasourceEnabledIf enabled = findAnnotation(element.get(), DatasourceEnabledIf.class);
            if (enabled != null) {
                if (TestDatasources.ACTIVE_DATASOURCES.containsKey(enabled.value())) {
                    return ConditionEvaluationResult.enabled("Datasource is active");
                } else {
                    return ConditionEvaluationResult. disabled("Datasource is inactive");
                }
            }
        }

        return ENABLED;
    }
}