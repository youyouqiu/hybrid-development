package com.zw.platform.util.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 排序
 */
@Data
public class SqlSortBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private String property; // 排序字段
	private String direction; // 排序方式 大写的ASC or DESC
}
