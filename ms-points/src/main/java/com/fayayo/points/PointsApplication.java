package com.fayayo.points;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author dalizu on 2021/3/6.
 * @version v1.0
 * @desc
 */
@MapperScan("com.fayayo.points.mapper")
@SpringBootApplication
public class PointsApplication {

    public static void main(String[] args) {
        SpringApplication.run(PointsApplication.class,args);
    }

}
