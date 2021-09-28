package com.zw.platform.commons;

/**
 * <p>
 * Title:
 * <p>
 * Copyright: Copyright (c) 2017
 * <p>
 * Company: ZhongWei
 * <p>
 * team: ZhongWeiTeam
 *
 * @version 1.0
 * @author: wanxing
 * @date 2017年9月28日 下午2:20:08
 */
public class RedisException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public RedisException() {
	}

	public RedisException(String msg) {
		super(msg);
	}

    public RedisException(Throwable cause) {
        super(cause);
    }

    public RedisException(String message, Throwable cause) {
        super(message, cause);
    }

    public RedisException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
	
}
