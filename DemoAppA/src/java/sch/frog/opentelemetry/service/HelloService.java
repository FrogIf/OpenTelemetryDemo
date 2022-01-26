package sch.frog.opentelemetry.service;

import org.springframework.stereotype.Service;

@Service
public class HelloService {
    
    public String hello(){
        return "success";
    }

}
