package com.fayayo.commons.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author dalizu on 2021/1/28.
 * @version v1.0
 * @desc
 */
@Getter
@Setter
@ApiModel(description = "注册用户信息")
public class DinersDTO implements Serializable {

    @ApiModelProperty("用户名")
    private String username;

    @ApiModelProperty("密码")
    private String password;

    @ApiModelProperty("手机号")
    private String phone;

    @ApiModelProperty("验证码")
    private String verifyCode;


}
