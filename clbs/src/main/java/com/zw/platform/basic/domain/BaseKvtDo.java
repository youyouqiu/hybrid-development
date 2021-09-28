package com.zw.platform.basic.domain;

import lombok.Data;

/**
 * @Author: zjc
 * @Description:返回map实体信息，允许一个key两个val
 * @Date: create in 2020/10/28 10:28
 */
@Data
public class BaseKvtDo<K, V, T> {
    /**
     * key
     */
    private K keyName;
    /**
     * val1
     */
    private V firstValue;
    /**
     * val2
     */
    private T secondVal;

}
