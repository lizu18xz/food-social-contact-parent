package com.fayayo.diners.controller;

import com.fayayo.commons.model.domain.ResultInfo;
import com.fayayo.commons.model.dto.DinersDTO;
import com.fayayo.commons.utils.ResultInfoUtil;
import com.fayayo.commons.vo.ShortDinerInfo;
import com.fayayo.diners.service.DinersService;
import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author dalizu on 2021/1/26.
 * @version v1.0
 * @desc
 */
@RestController
@Api("diners相关的接口")
public class DinersController {


    @Resource
    private DinersService dinersService;

    @Resource
    private HttpServletRequest request;

    @GetMapping("findByIds")
    public ResultInfo<List<ShortDinerInfo>> findByIds(String ids){

        List<ShortDinerInfo>dinerInfos= dinersService.findByIds(ids);
        return ResultInfoUtil.buildSuccess(request.getServletPath(),dinerInfos);
    }


    @PostMapping("register")
    public ResultInfo register(@RequestBody DinersDTO dinersDTO){

        return dinersService.register(dinersDTO,request.getServletPath());
    }


    @GetMapping("checkPhone")
    public ResultInfo checkPhone(String phone){

        dinersService.checkPhoneIsRegistered(phone);
        return ResultInfoUtil.buildSuccess(request.getServletPath());
    }


    @GetMapping("signin")
    public ResultInfo singIn(String account, String password){

       return dinersService.signIn(account,password,request.getServletPath());
    }


}
