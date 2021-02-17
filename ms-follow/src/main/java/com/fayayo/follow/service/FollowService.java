package com.fayayo.follow.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.fayayo.commons.constant.ApiConstant;
import com.fayayo.commons.constant.RedisKeyConstant;
import com.fayayo.commons.exception.ParameterException;
import com.fayayo.commons.model.domain.ResultInfo;
import com.fayayo.commons.model.pojo.Follow;
import com.fayayo.commons.utils.AssertUtil;
import com.fayayo.commons.utils.ResultInfoUtil;
import com.fayayo.commons.vo.ShortDinerInfo;
import com.fayayo.commons.vo.SignInDinerInfo;
import com.fayayo.follow.mapper.FollowMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author dalizu on 2021/2/16.
 * @version v1.0
 * @desc 关注取关的业务逻辑
 */
@Service
public class FollowService {

    @Value("${service.name.ms-oauth-server}")
    private String oauthServerName;

    @Value("${service.name.ms-diners-server}")
    private String dinersServerName;

    @Value("${service.name.ms-feeds-server}")
    private String feedsServerName;


    @Resource
    private RestTemplate restTemplate;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private FollowMapper followMapper;

    //获取粉丝列表
    public Set<Integer>findFollowers(Integer dinerId){
        AssertUtil.isNotNull(dinerId,"请选择要查看的用户");
        Set<Integer>followers=redisTemplate.opsForSet().members(RedisKeyConstant.followers.getKey()+dinerId);
        return followers;
    }


    //共同关注列表
    public ResultInfo findCommonsFriends(Integer dinerId,String accessToken,
                                         String path){

        //是否选择了关注对象，和谁一起看共同好友
        AssertUtil.isTrue(dinerId==null&& dinerId<1,"请选择要查看的人");

        SignInDinerInfo dinerInfo=loadSignInDinerInfo(accessToken);

        //获取登录用户关注的信息
        String loginDinerKey=RedisKeyConstant.following.getKey()+dinerInfo.getId();

        //获取登录用户查看对象的关注集合
        String dinerKey=RedisKeyConstant.following.getKey()+dinerId;

        //计算交集
        Set<Integer> dinerIds=redisTemplate.opsForSet().intersect(loginDinerKey,dinerKey);

        //没有共同好友的情况
        if(dinerIds == null || dinerIds.isEmpty()){
            return ResultInfoUtil.buildSuccess(path,new ArrayList<ShortDinerInfo>());
        }
        //调用食客

        ResultInfo resultInfo=restTemplate.getForObject(dinersServerName+"findByIds?access_token={accessToken}&ids={ids}",
                ResultInfo.class,accessToken, StrUtil.join(",",dinerIds));

        if(resultInfo.getCode()!=ApiConstant.SUCCESS_CODE){
            resultInfo.setPath(path);
            return resultInfo;
        }

        //处理结果集合
        List<LinkedHashMap>dinnerInfoMaps=(ArrayList)resultInfo.getData();
        List<ShortDinerInfo>dinerInfos=dinnerInfoMaps.stream().map(diner-> {
            return  BeanUtil.fillBeanWithMap(diner,new ShortDinerInfo(),false);
        }).collect(Collectors.toList());

        return ResultInfoUtil.buildSuccess(path,dinerInfos);
    }


    /**
     *关注的食客ID
     * 是否关注 1:关注 0:取关
     * */
    public ResultInfo follow(Integer followDinerId,int isFollowed,
                             String accessToken,String path){

        //是否选择了关注对象
        AssertUtil.isTrue(followDinerId == null || followDinerId<1,
                "请选中要关注的人");
        //获取登录信息(封装方法)
        SignInDinerInfo dinerInfo = loadSignInDinerInfo(accessToken);

        //获取当前用户与需要关注用户的一个关注信息
        Follow follow=followMapper.selectFollow(dinerInfo.getId(),followDinerId);

        //如果没有关注信息，并且进行关注操作 --添加关注
        if(follow == null && isFollowed==1){
            int count=followMapper.save(dinerInfo.getId(),followDinerId);
            if(count==1){
                addToRedisSet(dinerInfo.getId(),followDinerId);
                //保存Feed
                sendSaveOrRemoveFeed(followDinerId,accessToken,1);
            }
            return ResultInfoUtil.build(ApiConstant.SUCCESS_CODE,"关注成功",path,"关注成功");
        }
        //如果存在关注信息，并且进行取关操作 --取关
        if(follow!=null && follow.getIsValid() ==1 && isFollowed==0){
            //取消关注
            int count=followMapper.update(follow.getId(),isFollowed);
            if(count==1){
                removeFromRedisSet(dinerInfo.getId(),followDinerId);
                //移除Feeds
                sendSaveOrRemoveFeed(followDinerId,accessToken,0);
            }
            return ResultInfoUtil.build(ApiConstant.SUCCESS_CODE,"取关成功",path,"取关成功");
        }

        //如果存在关注信息,目前为取关状态，且需要关注--重新关注
        if(follow!=null && follow.getIsValid()==0 && isFollowed==1){
            //重新关注
            int count=followMapper.update(follow.getId(),isFollowed);
            if(count==1){
                addToRedisSet(dinerInfo.getId(),followDinerId);
                //添加Feed
                sendSaveOrRemoveFeed(followDinerId,accessToken,1);
            }
            return ResultInfoUtil.build(ApiConstant.SUCCESS_CODE,"关注成功",path,"关注成功");
        }

        return ResultInfoUtil.buildSuccess(path,"操作成功");
    }

    /**
     * 发送请求添加或者移除关注人的Feed列表
     *
     * @param followDinerId 关注好友的ID
     * @param accessToken   当前登录用户token
     * @param type          0=取关 1=关注
     */
    private void sendSaveOrRemoveFeed(Integer followDinerId, String accessToken, int type) {
        String feedsUpdateUrl = feedsServerName + "updateFollowingFeeds/"
                + followDinerId + "?access_token=" + accessToken;
        // 构建请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        // 构建请求体（请求参数）
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("type", type);
        HttpEntity<MultiValueMap<String, Object>> entity = new HttpEntity<>(body, headers);
        restTemplate.postForEntity(feedsUpdateUrl, entity, ResultInfo.class);
    }



    private void addToRedisSet(Integer dinerId, Integer followDinerId) {

        //关注集合
        redisTemplate.opsForSet().add(RedisKeyConstant.following.getKey()+dinerId,followDinerId);

        //粉丝集合
        redisTemplate.opsForSet().add(RedisKeyConstant.followers.getKey()+followDinerId,dinerId);


    }

    private void removeFromRedisSet(Integer dinerId, Integer followDinerId) {

        //关注集合
        redisTemplate.opsForSet().remove(RedisKeyConstant.following.getKey()+dinerId,followDinerId);

        //粉丝集合
        redisTemplate.opsForSet().remove(RedisKeyConstant.followers.getKey()+followDinerId,dinerId);


    }

    //获取登录用户信息
    private SignInDinerInfo loadSignInDinerInfo(String accessToken) {
        AssertUtil.mustLogin(accessToken);
        String url=oauthServerName+"user/me?access_token={accessToken}";
        ResultInfo resultInfo=restTemplate.getForObject(url,ResultInfo.class,accessToken);
        if(resultInfo.getCode()!= ApiConstant.SUCCESS_CODE){
            throw new ParameterException(resultInfo.getMessage());
        }
        SignInDinerInfo dinerInfo= BeanUtil.fillBeanWithMap((LinkedHashMap)resultInfo.getData(),
                new SignInDinerInfo(),false);
        return dinerInfo;
    }


}
