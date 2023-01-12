package rife.engine;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlForm;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.Test;
import rife.engine.continuations.*;
import rife.tools.StringUtils;

import static org.junit.jupiter.api.Assertions.*;

public class TestContinuations {
    @Test
    void testNoPause()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                get("/nopause", TestNoPause.class);
            }
        })) {
            try (final var webClient = new WebClient()) {
                HtmlPage page = webClient.getPage("http://localhost:8181/nopause");

                var text = page.getWebResponse().getContentAsString();
                assertEquals("", text);
            }
        }
    }

    @Test
    void testSimplePause()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                get("/simple", TestSimplePause.class);
            }
        })) {
            try (final var webClient = new WebClient()) {
                HtmlPage page = webClient.getPage("http://localhost:8181/simple");

                var text = page.getWebResponse().getContentAsString();
                var lines = StringUtils.splitToArray(text, "\n");
                assertEquals(2, lines.length);
                assertEquals("before simple pause", lines[0]);

                page = webClient.getPage("http://localhost:8181/simple?" + SpecialParameters.CONT_ID + "=" + lines[1]);
                assertEquals("after simple pause", page.getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    void testNull()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                get("/null", TestNull.class);
            }
        })) {
            try (final var webClient = new WebClient()) {
                HtmlPage page = webClient.getPage("http://localhost:8181/null");

                var text = page.getWebResponse().getContentAsString();
                var lines = StringUtils.splitToArray(text, "\n");
                assertEquals(2, lines.length);
                assertEquals("before null pause", lines[0]);

                page = webClient.getPage("http://localhost:8181/null?response=after%20null%20pause&" + SpecialParameters.CONT_ID + "=" + lines[1]);
                assertEquals("after null pause", page.getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    void testNullReference()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                get("/null_reference", TestNullReference.class);
            }
        })) {
            try (final var webClient = new WebClient()) {
                HtmlPage page = webClient.getPage("http://localhost:8181/null_reference");

                var text = page.getWebResponse().getContentAsString();

                page = webClient.getPage("http://localhost:8181/null_reference?" + SpecialParameters.CONT_ID + "=" + text);
                assertTrue(page.getWebResponse().getContentAsString().contains("java.lang.NullPointerException"));
            }
        }
    }

    @Test
    void testNullConditional()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                get("/null_conditional", TestNullConditional.class);
            }
        })) {
            try (final var webClient = new WebClient()) {
                HtmlPage page = webClient.getPage("http://localhost:8181/null_conditional?value=thevalue");

                var text = page.getWebResponse().getContentAsString();
                assertTrue(text.startsWith("thevalue"));

                page = webClient.getPage("http://localhost:8181/null_conditional?" + SpecialParameters.CONT_ID + "=" + text.substring(8));
                assertEquals("thevalue", page.getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    void testConditional()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                route("/conditional", TestConditional.class);
            }
        })) {
            try (final var webClient = new WebClient()) {
                HtmlPage page = webClient.getPage("http://localhost:8181/conditional");

                assertEquals("printing", page.getTitleText());
                var form = page.getFormByName("pause");
                assertNotNull(form);
                form.getInputsByName("answer").get(0).setValueAttribute("1");
                page = form.getInputsByName("submit").get(0).click();

                assertEquals("pausing", page.getTitleText());
                form = page.getFormByName("pause");
                assertNotNull(form);
                form.getInputsByName("answer").get(0).setValueAttribute("1"); // will not be checked
                page = form.getInputsByName("submit").get(0).click();

                assertEquals("pausingprinting", page.getTitleText());
                form = page.getFormByName("pause");
                assertNotNull(form);
                form.getInputsByName("answer").get(0).setValueAttribute("0");
                page = form.getInputsByName("submit").get(0).click();

                assertEquals("printing", page.getTitleText());
                form = page.getFormByName("pause");
                assertNotNull(form);

                page = webClient.getPage("http://localhost:8181/conditional");

                assertEquals("printing", page.getTitleText());
                form = page.getFormByName("pause");
                assertNotNull(form);
                form.getInputsByName("stop").get(0).setValueAttribute("1");
                page = form.getInputsByName("submit").get(0).click();

                assertEquals("stopping", page.getTitleText());
            }
        }
    }

    @Test
    void testMemberMethod()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                get("/member_method", TestMemberMethod.class);
            }
        })) {
            try (final var webClient = new WebClient()) {
                HtmlPage page = webClient.getPage("http://localhost:8181/member_method");

                var text = page.getWebResponse().getContentAsString();
                var lines = StringUtils.splitToArray(text, "\n");
                assertEquals(2, lines.length);
                assertEquals("before pause", lines[0]);

                page = webClient.getPage("http://localhost:8181/member_method?" + SpecialParameters.CONT_ID + "=" + lines[1]);
                assertEquals("me value 6899", page.getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    void testPrivateMethod()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                get("/private_method", TestPrivateMethod.class);
            }
        })) {
            try (final var webClient = new WebClient()) {
                HtmlPage page = webClient.getPage("http://localhost:8181/private_method");
                page = webClient.getPage("http://localhost:8181/private_method?" + SpecialParameters.CONT_ID + "=" + page.getWebResponse().getContentAsString());
                assertEquals("1234", page.getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    void testSynchronization()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                get("/synchronization", TestSynchronization.class);
            }
        })) {
            try (final var webClient = new WebClient()) {
                HtmlPage page = webClient.getPage("http://localhost:8181/synchronization");

                var text = page.getWebResponse().getContentAsString();
                var lines = StringUtils.splitToArray(text, "\n");
                assertEquals(2, lines.length);
                assertEquals("monitor this", lines[0]);

                page = webClient.getPage("http://localhost:8181/synchronization?" + SpecialParameters.CONT_ID + "=" + lines[1]);

                text = page.getWebResponse().getContentAsString();
                lines = StringUtils.splitToArray(text, "\n");
                assertEquals(2, lines.length);
                assertEquals("monitor member", lines[0]);

                page = webClient.getPage("http://localhost:8181/synchronization?" + SpecialParameters.CONT_ID + "=" + lines[1]);

                text = page.getWebResponse().getContentAsString();
                lines = StringUtils.splitToArray(text, "\n");
                assertEquals(2, lines.length);
                assertEquals("monitor static", lines[0]);

                page = webClient.getPage("http://localhost:8181/synchronization?" + SpecialParameters.CONT_ID + "=" + lines[1]);

                assertEquals("done", page.getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    void testThrow()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                route("/throw", TestThrow.class);
            }
        })) {
            try (final var webClient = new WebClient()) {
                HtmlPage page = webClient.getPage("http://localhost:8181/throw");

                var form = page.getFormByName("action");
                assertNotNull(form);
                form.getInputsByName("throw").get(0).setValueAttribute("1");
                page = form.getInputsByName("submit").get(0).click();

                assertEquals("do throw = true : throw message : finally message", page.getTitleText());
                form = page.getFormByName("action");
                assertNotNull(form);
                form.getInputsByName("throw").get(0).setValueAttribute("1"); // will not be checked
                page = form.getInputsByName("submit").get(0).click();

                assertEquals("do throw = true : throw message : finally message : all done", page.getTitleText());
                form = page.getFormByName("action");
                assertNotNull(form);

                page = webClient.getPage("http://localhost:8181/throw");

                form = page.getFormByName("action");
                assertNotNull(form);
                form.getInputsByName("throw").get(0).setValueAttribute("0");
                page = form.getInputsByName("submit").get(0).click();

                assertEquals("do throw = false : finally message", page.getTitleText());
                form = page.getFormByName("action");
                assertNotNull(form);
                form.getInputsByName("throw").get(0).setValueAttribute("1"); // will not be checked
                page = form.getInputsByName("submit").get(0).click();

                assertEquals("do throw = false : finally message : all done", page.getTitleText());
                form = page.getFormByName("action");
                assertNotNull(form);
            }
        }
    }

    @Test
    void testTryCatch()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                route("/try_catch", TestTryCatch.class);
            }
        })) {
            try (final var webClient = new WebClient()) {
                HtmlPage page = webClient.getPage("http://localhost:8181/try_catch");

                var form = page.getFormByName("action");
                assertNotNull(form);
                form.getInputsByName("throw").get(0).setValueAttribute("1");
                page = form.getInputsByName("submit").get(0).click();

                assertEquals("start : throw done catch", page.getTitleText());
                form = page.getFormByName("action");
                assertNotNull(form);
                form.getInputsByName("throw").get(0).setValueAttribute("1"); // will not be checked since the value of the first param is stored in a local variable in the element
                page = form.getInputsByName("submit").get(0).click();

                assertEquals("start : throw done catch : finally done", page.getTitleText());
                form = page.getFormByName("action");
                assertNotNull(form);
                form.getInputsByName("throw").get(0).setValueAttribute("0"); // will not be checked since the value of the first param is stored in a local variable in the element
                page = form.getInputsByName("submit").get(0).click();

                assertEquals("start : throw done catch : finally done : all done", page.getTitleText());
                form = page.getFormByName("action");
                assertNotNull(form);
                form.getInputsByName("throw").get(0).setValueAttribute("1"); // will not be checked since the value of the first param is stored in a local variable in the element
                page = form.getInputsByName("submit").get(0).click();

                assertEquals("start", page.getTitleText());
                form = page.getFormByName("action");
                assertNotNull(form);
                form.getInputsByName("throw").get(0).setValueAttribute("0");
                page = form.getInputsByName("submit").get(0).click();

                assertEquals("start : throw not done", page.getTitleText());
                form = page.getFormByName("action");
                assertNotNull(form);
                form.getInputsByName("throw").get(0).setValueAttribute("1"); // will not be checked since the value of the first param is stored in a local variable in the element
                page = form.getInputsByName("submit").get(0).click();

                assertEquals("start : throw not done : finally done", page.getTitleText());
                form = page.getFormByName("action");
                assertNotNull(form);
                form.getInputsByName("throw").get(0).setValueAttribute("0"); // will not be checked since the value of the first param is stored in a local variable in the element
                page = form.getInputsByName("submit").get(0).click();

                assertEquals("start : throw not done : finally done : all done", page.getTitleText());
                form = page.getFormByName("action");
                assertNotNull(form);
                form.getInputsByName("throw").get(0).setValueAttribute("1"); // will not be checked since the value of the first param is stored in a local variable in the element
                page = form.getInputsByName("submit").get(0).click();

                assertEquals("start", page.getTitleText());
            }
        }
    }

    @Test
    void testFinally()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                route("/finally", TestFinally.class);
            }
        })) {
            try (final var webClient = new WebClient()) {
                HtmlPage page = webClient.getPage("http://localhost:8181/finally");

                var text = page.getWebResponse().getContentAsString();
                var lines = StringUtils.splitToArray(text, "\n");
                assertEquals(2, lines.length);
                assertEquals("start", lines[0]);

                page = webClient.getPage("http://localhost:8181/finally?" + SpecialParameters.CONT_ID + "=" + lines[1]);
                text = page.getWebResponse().getContentAsString();
                lines = StringUtils.splitToArray(text, "\n");
                assertEquals(2, lines.length);
                assertEquals("try", lines[0]);

                page = webClient.getPage("http://localhost:8181/finally?" + SpecialParameters.CONT_ID + "=" + lines[1]);
                text = page.getWebResponse().getContentAsString();
                lines = StringUtils.splitToArray(text, "\n");
                assertEquals(2, lines.length);
                assertEquals("catch", lines[0]);

                page = webClient.getPage("http://localhost:8181/finally?" + SpecialParameters.CONT_ID + "=" + lines[1]);
                text = page.getWebResponse().getContentAsString();
                lines = StringUtils.splitToArray(text, "\n");
                assertEquals(2, lines.length);
                assertEquals("finally", lines[0]);

                page = webClient.getPage("http://localhost:8181/finally?" + SpecialParameters.CONT_ID + "=" + lines[1]);
                assertEquals("after finally", page.getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    void testInstanceOf()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                route("/instanceof", TestInstanceOf.class);
            }
        })) {
            try (final var webClient = new WebClient()) {
                HtmlPage page = webClient.getPage("http://localhost:8181/instanceof");

                var text = page.getWebResponse().getContentAsString();
                var lines = StringUtils.splitToArray(text, "\n");
                assertEquals(2, lines.length);
                assertEquals("before instanceof pause", lines[0]);

                page = webClient.getPage("http://localhost:8181/instanceof?" + SpecialParameters.CONT_ID + "=" + lines[1]);
                assertEquals("after instanceof pause", page.getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    void testInnerClass()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                route("/innerclass", TestInnerClass.class);
            }
        })) {
            try (final var webClient = new WebClient()) {
                HtmlPage page = webClient.getPage("http://localhost:8181/innerclass");

                var text = page.getWebResponse().getContentAsString();
                var lines = StringUtils.splitToArray(text, "\n");
                assertEquals(2, lines.length);
                assertEquals("before pause", lines[0]);

                page = webClient.getPage("http://localhost:8181/innerclass?" + SpecialParameters.CONT_ID + "=" + lines[1]);
                assertEquals("InnerClass's output", page.getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    void testAllTypes()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                route("/alltypes", TestAllTypes.class);
            }
        })) {
            try (final var webClient = new WebClient()) {
                String text = null;
                String[] lines = null;

                HtmlPage page = webClient.getPage("http://localhost:8181/alltypes");

                for (var i = 8; i < 40; i++) {
                    text = page.getWebResponse().getContentAsString();
                    lines = StringUtils.splitToArray(text, "\n");
                    assertEquals(2, lines.length);
                    assertEquals(TestAllTypes.BEFORE + " while " + i, lines[0]);

                    page = webClient.getPage("http://localhost:8181/alltypes?" + SpecialParameters.CONT_ID + "=" + lines[1]);
                }

                text = page.getWebResponse().getContentAsString();
                lines = StringUtils.splitToArray(text, "\n");
                assertEquals(2, lines.length);
                assertEquals(TestAllTypes.BEFORE + " a", lines[0]);

                page = webClient.getPage("http://localhost:8181/alltypes?" + SpecialParameters.CONT_ID + "=" + lines[1]);

                text = page.getWebResponse().getContentAsString();
                lines = StringUtils.splitToArray(text, "\n");
                assertEquals(2, lines.length);
                assertEquals(TestAllTypes.BEFORE + " b", lines[0]);

                page = webClient.getPage("http://localhost:8181/alltypes?" + SpecialParameters.CONT_ID + "=" + lines[1]);

                text = page.getWebResponse().getContentAsString();
                lines = StringUtils.splitToArray(text, "\n");
                assertEquals(2, lines.length);
                assertEquals(TestAllTypes.BEFORE + " c", lines[0]);

                page = webClient.getPage("http://localhost:8181/alltypes?" + SpecialParameters.CONT_ID + "=" + lines[1]);

                assertEquals("""
                    40,1209000,11,16,7,8,
                    9223372036854775807,0,9223372036854775709,922337203685477570,8,-1,99,
                    0.4,8.4,-80.4,-80.0,0.0,-1.0,
                    2389.98,2407.3799996185303,-10.0,-1.0,-0.0,2397.3799996185303,
                    local ok,some value 6899,
                    true|false|false,K|O,54.7|9.8,82324.45|997823.23|87.8998,98|12,8|11,
                    111111|444444|666666|999999,111111|444444|666666|999999,333|8888|99,333|66|99,
                    zero|one|two|null,zero|one|two|null,ini|mini|moo,
                    3:str 0 0|replaced|str 0 2|str 0 3||str 1 0|str 1 1|str 1 2|str 1 3||str 2 0|str 2 1|str 2 2|str 2 3,
                    3:str 0 0|replaced|str 0 2|str 0 3||str 1 0|str 1 1|str 1 2|str 1 3||str 2 0|str 2 1|str 2 2|str 2 3,
                    2:str 0 0|str 0 1||str 1 0|str 1 1,
                    -98|97,-98|97,98|23|11,
                    2:0|1|2|3|4||100|101|102|-89|104,
                    2:0|1|2|3|4||100|101|102|-89|104,
                    3:0|1|2||100|101|102||200|201|202,
                    2,4,member ok,8111|8333,2:31|32|33|34||35|36|37|38,
                    1,3,static ok,9111|9333,3:1|2|3|4||5|6|7|8||9|10|11|12,
                    2,4,member ok,8111|8333,2:31|32|33|34||35|36|37|38,
                    1,3,static ok,9111|9333,3:1|2|3|4||5|6|7|8||9|10|11|12,
                    100,400,member ok two,8333|8111|23687,1:35|36|37|38,
                    60,600,static ok two,23476|9333|9111|8334,2:9|10|11|12||1|2|3|4,
                    2:3:3:0|1|2|3|4|5|6|7||10|11|12|13|14|15|16|17||20|21|22|23|24|25|26|27|||100|101|102|103|104|105|106|107||110|111|112|113|114|115|116|117||120|121|122|123|-99|null|126|127,
                    2:3:3:0|1|2|3|4|5|6|7||10|11|12|13|14|15|16|17||20|21|22|23|24|25|26|27|||100|101|102|103|104|105|106|107||110|111|112|113|114|115|116|117||120|121|122|123|-99|null|126|127,
                    4:1|3||5|7||11|-199||17|19,
                    4:1|3||5|7||11|-199||17|19,
                    me value 6899,
                    2147483647,25,4,109912,118,-2147483648""", page.getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    void testFormSubmission()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                route("/form_submission", TestFormSubmission.class);
            }
        })) {
            try (final var webClient = new WebClient()) {
                HtmlPage page = webClient.getPage("http://localhost:8181/form_submission");

                assertEquals("0", page.getTitleText());
                var form = page.getFormByName("getanswer");
                assertNotNull(form);
                form.getInputsByName("answer").get(0).setValueAttribute("12");
                page = form.getInputsByName("submit").get(0).click();

                assertEquals("12", page.getTitleText());
                form = page.getFormByName("getanswer");
                assertNotNull(form);
                form.getInputsByName("answer").get(0).setValueAttribute("32");
                page = form.getInputsByName("submit").get(0).click();

                assertEquals("44", page.getTitleText());
                form = page.getFormByName("getanswer");
                assertNotNull(form);
                form.getInputsByName("answer").get(0).setValueAttribute("41");
                page = form.getInputsByName("submit").get(0).click();

                assertEquals("got a total of 85", page.getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    void testUniqueIDPerRequest()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                route("/unique", TestFormSubmission.class);
            }
        })) {
            try (final var webClient = new WebClient()) {
                HtmlPage page1 = webClient.getPage("http://localhost:8181/unique");
                assertEquals("0", page1.getTitleText());
                var form1 = page1.getFormByName("getanswer");
                assertNotNull(form1);
                var cont1 = form1.getInputsByName(SpecialParameters.CONT_ID).get(0).getValue();
                assertNotNull(cont1);
                form1.getInputsByName("answer").get(0).setValueAttribute("12");

                HtmlPage page2 = form1.getInputsByName("submit").get(0).click();
                assertEquals("12", page2.getTitleText());
                var form2 = page2.getFormByName("getanswer");
                assertNotNull(form2);
                var cont2 = form2.getInputsByName(SpecialParameters.CONT_ID).get(0).getValue();
                assertNotNull(cont2);
                form2.getInputsByName("answer").get(0).setValueAttribute("32");

                HtmlPage page3 = form2.getInputsByName("submit").get(0).click();
                assertEquals("44", page3.getTitleText());
                var form3 = page3.getFormByName("getanswer");
                assertNotNull(form3);
                var cont3 = form3.getInputsByName(SpecialParameters.CONT_ID).get(0).getValue();
                assertNotNull(cont3);
                form3.getInputsByName("answer").get(0).setValueAttribute("41");

                // check if previous continuation contexts makes the logic
                // start from scratch again
                HtmlPage page4 = form1.getInputsByName("submit").get(0).click();
                assertEquals("0", page4.getTitleText());
                var form4 = page4.getFormByName("getanswer");
                assertNotNull(form4);
                var cont4 = form4.getInputsByName(SpecialParameters.CONT_ID).get(0).getValue();
                assertNotNull(cont4);

                HtmlPage page5 = form2.getInputsByName("submit").get(0).click();
                assertEquals("0", page5.getTitleText());
                var form5 = page5.getFormByName("getanswer");
                assertNotNull(form5);
                var cont5 = form5.getInputsByName(SpecialParameters.CONT_ID).get(0).getValue();
                assertNotNull(cont5);

                // each should have a unique id
                assertNotEquals(cont4, cont1);
                assertNotEquals(cont4, cont2);
                assertNotEquals(cont4, cont3);
                assertNotEquals(cont4, cont5);
                assertNotEquals(cont5, cont1);
                assertNotEquals(cont5, cont2);
                assertNotEquals(cont5, cont3);
                assertNotEquals(cont5, cont4);

                // perform the last step in the calculation
                HtmlPage page6 = form3.getInputsByName("submit").get(0).click();
                assertEquals("got a total of 85", page6.getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    void testNumberGuess()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                route("/numberguess", TestNumberGuess.class);
            }
        })) {
            try (final var webClient = new WebClient()) {
                HtmlPage page = null;
                HtmlForm form = null;

                var low_bound = 0;
                var high_bound = 100;
                var last_guess = 50;

                var tries = 0;

                page = webClient.getPage("http://localhost:8181/numberguess");

                do {
                    form = page.getFormByName("perform_guess");
                    assertNotNull(form);
                    form.getInputsByName("guess").get(0).setValueAttribute(String.valueOf(last_guess));
                    page = form.getInputsByName("submit").get(0).click();
                    tries++;

                    var text = page.getWebResponse().getContentAsString();
                    if (text.contains("lower")) {
                        high_bound = last_guess;
                        last_guess = low_bound + (last_guess - low_bound) / 2;
                    } else if (text.contains("higher")) {
                        if (last_guess == low_bound &&
                            low_bound == high_bound - 1) {
                            last_guess = high_bound;
                        } else {
                            low_bound = last_guess;
                            last_guess = last_guess + (high_bound - last_guess) / 2;
                        }
                    }
                }
                while (page.getTitleText().equals("Perform a guess"));

                var text = page.getWebResponse().getContentAsString();
                assertTrue(text.contains("the answer was " + last_guess));
                assertTrue(text.contains("guessed it in " + tries + " tries"));
            }
        }
    }
}
