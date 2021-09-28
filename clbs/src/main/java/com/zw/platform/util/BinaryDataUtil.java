package com.zw.platform.util;

/**
 * @Author: zjc
 * @Description:
 * @Date: create in 2020/12/29 11:19
 */

/**
 * 二进制按位赋值工具类
 */
public class BinaryDataUtil {
    /**
     * 按位赋值
     * @param originData
     * @param pointerData
     * @param pointer
     * @return
     */
    public  static Long setBinaryPointerData(Long originData, Integer pointerData, Integer pointer) {
        if (pointerData == null || pointerData == -1) {
            return originData;
        }

        Integer moveData = 1 << pointer;
        if (pointerData == 1) {
            originData |= moveData;
        } else {
            originData &= originData & (originData ^ moveData);
        }

        return originData;

    }

    /**
     * 按位赋值
     * @param originData
     * @param pointerData
     * @param pointer
     * @return
     */
    public static  Integer setBinaryPointerData(Integer originData, Integer pointerData, Integer pointer) {
        if (pointerData == null || pointerData == -1) {
            return originData;
        }

        Integer moveData = 1 << pointer;
        if (pointerData == 1) {
            originData |= moveData;
        } else {
            originData &= originData & (originData ^ moveData);
        }

        return originData;

    }
}
