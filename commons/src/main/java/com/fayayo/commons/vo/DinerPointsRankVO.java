package com.fayayo.commons.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * @author dalizu on 2021/3/6.
 * @version v1.0
 * @desc
 */
@ApiModel(description = "用户积分排行榜")
@Getter
@Setter
public class DinerPointsRankVO extends ShortDinerInfo{

    @ApiModelProperty("总积分")
    private int total;

    @ApiModelProperty("排名")
    private int ranks;

    @ApiModelProperty(value = "是否是自己",example = "0=否,1=是")
    private int isMe;
}
