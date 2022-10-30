/*
 * Copyright 2001-2022 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.config.RifeConfig;
import rife.engine.exceptions.EngineException;
import rife.resources.ResourceFinderClasspath;
import rife.resources.exceptions.ResourceFinderErrorException;
import rife.tools.FileUtils;
import rife.tools.InnerClassException;
import rife.tools.InputStreamUser;
import rife.tools.exceptions.FileUtilsErrorException;
import rife.validation.ValidationError;

import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;

public class GetBeanSite extends Site {
    private final String prefix_;

    public GetBeanSite() {
        this("");
    }
    public GetBeanSite(String prefix) {
        prefix_ = prefix;
    }

    public void setup() {
        route("/bean/get", c -> {
            switch (c.method()) {
                case GET -> {
                    c.print("<form name=\"submissionform\" action=\"/bean/get\" method=\"post\" enctype=\"multipart/form-data\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "enum\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "string\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "stringbuffer\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "int\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "integer\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "char\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "character\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "boolean\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "booleanObject\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "byte\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "byteObject\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "double\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "doubleObject\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "float\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "floatObject\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "long\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "longObject\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "short\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "shortObject\">");
                    c.print("<input type=\"file\" name=\"" + prefix_ + "stringFile\"/>");
                    c.print("<input type=\"file\" name=\"" + prefix_ + "bytesFile\"/>");
                    c.print("<input type=\"file\" name=\"" + prefix_ + "streamFile\"/>");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "date\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "dateFormatted\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "datesFormatted\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "datesFormatted\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "serializableParam\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "serializableParams\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "serializableParams\">");
                    c.print("<input id=\"beanSubmit\" type=\"submit\">");
                    c.print("</form");
                }
                case POST -> {
                    BeanImpl bean;
                    if (prefix_.isEmpty()) {
                        bean = c.parametersBean(BeanImpl.class);
                    } else {
                        bean = c.parametersBean(BeanImpl.class, prefix_);
                    }

                    Set<ValidationError> errors = bean.getValidationErrors();
                    for (ValidationError error : errors) {
                        c.print(error.getIdentifier() + " : " + error.getSubject() + "\n");
                    }
                    c.print(bean.getEnum() + "," + bean.getString() + "," + bean.getStringbuffer() + "," + bean.getInt() + "," + bean.getInteger() + "," + bean.getChar() + "," + bean.getCharacter() + "," + bean.isBoolean() + "," + bean.getBooleanObject() + "," + bean.getByte() + "," + bean.getByteObject() + "," + bean.getDouble() + "," + bean.getDoubleObject() + "," + bean.getFloat() + "," + bean.getFloatObject() + "," + bean.getLong() + "," + bean.getLongObject() + "," + bean.getShort() + "," + bean.getShortObject());
                    c.print("," + bean.getStringFile());

                    try {
                        byte[] image_bytes = ResourceFinderClasspath.instance().useStream("uwyn.png", new InputStreamUser<>() {
                            public byte[] useInputStream(InputStream stream)
                            throws InnerClassException {
                                try {
                                    return FileUtils.readBytes(stream);
                                } catch (FileUtilsErrorException e) {
                                    throwException(e);
                                }

                                return null;
                            }
                        });

                        if (null == bean.getBytesFile()) {
                            c.print(",null");
                        } else {
                            c.print("," + Arrays.equals(image_bytes, bean.getBytesFile()));
                        }
                        c.print("," + bean.getConstrainedProperty("bytesFile").getName());

                        if (null == bean.getStreamFile()) {
                            c.print(",null");
                        } else {
                            c.print("," + Arrays.equals(image_bytes, FileUtils.readBytes(bean.getStreamFile())));
                        }
                        SimpleDateFormat sf = new SimpleDateFormat("EEE d MMM yyyy HH:mm:ss");
                        sf.setTimeZone(RifeConfig.tools().getDefaultTimeZone());
                        c.print("," + (null == bean.getDate() ? null : sf.format(bean.getDate())));
                        if (null == bean.getDatesFormatted()) {
                            c.print(",null");
                        } else {
                            for (Date date : bean.getDatesFormatted()) {
                                c.print(",");
                                if (null == date) {
                                    c.print("null");
                                } else {
                                    c.print(sf.format(date));
                                }
                            }
                        }
                        c.print("," + bean.getSerializableParam());
                        if (null == bean.getSerializableParams()) {
                            c.print(",null");
                        } else {
                            for (Object param : bean.getSerializableParams()) {
                                c.print("," + param);
                            }
                        }
                    } catch (ResourceFinderErrorException | FileUtilsErrorException e) {
                        throw new EngineException(e);
                    }
                }
            }
        });
    }
}
