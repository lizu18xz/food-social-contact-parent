package com.fayayo.diners.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.fayayo.commons.constant.ApiConstant;
import com.fayayo.commons.model.domain.ResultInfo;
import com.fayayo.commons.model.dto.DinersDTO;
import com.fayayo.commons.model.pojo.Diners;
import com.fayayo.commons.utils.AssertUtil;
import com.fayayo.commons.utils.ResultInfoUtil;
import com.fayayo.commons.vo.ShortDinerInfo;
import com.fayayo.diners.config.OAuth2ClientConfiguration;
import com.fayayo.diners.domain.OAuthDinerInfo;
import com.fayayo.diners.mapper.DinersMapper;
import com.fayayo.diners.vo.LoginDinerInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.http.client.support.BasicAuthenticationInterceptor;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author dalizu on 2021/1/26.
 * @version v1.0
 * @desc 食客服务业务逻辑
 */
@Service
public class DinersService {

    @Resource
    private RestTemplate restTemplate;

    @Value("${service.name.ms-oauth-server}")
    private String oauthServerName;

    @Resource
    private OAuth2ClientConfiguration oAuth2ClientConfiguration;

    @Resource
    private DinersMapper dinersMapper;

    @Resource
    private SendVerifyCodeService sendVerifyCodeService;


    public List<ShortDinerInfo> findByIds(String ids){
        AssertUtil.isNotEmpty(ids);
        String [] idArr=ids.split(",");
        List<ShortDinerInfo>dinerInfos=dinersMapper.findByIds(idArr);
        return dinerInfos;
    }



    /**
     * 用户注册
     *
     * @param dinersDTO
     * @param path
     * @return
     */
    public ResultInfo register(DinersDTO dinersDTO, String path) {
        // 参数非空校验
        String username = dinersDTO.getUsername();
        AssertUtil.isNotEmpty(username, "请输入用户名");
        String password = dinersDTO.getPassword();
        AssertUtil.isNotEmpty(password, "请输入密码");
        String phone = dinersDTO.getPhone();
        AssertUtil.isNotEmpty(phone, "请输入手机号");
        String verifyCode = dinersDTO.getVerifyCode();
        AssertUtil.isNotEmpty(verifyCode, "请输入验证码");
        // 获取验证码
        String code = sendVerifyCodeService.getCodeByPhone(phone);
        // 验证是否过期
        AssertUtil.isNotEmpty(code, "验证码已过期，请重新发送");
        // 验证码一致性校验
        AssertUtil.isTrue(!dinersDTO.getVerifyCode().equals(code), "验证码不一致，请重新输入");
        // 验证用户名是否已注册
        Diners diners = dinersMapper.selectByUsername(username.trim());
        AssertUtil.isTrue(diners != null, "用户名已存在，请重新输入");
        // 注册
        // 密码加密
        dinersDTO.setPassword(DigestUtil.md5Hex(password.trim()));
        dinersMapper.save(dinersDTO);
        // 自动登录
        return signIn(username.trim(), password.trim(), path);
    }


    public void checkPhoneIsRegistered(String phone){
        AssertUtil.isNotEmpty(phone,"手机号不能为空");
        Diners diners=dinersMapper.selectByPhone(phone);
        AssertUtil.isTrue(diners==null,"该手机号未注册");
        AssertUtil.isTrue(diners.getIsValid()==0,"该手机号已锁定,请先解锁");
    }


    public ResultInfo signIn(String account,String password,String path){
        //参数校验
        AssertUtil.isNotEmpty(account,"请输入登录账号");
        AssertUtil.isNotEmpty(password,"请输入登录密码");
        //构建请求头
        HttpHeaders headers=new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        //构建请求体
        MultiValueMap<String,Object> body=new LinkedMultiValueMap<>();
        body.add("username",account);
        body.add("password",password);
        body.setAll(BeanUtil.beanToMap(oAuth2ClientConfiguration));
        HttpEntity<MultiValueMap<String,Object>>entity=new HttpEntity<>(body,headers);
        //设置Authorization
        restTemplate.getInterceptors().add(new BasicAuthenticationInterceptor(
                oAuth2ClientConfiguration.getClientId(),oAuth2ClientConfiguration.getSecret()
        ));
        //发送请求
        ResponseEntity<ResultInfo> result=restTemplate.postForEntity(oauthServerName+"oauth/token",entity,ResultInfo.class);

        //处理返回结果
        AssertUtil.isTrue(result.getStatusCode()!= HttpStatus.OK,"登录失败");
        ResultInfo resultInfo=result.getBody();
        if(resultInfo.getCode()!= ApiConstant.SUCCESS_CODE){
            resultInfo.setData(resultInfo.getMessage());
            return resultInfo;
        }
        //LinkedHashMap  转对象
        OAuthDinerInfo dinerInfo=BeanUtil.fillBeanWithMap
                ((LinkedHashMap)resultInfo.getData(),new OAuthDinerInfo(),false);


        LoginDinerInfo loginDinerInfo=new LoginDinerInfo();
        loginDinerInfo.setToken(dinerInfo.getAccessToken());
        loginDinerInfo.setAvatarUrl(dinerInfo.getAvatarUrl());
        loginDinerInfo.setNickname(dinerInfo.getNickname());
        return ResultInfoUtil.buildSuccess(path,loginDinerInfo);

    }


}
