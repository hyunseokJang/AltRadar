package com.altradar.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.altradar.util.UpbitJwtUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UpbitOrderService {

    @Value("${upbit.accessKey}")
    private String accessKey;

    @Value("${upbit.secretKey}")
    private String secretKey;

    public String placeOrder(String market, String price, String side) {
        try {
            String serverUrl = "https://api.upbit.com/v1/orders";

            Map<String, String> params = new LinkedHashMap<>();
            params.put("market", market);         // ex: KRW-BTC
            params.put("side", side);             // bid = 매수, ask = 매도
            params.put("ord_type", "price");      // 시장가 매수는 price, 지정가는 limit
            params.put("price", price);           // 가격
            params.put("volume", null);           // 시장가 매수는 null

            String query = UpbitJwtUtil.buildQuery(params);
            String jwtToken = UpbitJwtUtil.createToken(query, accessKey, secretKey);

            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(serverUrl + "?" + query))
                    .header("Authorization", "Bearer " + jwtToken)
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();

        } catch (Exception e) {
            e.printStackTrace();
            return "오류: " + e.getMessage();
        }
    }
}