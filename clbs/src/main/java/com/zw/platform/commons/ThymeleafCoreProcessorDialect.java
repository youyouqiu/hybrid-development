package com.zw.platform.commons;

import org.thymeleaf.dialect.AbstractProcessorDialect;
import org.thymeleaf.processor.IProcessor;
import org.thymeleaf.standard.StandardDialect;

import java.util.HashSet;
import java.util.Set;

public class ThymeleafCoreProcessorDialect extends AbstractProcessorDialect {
    private static final String DIALECT_NAME = "Zwlbs Dialect core"; // 方言名称需要唯一。自定义方言名称Turingoal Dialect core
    public static final String DIALECT_PREFIX = "zw_core"; // 前缀

    public ThymeleafCoreProcessorDialect() {
        super(DIALECT_NAME, DIALECT_PREFIX, StandardDialect.PROCESSOR_PRECEDENCE);
    }

    /**
     * 自定义的Processor在这里添加
     */
    public Set<IProcessor> getProcessors(final String dialectPrefix) {
        final Set<IProcessor> processors = new HashSet<IProcessor>();
        processors.add(new PremissionProcessor()); // 授权premissionProcessor
        return processors;
    }

}
