package rife.bld;

public class Build extends Project {
    public void setup() {
        pkg = "com.example";
        name = "Myapp";
        version = version(0, 1, 0);
    }

    public static void main(String[] args) {
        new Build().start(args);
    }
}