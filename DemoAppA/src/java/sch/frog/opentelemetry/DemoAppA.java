package sch.frog.opentelemetry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

@SpringBootApplication
@EnableAspectJAutoProxy(exposeProxy = true)
public class DemoAppA {
    
    public static void main(String[] args){
        SpringApplication.run(DemoAppA.class, args);
    }

}
