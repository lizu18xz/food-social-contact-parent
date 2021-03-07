package com.fayayo.commons.model.pojo;

import com.fayayo.commons.model.base.BaseModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author dalizu on 2021/3/6.
 * @version v1.0
 * @desc
 */
@Getter
@Setter
public class DinerPoints extends BaseModel {

    @ApiModelProperty("关联DinerId")
    private Integer fkDinerId;

    @ApiModelProperty("积分")
    private Integer points;

    @ApiModelProperty(name = "类型",example = "0=签到 1=关注好友")
    private Integer types;

}
