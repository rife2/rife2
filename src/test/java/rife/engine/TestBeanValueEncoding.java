/*
 * Copyright 2001-2023 Geert Bevin (gbevin[remove] at uwyn dot com)
 * Licensed under the Apache License, Version 2.0 (the "License")
 */
package rife.engine;

import org.junit.jupiter.api.Test;
import rife.test.MockConversation;
import rife.validation.ConstrainedProperty;
import rife.validation.MetaData;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestBeanValueEncoding {
    public static class EncodingBean extends MetaData {
        private String text;

        @Override
        public void activateMetaData() {
            addConstraint(new ConstrainedProperty("rawText").displayedRaw(true));
        }

        public EncodingBean() {
        }

        public EncodingBean(String text) {
            this.text = text;
        }

        public String getRawText() {
            return text;
        }

        public void setRawText(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }
    }

    @Test
    void testBeanValueSurrogatePairEncoding() {
        var smiley = "😄";
        var m = new MockConversation(new Site() {
            public void setup() {
                get("/bean", c -> {
                    var t = c.template("bean_value_encoding");
                    t.setBean(new EncodingBean(smiley));
                    c.print(t);
                });
            }
        });
        assertEquals("rawText: " + smiley + "\n" +
            "text: &#128516;\n", m.doRequest("/bean").getText());
    }
}
