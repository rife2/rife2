package rife.engine;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.Test;
import rife.engine.continuations.TestPauseElement;
import rife.tools.StringUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestContinuations {
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

                page = webClient.getPage("http://localhost:8181/simple");
                assertEquals("after simple pause", page.getWebResponse().getContentAsString());
            }
        }
    }
}
