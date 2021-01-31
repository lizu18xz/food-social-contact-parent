package com.fayayo.diners.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author dalizu on 2021/1/26.
 * @version v1.0
 * @desc
 */
@Component
@ConfigurationProperties(prefix = "oauth2.client")
@Getter
@Setter
public class OAuth2ClientConfiguration {

    private String clientId;

    private String secret;

    private String grant_type;

    private String scope;

}
