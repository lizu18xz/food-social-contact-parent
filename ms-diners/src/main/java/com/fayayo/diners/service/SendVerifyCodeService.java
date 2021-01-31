package com.fayayo.diners.service;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.fayayo.commons.constant.RedisKeyConstant;
import com.fayayo.commons.utils.AssertUtil;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * @author dalizu on 2021/1/28.
 * @version v1.0
 * @desc 发送验证码的业务逻辑层
 */
@Service
public class SendVerifyCodeService {

    @Resource
    private RedisTemplate<String,String>redisTemplate;


    public void send(String phone){
        //校验非空
        AssertUtil.isNotEmpty(phone,"手机号不能为空");
        //根据手机号查询是否已经生成验证码,如果已经生成直接返回
        if(!checkCodeIsExpired(phone)){
            return;
        }
        //生成6位验证码
        String code=RandomUtil.randomNumbers(6);
        //调用短信服务发送短信
        //发送成功将code保存至redis,失效时间60s
        String key=RedisKeyConstant.verify_code.getKey()+phone;
        redisTemplate.opsForValue().set(key,code,60, TimeUnit.SECONDS);

    }

    private boolean checkCodeIsExpired(String phone) {
        String key=RedisKeyConstant.verify_code.getKey()+phone;
        String code=redisTemplate.opsForValue().get(key);
        return StrUtil.isBlank(code) ? true: false;
    }

    //根据手机号获取验证码
    public String getCodeByPhone(String phone){
        String key=RedisKeyConstant.verify_code.getKey()+phone;
        return redisTemplate.opsForValue().get(key);
    }


}
