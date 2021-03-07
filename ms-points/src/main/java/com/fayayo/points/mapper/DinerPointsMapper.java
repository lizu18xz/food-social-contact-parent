package com.fayayo.points.mapper;

import com.fayayo.commons.model.pojo.DinerPoints;
import com.fayayo.commons.vo.DinerPointsRankVO;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 积分 Mapper
 *
 * mysql v8才能执行
 */
public interface DinerPointsMapper {

    // 添加积分
    @Insert("insert into t_diner_points (fk_diner_id, points, types, is_valid, create_date, update_date) " +
            " values (#{fkDinerId}, #{points}, #{types}, 1, now(), now())")
    void save(DinerPoints dinerPoints);


    /**
     * select
     * t1.fk_diner_id,sum(t1.points)total,
     * t2.nickname,
     * t2.avatar_url,
     * rank() over (order by sum(t1.points) desc) ranks,
     * from t_diners_points t1
     * left join t_diners t2 on t1.fk_diner_id=t2.id
     * where t1.is_valid=1 and t2.is_valid=1
     * group by t1.fk_diner_id
     * order by total desc limit 20;
     *
     * */
    // 查询积分排行榜 TOPN
    @Select("SELECT t1.fk_diner_id AS id, " +
            " sum( t1.points ) AS total, " +
            " rank () over ( ORDER BY sum( t1.points ) DESC ) AS ranks," +
            " t2.nickname, t2.avatar_url " +
            " FROM t_diner_points t1 LEFT JOIN t_diners t2 ON t1.fk_diner_id = t2.id " +
            " WHERE t1.is_valid = 1 AND t2.is_valid = 1 " +
            " GROUP BY t1.fk_diner_id " +
            " ORDER BY total DESC LIMIT #{top}")
    List<DinerPointsRankVO> findTopN(@Param("top") int top);

    // 根据食客 ID 查询当前食客的积分排名
    @Select("SELECT id, total, ranks, nickname, avatar_url FROM (" +
            " SELECT t1.fk_diner_id AS id, " +
            " sum( t1.points ) AS total, " +
            " rank () over ( ORDER BY sum( t1.points ) DESC ) AS ranks," +
            " t2.nickname, t2.avatar_url " +
            " FROM t_diner_points t1 LEFT JOIN t_diners t2 ON t1.fk_diner_id = t2.id " +
            " WHERE t1.is_valid = 1 AND t2.is_valid = 1 " +
            " GROUP BY t1.fk_diner_id " +
            " ORDER BY total DESC ) r " +
            " WHERE id = #{dinerId}")
    DinerPointsRankVO findDinerRank(@Param("dinerId") int dinerId);



}