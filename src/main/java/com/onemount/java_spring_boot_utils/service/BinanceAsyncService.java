package com.onemount.java_spring_boot_utils.service;

import com.github.benmanes.caffeine.cache.Cache;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class BinanceAsyncService {
    private final RestTemplate restTemplate;
    private final String BASE_URL = "https://api.binance.com";
    private final String FUTURES_URL = "https://fapi.binance.com";

    private final Integer delayTime = 400; //ms
    private final Map<String, Long> cacheDelay = new HashMap<>();

    @Autowired
    @Qualifier("caffeineCache")
    Cache<String, Object> cache;

    public BinanceAsyncService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Async
    public void fetchFromBinance(String symbol) {
        String key = "ticker:price:" + symbol;
        Long prevTime = cacheDelay.get(key);
        Long currentTime = System.currentTimeMillis();
        if (prevTime != null && (currentTime < prevTime)) {
            return;
        }
        cacheDelay.put(key, currentTime + delayTime);
        URI uri = UriComponentsBuilder.fromHttpUrl(BASE_URL + "/api/v3/ticker/price")
                .queryParam("symbol", symbol)
                .build().toUri();
        Object response = restTemplate.getForObject(uri, Object.class);
        if (response != null) {
            cache.put(key, response);
        }
    }
}
