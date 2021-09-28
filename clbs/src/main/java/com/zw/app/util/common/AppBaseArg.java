package com.zw.app.util.common;

import com.zw.app.entity.BaseEntity;
import lombok.Data;
import org.springframework.validation.BindingResult;

import javax.servlet.http.HttpServletRequest;


@Data
public class AppBaseArg {
    private HttpServletRequest request;

    private BindingResult result;

    private Object service;

    private BaseEntity baseEntity;

    private String exceptionIfo;

    public static AppBaseArg getInstance(HttpServletRequest request, BindingResult result, Object service,
                                         BaseEntity baseEntity) {
        AppBaseArg appBaseArg = new AppBaseArg();
        appBaseArg.request = request;
        appBaseArg.result = result;
        appBaseArg.baseEntity = baseEntity;
        appBaseArg.service = service;
        appBaseArg.exceptionIfo = baseEntity.getExceptionInfo();
        return appBaseArg;
    }

}
