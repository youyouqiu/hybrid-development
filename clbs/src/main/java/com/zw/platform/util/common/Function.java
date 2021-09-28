package com.zw.platform.util.common;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Data;

@Data
public abstract class Function {
    private String initName;
    private static Logger logger = LoggerFactory.getLogger(Function.class);

    protected Function(String initName) {
        this.initName = initName;
    }

    public abstract void execute();

    public void executeInit() {
        try {
            this.execute();
        } catch (Exception e) {
            logger.error(this.getInitName() + "初始化错误", e);
        }
    }
}