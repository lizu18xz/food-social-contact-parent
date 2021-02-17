package com.fayayo.feeds.service;

import cn.hutool.core.bean.BeanUtil;
import com.fayayo.commons.constant.ApiConstant;
import com.fayayo.commons.constant.RedisKeyConstant;
import com.fayayo.commons.exception.ParameterException;
import com.fayayo.commons.model.domain.ResultInfo;
import com.fayayo.commons.model.pojo.Feeds;
import com.fayayo.commons.utils.AssertUtil;
import com.fayayo.commons.vo.FeedsVO;
import com.fayayo.commons.vo.ShortDinerInfo;
import com.fayayo.commons.vo.SignInDinerInfo;
import com.fayayo.feeds.mapper.FeedsMapper;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author dalizu on 2021/2/16.
 * @version v1.0
 * @desc
 */
@Service
public class FeedsService {

    @Value("${service.name.ms-oauth-server}")
    private String oauthServerName;

    @Value("${service.name.ms-follow-server}")
    private String followServerName;

    @Value("${service.name.ms-diners-server}")
    private String dinersServerName;

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private RedisTemplate redisTemplate;

    @Resource
    private FeedsMapper feedsMapper;

    /**
     *
     * 查询feeds
     * 根据时间由近至远, 每次查询20条
     * page第几页
     * */
    public List<FeedsVO> selectForPage(Integer page,String accessToken){
        if(page==null){
            page=1;
        }
        //获取登录用户
        SignInDinerInfo dinerInfo=loadSignInDinerInfo(accessToken);
        //我关注的好友的feedKey
        String key=RedisKeyConstant.following_feeds.getKey()+dinerInfo.getId();
        //分页(0-19 是20条)  ZREVRANGE
        long start=(page -1)*ApiConstant.PAGE_SIZE;
        long end=page * ApiConstant.PAGE_SIZE-1;
        Set<Integer> feedIds=redisTemplate.opsForZSet().reverseRange(key,start,end);
        //根据主键查询Feed
        if(feedIds==null||feedIds.isEmpty()){
            return Lists.newArrayList();
        }
        List<Feeds> feeds=feedsMapper.findFeedsByIds(feedIds);
        //首先初始化关注好友的ID集合
        List<Integer>followingDinerIds=new ArrayList<>();
        //添加用户ID至集合
        List<FeedsVO>feedsVOS=feeds.stream().map(feed->{
            FeedsVO feedsVO=new FeedsVO();
            BeanUtil.copyProperties(feed,feedsVO);
            followingDinerIds.add(feed.getFkDinerId());
            return feedsVO;
        }).collect(Collectors.toList());
        //调用远程服务获取feed中用户的信息

        ResultInfo resultInfo=restTemplate.getForObject(dinersServerName+ "findByIds?access_token=${accessToken}&ids={ids}",
                ResultInfo.class,accessToken,followingDinerIds);
        if(resultInfo.getCode()!= ApiConstant.SUCCESS_CODE){
            throw new ParameterException(resultInfo.getMessage());
        }

        List<LinkedHashMap> dinerInfoMaps= (ArrayList)resultInfo.getData();
        //构建一个map,key:用户ID value:ShortDinerInfo
        Map<Integer, ShortDinerInfo> dinerInfoMap=dinerInfoMaps.stream()
                .collect(Collectors.toMap(
                        diner->(Integer)diner.get("id"),
                        diner->BeanUtil.fillBeanWithMap(diner,new ShortDinerInfo(),true)
                ));
        //返回VO
        feedsVOS.forEach(feedVO->{
            feedVO.setDinerInfo(dinerInfoMap.get(feedVO.getFkDinerId()));
        });
        return feedsVOS;
    }



