package com.zw.app.util.common;

import com.zw.platform.util.JsonUtil;

import java.io.Serializable;

/**
 * App消息返回实体
 * @author hujun
 * @date 2018/8/20 10:13
 */
public class AppResultBean implements Serializable {

    private static final long serialVersionUID = 1L;
    /** 成功 */
    public static final int SUCCESS = 200;
    /** 参数错误 */
    public static final int PARAM_ERROR = 400;
    /** 未登录 */
    public static final int LOG_OUT = 401;
    /** 权限不足 */
    public static final int INSUFFICIENT_PRIVILEGES = 403;
    /** 未找到资源 */
    public static final int NO_RESOURCES = 404;
    /** 请求超时 */
    public static final int REQUEST_TIMEOUT = 408;
    /** 服务器错误 */
    public static final int SERVER_ERROR = 500;
    /** 服务不可用 */
    public static final int SERVER_DISABLED = 503;

    private int statusCode;// 状态码
    private String msg;// 返回消息
    private String exceptionDetailMsg;// 详细错误信息
    private Object obj;// 返回结果

    public AppResultBean() {
        this.statusCode = SUCCESS;
    }

    public AppResultBean(String message) {
        this.statusCode = SUCCESS;
        this.msg = message;
    }

    public AppResultBean(Object object) {
        this.statusCode = SUCCESS;
        this.obj = object;
    }

    public AppResultBean(int statusCode) {
        this.statusCode = statusCode;
    }

    public AppResultBean(int statusCode, String exceptionDetailMsg) {
        this.statusCode = statusCode;
        this.exceptionDetailMsg = exceptionDetailMsg;
    }

    public boolean isSuccess() {
        if (this.statusCode == SUCCESS) {
            return true;
        }
        return false;
    }

    public int getStatusCode() {
        return this.statusCode;
    }

    public String getMsg() {
        return this.msg;
    }

    public Object getObj() {
        return this.obj;
    }

    public String getExceptionDetailMsg() {
        return this.exceptionDetailMsg;
    }

    public String toString() {
        return JsonUtil.object2Json(this);
    }

}
