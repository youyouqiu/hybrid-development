package com.zw.platform.util.common;


public class BusinessException extends RuntimeException {
    private static final long serialVersionUID = 1L;
    private String code;
    private String type;
    private String detailMsg;
    private String suggestionMsg;
    private Exception exception;

    public BusinessException() {
        this.code = "X000";
        this.type = "系统错误";
        this.detailMsg = "未知系统错误";
        this.suggestionMsg = "未知系统错误，请联系管理员！";
    }

    public BusinessException(Exception e) {
        this.exception = e;
    }

    public BusinessException(String codeParm) {
        super(codeParm);
        BusinessException e = BusinessExceptionWapper.getBusinessException(codeParm);
        this.code = codeParm;
        this.type = e.getType();
        this.detailMsg = e.getDetailMsg();
        this.suggestionMsg = e.getSuggestionMsg();
    }

    public BusinessException(String code, String detailMsg) {
        this.code = code;
        this.detailMsg = detailMsg;
    }

    public BusinessException(String codeParm, Exception e) {
        this(codeParm);
        this.exception = e;
    }

    @Override
    public String getMessage() {
        return this.code + ":" + this.type + "：" + this.detailMsg + "(" + this.suggestionMsg + ")";
    }

    public String getCode() {
        return this.code;
    }

    public void setCode(String codeParm) {
        this.code = codeParm;
    }

    public String getType() {
        return this.type;
    }

    public void setType(String typeParm) {
        this.type = typeParm;
    }

    public String getDetailMsg() {
        return this.detailMsg;
    }

    public void setDetailMsg(String detailMsgParm) {
        this.detailMsg = detailMsgParm;
    }

    public String getSuggestionMsg() {
        return this.suggestionMsg;
    }

    public void setSuggestionMsg(String suggestionMsgParm) {
        this.suggestionMsg = suggestionMsgParm;
    }

    public Exception getException() {
        return this.exception;
    }

    public void setException(Exception exceptionParm) {
        this.exception = exceptionParm;
    }
}