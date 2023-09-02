package rife.engine;

import com.gargoylesoftware.htmlunit.ElementNotFoundException;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import org.junit.jupiter.api.Test;
import rife.web.PagedNavigation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class TestPagedNavigation {
    HtmlPage page = null;

    @Test
    void testDefaults()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            @Override
            public void setup() {
                get("/defaults", c -> {
                    var t = c.template("paged_navigation");
                    var offset = c.parameterInt("offset");
                    t.setValue("offset", offset);
                    PagedNavigation.generate(c, t, 80, 10, offset, 3);
                    c.print(t.getBlock("content"));
                });
            }
        })) {
            try (final var webClient = new WebClient()) {
                // check the 'next' link
                page = webClient.getPage("http://localhost:8181/defaults");
                var response0 = page.getWebResponse().getContentAsString();
                assertEquals(response0, "0 : Pages: 8 ( first prev <a href=\"http://localhost:8181/defaults?offset=10\">next</a> <a href=\"http://localhost:8181/defaults?offset=70\">last</a> |  1  <a href=\"http://localhost:8181/defaults?offset=10\">2</a>  <a href=\"http://localhost:8181/defaults?offset=20\">3</a>  <a href=\"http://localhost:8181/defaults?offset=30\">4</a>  ...  )");
                page = page.getAnchorByText("next").click();
                var response10 = page.getWebResponse().getContentAsString();
                assertEquals(response10, "10 : Pages: 8 ( <a href=\"http://localhost:8181/defaults?offset=0\">first</a> <a href=\"http://localhost:8181/defaults?offset=0\">prev</a> <a href=\"http://localhost:8181/defaults?offset=20\">next</a> <a href=\"http://localhost:8181/defaults?offset=70\">last</a> |  <a href=\"http://localhost:8181/defaults?offset=0\">1</a>  2  <a href=\"http://localhost:8181/defaults?offset=20\">3</a>  <a href=\"http://localhost:8181/defaults?offset=30\">4</a>  <a href=\"http://localhost:8181/defaults?offset=40\">5</a>  ...  )");
                page = page.getAnchorByText("next").click();
                var response20 = page.getWebResponse().getContentAsString();
                assertEquals(response20, "20 : Pages: 8 ( <a href=\"http://localhost:8181/defaults?offset=0\">first</a> <a href=\"http://localhost:8181/defaults?offset=10\">prev</a> <a href=\"http://localhost:8181/defaults?offset=30\">next</a> <a href=\"http://localhost:8181/defaults?offset=70\">last</a> |  <a href=\"http://localhost:8181/defaults?offset=0\">1</a>  <a href=\"http://localhost:8181/defaults?offset=10\">2</a>  3  <a href=\"http://localhost:8181/defaults?offset=30\">4</a>  <a href=\"http://localhost:8181/defaults?offset=40\">5</a>  <a href=\"http://localhost:8181/defaults?offset=50\">6</a>  ...  )");
                page = page.getAnchorByText("next").click();
                var response30 = page.getWebResponse().getContentAsString();
                assertEquals(response30, "30 : Pages: 8 ( <a href=\"http://localhost:8181/defaults?offset=0\">first</a> <a href=\"http://localhost:8181/defaults?offset=20\">prev</a> <a href=\"http://localhost:8181/defaults?offset=40\">next</a> <a href=\"http://localhost:8181/defaults?offset=70\">last</a> |  <a href=\"http://localhost:8181/defaults?offset=0\">1</a>  <a href=\"http://localhost:8181/defaults?offset=10\">2</a>  <a href=\"http://localhost:8181/defaults?offset=20\">3</a>  4  <a href=\"http://localhost:8181/defaults?offset=40\">5</a>  <a href=\"http://localhost:8181/defaults?offset=50\">6</a>  <a href=\"http://localhost:8181/defaults?offset=60\">7</a>  ...  )");
                page = page.getAnchorByText("next").click();
                var response40 = page.getWebResponse().getContentAsString();
                assertEquals(response40, "40 : Pages: 8 ( <a href=\"http://localhost:8181/defaults?offset=0\">first</a> <a href=\"http://localhost:8181/defaults?offset=30\">prev</a> <a href=\"http://localhost:8181/defaults?offset=50\">next</a> <a href=\"http://localhost:8181/defaults?offset=70\">last</a> |  ...  <a href=\"http://localhost:8181/defaults?offset=10\">2</a>  <a href=\"http://localhost:8181/defaults?offset=20\">3</a>  <a href=\"http://localhost:8181/defaults?offset=30\">4</a>  5  <a href=\"http://localhost:8181/defaults?offset=50\">6</a>  <a href=\"http://localhost:8181/defaults?offset=60\">7</a>  <a href=\"http://localhost:8181/defaults?offset=70\">8</a>  )");
                page = page.getAnchorByText("next").click();
                var response50 = page.getWebResponse().getContentAsString();
                assertEquals(response50, "50 : Pages: 8 ( <a href=\"http://localhost:8181/defaults?offset=0\">first</a> <a href=\"http://localhost:8181/defaults?offset=40\">prev</a> <a href=\"http://localhost:8181/defaults?offset=60\">next</a> <a href=\"http://localhost:8181/defaults?offset=70\">last</a> |  ...  <a href=\"http://localhost:8181/defaults?offset=20\">3</a>  <a href=\"http://localhost:8181/defaults?offset=30\">4</a>  <a href=\"http://localhost:8181/defaults?offset=40\">5</a>  6  <a href=\"http://localhost:8181/defaults?offset=60\">7</a>  <a href=\"http://localhost:8181/defaults?offset=70\">8</a>  )");
                page = page.getAnchorByText("next").click();
                var response60 = page.getWebResponse().getContentAsString();
                assertEquals(response60, "60 : Pages: 8 ( <a href=\"http://localhost:8181/defaults?offset=0\">first</a> <a href=\"http://localhost:8181/defaults?offset=50\">prev</a> <a href=\"http://localhost:8181/defaults?offset=70\">next</a> <a href=\"http://localhost:8181/defaults?offset=70\">last</a> |  ...  <a href=\"http://localhost:8181/defaults?offset=30\">4</a>  <a href=\"http://localhost:8181/defaults?offset=40\">5</a>  <a href=\"http://localhost:8181/defaults?offset=50\">6</a>  7  <a href=\"http://localhost:8181/defaults?offset=70\">8</a>  )");
                page = page.getAnchorByText("next").click();
                var response70 = page.getWebResponse().getContentAsString();
                assertEquals(response70, "70 : Pages: 8 ( <a href=\"http://localhost:8181/defaults?offset=0\">first</a> <a href=\"http://localhost:8181/defaults?offset=60\">prev</a> next last |  ...  <a href=\"http://localhost:8181/defaults?offset=40\">5</a>  <a href=\"http://localhost:8181/defaults?offset=50\">6</a>  <a href=\"http://localhost:8181/defaults?offset=60\">7</a>  8  )");
                assertThrows(ElementNotFoundException.class, () -> page.getAnchorByText("next"));

                // check the 'previous' link
                page = page.getAnchorByText("prev").click();
                assertEquals(response60, page.getWebResponse().getContentAsString());
                page = page.getAnchorByText("prev").click();
                assertEquals(response50, page.getWebResponse().getContentAsString());
                page = page.getAnchorByText("prev").click();
                assertEquals(response40, page.getWebResponse().getContentAsString());
                page = page.getAnchorByText("prev").click();
                assertEquals(response30, page.getWebResponse().getContentAsString());
                page = page.getAnchorByText("prev").click();
                assertEquals(response20, page.getWebResponse().getContentAsString());
                page = page.getAnchorByText("prev").click();
                assertEquals(response10, page.getWebResponse().getContentAsString());
                page = page.getAnchorByText("prev").click();
                assertEquals(response0, page.getWebResponse().getContentAsString());
                assertThrows(ElementNotFoundException.class, () -> page.getAnchorByText("prev"));

                // check the 'last' and 'first' links
                assertEquals(response0, page.getWebResponse().getContentAsString());
                assertEquals(response70, page.getAnchorByText("last").click().getWebResponse().getContentAsString());
                assertThrows(ElementNotFoundException.class, () -> page.getAnchorByText("first"));
                page = page.getAnchorByText("next").click();
                assertEquals(response10, page.getWebResponse().getContentAsString());
                assertEquals(response70, page.getAnchorByText("last").click().getWebResponse().getContentAsString());
                assertEquals(response0, page.getAnchorByText("first").click().getWebResponse().getContentAsString());
                page = page.getAnchorByText("next").click();
                assertEquals(response20, page.getWebResponse().getContentAsString());
                assertEquals(response70, page.getAnchorByText("last").click().getWebResponse().getContentAsString());
                assertEquals(response0, page.getAnchorByText("first").click().getWebResponse().getContentAsString());
                page = page.getAnchorByText("next").click();
                assertEquals(response30, page.getWebResponse().getContentAsString());
                assertEquals(response70, page.getAnchorByText("last").click().getWebResponse().getContentAsString());
                assertEquals(response0, page.getAnchorByText("first").click().getWebResponse().getContentAsString());
                page = page.getAnchorByText("next").click();
                assertEquals(response40, page.getWebResponse().getContentAsString());
                assertEquals(response70, page.getAnchorByText("last").click().getWebResponse().getContentAsString());
                assertEquals(response0, page.getAnchorByText("first").click().getWebResponse().getContentAsString());
                page = page.getAnchorByText("next").click();
                assertEquals(response50, page.getWebResponse().getContentAsString());
                assertEquals(response70, page.getAnchorByText("last").click().getWebResponse().getContentAsString());
                assertEquals(response0, page.getAnchorByText("first").click().getWebResponse().getContentAsString());
                page = page.getAnchorByText("next").click();
                assertEquals(response60, page.getWebResponse().getContentAsString());
                assertEquals(response70, page.getAnchorByText("last").click().getWebResponse().getContentAsString());
                assertEquals(response0, page.getAnchorByText("first").click().getWebResponse().getContentAsString());
                page = page.getAnchorByText("next").click();
                assertEquals(response70, page.getWebResponse().getContentAsString());
                assertThrows(ElementNotFoundException.class, () -> page.getAnchorByText("last"));
                page = page.getAnchorByText("first").click();
                assertEquals(response0, page.getWebResponse().getContentAsString());

                // check the absolute links
                assertThrows(ElementNotFoundException.class, () -> page.getAnchorByText("1"));
                assertEquals(response10, page.getAnchorByText("2").click().getWebResponse().getContentAsString());
                assertEquals(response20, page.getAnchorByText("3").click().getWebResponse().getContentAsString());
                assertEquals(response30, page.getAnchorByText("4").click().getWebResponse().getContentAsString());
                assertThrows(ElementNotFoundException.class, () -> page.getAnchorByText("5"));
                assertThrows(ElementNotFoundException.class, () -> page.getAnchorByText("6"));
                assertThrows(ElementNotFoundException.class, () -> page.getAnchorByText("7"));
                assertThrows(ElementNotFoundException.class, () -> page.getAnchorByText("8"));
                page = page.getAnchorByText("next").click();
                assertEquals(response10, page.getWebResponse().getContentAsString());
                assertEquals(response0, page.getAnchorByText("1").click().getWebResponse().getContentAsString());
                assertThrows(ElementNotFoundException.class, () -> page.getAnchorByText("2"));
                assertEquals(response20, page.getAnchorByText("3").click().getWebResponse().getContentAsString());
                assertEquals(response30, page.getAnchorByText("4").click().getWebResponse().getContentAsString());
                assertEquals(response40, page.getAnchorByText("5").click().getWebResponse().getContentAsString());
                assertThrows(ElementNotFoundException.class, () -> page.getAnchorByText("6"));
                assertThrows(ElementNotFoundException.class, () -> page.getAnchorByText("7"));
                assertThrows(ElementNotFoundException.class, () -> page.getAnchorByText("8"));
                page = page.getAnchorByText("next").click();
                assertEquals(response20, page.getWebResponse().getContentAsString());
                assertEquals(response0, page.getAnchorByText("1").click().getWebResponse().getContentAsString());
                assertEquals(response10, page.getAnchorByText("2").click().getWebResponse().getContentAsString());
                assertThrows(ElementNotFoundException.class, () -> page.getAnchorByText("3"));
                assertEquals(response30, page.getAnchorByText("4").click().getWebResponse().getContentAsString());
                assertEquals(response40, page.getAnchorByText("5").click().getWebResponse().getContentAsString());
                assertEquals(response50, page.getAnchorByText("6").click().getWebResponse().getContentAsString());
                assertThrows(ElementNotFoundException.class, () -> page.getAnchorByText("7"));
                assertThrows(ElementNotFoundException.class, () -> page.getAnchorByText("8"));
                page = page.getAnchorByText("next").click();
                assertEquals(response30, page.getWebResponse().getContentAsString());
                assertEquals(response0, page.getAnchorByText("1").click().getWebResponse().getContentAsString());
                assertEquals(response10, page.getAnchorByText("2").click().getWebResponse().getContentAsString());
                assertEquals(response20, page.getAnchorByText("3").click().getWebResponse().getContentAsString());
                assertThrows(ElementNotFoundException.class, () -> page.getAnchorByText("4"));
                assertEquals(response40, page.getAnchorByText("5").click().getWebResponse().getContentAsString());
                assertEquals(response50, page.getAnchorByText("6").click().getWebResponse().getContentAsString());
                assertEquals(response60, page.getAnchorByText("7").click().getWebResponse().getContentAsString());
                assertThrows(ElementNotFoundException.class, () -> page.getAnchorByText("8"));
                page = page.getAnchorByText("next").click();
                assertEquals(response40, page.getWebResponse().getContentAsString());
                assertThrows(ElementNotFoundException.class, () -> page.getAnchorByText("1"));
                assertEquals(response10, page.getAnchorByText("2").click().getWebResponse().getContentAsString());
                assertEquals(response20, page.getAnchorByText("3").click().getWebResponse().getContentAsString());
                assertEquals(response30, page.getAnchorByText("4").click().getWebResponse().getContentAsString());
                assertThrows(ElementNotFoundException.class, () -> page.getAnchorByText("5"));
                assertEquals(response50, page.getAnchorByText("6").click().getWebResponse().getContentAsString());
                assertEquals(response60, page.getAnchorByText("7").click().getWebResponse().getContentAsString());
                assertEquals(response70, page.getAnchorByText("8").click().getWebResponse().getContentAsString());
                page = page.getAnchorByText("next").click();
                assertEquals(response50, page.getWebResponse().getContentAsString());
                assertThrows(ElementNotFoundException.class, () -> page.getAnchorByText("1"));
                assertThrows(ElementNotFoundException.class, () -> page.getAnchorByText("2"));
                assertEquals(response20, page.getAnchorByText("3").click().getWebResponse().getContentAsString());
                assertEquals(response30, page.getAnchorByText("4").click().getWebResponse().getContentAsString());
                assertEquals(response40, page.getAnchorByText("5").click().getWebResponse().getContentAsString());
                assertThrows(ElementNotFoundException.class, () -> page.getAnchorByText("6"));
                assertEquals(response60, page.getAnchorByText("7").click().getWebResponse().getContentAsString());
                assertEquals(response70, page.getAnchorByText("8").click().getWebResponse().getContentAsString());
                page = page.getAnchorByText("next").click();
                assertEquals(response60, page.getWebResponse().getContentAsString());
                assertThrows(ElementNotFoundException.class, () -> page.getAnchorByText("1"));
                assertThrows(ElementNotFoundException.class, () -> page.getAnchorByText("2"));
                assertThrows(ElementNotFoundException.class, () -> page.getAnchorByText("3"));
                assertEquals(response30, page.getAnchorByText("4").click().getWebResponse().getContentAsString());
                assertEquals(response40, page.getAnchorByText("5").click().getWebResponse().getContentAsString());
                assertEquals(response50, page.getAnchorByText("6").click().getWebResponse().getContentAsString());
                assertThrows(ElementNotFoundException.class, () -> page.getAnchorByText("7"));
                assertEquals(response70, page.getAnchorByText("8").click().getWebResponse().getContentAsString());
                page = page.getAnchorByText("next").click();
                assertEquals(response70, page.getWebResponse().getContentAsString());
                assertThrows(ElementNotFoundException.class, () -> page.getAnchorByText("1"));
                assertThrows(ElementNotFoundException.class, () -> page.getAnchorByText("2"));
                assertThrows(ElementNotFoundException.class, () -> page.getAnchorByText("3"));
                assertThrows(ElementNotFoundException.class, () -> page.getAnchorByText("4"));
                assertEquals(response40, page.getAnchorByText("5").click().getWebResponse().getContentAsString());
                assertEquals(response50, page.getAnchorByText("6").click().getWebResponse().getContentAsString());
                assertEquals(response60, page.getAnchorByText("7").click().getWebResponse().getContentAsString());
                assertThrows(ElementNotFoundException.class, () -> page.getAnchorByText("8"));
            }
        }
    }
    
    @Test
    void testNegativeOffset()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            @Override
            public void setup() {
                get("/defaults", c -> {
                    var t = c.template("paged_navigation");
                    var offset = c.parameterInt("offset");
                    t.setValue("offset", offset);
                    PagedNavigation.generate(c, t, 80, 10, offset, 3);
                    c.print(t.getBlock("content"));
                });
            }
        })) {
            try (final var webClient = new WebClient()) {
                page = webClient.getPage("http://localhost:8181/defaults?offset=-10");
                assertEquals(page.getWebResponse().getContentAsString(), "-10 : Pages: 8 ( first prev <a href=\"http://localhost:8181/defaults?offset=10\">next</a> <a href=\"http://localhost:8181/defaults?offset=70\">last</a> |  1  <a href=\"http://localhost:8181/defaults?offset=10\">2</a>  <a href=\"http://localhost:8181/defaults?offset=20\">3</a>  <a href=\"http://localhost:8181/defaults?offset=30\">4</a>  ...  )");
            }
        }
    }
    
    @Test
    void testOffsetEqualToCount()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            @Override
            public void setup() {
                get("/defaults", c -> {
                    var t = c.template("paged_navigation");
                    var offset = c.parameterInt("offset");
                    t.setValue("offset", offset);
                    PagedNavigation.generate(c, t, 80, 10, offset, 3);
                    c.print(t.getBlock("content"));
                });
            }
        })) {
            try (final var webClient = new WebClient()) {
                page = webClient.getPage("http://localhost:8181/defaults?offset=80");
                assertEquals(page.getWebResponse().getContentAsString(), "80 : Pages: 8 ( <a href=\"http://localhost:8181/defaults?offset=0\">first</a> <a href=\"http://localhost:8181/defaults?offset=60\">prev</a> next last |  ...  <a href=\"http://localhost:8181/defaults?offset=40\">5</a>  <a href=\"http://localhost:8181/defaults?offset=50\">6</a>  <a href=\"http://localhost:8181/defaults?offset=60\">7</a>  8  )");
            }
        }
    }
    
    @Test
    void testOffsetLargerThanCount()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            @Override
            public void setup() {
                get("/defaults", c -> {
                    var t = c.template("paged_navigation");
                    var offset = c.parameterInt("offset");
                    t.setValue("offset", offset);
                    PagedNavigation.generate(c, t, 80, 10, offset, 3);
                    c.print(t.getBlock("content"));
                });
            }
        })) {
            try (final var webClient = new WebClient()) {
                page = webClient.getPage("http://localhost:8181/defaults?offset=800");
                assertEquals(page.getWebResponse().getContentAsString(), "800 : Pages: 8 ( <a href=\"http://localhost:8181/defaults?offset=0\">first</a> <a href=\"http://localhost:8181/defaults?offset=60\">prev</a> next last |  ...  <a href=\"http://localhost:8181/defaults?offset=40\">5</a>  <a href=\"http://localhost:8181/defaults?offset=50\">6</a>  <a href=\"http://localhost:8181/defaults?offset=60\">7</a>  8  )");
            }
        }
    }
    
    @Test
    void testCustom()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            @Override
            public void setup() {
                get("/custom", c -> {
                    var t = c.template("paged_navigation");
                    var offset = c.parameterInt("myoff");
                    t.setValue("offset", offset);
                    PagedNavigation.generate(c, t, 10, 3, offset, 4, "myoff");
                    c.print(t.getBlock("content"));
                });
            }
        })) {
            try (final var webClient = new WebClient()) {
                page = webClient.getPage("http://localhost:8181/custom");
                assertEquals(page.getWebResponse().getContentAsString(), "0 : Pages: 4 ( first prev <a href=\"http://localhost:8181/custom?myoff=3\">next</a> <a href=\"http://localhost:8181/custom?myoff=9\">last</a> |  1  <a href=\"http://localhost:8181/custom?myoff=3\">2</a>  <a href=\"http://localhost:8181/custom?myoff=6\">3</a>  <a href=\"http://localhost:8181/custom?myoff=9\">4</a>  )");
                page = page.getAnchorByText("next").click();
                assertEquals(page.getWebResponse().getContentAsString(), "3 : Pages: 4 ( <a href=\"http://localhost:8181/custom?myoff=0\">first</a> <a href=\"http://localhost:8181/custom?myoff=0\">prev</a> <a href=\"http://localhost:8181/custom?myoff=6\">next</a> <a href=\"http://localhost:8181/custom?myoff=9\">last</a> |  <a href=\"http://localhost:8181/custom?myoff=0\">1</a>  2  <a href=\"http://localhost:8181/custom?myoff=6\">3</a>  <a href=\"http://localhost:8181/custom?myoff=9\">4</a>  )");
                page = page.getAnchorByText("next").click();
                assertEquals(page.getWebResponse().getContentAsString(), "6 : Pages: 4 ( <a href=\"http://localhost:8181/custom?myoff=0\">first</a> <a href=\"http://localhost:8181/custom?myoff=3\">prev</a> <a href=\"http://localhost:8181/custom?myoff=9\">next</a> <a href=\"http://localhost:8181/custom?myoff=9\">last</a> |  <a href=\"http://localhost:8181/custom?myoff=0\">1</a>  <a href=\"http://localhost:8181/custom?myoff=3\">2</a>  3  <a href=\"http://localhost:8181/custom?myoff=9\">4</a>  )");
                page = page.getAnchorByText("next").click();
                assertEquals(page.getWebResponse().getContentAsString(), "9 : Pages: 4 ( <a href=\"http://localhost:8181/custom?myoff=0\">first</a> <a href=\"http://localhost:8181/custom?myoff=6\">prev</a> next last |  <a href=\"http://localhost:8181/custom?myoff=0\">1</a>  <a href=\"http://localhost:8181/custom?myoff=3\">2</a>  <a href=\"http://localhost:8181/custom?myoff=6\">3</a>  4  )");
            }
        }
    }

    @Test
    void testNegativeCount()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            @Override
            public void setup() {
                get("/negativecount", c -> {
                    var t = c.template("paged_navigation");
                    var offset = c.parameterInt("myoff");
                    t.setValue("offset", offset);
                    PagedNavigation.generate(c, t, -10, 3, offset, 4, "myoff");
                    c.print(t.getBlock("content"));
                });
            }
        })) {
            try (final var webClient = new WebClient()) {
                page = webClient.getPage("http://localhost:8181/negativecount");
                assertEquals(page.getWebResponse().getContentAsString(), "0 : Pages: 0 ( first prev next last |  )");
            }
        }
    }

    @Test
    void testNoRangeCount()
    throws Exception {
        try (final var server = new TestServerRunner(new Site() {
            @Override
            public void setup() {
                get("/norangecount", c -> {
                    var t = c.template("paged_navigation_norangecount");
                    var offset = c.parameterInt("offset");
                    t.setValue("offset", offset);
                    PagedNavigation.generate(c, t, 80, 10, offset, 3);
                    c.print(t.getBlock("content"));
                });
            }
        })) {
            try (final var webClient = new WebClient()) {
                page = webClient.getPage("http://localhost:8181/norangecount");
                assertEquals(page.getWebResponse().getContentAsString(), "0 : Pages: ( first prev <a href=\"http://localhost:8181/norangecount?offset=10\">next</a> <a href=\"http://localhost:8181/norangecount?offset=70\">last</a> |  1  <a href=\"http://localhost:8181/norangecount?offset=10\">2</a>  <a href=\"http://localhost:8181/norangecount?offset=20\">3</a>  <a href=\"http://localhost:8181/norangecount?offset=30\">4</a>  ...  )");
            }
        }
    }
}
