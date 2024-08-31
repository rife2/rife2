[![License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Java](https://img.shields.io/badge/java-17%2B-blue)](https://www.oracle.com/java/technologies/javase/jdk17-archive-downloads.html)
[![bld](https://img.shields.io/badge/2.1.0-FA9052?label=bld&labelColor=2392FF)](https://rife2.com/bld)
[![Release](https://img.shields.io/github/release/rife2/rife2.svg)](https://github.com/rife2/rife2/releases/latest)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.uwyn.rife2/rife2/badge.svg?color=blue)](https://maven-badges.herokuapp.com/maven-central/com.uwyn.rife2/rife2)
[![Nexus Snapshot](https://img.shields.io/nexus/s/com.uwyn.rife2/rife2?server=https%3A%2F%2Fs01.oss.sonatype.org%2F)](https://s01.oss.sonatype.org/content/repositories/snapshots/com/uwyn/rife2/rife2/)
[![bld-ci](https://github.com/rife2/rife2/actions/workflows/bld.yml/badge.svg)](https://github.com/rife2/rife2/actions/workflows/bld.yml)
[![Tests](https://rife2.com/tests-badge/badge/com.uwyn.rife2/rife2)](https://github.com/rife2/rife2/actions/workflows/bld.yml)

<br>

<p align="center"><img src="https://github.com/rife2/rife2/raw/main/images/rife2_logo.png" width="200"></p>

# Welcome

RIFE2 is a full-stack, no-declaration, framework to quickly and effortlessly
create web applications with modern Java.

RIFE2 is built on the foundations of the original RIFE framework that was
popular from 2002-2010. Since then, the world and Java have changed and many of
the original RIFE APIs could finally be replaced with pure Java, no-XML,
no-YAML, leaving only type-safe expressive code.

RIFE2 preserves most of the original features and adds new ones, for a fraction
of the footprint and with even greater developer productivity than before.
RIFE2 is created by Geert Bevin, one of the first Java Champions and speaker at
many Java conferences.

> **TIP:** If you use IntelliJ IDEA as your IDE, consider installing the
> [RIFE2 IDEA Plug-in](https://github.com/rife2/rife2-idea).  
> It will greatly enhance your coding experience.

**This is a quick tutorial, the [full documentation](https://github.com/rife2/rife2/wiki)
contains a lot more information.**

**The [RIFE2 Javadocs](https://rife2.github.io/rife2/) complement the
documentation with many more details.**

<a href="https://www.youtube.com/watch?feature=player_embedded&v=AZWzYwAHDIE" target="_blank">
 <img src="https://img.youtube.com/vi/AZWzYwAHDIE/maxresdefault.jpg" alt="Watch the video" width="640" height="360" border="0" />
</a>

## Why RIFE2?

A frequently asked question is: "Why choose RIFE2 over other popular frameworks"?

The short answer is that *RIFE2 is different*, it's designed to create web
applications quickly with small teams. It has challenged and will always
challenge the status-quo. It's not meant to replace enterprise-level frameworks
like Spring or JEE, though I've used it for enterprise applications. RIFE2
leverages the power of the Java platform for web applications that you'd usually
write with scripting languages. Productivity and maintainability is key, and
you'll find that you get 90% of the work done for 10% of the effort, and can
still integrate with other Java libraries and frameworks where you need it.

RIFE2 has features that after 20 years still can't be found elsewhere:  
web continuations, bidirectional template engine, bean-centric metadata system,
full-stack without dependencies, metadata-driven SQL builders, content
management framework, full localization support, resource abstraction, persisted
cron-like scheduler, continuations-based workflow engine.

Most of these features have stood the test of time and after 20 years still
prove to be great choices for web application development. RIFE2 has learned
from decades of experience and improves on these original features in many ways.

RIFE2 also has features that have been adopted by others, but that usually lack
the convenience of the tight integration throughout a full-stack.

For instance: out-of-container tests can analyze the structure of the resulting
templates without having to parse HTML, the authentication system is built from
all the other pieces of the full-stack and seamlessly integrates into your web
application, URLs are generated from the configuration you created without the
risk of becoming stale, the logic-less templates are really purely content
driven and can generate any text-based format (JSON, XML, HTML, SVG, SQL), ...
and much more.

*RIFE2 is the red pill*, ready to show you how deep the rabbit hole can go, if
you're up for it!

# Quickstart

## Hello World Example

This is how you get started with a `Hello World` site.

```java
public class HelloWorld extends Site {
    public void setup() {
        get("/hello", c -> c.print("Hello World"));
    }

    public static void main(String[] args) {
        new Server().start(new HelloWorld());
    }
}
```

The `main` method spins up the integrated embedded Jetty server, so that you can
immediately start coding. The same `HelloWorld` class can be added as a
parameter value to your `web.xml`, requiring absolute no changes to your code
between development and production.

Out-of-container testing is a first-class citizen in RIFE2, directly interacting
with your `Site` class to simulate full request-response interactions,
without having to spin up a servlet container.

This is how you could test the example above with JUnit 5:

```java
class HelloTest {
    @Test void verifyHelloWorld() {
        var m = new MockConversation(new HelloWorld());
        assertEquals("Hello World", m.doRequest("/hello").getText());
    }
}
```

## Type-safe Links and URLs

One of the most brittle aspects of web application development is typing links
and URLs as text literals, without anything guaranteeing they remain correct
when your routes change or when you deploy your application in different web
application contexts. RIFE2's routing API allows all your application links to
be generated correctly without any effort on your behalf.

Let's add a new route that contains an HTML link towards the previous Hello
World route.

You can see that routes don't have to be created inside the `setup()` method,
but can also be created as part of your `Site`'s construction, allowing the
routes to be stored in fields.

```java
public class HelloLink extends Site {
    Route hello = get("/hello", c -> c.print("Hello World"));
    Route link = get("/link", c-> c.print("<a href='" + c.urlFor(hello) + "'>Hello</a>"));

    public static void main(String[] args) {
        new Server().start(new HelloLink());
    }
}
```

We can now test this as such:

```java
class HelloTest {
    @Test void verifyHelloLink() {
        var m = new MockConversation(new HelloLink());
        assertEquals("Hello World", m.doRequest("/link")
            .getParsedHtml().getLinkWithText("Hello")
            .follow().getText());
    }
}
```

## Bidirectional Logic-Less Templates

The main impetus that had me create RIFE2, was RIFE's unique template engine.

RIFE2's templates contain two main concepts:
* *values* - that can be filled in with content and data
* *blocks* - that will be stripped away and that provide content snippets

Your Java code will compose the final layout by assigning and appending blocks,
and by putting data into values. Let's rewrite the `HelloLink` example above with a template.

In this example, no template manipulation is done in Java yet.

Instead, it introduces the `{{v route:hello/}}` value tag, which will
automatically be replaced with the URL of the route that is available with that
field name in your active `Site`.

```java
public class HelloTemplate extends Site {
    Route hello = get("/hello", c -> c.print("Hello World"));
    Route link = get("/link", c-> c.print(c.template("HelloTemplate")));

    public static void main(String[] args) {
        new Server().start(new HelloTemplate());
    }
}
```
With `HelloTemplate.html` being:

```html
<!DOCTYPE html>
<html lang="en">
<body>
<a href="{{v route:hello/}}">Hello</a>
</body>
</html>
```

Note that RIFE2 internally transforms your templates into Java classes by
generating optimized bytecode.

This happens on-the-fly during development. For production, templates can be
pre-compiled, making them incredibly fast. 

## Template Manipulation

Let's change the example some more and create a single route that can respond to
both `get` and `post` requests.

* the `get` request will display a form with a single button to click.
* the `post` request will receive the form's submission and display `Hello World`.

```java
public class HelloForm extends Site {
    Route hello = route("/hello", c -> {
        var t = c.template("HelloForm");
        switch (c.method()) {
            case GET -> t.setBlock("content", "form");
            case POST -> t.setBlock("content", "text");
        }
        c.print(t);
    });

    public static void main(String[] args) {
        new Server().start(new HelloForm());
    }
}
```

With `HelloForm.html` being:

```html
<!DOCTYPE html>
<html lang="en">
<body>
<!--v content/-->
<!--b form-->
<form action="{{v route:action:hello/}}" method="post" name="hello">
  <!--v route:inputs:hello/-->
  <input type="submit" name="Submit">
</form>
<!--/b-->
<!--b text--><p id="greeting">Hello World</p><!--/b-->
</body>
</html>
```

> **NOTE:** that the `route:` value tag from the above has been split into
> `route:action:` and `route:inputs:`, generating hidden HTML form inputs for
> parameters instead of query string parameters.

You can see that the template contains all the pieces to create both pages:

* the value named `content`
* the block named `form`
* the block named `text`

In Java, we simply assign either block to the value, depending on what we want
to display.

Another benefit is that RIFE2's template tags can be HTML comments, making them
completely invisible. This allows you to work on your HTML design as usual and
preview the template file with a regular browser.

Finally, let's include a test for this functionality:

```java
class HelloTest {
    @Test void verifyHelloForm() {
        var m = new MockConversation(new HelloForm());
        var r = m.doRequest("/hello").getParsedHtml()
            .getFormWithName("hello").submit();
        assertEquals("Hello World", r.getParsedHtml()
            .getDocument().body()
            .getElementById("greeting").text());
    }
}
```

## Just the top of the rabbit hole

Thanks for reading until the end!

This was merely a quick introduction to whet your appetite, RIFE2 comes with a
comprehensive and easy to read manual with many examples and pragmatic
explanations.

If you have any questions, suggestions, ideas or just want to chat, feel free
to post on the [forums](https://forum.uwyn.com) or to join
us on [Discord](https://discord.gg/DZRYPtkb6J).

**Read more in the [full documentation](https://github.com/rife2/rife2/wiki)
and [RIFE2 Javadocs](https://rife2.github.io/rife2/).**
