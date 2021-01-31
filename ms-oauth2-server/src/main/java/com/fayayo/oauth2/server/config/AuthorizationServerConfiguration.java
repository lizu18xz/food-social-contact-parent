package com.fayayo.oauth2.server.config;

import com.fayayo.commons.model.domain.SignInIdentity;
import com.fayayo.oauth2.server.service.UserService;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

import javax.annotation.Resource;
import java.util.LinkedHashMap;

/**
 * @author dalizu on 2021/1/23.
 * @version v1.0
 * @desc 认证
 */
@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfiguration extends AuthorizationServerConfigurerAdapter {


    @Resource
    private RedisTokenStore redisTokenStore;

    @Resource
    private AuthenticationManager authenticationManager;

    @Resource
    private ClientOAuth2DataConfiguration clientOAuth2DataConfiguration;

    @Resource
    private PasswordEncoder passwordEncoder;

    @Resource
    private UserService userService;

    /**
     * 配置令牌断点的安全约束
     * */
    @Override
    public void configure(AuthorizationServerSecurityConfigurer security) throws Exception {
        //允许访问 token的公钥，默认/oauth/token_key是受保护的
        security.tokenKeyAccess("permitAll()")
         //允许检查 token 的状态
        .checkTokenAccess("permitAll()");
    }

    /**
     * 客户端的配置  -- 授权模型
     *
     * */
    @Override
    public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
        clients.inMemory().withClient(clientOAuth2DataConfiguration.getClientId())
                .secret(passwordEncoder.encode(clientOAuth2DataConfiguration.getSecret()))
                .authorizedGrantTypes(clientOAuth2DataConfiguration.getGrant_types())
                .accessTokenValiditySeconds(clientOAuth2DataConfiguration.getTokenValidityTime())//token有效期
                .refreshTokenValiditySeconds(clientOAuth2DataConfiguration.getRefreshTokenValidityTime())//刷新有效期
                .scopes(clientOAuth2DataConfiguration.getScopes());


    }

    /**
     * 控制用户,让认证服务器知道有哪些用户可以来访问我的认证服务器
     * 配置授权以及令牌访问端点和令牌服务
     * 校验完用户名密码,security会生成令牌存入redis
     * */
    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        //认证器
        endpoints.authenticationManager(authenticationManager)
                //具体登录的逻辑
                .userDetailsService(userService)
                //token存储的方式
                .tokenStore(redisTokenStore)
                //令牌增强对象,增强返回的结果
                .tokenEnhancer((accessToken,authentication)->{
                    //获取登录后用户的信息.然后设置
                    SignInIdentity signInIdentity= (SignInIdentity) authentication.getPrincipal();
                    DefaultOAuth2AccessToken token= (DefaultOAuth2AccessToken) accessToken;
                    LinkedHashMap<String,Object>map=new LinkedHashMap<>();
                    map.put("nickname",signInIdentity.getNickname());
                    map.put("avatarUrl",signInIdentity.getNickname());
                    token.setAdditionalInformation(map);
                    return token;
                });

    }


}
