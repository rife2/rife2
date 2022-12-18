<p align="center"><img src="https://github.com/gbevin/rife2/raw/main/rife2_logo.png" width="200"></p>

# Welcome

RIFE2 is a full-stack, no-declaration, framework to quickly and effortlessly create web applications with modern Java.

RIFE2 is built on the foundations of the original RIFE framework that was popular from 2002-2010.
Since then, the world and Java have changed and many of the original RIFE APIs can finally be replaced with pure Java, no-XML, no-YAML, leaving only type-safe expressive code.  

**NOTE: The documentation and this readme are work-in-progress**

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

The `main` method spins up the integrated embedded Jetty server, so that you can immediately start coding. The same `HelloWorld` class can be added as a
parameter value to your `web.xml`, requiring absolute no changes to your code between development and production.

Out-of-container testing is a first-class citizen in RIFE2, directly interacting with your `Site` class to simulate full request-response interactions,
without having to spin up a servlet container.

Let's test the example above with JUnit 5:

```java
class HelloTest {
    @Test void verifyHelloWorld() {
        var m = new MockConversation(new HelloWorld());
        assertEquals("Hello World", m.doRequest("/hello").getText());
    }
}
```

Here's an example snippet that should help you compile and run this example with Gradle.
Please make sure to adapt the artifact versions to the latest ones.

```kotlin
application {
    mainClass.set("HelloWorld")
}

dependencies {
    implementation("com.uwyn.rife2:rife2:0.8.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.8.2")
    testImplementation("org.jsoup:jsoup:1.15.3")
    runtimeOnly("org.eclipse.jetty:jetty-server:11.0.12")
    runtimeOnly("org.eclipse.jetty:jetty-servlet:11.0.12")
}
```

RIFE2 doesn't publish dependencies for _jsoup_ nor _Jetty_ because neither of them should be packaged with a production deployment.

You will want to:
* depend on `org.jsoup:jsoup` if you want to parse HTML pages in RIFE2's web testing API
* depend on `org.eclipse.jetty` if you're launching the embedded server like in these examples

RIFE2 also ships with example Gradle projects that should help you get set up quickly.

Please take a look here:
* Hello World app : https://github.com/gbevin/rife2/tree/main/app
* Run Hello World standalone : https://github.com/gbevin/rife2/tree/main/standalone
* Package Hello World war : https://github.com/gbevin/rife2/tree/main/war

Once you've got everything set up, give it a try and visit [http://localhost:8080/hello](http://localhost:8080/hello)

## Type-safe Links and URLs

One of the most brittle aspects of web application development is typing links and URLs as text literals, without anything guaranteeing they remain correct
when your routes change or when you deploy your application in different web application contexts. RIFE2's routing API allows all your application links to be
generated correctly without any effort on your behalf.

Let's add a new route that contains an HTML link towards the previous Hello World route.

You can see that routes don't have to be created inside the `setup()` method, but can also be created as part of your `Site`'s construction,
allowing the routes to be stored in fields.

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

The main impetus that had me resume work on RIFE2, was the unique template engine.

RIFE2's templates contain two main concepts:
* *values* - that can be filled in with content and data
* *blocks* - that will be stripped away and that provide content snippets

Your Java code will compose the final layout by assigning and appending blocks, and by putting data into values.
Let's rewrite the `HelloLink` example above with a template.

In this example, no template manipulation is done in Java yet.

Instead, it introduces the `{{v route:hello/}}` value tag, which will automatically be replaced with the URL of the route that is available with that field name in your active `Site`.

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

Note that RIFE2 internally transforms your templates into Java classes by generating heavily optimized bytecode.

This happens on-the-fly during development. For production, templates can be pre-compiled, making them incredibly fast. 

## Template Manipulation

Let's change the example some more and create a single route that can respond to both `get` and `post` requests.

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
<form action="{{v route:hello/}}" method="post" name="hello">
  <input type="submit" name="Submit">
</form>
<!--/b-->
<!--b text--><p id="greeting">Hello World</p><!--/b-->
</body>
</html>
```

You can see that the template contains all the pieces to create both pages:

* the value named `content`
* the block named `form`
* the block named `text`

In Java, we simply assign either block to the value, depending on what we want to display.

Another benefit is that RIFE2's template tags can be HTML comments, making them completely invisible.
This allows you to work on your HTML design as usual and preview the template file with a regular browser.

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