package com.josegs98.weather_api.config;

import com.josegs98.weather_api.client.dto.VisualCrossingResponse;
import com.josegs98.weather_api.constant.CacheNamesConstant;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;

import java.time.Duration;

@Configuration
@EnableCaching
@RequiredArgsConstructor
@EnableConfigurationProperties(WeatherCacheProperties.class)
public class WeatherCacheConfig {
    private final WeatherCacheProperties weatherCacheProperties;

    @Bean
    public RedisCacheConfiguration redisCacheConfiguration(){
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(30))
                .prefixCacheNameWith("weather-api::")
                .disableCachingNullValues()
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new JacksonJsonRedisSerializer<>(VisualCrossingResponse.class)));
    }

    @Bean
    public RedisCacheManagerBuilderCustomizer redisCacheManagerBuilderCustomizer(RedisCacheConfiguration cacheConfigurationBase){
        return builder ->
                builder.withCacheConfiguration(CacheNamesConstant.FORECAST_TODAY, cacheConfigurationBase.entryTtl(weatherCacheProperties.forecastTodayTtl()))
                        .withCacheConfiguration(CacheNamesConstant.FORECAST_RANGE, cacheConfigurationBase.entryTtl(weatherCacheProperties.forecastRangeTtl()));
    }
}
