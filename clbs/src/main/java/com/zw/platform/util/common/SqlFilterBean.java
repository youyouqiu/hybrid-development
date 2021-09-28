package com.zw.platform.util.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 过滤条件
 */
@Data
public class SqlFilterBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private String property; // 属性
	private String operator; // 操作方式
	private Object value; // 值
}
