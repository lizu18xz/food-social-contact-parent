package com.fayayo.oauth2.server.controller;

import com.fayayo.commons.model.domain.ResultInfo;
import com.fayayo.commons.model.domain.SignInIdentity;
import com.fayayo.commons.utils.ResultInfoUtil;
import com.fayayo.commons.vo.SignInDinerInfo;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2RefreshToken;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author dalizu on 2021/1/26.
 * @version v1.0
 * @desc 用户中心
 */
@RestController
public class UserController {

    @Resource
    private HttpServletRequest request;

    @Resource
    private RedisTokenStore redisTokenStore;


    //http://localhost:8082/user/me?access_token=8ad22e25-bf0c-478f-b07c-96854f8c97fe
    /**
     * 访问资源服务器需要先获取access_token
     * */
    @GetMapping("user/me")
    public ResultInfo getCurrentUser(Authentication authentication){

        SignInIdentity signInIdentity= (SignInIdentity) authentication.getPrincipal();
        SignInDinerInfo dinerInfo=new SignInDinerInfo();
        BeanUtils.copyProperties(signInIdentity,dinerInfo);

        return ResultInfoUtil.buildSuccess(request.getServletPath(),dinerInfo);
    }


    @GetMapping("user/logout")
    public ResultInfo logout(String access_token,String authorization){

        if(StringUtils.isBlank(access_token)){
            access_token=authorization;
        }

        if(StringUtils.isBlank(access_token)){
            return ResultInfoUtil.buildSuccess(request.getServletPath(),"退出成功");
        }

        //判断bearer token是否为空
        if(access_token.toLowerCase().contains("bearer ".toLowerCase())){
            access_token=access_token.toLowerCase().replace("bearer ","");
        }

        //清除redis token信息
        OAuth2AccessToken oAuth2AccessToken=redisTokenStore.readAccessToken(access_token);
        if(oAuth2AccessToken!=null){
            redisTokenStore.removeAccessToken(oAuth2AccessToken);
            OAuth2RefreshToken oAuth2RefreshToken=oAuth2AccessToken.getRefreshToken();
            redisTokenStore.removeRefreshToken(oAuth2RefreshToken);
        }

        return ResultInfoUtil.buildSuccess(request.getServletPath(),"退出成功");
    }

}
