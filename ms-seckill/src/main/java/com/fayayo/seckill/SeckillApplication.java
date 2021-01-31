package com.fayayo.seckill;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author dalizu on 2021/1/21.
 * @version v1.0
 * @desc
 */
@MapperScan("com.fayayo.seckill.mapper")
@SpringBootApplication
public class SeckillApplication {

    public static void main(String[] args) {
        SpringApplication.run(SeckillApplication.class,args);
    }

}
