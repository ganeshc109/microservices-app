package com.example.demo.config;

import com.example.demo.util.RsaKeyUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.security.PrivateKey;
import java.security.PublicKey;

@Configuration
public class JwtConfig {

    @Bean
    public PrivateKey privateKey() throws Exception {
        return RsaKeyUtil.getPrivateKey("src/main/resources/keys/private_key.pem");
    }

    @Bean
    public PublicKey publicKey() throws Exception {
        return RsaKeyUtil.getPublicKey("src/main/resources/keys/public_key.pem");
    }
}