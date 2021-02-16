package com.fayayo.seckill.config;

import com.fayayo.seckill.model.RedisLock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;

import javax.annotation.Resource;

/**
 * @author dalizu on 2021/2/7.
 * @version v1.0
 * @desc
 */
@Configuration
public class RedisLockConfiguration {

    @Resource
    private RedisTemplate redisTemplate;

    @Bean
    public RedisLock redisLock(){
        RedisLock redisLock=new RedisLock(redisTemplate);
        return redisLock;
    }

}
