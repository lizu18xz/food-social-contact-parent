package com.fayayo.registry;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @author dalizu on 2021/1/21.
 * @version v1.0
 * @desc
 */
@EnableEurekaServer
@SpringBootApplication
public class RegistryApplication {

    public static void main(String[] args) {
        SpringApplication.run(RegistryApplication.class,args);
    }

}
