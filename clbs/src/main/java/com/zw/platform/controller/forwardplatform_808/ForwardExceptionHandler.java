package com.zw.platform.controller.forwardplatform_808;

import com.zw.platform.util.common.JsonResultBean;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice("com.zw.platform,controller.forwardplatform_808")
public class ForwardExceptionHandler {
    private static final Logger log = LogManager.getLogger(ForwardExceptionHandler.class);

    @Value("${sys.error.msg}")
    private String sysErrorMsg;

    @ExceptionHandler(Exception.class)
    public final JsonResultBean exceptionHandle(Exception e) {
        log.error("监控对象转发管理异常", e);
        return new JsonResultBean(JsonResultBean.FAULT, sysErrorMsg);
    }
}
