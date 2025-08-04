package com.altradar.config;

import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;

import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.NoopHostnameVerifier;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.ssl.SSLContextBuilder;
import org.apache.hc.core5.util.TimeValue;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    // ✅ live 프로파일 전용 secure RestTemplate
    @Bean
    @Profile("live")
    public RestTemplate secureRestTemplate() {
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setDefaultMaxPerRoute(400);
        connectionManager.setMaxTotal(1000);
        connectionManager.setValidateAfterInactivity(TimeValue.ofMinutes(1));

        CloseableHttpClient httpClient = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .evictIdleConnections(TimeValue.ofMinutes(1))
                .build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
        return new RestTemplate(requestFactory);
    }

    @Bean
    @Profile("!live")
    public RestTemplate insecureRestTemplate() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException { 
    	// Create an SSL context that bypasses certificate validation

    	        RestTemplateBuilder builder = new RestTemplateBuilder();

    	        // Create an SSLContext that bypasses SSL validation
    	        SSLContext sslContext = SSLContextBuilder.create()
    	                .loadTrustMaterial((X509Certificate[] chain, String authType) -> true) // Trust all certificates
    	                .build();

    	        // Create SSLConnectionSocketFactory with the SSLContext and NoopHostnameVerifier
    	        SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(
    	                sslContext, NoopHostnameVerifier.INSTANCE);

    	        // Create a registry of custom connection socket factories for both HTTP and HTTPS
    	        Registry<ConnectionSocketFactory> socketFactoryRegistry = RegistryBuilder.<ConnectionSocketFactory>create()
    	                .register("http", PlainConnectionSocketFactory.INSTANCE)
    	                .register("https", sslSocketFactory)
    	                .build();

    	        // Create a connection manager using the custom registry
    	        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(socketFactoryRegistry);
    	        connectionManager.setMaxTotal(100);  // Set max total connections
    	        connectionManager.setDefaultMaxPerRoute(20);  // Set max connections per route
    	        connectionManager.setValidateAfterInactivity(TimeValue.ofSeconds(30));  // Validate connections after 30 seconds of inactivity

    	        // Create the CloseableHttpClient with the custom connection manager
    	        CloseableHttpClient httpClient = HttpClients.custom()
    	                .setConnectionManager(connectionManager)
    	                .evictIdleConnections(TimeValue.ofMinutes(5))  // Evict idle connections after 5 minutes
    	                .build();

    	        // Use HttpComponentsClientHttpRequestFactory to integrate the custom HttpClient with RestTemplate
    	        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(httpClient);

    	        // Build the RestTemplate using the custom request factory
    	        return builder
    	                .requestFactory(() -> factory)
    	                .build();
    	}
}