package com.zw.platform.commons;

import com.alibaba.fastjson.parser.DefaultJSONParser;
import com.alibaba.fastjson.parser.JSONToken;
import com.alibaba.fastjson.parser.deserializer.ObjectDeserializer;
import com.alibaba.fastjson.serializer.JSONSerializer;
import com.alibaba.fastjson.serializer.ObjectSerializer;

import javax.naming.InvalidNameException;
import javax.naming.Name;
import javax.naming.ldap.LdapName;
import java.io.IOException;
import java.lang.reflect.Type;

/**
 * @author Chen Feng
 * @version 1.0 2018/2/27
 */
public class LdapNameSerializer implements ObjectSerializer, ObjectDeserializer {
    @Override
    public void write(JSONSerializer serializer, Object object, Object fieldName, Type fieldType, int features)
        throws IOException {
        Name name = (Name) object;
        serializer.write(name.toString());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialze(DefaultJSONParser parser, Type type, Object fieldName) {
        LdapName name = null;
        try {
            name = new LdapName(parser.parseObject(String.class));
        } catch (InvalidNameException e) {
            e.printStackTrace();
        }
        return (T) name;
    }

    @Override
    public int getFastMatchToken() {
        return JSONToken.LITERAL_STRING;
    }
}
