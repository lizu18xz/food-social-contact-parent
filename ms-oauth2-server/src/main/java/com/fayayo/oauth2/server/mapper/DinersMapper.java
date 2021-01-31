package com.fayayo.oauth2.server.mapper;

import com.fayayo.commons.model.pojo.Diners;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author dalizu on 2021/1/23.
 * @version v1.0
 * @desc
 */
public interface DinersMapper {


    // 根据用户名 or 手机号 or 邮箱查询用户信息
    @Select("select id, username, nickname, phone, email, " +
            "password, avatar_url, roles, is_valid from t_diners where " +
            "(username = #{account} or phone = #{account} or email = #{account})")
    Diners selectByAccountInfo(@Param("account") String account);

}
