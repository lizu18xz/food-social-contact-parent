package com.fayayo.follow;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author dalizu on 2021/2/16.
 * @version v1.0
 * @desc
 */
@MapperScan("com.fayayo.follow.mapper")
@SpringBootApplication
public class FollowAppication {

    public static void main(String[] args) {
        SpringApplication.run(FollowAppication.class,args);
    }

}
