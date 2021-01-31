package com.fayayo.oauth2.server.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author dalizu on 2021/1/23.
 * @version v1.0
 * @desc
 */
@Component
@ConfigurationProperties(prefix = "client.oauth2")
@Data
public class ClientOAuth2DataConfiguration {

    private String clientId;

    private String secret;

    private String[] grant_types;

    private int tokenValidityTime;

    private int refreshTokenValidityTime;

    private String []scopes;

}
