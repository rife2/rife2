/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import rife.config.RifeConfig;
import rife.engine.annotations.ActiveSite;
import rife.engine.annotations.FlowDirection;
import rife.engine.annotations.Parameter;
import rife.engine.annotations.ParametersBean;
import rife.tools.Convert;

import java.time.Instant;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class AnnotationParametersSite extends Site {
    Route routeOut;
    Route routeIn;
    Route routePathInfo;

    public static class ParentInElement implements Element {
        @Parameter int intParam = -4;
        @Parameter("param3") int intParam2 = -5;
        @ParametersBean(prefix = "theBean_") BeanImpl paramsBean;

        @Override
        public void process(Context c) {
        }
    }

    public static class AnnotatedInElement extends ParentInElement {
        @Parameter String stringParam = "defaultParam";
        @Parameter("param2") String stringParam2 = "defaultParam2";
        @Parameter("param6") String stringParam3 = "defaultParam3";

        @Override
        public void process(Context c) {
            super.process(c);

            c.print(stringParam + "\n");
            c.print(intParam + "\n");
            c.print(stringParam2 + "\n");
            c.print(intParam2 + "\n");

            paramsBean.printToContext(c);
        }
    }

    public static class ParentOutElement implements Element {
        @Parameter(value = "param2", flow = FlowDirection.OUT) String stringParam2 = "value3";
        @ParametersBean(prefix = "theBean_", flow = FlowDirection.OUT) BeanImpl paramsBean;
        @Parameter int switchRoute = 0;

        @Override
        public void process(Context c) {
        }
    }

    public static class AnnotatedOutElement extends ParentOutElement {
        @ActiveSite AnnotationParametersSite site;

        @Parameter(flow = FlowDirection.OUT) String stringParam = "value1";
        @Parameter(flow = FlowDirection.OUT) int intParam = 222;
        @Parameter(value = "param3", flow = FlowDirection.OUT) int intParam2 = 444;
        @Parameter(value = "param5", flow = FlowDirection.OUT) String stringParam3 = "value5";

        @Override
        public void process(Context c) {
            super.process(c);

            paramsBean = new BeanImpl();
            paramsBean.setEnum(BeanImpl.Day.WEDNESDAY);
            paramsBean.setString("theString");
            paramsBean.setStringbuffer(new StringBuffer("the stringbuffer"));
            paramsBean.setInt(23154);
            paramsBean.setInteger(893749);
            paramsBean.setChar('u');
            paramsBean.setCharacter('R');
            paramsBean.setBoolean(true);
            paramsBean.setBooleanObject(false);
            paramsBean.setByte((byte) 120);
            paramsBean.setByteObject((byte) 21);
            paramsBean.setDouble(34878.34);
            paramsBean.setDoubleObject(25435.98);
            paramsBean.setFloat(3434.76f);
            paramsBean.setFloatObject(6534.8f);
            paramsBean.setLong(34347897L);
            paramsBean.setLongObject(2335454L);
            paramsBean.setShort((short) 32);
            paramsBean.setShortObject((short) 12);

            var cal1 = new GregorianCalendar(2005, Calendar.AUGUST, 20, 9, 44, 0);
            var cal2 = new GregorianCalendar(2005, Calendar.AUGUST, 21, 11, 6, 14);
            var cal3 = new GregorianCalendar(2006, Calendar.JULY, 17, 16, 5, 31);
            cal1.setTimeZone(RifeConfig.tools().getDefaultTimeZone());
            cal2.setTimeZone(RifeConfig.tools().getDefaultTimeZone());
            cal3.setTimeZone(RifeConfig.tools().getDefaultTimeZone());
            paramsBean.setDate(cal1.getTime());
            paramsBean.setDateFormatted(cal1.getTime());
            paramsBean.setDatesFormatted(new Date[]{cal2.getTime(), cal3.getTime()});

            var cal4 = new GregorianCalendar(2006, Calendar.AUGUST, 20, 8, 44, 0);
            var cal5 = new GregorianCalendar(2007, Calendar.AUGUST, 21, 10, 6, 14);
            var cal6 = new GregorianCalendar(2008, Calendar.JULY, 17, 15, 5, 31);
            cal4.setTimeZone(RifeConfig.tools().getDefaultTimeZone());
            cal5.setTimeZone(RifeConfig.tools().getDefaultTimeZone());
            cal6.setTimeZone(RifeConfig.tools().getDefaultTimeZone());
            paramsBean.setInstant(Convert.toInstant(cal4));
            paramsBean.setInstantFormatted(Convert.toInstant(cal4));
            paramsBean.setInstantsFormatted(new Instant[]{Convert.toInstant(cal5), Convert.toInstant(cal6)});
            
            paramsBean.setSerializableParam(new BeanImpl.SerializableParam(13, "Thirteen"));
            paramsBean.setSerializableParams(new BeanImpl.SerializableParam[]{
                new BeanImpl.SerializableParam(9, "Nine"),
                new BeanImpl.SerializableParam(91, "NinetyOne")
            });

            switch (switchRoute) {
                case 1 -> c.print(c.urlFor(site.routeIn));
                case 2 -> c.print(c.urlFor(site.routePathInfo));
            }
        }
    }

    @Override
    public void setup() {
        routeOut = get("/out", AnnotatedOutElement.class);
        routeIn = get("/in", AnnotatedInElement.class);
        routePathInfo = get("/pathinfo", PathInfoHandling.MAP(m -> m.t("some").s().p("intParam", "\\d+").s().p("param3").s().p("theBean_string")), AnnotatedInElement::new);
    }
}
