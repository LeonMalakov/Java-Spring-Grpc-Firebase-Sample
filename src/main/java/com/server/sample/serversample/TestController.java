package com.server.sample.serversample;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
public class TestController {
    @GetMapping
    public TestRequest Get() {
        return new TestRequest("Abc", 1);
    }
}
