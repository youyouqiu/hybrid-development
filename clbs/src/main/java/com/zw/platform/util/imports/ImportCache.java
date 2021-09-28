package com.zw.platform.util.imports;

import com.zw.platform.util.imports.lock.BaseImportHandler;
import com.zw.platform.util.imports.lock.ImportModule;
import lombok.Data;

import java.io.Closeable;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 导入进度缓存
 *
 * @author Zhang Yanhui
 * @since 2020/9/15 14:08
 */

@Data
public class ImportCache implements Closeable {
    /**
     * module -> username -> handlers
     * <p>能正常使用的前提是单模块单用户至多同时有1个导入任务
     */
    public static final Map<ImportModule, Map<String, List<BaseImportHandler>>> CACHE = new ConcurrentHashMap<>();

    private final ImportModule module;
    private final String username;
    private final List<BaseImportHandler> handlers;

    public ImportCache(ImportModule module, String username, List<BaseImportHandler> handlers) {
        this.module = module;
        this.username = username;
        this.handlers = handlers;
        CACHE.computeIfAbsent(module, m -> new ConcurrentHashMap<>()).put(username, handlers);
    }

    public ImportCache(ImportModule module, String username, BaseImportHandler handler) {
        this(module, username, Collections.singletonList(handler));
    }

    @Override
    public void close() {
        Optional.ofNullable(CACHE.get(module)).ifPresent(map -> map.remove(username));
    }
}
