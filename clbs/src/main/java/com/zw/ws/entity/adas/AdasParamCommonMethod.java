package com.zw.ws.entity.adas;

import com.zw.platform.util.BinaryDataUtil;
import com.zw.platform.util.Reflections;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

/**
 * 主动安全参数设置公共方法
 */
public interface AdasParamCommonMethod {

    /**
     * 按位进行运算的方法
     * @param originData  原始数据
     * @param pointerData 要设置的值
     * @param pointer     要设置的为
     * @return
     */
    default Integer calBinaryData(Integer originData, Integer pointerData, Integer pointer) {
        return BinaryDataUtil.setBinaryPointerData(originData, pointerData, pointer);

    }

    /**
     * 按位进行运算的方法
     * @param originData  原始数据
     * @param pointerData 要设置的值
     * @param pointer     要设置的为
     * @return
     */
    default Long calBinaryData(Long originData, Integer pointerData, Integer pointer) {
        return BinaryDataUtil.setBinaryPointerData(originData, pointerData, pointer);

    }

    /**
     * 整形数值转换方法
     * @param data
     * @return
     */
    default Integer parseIntData(String data) {
        try {
            return data == null ? null : Integer.parseInt(data.split("0x")[1]);
        } catch (Exception e) {
            return Integer.parseInt(data);
        }

    }

    /**
     * 属性赋值方法，如果值不为空和-1，则进行赋值
     * @param fieldName
     * @param value
     */
    default void setValIfPresent(String fieldName, Integer value) {
        if (value != null && value != -1) {
            Optional.ofNullable(value).ifPresent(val -> Reflections.setValIfPresent(fieldName, val, this));
        }

    }

    String BLANK_DEFAULT = "-1";

    default boolean isNotBlankVal(String val) {
        return StringUtils.isNotBlank(val) && !BLANK_DEFAULT.equals(val);
    }

    /**
     * 获取最终通道的值，前端传递的是数字，这里需要做二进制的移位操作
     */
    default Integer getFinalChannelVal(Integer channel) {
        if (channel == null) {
            return channel;
        }
        return 1 << channel;
    }

}
