package com.altradar.util;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;



public class UpbitJwtUtil {

    public static String createToken(String query, String accessKey, String secretKey) {
        String nonce = UUID.randomUUID().toString();
        String payload = "access_key=" + accessKey + "&nonce=" + nonce;
        if (!query.isEmpty()) {
            payload += "&query=" + query;
        }

        Algorithm algorithm = Algorithm.HMAC256(secretKey);
        return JWT.create()
                .withClaim("access_key", accessKey)
                .withClaim("nonce", nonce)
                .withClaim("query", query)
                .sign(algorithm);
    }

    public static String buildQuery(Map<String, String> params) {
        return params.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .map(e -> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));
    }
}