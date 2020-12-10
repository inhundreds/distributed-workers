package com.test;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;

@Configuration
class AppConfiguration {

    @Bean
    HttpClient httpClient() {
        return HttpClient.newHttpClient();
    }

}