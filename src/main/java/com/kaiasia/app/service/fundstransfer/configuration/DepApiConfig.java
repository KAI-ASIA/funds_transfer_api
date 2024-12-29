package com.kaiasia.app.service.fundstransfer.configuration;

import lombok.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "dep-api")
@Data
public class DepApiConfig {
    @Autowired
    private Environment env;
    private DepApiProperties authApi;
    private DepApiProperties t24utilsApi;
    private DepApiProperties napasApi;
    public DepApiProperties getApiProperties(String name) {
        String prefix = "dep-api." + name;
        return DepApiProperties
                .builder()
                .url(env.getProperty(prefix + ".url"))
                .apiKey(env.getProperty(prefix + ".apiKey"))
                .apiName(env.getProperty(prefix + ".apiName"))
                .timeout(Long.parseLong(StringUtils.defaultString(env.getProperty(prefix + ".timeout"))))
                .build();
    }


}
