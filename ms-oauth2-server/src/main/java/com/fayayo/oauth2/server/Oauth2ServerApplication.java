package com.fayayo.oauth2.server;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author dalizu on 2021/1/23.
 * @version v1.0
 * @desc
 */
@MapperScan("com.fayayo.oauth2.server.mapper")
@SpringBootApplication
public class Oauth2ServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(Oauth2ServerApplication.class,args);
    }

}
