package com.zw.platform.basic.util;

import org.springframework.core.convert.converter.Converter;

import java.util.Date;

/**
 * @Author: zjc
 * @Description:
 * @Date: create in 2020/10/22 10:27
 */
public class LongDateConvert implements Converter<Long, Date> {
    @Override
    public Date convert(Long source) {

        return new Date(source);
    }
}
