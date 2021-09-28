package com.zw.platform.util.common;

import com.zw.platform.util.JsonUtil;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;

public class JsonResultBean implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final boolean SUCCESS = true;
    public static final boolean FAULT = false;
    private boolean success;
    private String msg;
    private String exceptionDetailMsg;
    private Object obj;

    public JsonResultBean() {
        this.success = true;
    }

    public JsonResultBean(String message) {
        this.success = true;
        this.msg = message;
    }

    public JsonResultBean(Object object) {
        this.success = true;
        this.obj = object;
    }

    public JsonResultBean(String message, Object object) {
        this.success = true;
        this.msg = message;
        this.obj = object;
    }

    public JsonResultBean(boolean suc) {
        this.success = suc;
    }

    public JsonResultBean(boolean suc, String message) {
        this.success = suc;
        this.msg = message;
    }

    public JsonResultBean(Throwable exceptionMessage) {
        exceptionMessage.printStackTrace(new PrintWriter(new StringWriter()));

        this.success = false;

        this.msg = exceptionMessage.getMessage();
    }

    public JsonResultBean(Throwable exceptionMessage, boolean detailMsg) {
        exceptionMessage.printStackTrace(new PrintWriter(new StringWriter()));

        this.success = false;

        this.msg = exceptionMessage.getMessage();
        if (detailMsg) {
            this.exceptionDetailMsg = exceptionMessage.toString();
        }
    }

    public boolean isSuccess() {
        return this.success;
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

    @Override
    public String toString() {
        return JsonUtil.object2Json(this);
    }
}