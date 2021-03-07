package com.fayayo.restaurants.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.fayayo.commons.constant.ApiConstant;
import com.fayayo.commons.constant.RedisKeyConstant;
import com.fayayo.commons.exception.ParameterException;
import com.fayayo.commons.model.domain.ResultInfo;
import com.fayayo.commons.model.pojo.Restaurant;
import com.fayayo.commons.model.pojo.Reviews;
import com.fayayo.commons.utils.AssertUtil;
import com.fayayo.commons.vo.ReviewsVO;
import com.fayayo.commons.vo.ShortDinerInfo;
import com.fayayo.commons.vo.SignInDinerInfo;
import com.fayayo.restaurants.mapper.ReviewsMapper;
import com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReviewsService {

    @Value("${service.name.ms-oauth-server}")
    private String oauthServerName;
    @Value("${service.name.ms-diners-server}")
    private String dinersServerName;
    @Resource
    private RestaurantService restaurantService;
    @Resource
    private ReviewsMapper reviewsMapper;
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private RestTemplate restTemplate;
    private static final int NINE = 9;

    /**
     * 添加餐厅评论
     *
     * @param restaurantId 餐厅 ID
     * @param accessToken  登录用户 Token
     * @param content      评论内容
     * @param likeIt       是否喜欢
     */
    public void addReview(Integer restaurantId, String accessToken,
                          String content, int likeIt) {
        // 参数校验
        AssertUtil.isTrue(restaurantId == null || restaurantId < 1,
                "请选择要评论的餐厅");
        AssertUtil.isNotEmpty(content, "请输入评论内容");
        AssertUtil.isTrue(content.length() > 800, "评论内容过长，请重新输入");
        // 判断餐厅是否存在
        Restaurant restaurant = restaurantService.findById(restaurantId);
        AssertUtil.isTrue(restaurant == null, "该餐厅不存在");
        // 获取登录用户信息
        SignInDinerInfo signInDinerInfo = loadSignInDinerInfo(accessToken);
        // 插入数据库
        Reviews reviews = new Reviews();
        reviews.setContent(content);
        reviews.setFkRestaurantId(restaurantId);
        reviews.setFkDinerId(signInDinerInfo.getId());
        // 这里需要后台操作处理餐厅数据(喜欢/不喜欢餐厅)做自增处理
        reviews.setLikeIt(likeIt);
        int count = reviewsMapper.saveReviews(reviews);
        if (count == 0) {
            return;
        }
        // 写入餐厅最新评论至 Redis
        String key = RedisKeyConstant.restaurant_new_reviews.getKey() + restaurantId;
        redisTemplate.opsForList().leftPush(key, reviews);
    }

    /**
     * 获取餐厅最新评论
     *
     * @param restaurantId
     * @param accessToken
     * @return
     */
    public List<ReviewsVO> findNewReviews(Integer restaurantId, String accessToken) {
        // 参数校验
        AssertUtil.isTrue(restaurantId == null || restaurantId < 1,
                "请选择要查看的餐厅");
        // 获取 Key
        String key = RedisKeyConstant.restaurant_new_reviews.getKey() + restaurantId;
        // 从 Redis 取十条最新评论
        List<LinkedHashMap> reviews = redisTemplate.opsForList().range(key, 0, NINE);
        // 初始化 VO 集合
        List<ReviewsVO> reviewsVOS = Lists.newArrayList();
        // 初始化用户 ID 集合
        List<Integer> dinerIds = Lists.newArrayList();
        // 循环处理评论集合
        reviews.forEach(review -> {
            ReviewsVO reviewsVO = BeanUtil.fillBeanWithMap(review, new ReviewsVO(), true);
            reviewsVOS.add(reviewsVO);
            dinerIds.add(reviewsVO.getFkDinerId());
        });
        // 获取评论用户信息
        ResultInfo resultInfo = restTemplate.getForObject(dinersServerName +
                        "findByIds?access_token=${accessToken}&ids={ids}",
                ResultInfo.class, accessToken, StrUtil.join(",", dinerIds));
        if (resultInfo.getCode() != ApiConstant.SUCCESS_CODE) {
            throw new ParameterException(resultInfo.getCode(), resultInfo.getMessage());
        }
        List<LinkedHashMap> dinerInfoMaps = (List<LinkedHashMap>) resultInfo.getData();
        Map<Integer, ShortDinerInfo> dinerInfos = dinerInfoMaps.stream()
                .collect(Collectors.toMap(
                        // key
                        diner -> (int) diner.get("id"),
                        // value
                        diner -> BeanUtil.fillBeanWithMap(diner, new ShortDinerInfo(), true))
                );
        // 完善头像昵称信息
        reviewsVOS.forEach(review -> {
            ShortDinerInfo dinerInfo = dinerInfos.get(review.getFkDinerId());
            if (dinerInfo != null) {
                review.setDinerInfo(dinerInfo);
            }
        });
        // redis list 中只保留最新十条  ltrim
        return reviewsVOS;
    }

    /**
     * 获取登录用户信息
     *
     * @param accessToken
     * @return
     */
    private SignInDinerInfo loadSignInDinerInfo(String accessToken) {
        // 必须登录
        AssertUtil.mustLogin(accessToken);
        String url = oauthServerName + "user/me?access_token={accessToken}";
        ResultInfo resultInfo = restTemplate.getForObject(url, ResultInfo.class, accessToken);
        if (resultInfo.getCode() != ApiConstant.SUCCESS_CODE) {
            throw new ParameterException(resultInfo.getCode(), resultInfo.getMessage());
        }
        SignInDinerInfo dinerInfo = BeanUtil.fillBeanWithMap((LinkedHashMap) resultInfo.getData(),
                new SignInDinerInfo(), false);
        if (dinerInfo == null) {
            throw new ParameterException(ApiConstant.NO_LOGIN_CODE, ApiConstant.NO_LOGIN_MESSAGE);
        }
        return dinerInfo;
    }

}
