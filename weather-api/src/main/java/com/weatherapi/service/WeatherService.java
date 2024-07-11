package com.weatherapi.service;

import com.weatherapi.exception.RateLimitExceededException;
import com.weatherapi.model.WeatherReport;
import com.weatherapi.repository.WeatherReportRepository;
import com.weatherapi.util.ApiKeyManager;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WeatherService {

    private final WeatherReportRepository repository;
    private final RestTemplate restTemplate;
    private final ApiKeyManager apiKeyManager;

    @Value("${openweathermap.api.url}")
    private String apiUrl;

    public WeatherReport getWeatherReport(String city, String country, String userApiKey) {
        if (!apiKeyManager.isValidApiKey(userApiKey)) {
            throw new IllegalArgumentException("Invalid API key");
        }

        if (!apiKeyManager.allowRequest(userApiKey)) {
            throw new RateLimitExceededException("Rate limit exceeded for API key: " + userApiKey);
        }

        return repository.findFirstByCityAndCountryOrderByTimestampDesc(city, country)
                .filter(this::isReportFresh)
                .orElseGet(() -> fetchAndSaveWeatherReport(city, country, userApiKey));
    }

    private WeatherReport fetchAndSaveWeatherReport(String city, String country, String userApiKey) {
        String url = String.format("%s?q=%s,%s&appid=%s", apiUrl, city, country, userApiKey);
        Map<String, Object> response = restTemplate.getForObject(url, Map.class);

        if (response != null && response.containsKey("weather")) {
            List<Map<String, Object>> weather = (List<Map<String, Object>>) response.get("weather");
            String description = (String) weather.get(0).get("description");

            WeatherReport report = new WeatherReport(null, city, country, description, Instant.now().getEpochSecond());
            return repository.save(report);
        }

        throw new RuntimeException("Failed to fetch weather data");
    }

    private boolean isReportFresh(WeatherReport report) {
        return Instant.now().getEpochSecond() - report.timestamp() < 3600; // 1 hour
    }
}