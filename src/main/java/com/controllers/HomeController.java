package com.controllers;

import com.security.Authorities;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/hello")
@RestController
public class HomeController {
    private Logger logger = Logger.getLogger(HomeController.class);

    @RequestMapping("/1")
    String hello(@RequestParam String user,@RequestParam String password){
        logger.info("Hello endpoint : " + user + " " + password);
//        authentication.autoLogin(user,password);
        return "good";
    }

    @RequestMapping("/2")
    String hello2(@RequestParam String user,@RequestParam String password){
        logger.info("Hello endpoint : " + Authorities.ADMIN);
//        authentication.autoLogin(user,password);
        return "good";
    }

}
