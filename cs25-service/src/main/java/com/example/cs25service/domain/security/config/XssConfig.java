package com.example.cs25service.domain.security.config;

import com.example.cs25service.domain.security.common.HTMLCharacterEscapes;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.util.List;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class XssConfig implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        ObjectMapper escapeObjectMapper = new ObjectMapper();
        escapeObjectMapper.getFactory().setCharacterEscapes(new HTMLCharacterEscapes());
        escapeObjectMapper.registerModule(new JavaTimeModule());

        MappingJackson2HttpMessageConverter escapeConverter =
            new MappingJackson2HttpMessageConverter(escapeObjectMapper);
        escapeConverter.setSupportedMediaTypes(List.of(MediaType.APPLICATION_JSON));

        // 이 converter는 @ResponseBody 응답용에만 적용됨
        converters.add(0, escapeConverter); // 우선순위 0번에 등록
    }
}