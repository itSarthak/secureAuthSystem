package com.pipeline.backend.configuration;

import com.pipeline.backend.utils.AesCryptoUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Base64;

@Configuration
public class CryptoConfiguration {

    @Bean
    public AesCryptoUtil aesCryptoUtil(
        @Value("${security.aes.secret-key}") String secretKey
    ) {
        byte[] key = Base64.getDecoder().decode(secretKey);
        return new AesCryptoUtil(key);
    }
}
