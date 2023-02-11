package rife.services;

import rife.apis.ServiceProvider;

public class HelloService implements ServiceProvider {
    @Override
    public String serviceApi() {
        return "Hello World";
    }
}
