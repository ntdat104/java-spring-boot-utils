package com.onemount.java_spring_boot_utils.service;

import com.github.benmanes.caffeine.cache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Map;

@Service
public class BinanceApiService {
    private final RestTemplate restTemplate;
    private final String BASE_URL = "https://api.binance.com";
    private final String FUTURES_URL = "https://fapi.binance.com";

    private final BinanceAsyncService binanceAsyncService;

    @Autowired
    @Qualifier("caffeineCache")
    Cache<String, Object> cache;

    public BinanceApiService(RestTemplate restTemplate, BinanceAsyncService binanceAsyncService) {
        this.restTemplate = restTemplate;
        this.binanceAsyncService = binanceAsyncService;
    }

    public Object getPing() {
        return restTemplate.getForObject(BASE_URL + "/api/v3/ping", Map.class);
    }

    public Object getServerTime() {
        return restTemplate.getForObject(BASE_URL + "/api/v3/time", Map.class);
    }

    @Cacheable(cacheNames = "exchangeInfo")
    public Object getExchangeInfo() {
        return restTemplate.getForObject(BASE_URL + "/api/v3/exchangeInfo", Map.class);
    }

    public Object getTickerPrice(String symbol) {
        String key = "ticker:price:" + symbol;
        Object valueCache = cache.getIfPresent(key);
        if (valueCache != null) {
            binanceAsyncService.fetchFromBinance(symbol);
            return valueCache;
        }
        URI uri = UriComponentsBuilder.fromHttpUrl(BASE_URL + "/api/v3/ticker/price")
                .queryParam("symbol", symbol)
                .build().toUri();
        Object response = restTemplate.getForObject(uri, Object.class);
        if (response != null) {
            cache.put(key, response);
        }
        return response;
    }

    @Cacheable(cacheNames = "allPrice")
    public Object getAllTickerPrices() {
        URI uri = UriComponentsBuilder.fromHttpUrl(BASE_URL + "/api/v3/ticker/price").build().toUri();
        return restTemplate.getForObject(uri, Object.class);
    }

    @Cacheable(cacheNames = "getBookTicker", key = "#symbol")
    public Object getBookTicker(String symbol) {
        URI uri = UriComponentsBuilder.fromHttpUrl(BASE_URL + "/api/v3/ticker/bookTicker")
                .queryParam("symbol", symbol)
                .build().toUri();
        return restTemplate.getForObject(uri, Object.class);
    }

    @Cacheable(cacheNames = "getDepth", key = "#symbol + '-' + #limit")
    public Object getDepth(String symbol, int limit) {
        URI uri = UriComponentsBuilder.fromHttpUrl(BASE_URL + "/api/v3/depth")
                .queryParam("symbol", symbol)
                .queryParam("limit", limit)
                .build().toUri();
        return restTemplate.getForObject(uri, Map.class);
    }

    @Cacheable(cacheNames = "getRecentTrades", key = "#symbol + '-' + #limit")
    public Object getRecentTrades(String symbol, int limit) {
        URI uri = UriComponentsBuilder.fromHttpUrl(BASE_URL + "/api/v3/trades")
                .queryParam("symbol", symbol)
                .queryParam("limit", limit)
                .build().toUri();
        return restTemplate.getForObject(uri, Object.class);
    }

    @Cacheable(cacheNames = "getKlines", key = "#symbol + '-' + #interval + '-' + #limit")
    public Object getKlines(String symbol, String interval, int limit) {
        URI uri = UriComponentsBuilder.fromHttpUrl(BASE_URL + "/api/v3/klines")
                .queryParam("symbol", symbol)
                .queryParam("interval", interval)
                .queryParam("limit", limit)
                .build().toUri();
        return restTemplate.getForObject(uri, Object.class);
    }

    public Object getFuturesPing() {
        return restTemplate.getForObject(FUTURES_URL + "/fapi/v1/ping", Map.class);
    }

    public Object getFuturesTime() {
        return restTemplate.getForObject(FUTURES_URL + "/fapi/v1/time", Map.class);
    }

    public Object getFuturesExchangeInfo() {
        return restTemplate.getForObject(FUTURES_URL + "/fapi/v1/exchangeInfo", Map.class);
    }

    public Object getFuturesDepth(String symbol, int limit) {
        URI uri = UriComponentsBuilder.fromHttpUrl(FUTURES_URL + "/fapi/v1/depth")
                .queryParam("symbol", symbol)
                .queryParam("limit", limit)
                .build().toUri();
        return restTemplate.getForObject(uri, Map.class);
    }

    public Object getFuturesAggTrades(String symbol, int limit) {
        URI uri = UriComponentsBuilder.fromHttpUrl(FUTURES_URL + "/fapi/v1/aggTrades")
                .queryParam("symbol", symbol)
                .queryParam("limit", limit)
                .build().toUri();
        return restTemplate.getForObject(uri, Object.class);
    }

    public Object getFuturesTickerPrice(String symbol) {
        URI uri = UriComponentsBuilder.fromHttpUrl(FUTURES_URL + "/fapi/v1/ticker/price")
                .queryParam("symbol", symbol)
                .build().toUri();
        return restTemplate.getForObject(uri, Object.class);
    }

    public Object getFuturesKlines(String symbol, String interval, int limit) {
        URI uri = UriComponentsBuilder.fromHttpUrl(FUTURES_URL + "/fapi/v1/klines")
                .queryParam("symbol", symbol)
                .queryParam("interval", interval)
                .queryParam("limit", limit)
                .build().toUri();
        return restTemplate.getForObject(uri, Object.class);
    }
}
