package br.com.fiap.lambda.util;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonMapper {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    public static <T> T fromJson(String json, Class<T> clazz) {
        try {
            return MAPPER.readValue(json, clazz);
        } catch (Exception e) {
            throw new RuntimeException("Falha ao desserializar JSON: " + json, e);
        }
    }
}
