package com.fayayo.diners.domain;

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
public class OAuthDinerInfo implements Serializable {

    private String nickname;

    private String avatarUrl;

    private String accessToken;

    private String expireIn;

    private List<String> scopes;

    private String refreshToken;

}
