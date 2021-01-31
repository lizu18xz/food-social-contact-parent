package com.fayayo.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author dalizu on 2021/1/28.
 * @version v1.0
 * @desc
 */
@Data
@Component
@ConfigurationProperties(prefix = "secure.ignore")
public class IgnoreUrlsConfig {

    private List<String>urls;

}
