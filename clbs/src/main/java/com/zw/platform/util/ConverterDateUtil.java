package com.zw.platform.util;

import org.apache.commons.beanutils.Converter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public interface ConverterDateUtil extends Converter {

    default Object convert(Class type, Object value) {

        if (value == null) {
            return null;
        }

        if (value.getClass() == java.util.Date.class) {
            return value;
        }

        //当是从数据库查出是java.sql.Timestamp特殊处理
        if (value.getClass() == java.sql.Timestamp.class) {
            return new Date(((java.sql.Timestamp)value).getTime());
        }
        //当从数据库查出是java.sql.date特殊处理
        if (value.getClass() == java.sql.Date.class) {
            return new Date(((java.sql.Date) value).getTime());
        }

        //当获取的时间是时间戳时
        if (value.getClass() == java.lang.Long.class) {
            return new Date((java.lang.Long)value);
        }

        String dateStr = String.valueOf(value);
        SimpleDateFormat spdt = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = spdt.parse(dateStr);
            return date;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}
