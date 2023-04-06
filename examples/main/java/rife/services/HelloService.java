package rife.services;

import rife.apis.MyService;

public class HelloService implements MyService {
    @Override
    public String serviceApi() {
        return "Hello World";
    }
}
