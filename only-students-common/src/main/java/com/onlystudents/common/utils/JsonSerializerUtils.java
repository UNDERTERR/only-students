package com.onlystudents.common.utils;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class JsonSerializerUtils {

    // 序列化格式（带T，给ES/MQ生产者用）
    public static final DateTimeFormatter SERIALIZE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    // 反序列化格式（兼容空格/T，给MQ消费者兜底）
    public static final DateTimeFormatter DESERIALIZE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd['T'][ ]HH:mm:ss");

    private static final ObjectMapper GLOBAL_OBJECT_MAPPER;

    static {
        GLOBAL_OBJECT_MAPPER = new ObjectMapper();
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // 序列化：输出带T的格式
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(SERIALIZE_FORMATTER));
        // 反序列化：兼容空格/T格式
        javaTimeModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(DESERIALIZE_FORMATTER));

        GLOBAL_OBJECT_MAPPER.registerModule(javaTimeModule);
        GLOBAL_OBJECT_MAPPER.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        // 可选：忽略未知字段，避免消息新增字段导致解析失败
        GLOBAL_OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public static ObjectMapper getGlobalObjectMapper() {
        return GLOBAL_OBJECT_MAPPER;
    }
}
