package sch.frog.opentelemetry.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import sch.frog.opentelemetry.config.IOpenTelemetryOperator;
import sch.frog.opentelemetry.config.OpenTelemetryEnv;
import sch.frog.opentelemetry.service.HelloService;

import java.util.HashMap;

@RestController
public class HelloController {

    private static final Logger logger = LoggerFactory.getLogger(HelloController.class);

    @Value("${frog.appb.address}")
    private String appBAddress;

    @Autowired
    private HelloService helloService;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private IOpenTelemetryOperator openTelemetryOperator;
    
    @RequestMapping("/hello")
    public String hello(){
        String hello = helloService.hello();
        logger.info("service return : {}", hello);
        openTelemetryOperator.event("hello return", new HashMap<>(){{
            put("result", hello);
        }});

        String url = appBAddress + "/hello";
        ResponseEntity<String> res = restTemplate.getForEntity(url, String.class);
        logger.info("call app b , url : {}, response : {}", url, res.getBody());
        return "hello open telemetry";
    }

}
