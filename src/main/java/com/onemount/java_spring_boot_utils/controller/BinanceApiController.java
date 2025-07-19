package com.onemount.java_spring_boot_utils.controller;

import com.onemount.java_spring_boot_utils.service.BinanceApiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/crypto")
public class BinanceApiController {
    private final BinanceApiService binanceApiService;

    public BinanceApiController(BinanceApiService binanceApiService) {
        this.binanceApiService = binanceApiService;
    }

    @GetMapping("/ping")
    public ResponseEntity<?> ping() {
        return ResponseEntity.ok(binanceApiService.getPing());
    }

    @GetMapping("/time")
    public ResponseEntity<?> serverTime() {
        return ResponseEntity.ok(binanceApiService.getServerTime());
    }

    @GetMapping("/exchangeInfo")
    public ResponseEntity<?> exchangeInfo() {
        return ResponseEntity.ok(binanceApiService.getExchangeInfo());
    }

    @GetMapping("/ticker/price")
    public ResponseEntity<?> tickerPrice(@RequestParam String symbol) {
        return ResponseEntity.ok(binanceApiService.getTickerPrice(symbol));
    }

    @GetMapping("/ticker/allPrices")
    public ResponseEntity<?> allPrices() {
        return ResponseEntity.ok(binanceApiService.getAllTickerPrices());
    }

    @GetMapping("/bookTicker")
    public ResponseEntity<?> bookTicker(@RequestParam String symbol) {
        return ResponseEntity.ok(binanceApiService.getBookTicker(symbol));
    }

    @GetMapping("/depth")
    public ResponseEntity<?> depth(@RequestParam String symbol, @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(binanceApiService.getDepth(symbol, limit));
    }

    @GetMapping("/trades")
    public ResponseEntity<?> recentTrades(@RequestParam String symbol, @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(binanceApiService.getRecentTrades(symbol, limit));
    }

    @GetMapping("/klines")
    public ResponseEntity<?> klines(@RequestParam String symbol,
                                     @RequestParam String interval,
                                     @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(binanceApiService.getKlines(symbol, interval, limit));
    }

    // --- FUTURES ---

    @GetMapping("/futures/ping")
    public ResponseEntity<?> futuresPing() {
        return ResponseEntity.ok(binanceApiService.getFuturesPing());
    }

    @GetMapping("/futures/time")
    public ResponseEntity<?> futuresTime() {
        return ResponseEntity.ok(binanceApiService.getFuturesTime());
    }

    @GetMapping("/futures/exchangeInfo")
    public ResponseEntity<?> futuresExchangeInfo() {
        return ResponseEntity.ok(binanceApiService.getFuturesExchangeInfo());
    }

    @GetMapping("/futures/depth")
    public ResponseEntity<?> futuresDepth(@RequestParam String symbol, @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(binanceApiService.getFuturesDepth(symbol, limit));
    }

    @GetMapping("/futures/aggTrades")
    public ResponseEntity<?> futuresAggTrades(@RequestParam String symbol, @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(binanceApiService.getFuturesAggTrades(symbol, limit));
    }

    @GetMapping("/futures/ticker/price")
    public ResponseEntity<?> futuresTickerPrice(@RequestParam String symbol) {
        return ResponseEntity.ok(binanceApiService.getFuturesTickerPrice(symbol));
    }

    @GetMapping("/futures/klines")
    public ResponseEntity<?> futuresKlines(@RequestParam String symbol,
                                            @RequestParam String interval,
                                            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(binanceApiService.getFuturesKlines(symbol, interval, limit));
    }

}
