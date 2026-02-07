package com.beanbrewcafe.barista.controller;

import com.beanbrewcafe.barista.model.Order;
import com.beanbrewcafe.barista.service.TestDataService;
import com.beanbrewcafe.barista.service.TestMetricsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TestController {

    private final TestDataService testDataService;
    private final TestMetricsService testMetricsService;

    /**
     * Generate 100 test orders
     * POST /api/test/generate
     */
    @PostMapping("/generate")
    public ResponseEntity<String> generateTestOrders() {
        List<Order> orders = testDataService.generate100TestOrders();
        return ResponseEntity.ok("Generated " + orders.size() + " test orders successfully");
    }

    /**
     * Clear all test data
     * DELETE /api/test/clear
     */
    @DeleteMapping("/clear")
    public ResponseEntity<String> clearTestData() {
        testDataService.clearTestData();
        return ResponseEntity.ok("Test data cleared successfully");
    }

    /**
     * Get test metrics
     * GET /api/test/metrics
     */
    @GetMapping("/metrics")
    public ResponseEntity<TestMetricsService.TestMetrics> getTestMetrics() {
        return ResponseEntity.ok(testMetricsService.getTestMetrics());
    }

    /**
     * Get time series data
     * GET /api/test/timeseries
     */
    @GetMapping("/timeseries")
    public ResponseEntity<List<TestMetricsService.TimeSeriesPoint>> getTimeSeries() {
        return ResponseEntity.ok(testMetricsService.getTimeSeriesData());
    }
}