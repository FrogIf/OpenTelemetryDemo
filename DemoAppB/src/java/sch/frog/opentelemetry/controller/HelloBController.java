package sch.frog.opentelemetry.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import sch.frog.opentelemetry.repository.TestDataRepository;

@RestController
public class HelloBController {

    private static final Logger logger = LoggerFactory.getLogger(HelloBController.class);

    @Autowired
    private TestDataRepository testDataRepository;
    
    @RequestMapping("/hello")
    public String hello(){
        logger.info("data count : {}", testDataRepository.count());
        return "success";
    }


}
