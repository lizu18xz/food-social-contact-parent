package com.fayayo.oauth2.server.service;

import com.fayayo.commons.model.domain.SignInIdentity;
import com.fayayo.commons.model.pojo.Diners;
import com.fayayo.commons.utils.AssertUtil;
import com.fayayo.oauth2.server.mapper.DinersMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author dalizu on 2021/1/23.
 * @version v1.0
 * @desc
 */
@Slf4j
@Service
public class UserService implements UserDetailsService {

    @Resource
    DinersMapper dinersMapper;

    /**
     * 这里我们自己获取密码，交给security去校验。
     * */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        log.info("loadUserByUsername start");
        AssertUtil.isNotEmpty(username,"请输入用户名");
        Diners diners= dinersMapper.selectByAccountInfo(username);
        if(diners==null){
            throw new UsernameNotFoundException("用户名或密码错误请重新输入");
        }

        //初始化登录认证对象
        SignInIdentity signInIdentity=new SignInIdentity();
        BeanUtils.copyProperties(diners,signInIdentity);
        return signInIdentity;
       /* return new User(username,diners.getPassword(),
                AuthorityUtils.commaSeparatedStringToAuthorityList(diners.getRoles()));*/

    }

}
