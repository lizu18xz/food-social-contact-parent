package com.fayayo.commons.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author dalizu on 2021/2/16.
 * @version v1.0
 * @desc
 */
@Getter
@Setter
@ApiModel(value = "ShortDinerInfo",description = "关注食客信息")
public class ShortDinerInfo implements Serializable {

    @ApiModelProperty("主键")
    public Integer id;

    @ApiModelProperty("昵称")
    private String nickName;

    @ApiModelProperty("头像")
    private String avatarUrl;
}
