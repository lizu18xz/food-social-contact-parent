package com.fayayo.oauth2.server.config;

import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

import javax.annotation.Resource;

/**
 * @author dalizu on 2021/1/23.
 * @version v1.0
 * @desc
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {

    //注入redis连接工厂
    @Resource
    private RedisConnectionFactory redisConnectionFactory;

    @Bean
    public RedisTokenStore redisTokenStore(){
        RedisTokenStore redisTokenStore=new RedisTokenStore(redisConnectionFactory);
        redisTokenStore.setPrefix("TOKEN:");
        return redisTokenStore;
    }

    //初始化密码编码器
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new PasswordEncoder() {
            /**
             *  *加密
             *  原始密码
             */
            @Override
            public String encode(CharSequence rawPassword) {
                return DigestUtils.md5Hex(rawPassword.toString());
            }

            /**
             *
             * 校验
             * 原始密码
             * 加密密码
             */
            @Override
            public boolean matches(CharSequence rawPassword, String encodesPassword) {
                return DigestUtils.md5Hex(rawPassword.toString()).equals(encodesPassword.toLowerCase());
            }
        };
    }

    //初始化认证管理对象

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }


    //放行和认证的规则
    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.csrf().disable()
                .authorizeRequests()
                //放行的请求
                .antMatchers("/oauth/**","/actuator/**").permitAll()
                .and()
                //其他请求
                .authorizeRequests()
                .anyRequest().authenticated();

    }
}
