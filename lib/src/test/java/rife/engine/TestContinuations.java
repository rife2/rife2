package rife.engine;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.Test;
import rife.engine.continuations.*;
import rife.tools.StringUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
                get("/simple", TestPauseElement.class);
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
}
