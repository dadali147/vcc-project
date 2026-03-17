package com.vcc.framework.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * HTTP 消息转换器配置 - 确保 UTF-8 编码
 */
@Configuration
public class HttpEncodingConfig implements WebMvcConfigurer
{
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters)
    {
        // 1. String 转换器 - UTF-8
        StringHttpMessageConverter stringHttpMessageConverter = new StringHttpMessageConverter(StandardCharsets.UTF_8);
        List<MediaType> stringMediaTypes = new ArrayList<>();
        stringMediaTypes.add(MediaType.TEXT_PLAIN);
        stringMediaTypes.add(MediaType.TEXT_HTML);
        stringHttpMessageConverter.setSupportedMediaTypes(stringMediaTypes);
        converters.add(0, stringHttpMessageConverter);

        // 2. JSON 转换器 - UTF-8
        MappingJackson2HttpMessageConverter jackson2HttpMessageConverter = new MappingJackson2HttpMessageConverter();
        jackson2HttpMessageConverter.setDefaultCharset(StandardCharsets.UTF_8);
        List<MediaType> jsonMediaTypes = new ArrayList<>();
        jsonMediaTypes.add(MediaType.APPLICATION_JSON);
        jsonMediaTypes.add(new MediaType("application", "*+json", StandardCharsets.UTF_8));
        jackson2HttpMessageConverter.setSupportedMediaTypes(jsonMediaTypes);
        converters.add(1, jackson2HttpMessageConverter);
    }
}
