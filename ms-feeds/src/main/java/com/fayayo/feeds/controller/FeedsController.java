package com.fayayo.feeds.controller;

import com.fayayo.commons.model.domain.ResultInfo;
import com.fayayo.commons.model.pojo.Feeds;
import com.fayayo.commons.utils.ResultInfoUtil;
import com.fayayo.feeds.service.FeedsService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author dalizu on 2021/2/16.
 * @version v1.0
 * @desc
 */
@RestController
public class FeedsController {

    @Resource
    private FeedsService feedsService;

    @Resource
    private HttpServletRequest request;

    //查询分页获取关注的feed数据
    @GetMapping("{page}")
    public ResultInfo selectForPage(@PathVariable Integer page,String access_token){
       return ResultInfoUtil.buildSuccess(request.getServletPath(),
               feedsService.selectForPage(page,access_token));
    }


    //变更feed
    @PostMapping("updateFollowingFeeds/{followingDinerId}")
    public ResultInfo<String> addFollowingFeed(@PathVariable Integer followingDinerId, String access_token,
                                               @RequestParam int type){
        feedsService.addFollowingFeed(followingDinerId,access_token,type);
        return ResultInfoUtil.buildSuccess(request.getServletPath(),"变更成功");
    }


    @DeleteMapping("{id}")
    public ResultInfo<String> delete(@PathVariable Integer id, String access_token){
        feedsService.delete(id,access_token);
        return ResultInfoUtil.buildSuccess(request.getServletPath(),"删除成功");
    }


    @PostMapping
    public ResultInfo<String> create(@RequestBody Feeds feeds, String access_token){
        feedsService.create(feeds,access_token);

        return ResultInfoUtil.buildSuccess(request.getServletPath(),"添加成功");
    }

}
