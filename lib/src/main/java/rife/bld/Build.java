package rife.bld;

import static rife.bld.dependencies.Scope.*;

public class Build extends Project {
    public void setup() {
        pkg = "com.example";
        name = "Myapp";
        version = version(0,1,0);

        scope(compile)
            .include(dependency("com.uwyn.rife2", "rife2", version(1,4,0)));
        scope(test)
            .include(dependency("org.jsoup", "jsoup", version(1,15,3)))
            .include(dependency("org.junit.jupiter", "junit-jupiter", version(5,9,1)));
        scope(standalone)
            .include(dependency("org.eclipse.jetty", "jetty-server", version(11,0,13)))
            .include(dependency("org.eclipse.jetty", "jetty-servlet", version(11,0,13)))
            .include(dependency("org.slf4j", "slf4j-simple", version(2,0,5)));
    }

    public static void main(String[] args) {
        new Build().start(args);
    }
}