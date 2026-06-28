package com.pedritopos.shared.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@ConfigurationProperties(prefix = "cloudflare.r2")
@Getter
@Setter
public class R2Properties {

    private String accountId;
    private String accessKey;
    private String secretKey;
    private String bucket;
    private String publicUrl;
}
