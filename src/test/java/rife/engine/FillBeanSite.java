/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.config.RifeConfig;
import rife.engine.exceptions.EngineException;
import rife.resources.ResourceFinderClasspath;
import rife.resources.exceptions.ResourceFinderErrorException;
import rife.tools.Convert;
import rife.tools.FileUtils;
import rife.tools.InnerClassException;
import rife.tools.InputStreamUser;
import rife.tools.exceptions.FileUtilsErrorException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Arrays;

public class FillBeanSite extends Site {
    private final String prefix_;

    public FillBeanSite() {
        this("");
    }
    public FillBeanSite(String prefix) {
        prefix_ = prefix;
    }

    public void setup() {
        route("/bean/fill", c -> {
            switch (c.method()) {
                case GET -> {
                    c.print("<form name=\"submissionform\" action=\"/bean/fill\" method=\"post\" enctype=\"multipart/form-data\">");
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
                    c.print("<input type=\"text\" name=\"" + prefix_ + "instant\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "instantFormatted\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "instantsFormatted\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "instantsFormatted\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "serializableParam\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "serializableParams\">");
                    c.print("<input type=\"text\" name=\"" + prefix_ + "serializableParams\">");
                    c.print("<input id=\"beanSubmit\" type=\"submit\">");
                    c.print("</form");
                }
                case POST -> {
                    var bean = new BeanImpl();

                    bean.setEnum(BeanImpl.Day.FRIDAY);
                    bean.setString("string");
                    bean.setStringbuffer(new StringBuffer("stringbuffer"));
                    bean.setInt(999);
                    bean.setInteger(111);
                    bean.setChar('a');
                    bean.setCharacter('b');
                    bean.setBoolean(false);
                    bean.setBooleanObject(true);
                    bean.setByte((byte)22);
                    bean.setByteObject((byte)33);
                    bean.setDouble(123.45d);
                    bean.setDoubleObject(234.56d);
                    bean.setFloat(321.54f);
                    bean.setFloatObject(432.65f);
                    bean.setLong(44L);
                    bean.setLongObject(55L);
                    bean.setShort((short)66);
                    bean.setShortObject((short)77);
                    bean.setStringFile("stringFile");
                    bean.setBytesFile(new byte[] {1, 2, 3});
                    bean.setStreamFile(new ByteArrayInputStream("streamFile".getBytes(StandardCharsets.UTF_8)));

                    if (prefix_.isEmpty()) {
                        c.parametersBean(bean);
                    } else {
                        c.parametersBean(bean, prefix_);
                    }

                    var errors = bean.getValidationErrors();
                    for (var error : errors) {
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

                        var sf = new SimpleDateFormat("EEE d MMM yyyy HH:mm:ss");
                        sf.setTimeZone(RifeConfig.tools().getDefaultTimeZone());
                        c.print("," + (null == bean.getDate() ? null : sf.format(bean.getDate())));
                        if (null == bean.getDatesFormatted()) {
                            c.print(",null");
                        } else {
                            for (var date : bean.getDatesFormatted()) {
                                c.print(",");
                                if (null == date) {
                                    c.print("null");
                                } else {
                                    c.print(sf.format(date));
                                }
                            }
                        }
                        
                        c.print("," + (null == bean.getInstant() ? null : sf.format(Convert.toDate(bean.getInstant()))));
                        if (null == bean.getInstantsFormatted()) {
                            c.print(",null");
                        } else {
                            for (var instant : bean.getInstantsFormatted()) {
                                c.print(",");
                                if (null == instant) {
                                    c.print("null");
                                } else {
                                    c.print(sf.format(Convert.toDate(instant)));
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
