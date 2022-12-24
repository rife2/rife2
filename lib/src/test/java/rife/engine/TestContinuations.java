package rife.engine;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.Test;
import rife.engine.continuations.*;
import rife.tools.StringUtils;

import static org.junit.jupiter.api.Assertions.*;

public class TestContinuations {
    @Test
    public void testNoPause()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                get("/nopause", TestNoPause.class);
            }
        })) {
            try (final var webClient = new WebClient()) {
                HtmlPage page = webClient.getPage("http://localhost:8181/nopause");

                String text = page.getWebResponse().getContentAsString();
                assertEquals("", text);
            }
        }
    }

    @Test
    public void testSimplePause()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                get("/simple", TestSimplePause.class);
            }
        })) {
            try (final var webClient = new WebClient()) {
                HtmlPage page = webClient.getPage("http://localhost:8181/simple");

                String text = page.getWebResponse().getContentAsString();
                String[] lines = StringUtils.splitToArray(text, "\n");
                assertEquals(2, lines.length);
                assertEquals("before simple pause", lines[0]);

                page = webClient.getPage("http://localhost:8181/simple?" + SpecialParameters.CONT_ID + "=" + lines[1]);
                assertEquals("after simple pause", page.getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    public void testNull()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                get("/null", TestNull.class);
            }
        })) {
            try (final var webClient = new WebClient()) {
                HtmlPage page = webClient.getPage("http://localhost:8181/null");

                String text = page.getWebResponse().getContentAsString();
                String[] lines = StringUtils.splitToArray(text, "\n");
                assertEquals(2, lines.length);
                assertEquals("before null pause", lines[0]);

                page = webClient.getPage("http://localhost:8181/null?response=after%20null%20pause&" + SpecialParameters.CONT_ID + "=" + lines[1]);
                assertEquals("after null pause", page.getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    public void testNullReference()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                get("/null_reference", TestNullReference.class);
            }
        })) {
            try (final var webClient = new WebClient()) {
                HtmlPage page = webClient.getPage("http://localhost:8181/null_reference");

                String text = page.getWebResponse().getContentAsString();

                page = webClient.getPage("http://localhost:8181/null_reference?" + SpecialParameters.CONT_ID + "=" + text);
                assertTrue(page.getWebResponse().getContentAsString().contains("java.lang.NullPointerException"));
            }
        }
    }

    @Test
    public void testNullConditional()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                get("/null_conditional", TestNullConditional.class);
            }
        })) {
            try (final var webClient = new WebClient()) {
                HtmlPage page = webClient.getPage("http://localhost:8181/null_conditional?value=thevalue");

                String text = page.getWebResponse().getContentAsString();
                assertTrue(text.startsWith("thevalue"));

                page = webClient.getPage("http://localhost:8181/null_conditional?" + SpecialParameters.CONT_ID + "=" + text.substring(8));
                assertEquals("thevalue", page.getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    public void testConditional()
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
    public void testMemberMethod()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            public void setup() {
                get("/member_method", TestMemberMethod.class);
            }
        })) {
            try (final var webClient = new WebClient()) {
                HtmlPage page = webClient.getPage("http://localhost:8181/member_method");

                String text = page.getWebResponse().getContentAsString();
                String[] lines = StringUtils.splitToArray(text, "\n");
                assertEquals(2, lines.length);
                assertEquals("before pause", lines[0]);

                page = webClient.getPage("http://localhost:8181/member_method?" + SpecialParameters.CONT_ID + "=" + lines[1]);
                assertEquals("me value 6899", page.getWebResponse().getContentAsString());
            }
        }
    }

    @Test
    public void testPrivateMethod()
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

    // TODO : max frames calculation causes ASM to load the element class from the classloader during the transformation
//    @Test
//    public void testSynchronization()
//    throws Exception {
//        try (final var server = new TestServerRunner(new Site() {
//            public void setup() {
//                get("/synchronization", TestSynchronization.class);
//            }
//        })) {
//            try (final var webClient = new WebClient()) {
//                HtmlPage page = webClient.getPage("http://localhost:8181/synchronization");
//
//                String text = page.getWebResponse().getContentAsString();
//                String[] lines = StringUtils.splitToArray(text, "\n");
//                assertEquals(2, lines.length);
//                assertEquals("monitor this", lines[0]);
//
//                page = webClient.getPage("http://localhost:8181/synchronization?" + SpecialParameters.CONT_ID + "=" + lines[1]);
//
//                text = page.getWebResponse().getContentAsString();
//                lines = StringUtils.splitToArray(text, "\n");
//                assertEquals(2, lines.length);
//                assertEquals("monitor this", lines[0]);
//            }
//        }
//    }

    @Test
    public void testThrow()
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
}
