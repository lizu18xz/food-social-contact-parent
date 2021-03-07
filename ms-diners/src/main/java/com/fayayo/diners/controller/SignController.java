package com.fayayo.diners.controller;

import com.fayayo.commons.model.domain.ResultInfo;
import com.fayayo.commons.utils.ResultInfoUtil;
import com.fayayo.diners.service.SignService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @author dalizu on 2021/3/6.
 * @version v1.0
 * @desc
 */
@RestController
@RequestMapping("sing")
public class SignController {

    @Resource
    private SignService signService;

    @Resource
    private HttpServletRequest request;

    @GetMapping
    public ResultInfo getSignInfo(String access_token, @RequestParam(required = false)String date){
        Map<String,Boolean> signInfo=signService.getSignInfo(access_token,date);
        return ResultInfoUtil.buildSuccess(request.getServletPath(),signInfo);
    }

    @GetMapping("count")
    public ResultInfo getSignCount(String access_token, @RequestParam(required = false)String date){
        long count=signService.getSignCount(access_token,date);
        return ResultInfoUtil.buildSuccess(request.getServletPath(),count);
    }


    /**
     *
     * 签到并且可以不签
     * */
    @PostMapping
    public ResultInfo sign(String access_token, @RequestParam(required = false)String date){

        int count=signService.doSign(access_token,date);

        return ResultInfoUtil.buildSuccess(request.getServletPath(),count);
    }


}
