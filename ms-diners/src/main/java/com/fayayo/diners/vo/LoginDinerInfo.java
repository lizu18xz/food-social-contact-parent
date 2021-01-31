package com.fayayo.diners.vo;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author dalizu on 2021/1/26.
 * @version v1.0
 * @desc
 */
@Getter
@Setter
public class LoginDinerInfo implements Serializable {

    private String nickname;

    private String avatarUrl;

    private String token;

}
