package com.fayayo.gateway.filter;

import com.fayayo.gateway.component.HandleException;
import com.fayayo.gateway.config.IgnoreUrlsConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.annotation.Resource;

/**
 * @author dalizu on 2021/1/28.
 * @version v1.0
 * @desc 网关全局过滤器
 */
@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Resource
    private IgnoreUrlsConfig ignoreUrlsConfig;

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private HandleException handleException;

    /**
     * 统一身份校验处理
     * */
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("filters start");
        //判断是否在白名单中
        AntPathMatcher pathMatcher=new AntPathMatcher();
        boolean flag=false;
        String path=exchange.getRequest().getURI().getPath();
        for (String url:ignoreUrlsConfig.getUrls()){
            if(pathMatcher.match(url,path)){
                flag=true;
                break;
            }
        }
        //白名单放行
        if(flag){
           return chain.filter(exchange);
        }
        //获取access_token
        String access_token=exchange.getRequest().getQueryParams().getFirst("access_token");
        log.info("filters start access_token:{}",access_token);
        //判断token是否为空
        if(StringUtils.isBlank(access_token)){
            return handleException.writeError(exchange,"请登录");
        }
        //校验token是否有效
        String checkTokenUrl="http://ms-oauth2-server/oauth/check_token?token=".concat(access_token);
        //发送远程请求验证token
        try {
            ResponseEntity<String> entity= restTemplate.getForEntity(checkTokenUrl,String.class);
            //log.info("check token");
            //token 无效的业务逻辑处理
            if(entity.getStatusCode()!= HttpStatus.OK){
                return handleException.writeError(exchange,"token 是无效的".concat(access_token));
            }
            if(StringUtils.isBlank(entity.getBody())){
                return handleException.writeError(exchange,"token is invalid".concat(access_token));
            }
        }catch (Exception e){
            return handleException.writeError(exchange,"token 是无效的".concat(access_token));
        }
        //放行
        return chain.filter(exchange);
    }

    /**
     * 网关过滤器排序，数字越小优先级越高
     * */
    @Override
    public int getOrder() {
        return 0;
    }


}
