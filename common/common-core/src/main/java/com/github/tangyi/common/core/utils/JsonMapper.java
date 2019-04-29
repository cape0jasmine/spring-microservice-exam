package com.github.tangyi.common.core.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by tangyi on 2017/3/14.
 */
public class JsonMapper extends ObjectMapper {

    private static final long serialVersionUID = 1L;

    private static Logger logger = LoggerFactory.getLogger(JsonMapper.class);

    private static JsonMapper mapper;

    public static JsonMapper getInstance() {
        if (mapper == null){
            mapper = new JsonMapper().enableSimple();
        }
        return mapper;
    }

    public String toJson(Object object) {
        try {
            return this.writeValueAsString(object);
        } catch (IOException e) {
            logger.warn("将解析JSON为字符串失败:" + object, e);
            return null;
        }
    }

    public <T> T fromJson(String jsonString, Class<T> clazz) {
        if (StringUtils.isEmpty(jsonString)) {
            return null;
        }
        try {
            return this.readValue(jsonString, clazz);
        } catch (IOException e) {
            logger.warn("将解析JSON为对象失败:" + jsonString, e);
            return null;
        }
    }

    public <T> T fromJson(String jsonString, JavaType javaType) {
        if (StringUtils.isEmpty(jsonString)) {
            return null;
        }
        try {
            return (T) this.readValue(jsonString, javaType);
        } catch (IOException e) {
            logger.warn("将解析JSON为对象失败:" + jsonString, e);
            return null;
        }
    }

    public static Object fromJsonObject(String jsonString,  JavaType javaType){
        return JsonMapper.getInstance().fromJson(jsonString, javaType);
    }


    public JavaType createCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
        return this.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }

    public JsonMapper enableSimple() {
        this.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
        this.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        return this;
    }

    public ObjectMapper getMapper() {
        return this;
    }


    public static String toJsonString(Object object){
        return JsonMapper.getInstance().toJson(object);
    }

    public static Object fromJsonString(String jsonString, Class<?> clazz){
        return JsonMapper.getInstance().fromJson(jsonString, clazz);
    }

}