    /**
     * 变更feed (提供给follow服务调用)
     * 关注和取关后还需要更新其feeds的信息
     * 关注好友的id
     *
     * type 1关注  0取关
     * */
    @Transactional(rollbackFor = Exception.class)
    public void addFollowingFeed(Integer followingDinerId,String accessToken,int type){

        //请选择关注的好友
        AssertUtil.isTrue(followingDinerId==null || followingDinerId<1,"请选择关注的好友");
        //获取登录信息
        SignInDinerInfo dinerInfo=loadSignInDinerInfo(accessToken);
        //获取关注或者取关的食客的所有feed
        List<Feeds>feedsList=feedsMapper.findByDinerId(followingDinerId);
        //根据type操作
        String key=RedisKeyConstant.following_feeds.getKey()+dinerInfo.getId();
        if(type==0){
            //取关 删除取关人所有的feed信息
            List<Integer>feedIds=feedsList.stream().map(feed -> {
                return feed.getId();
            }).collect(Collectors.toList());
            redisTemplate.opsForZSet().remove(key,feedIds.toArray(new Integer[]{}));

        }else{
            //关注
            Set<ZSetOperations.TypedTuple>typedTuples=feedsList.stream().map(feed -> {

                //key  值,score(最后修改的时间)
                return new DefaultTypedTuple<>(feed.getId(),(double) feed.getUpdateDate().getTime());

            }).collect(Collectors.toSet());
            redisTemplate.opsForZSet().add(key,typedTuples);
        }

    }


    //删除feed
    @Transactional(rollbackFor = Exception.class)
    public void delete(Integer id,String accessToken){

        //请选择需要删除的feed
        AssertUtil.isTrue(id==null||id<1,"请选择要删除的feeds");
        //获取登录的用户
        SignInDinerInfo dinerInfo=loadSignInDinerInfo(accessToken);
        //获取feed的内容
        Feeds feeds=feedsMapper.findById(id);
        //判断是否已经删除了,是否是自己发布的feed
        AssertUtil.isTrue(feeds==null,"该feed已经被删除");
        AssertUtil.isTrue(!feeds.getFkDinerId().equals(dinerInfo.getId()),"只能删除自己发布的Feeds");
        //删除逻辑删除
        int count=feedsMapper.delete(id);
        if(count==0){
            return;
        }
        //将内容从粉丝的集合中删除---异步消息队列优化
        //先获取我的粉丝
        List<Integer> followers=findFollowers(dinerInfo.getId());
        //移除
        followers.forEach(follower->{
            String key=RedisKeyConstant.following_feeds.getKey()+follower;
            redisTemplate.opsForZSet().remove(key,feeds.getId());
        });

    }



    //添加feeds
    @Transactional(rollbackFor = Exception.class)
    public void create(Feeds feeds,String accessToken){
        //非空校验
        AssertUtil.isNotEmpty(feeds.getContent(),"请输入内容");
        AssertUtil.isTrue(feeds.getContent().length()>255,"输入内容过长");
        //获取当前登录用户
        SignInDinerInfo dinerInfo=loadSignInDinerInfo(accessToken);
        //feed关联用户信息
        feeds.setFkDinerId(dinerInfo.getId());
        //添加feed
        int count=feedsMapper.save(feeds);
        AssertUtil.isTrue(count ==0,"添加失败");
        //推送到粉丝的列表中  --TODO 后续这里应该改造采用异步消息队列解决性能问题
        //先获取当前登录用户的粉丝的id集合
        List<Integer> followers=findFollowers(dinerInfo.getId());
        //推送 feed
        long now=System.currentTimeMillis();
        followers.forEach(follower->{
            String key=RedisKeyConstant.following_feeds.getKey()+follower;
            redisTemplate.opsForZSet().add(key,feeds.getId(),now);
        });

    }

    private List<Integer> findFollowers(Integer dinerId) {

        String url=followServerName+"followers/"+dinerId;

        ResultInfo resultInfo=restTemplate.getForObject(url,ResultInfo.class);
        if(resultInfo.getCode()!= ApiConstant.SUCCESS_CODE){
            throw new ParameterException(resultInfo.getMessage());
        }
        List<Integer>followers=(List<Integer>)resultInfo.getData();
        return followers;
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
