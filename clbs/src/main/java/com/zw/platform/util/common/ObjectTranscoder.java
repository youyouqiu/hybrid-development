package com.zw.platform.util.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;

public class ObjectTranscoder {
    private static Logger logger = LogManager.getLogger(ObjectTranscoder.class);

    public static byte[] serialize(Object value) {
        if (value == null) {
            throw new NullPointerException("Can't serialize null");
        }
        byte[] rv;
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream os = new ObjectOutputStream(bos)) {
            os.writeObject(value);
            rv = bos.toByteArray();
        } catch (IOException e) {
            throw new IllegalArgumentException("Non-serializable object", e);
        }
        return rv;
    }

    public static Object deserialize(byte[] in) {
        if (in == null) {
            return null;
        }
        Object rv = null;
        try (ByteArrayInputStream bis = new ByteArrayInputStream(in);
            ObjectInputStream is = new ObjectInputStream(bis)) {
            rv = is.readObject();
        } catch (Exception e) {
            logger.error("deserialize异常" + e);
        }
        return rv;
    }

    /**
     * 对象深拷贝，需要兑现实现Serializable接口
     */
    public static <T> T deepClone(T t) {
        try {
            // 将对象写到流里
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream oo = new ObjectOutputStream(bo);
            oo.writeObject(t);
            // 从流里读出来
            ByteArrayInputStream bi = new ByteArrayInputStream(bo.toByteArray());
            ObjectInputStream oi = new ObjectInputStream(bi);
            return (T) (oi.readObject());
        } catch (Exception e) {
            logger.error(t.getClass().getName() + "深拷贝异常！", e);
            return null;
        }
    }
}
