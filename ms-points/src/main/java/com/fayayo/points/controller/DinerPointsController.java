package com.fayayo.points.controller;

import com.fayayo.commons.model.domain.ResultInfo;
import com.fayayo.commons.utils.ResultInfoUtil;
import com.fayayo.commons.vo.DinerPointsRankVO;
import com.fayayo.points.service.DinerPointsService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author dalizu on 2021/3/6.
 * @version v1.0
 * @desc
 */
@RestController
public class DinerPointsController {

    @Resource
    private DinerPointsService dinerPointsService;

    @Resource
    private HttpServletRequest request;

    /**
     * 从redis获取积分排名信息
     *
     */
    @PostMapping
    public ResultInfo<List<DinerPointsRankVO>> findDinerPointRankFromRedis(String access_token) {
        List<DinerPointsRankVO> rankVOS= dinerPointsService.findDinerPointRankFromRedis(access_token);
        return ResultInfoUtil.buildSuccess(request.getServletPath(), rankVOS);
    }


    /**
     * 添加积分
     *
     * @param dinerId 食客ID
     * @param points  积分
     * @param types   类型 0=签到，1=关注好友，2=添加Feed，3=添加商户评论
     * @return
     */
    @PostMapping
    public ResultInfo<Integer> addPoints(@RequestParam(required = false) Integer dinerId,
                                         @RequestParam(required = false) Integer points,
                                         @RequestParam(required = false) Integer types) {
        dinerPointsService.addPoints(dinerId, points, types);
        return ResultInfoUtil.buildSuccess(request.getServletPath(), points);
    }

}
