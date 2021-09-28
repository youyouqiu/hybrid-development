package com.zw.platform.util.imports.lock;

import com.google.common.collect.Lists;
import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.imports.ProgressBar;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.function.Function;

/**
 * 导入公共接口
 * @author create by zhouzongbo on 2020/9/2.
 */

@Slf4j
public abstract class BaseImportHandler {

    /**
     * 默认分区大小
     */
    private static final int DEFAULT_PARTITION_SIZE = 500;

    @Getter
    protected final ProgressBar progressBar = new ProgressBar(module(), stage());

    /**
     * 调用该方法, 执行导入操作
     * @return ImportResult
     */
    public JsonResultBean execute() {
        final ImportTransaction bean = BeanUtil.getBean(ImportTransaction.class);
        return bean.transaction(this);
    }

    /**
     * 所属模块
     * @return ImportModule
     */
    public abstract ImportModule module();

    /**
     * 阶段，默认0
     * @return 阶段
     */
    public int stage() {
        return 0;
    }

    /**
     * 需要加锁的表
     * @return ImportTable array
     */
    public abstract ImportTable[] tables();

    /**
     * 进行数据唯一性校验(数据是否存在于数据库中)
     * @return true: 数据库不存在; false: 存在
     */
    public boolean uniqueValid() {
        return true;
    }

    /**
     * 写数据库
     * @return true: 成功; false: 失败;
     */
    public abstract boolean addMysql();

    /**
     * 新增或则更新redis
     * 不需要更新的就不实现
     */
    public void addOrUpdateRedis() {
    }

    /**
     * 分批更新，默认每次1000条
     * @param source      数据源
     * @param addFunction 新增function
     * @param <S>         数据源类型
     */
    public <S> void partition(List<S> source, Function<List<S>, Boolean> addFunction) {
        if (CollectionUtils.isEmpty(source)) {
            return;
        }
        this.partition(source, addFunction, DEFAULT_PARTITION_SIZE);
    }

    /**
     * 分批更新, 同时更新进度条
     * @param source        数据源
     * @param addFunction   新增function
     * @param partitionSize 分区条数
     * @param <S>           数据源类型
     */
    public <S> void partition(List<S> source, Function<List<S>, Boolean> addFunction, int partitionSize) {
        for (List<S> partition : Lists.partition(source, partitionSize)) {
            addFunction.apply(partition);
            progressBar.addProgress(partition.size());
        }
    }
}
