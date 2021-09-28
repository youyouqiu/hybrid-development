package com.zw.platform.util.spring;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractHttpMessageConverter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;

/**
 * spring 消息解析
 * @author create by hjj
 */
public class SpringMessageConverterFastJson extends AbstractHttpMessageConverter<Object> {
    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private SerializerFeature[] serializerFeature;

    public SpringMessageConverterFastJson() {
        super(new MediaType("application", "json", DEFAULT_CHARSET));
    }

    public SerializerFeature[] getSerializerFeature() {
        return this.serializerFeature;
    }

    public void setSerializerFeature(SerializerFeature[] serializerFeatureP) {
        this.serializerFeature = serializerFeatureP;
    }

    @Override
    public boolean canRead(Class<?> clazz, MediaType mediaType) {
        return true;
    }

    @Override
    public boolean canWrite(Class<?> clazz, MediaType mediaType) {
        return true;
    }

    @Override
    protected boolean supports(Class<?> clazz) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected Object readInternal(Class<? extends Object> clazz, HttpInputMessage inputMessage) throws IOException {
        int i;
        // 不晓得为撒， 这里大家都没有释放, 那就不释放吧
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        while ((i = inputMessage.getBody().read()) != -1) {
            baos.write(i);
        }
        return baos.toString();

    }

    @Override
    protected void writeInternal(Object o, HttpOutputMessage outputMessage) throws IOException {
        SerializeConfig sc = new SerializeConfig();
        sc.put(java.util.Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
        sc.put(java.sql.Date.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
        sc.put(Timestamp.class, new SimpleDateFormatSerializer("yyyy-MM-dd HH:mm:ss"));
        String jsonString = JSON.toJSONString(o, sc, this.serializerFeature);
        OutputStream out = outputMessage.getBody();
        out.write(jsonString.getBytes(DEFAULT_CHARSET));
        out.flush();
    }
}