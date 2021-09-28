package com.zw.platform.util.common;

import java.beans.PropertyEditorSupport;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.support.WebBindingInitializer;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.util.HtmlUtils;

/**
 * @author Administrator
 * 全局转义xss攻击
 */
public class WebBinderInitializerUtils implements WebBindingInitializer {
 
    @Override
    public void initBinder(WebDataBinder binder, WebRequest request) {
        binder.registerCustomEditor(String.class, new PropertyEditorSupport() {
            @Override
            public void setAsText(String text) throws IllegalArgumentException {
                if (StringUtils.isBlank(text)) {
                    return;
                }
                try {
                    //Spring自带html标签转义与反转义
                    super.setValue(HtmlUtils.htmlEscape(text));
                } catch (Exception e) {
                    throw new IllegalArgumentException(e);
                }
            }
        });
    }
}

