package com.it.orderservis.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class RestClientConfig {

    /*
     * Builder normale.
     * Viene lasciato disponibile a Spring/Eureka
     * per chiamate dirette come localhost:8761.
     */
    @Bean
    @Primary
    public RestClient.Builder restClientBuilder() {

        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(Duration.ofSeconds(2));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        return RestClient.builder()
                .requestFactory(requestFactory);
    }


    /*
     * Builder usato dai nostri client applicativi.
     * Risolve user-servis, product-servis,
     * payment-service, notification-service tramite Eureka.
     */
    @Bean
    @LoadBalanced
    public RestClient.Builder loadBalancedRestClientBuilder() {

        SimpleClientHttpRequestFactory requestFactory =
                new SimpleClientHttpRequestFactory();

        requestFactory.setConnectTimeout(Duration.ofSeconds(2));
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        return RestClient.builder()
                .requestFactory(requestFactory);
    }


    /*
     * RestClient effettivamente iniettato nei nostri
     * UserClient, ProductClient, PaymentClient e NotificationClient.
     */
    @Bean
    public RestClient restClient(
            @Qualifier("loadBalancedRestClientBuilder")
            RestClient.Builder builder
    ) {
        return builder.build();
    }
}