package com.fayayo.diners.controller;

import com.fayayo.commons.model.domain.ResultInfo;
import com.fayayo.commons.utils.ResultInfoUtil;
import com.fayayo.diners.service.SendVerifyCodeService;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author dalizu on 2021/1/28.
 * @version v1.0
 * @desc
 */
@RestController
public class SendVerifyCodeController {

    @Resource
    private SendVerifyCodeService sendVerifyCodeService;

    @Resource
    private HttpServletRequest request;

    public ResultInfo send(String phone){
        sendVerifyCodeService.send(phone);
        return ResultInfoUtil.buildSuccess("发送成功",request.getServletPath());
    }

}
