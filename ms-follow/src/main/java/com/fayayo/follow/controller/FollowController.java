package com.fayayo.follow.controller;

import com.fayayo.commons.model.domain.ResultInfo;
import com.fayayo.commons.utils.ResultInfoUtil;
import com.fayayo.follow.service.FollowService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author dalizu on 2021/2/16.
 * @version v1.0
 * @desc
 */
@RestController
public class FollowController {

    @Resource
    private FollowService followService;

    @Resource
    private HttpServletRequest request;


    //获取粉丝列表ids
    @GetMapping("followers/{dinerId}")
    public ResultInfo findFollowers(@PathVariable Integer dinerId){

        return ResultInfoUtil.buildSuccess(request.getServletPath(),
                followService.findFollowers(dinerId));
    }


    //共同关注好友列表
    @GetMapping("commons/{dinerId}")
    public ResultInfo findCommonsFriends(@PathVariable Integer dinerId,String access_token){

        return followService.findCommonsFriends(dinerId,access_token,request.getServletPath());
    }


    @PostMapping("/{followDinerId}")
    public ResultInfo follow(@PathVariable Integer followDinerId,
                             @RequestParam int isFollowed,String access_token){

        ResultInfo resultInfo=followService.follow(followDinerId,isFollowed,
                access_token,request.getServletPath());
        return resultInfo;
    }



}
