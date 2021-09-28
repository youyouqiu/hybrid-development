package com.zw.platform.basic.domain;

import lombok.Data;

/**
 * @Author: zjc
 * @Description:返回map实体信息，允许一个key一个val
 * @Date: create in 2020/10/28 10:28
 */
@Data
public class BaseKvDo<K, V> {
    /**
     * key
     */
    private K keyName;
    /**
     * val
     */
    private V firstVal;
}
