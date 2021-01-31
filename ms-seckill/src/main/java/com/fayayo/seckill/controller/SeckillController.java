package com.fayayo.seckill.controller;

import com.fayayo.commons.model.domain.ResultInfo;
import com.fayayo.commons.model.pojo.SeckillVouchers;
import com.fayayo.commons.utils.ResultInfoUtil;
import com.fayayo.seckill.service.SeckillService;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author dalizu on 2021/1/28.
 * @version v1.0
 * @desc
 */
@RestController
public class SeckillController {

    @Resource
    private SeckillService seckillService;

    @Resource
    private HttpServletRequest request;

    /**
     * 秒杀下单
     * */
    @PostMapping("{voucherId}")
    public ResultInfo<String>doSeckill(@PathVariable Integer voucherId,String access_token){

        return seckillService.doSeckill(voucherId,access_token,request.getServletPath());
    }


    @PostMapping("add")
    public ResultInfo addSeckillVouchers(@RequestBody SeckillVouchers seckillVouchers){
        seckillService.addSeckillVouchers(seckillVouchers);
        return ResultInfoUtil.buildSuccess(request.getServletPath(),"添加成功");
    }


}
