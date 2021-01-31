package com.fayayo.diners.mapper;

import com.fayayo.commons.model.dto.DinersDTO;
import com.fayayo.commons.model.pojo.Diners;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * @author dalizu on 2021/1/28.
 * @version v1.0
 * @desc
 */
public interface DinersMapper {


    @Select("select id,username,phone,email,is_valid "+
    " from t_diners where phone= #{phone}")
    Diners selectByPhone(@Param("phone")String phone);


    // 根据用户名查询食客信息
    @Select("select id, username, phone, email, is_valid " +
            " from t_diners where username = #{username}")
    Diners selectByUsername(@Param("username") String username);

    // 新增食客信息
    @Insert("insert into " +
            " t_diners (username, password, phone, roles, is_valid, create_date, update_date) " +
            " values (#{username}, #{password}, #{phone}, \"ROLE_USER\", 1, now(), now())")
    int save(DinersDTO dinersDTO);

}
