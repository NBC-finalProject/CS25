package com.example.cs25service.domain.security.config;

//@Configuration
//public class XssConfig implements WebMvcConfigurer {
//
//    @Override
//    public void extendMessageConverters(List<HttpMessageConverter<?>> converters) {
//        ObjectMapper escapeObjectMapper = new ObjectMapper();
//        escapeObjectMapper.getFactory().setCharacterEscapes(new HTMLCharacterEscapes());
//        escapeObjectMapper.registerModule(new JavaTimeModule());
//
//        MappingJackson2HttpMessageConverter escapeConverter =
//            new MappingJackson2HttpMessageConverter(escapeObjectMapper);
//
//        escapeConverter.setSupportedMediaTypes(
//            List.of(MediaType.APPLICATION_JSON, MediaType.TEXT_EVENT_STREAM));
//
//        // 기존 기본 컨버터는 유지한 채, escape converter만 추가
//        converters.add(0, escapeConverter);
//    }
//}