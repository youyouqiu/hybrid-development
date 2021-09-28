package com.zw.talkback.util.common;

import com.zw.platform.util.common.JsonResultBean;

/**
 * 功能描述:查询JsonResultBean的函数定义
 */
@FunctionalInterface
public interface QueryResultBeanFunction {
    JsonResultBean execute() throws Exception;
}
