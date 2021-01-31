package com.fayayo.diners.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author dalizu on 2021/1/21.
 * @version v1.0
 * @desc
 */
@RestController
@RequestMapping("hello")
public class HelloController {

    @GetMapping
    public String hello(String name){

        return "hello"+name;

    }

}
