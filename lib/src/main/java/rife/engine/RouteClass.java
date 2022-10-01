/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.engine.annotations.Parameter;
import rife.engine.exceptions.EngineException;
import rife.tools.Convert;
import rife.tools.exceptions.ConversionException;

import java.lang.reflect.Modifier;

public record RouteClass(RequestMethod method, String path, Class<? extends Element> elementClass) implements Route {
    @Override
    public void process(Context context) throws EngineException {
        try {
            var element = elementClass().getDeclaredConstructor().newInstance();

            // auto assign annotated parameters
            var params = context.request().getParameters();
            for (var field : element.getClass().getDeclaredFields()) {
                field.setAccessible(true);

                if (Modifier.isStatic(field.getModifiers()) ||
                    Modifier.isFinal(field.getModifiers()) ||
                    Modifier.isTransient(field.getModifiers())) {
                    continue;
                }

                if (field.isAnnotationPresent(Parameter.class)) {
                    var name = field.getName();
                    var values = params.get(name);
                    if (values != null && values.length > 0) {
                        var type = field.getType();
                        Object value;
                        try {
                            value = Convert.toType(values[0], type);
                        }
            			catch (ConversionException e) {
                            value = Convert.getDefaultValue(type);
                        }
                        field.set(element, value);
                    }
                }
            }
            element.process(context);
        } catch (Exception e) {
            throw new EngineException(e);
        }
    }
}
