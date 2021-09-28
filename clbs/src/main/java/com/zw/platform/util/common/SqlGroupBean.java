package com.zw.platform.util.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 分组
 */
@Data
public class SqlGroupBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private String property; // 分组字段
}
