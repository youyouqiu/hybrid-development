package com.zw.platform.util.imports.lock;

import com.zw.platform.util.common.JsonResultBean;

import java.util.Collection;
import java.util.function.Function;

/**
 * 导入手动提交事务
 * @author create by zhouzongbo on 2020/9/2.
 */
public interface ImportTransaction {

    /**
     * 导入模块分批更新
     * 这里包含事务提交和表锁
     * @param source      数据源
     * @param addFunction 新增function
     * @param username    用户名
     * @param importTable 导入表
     * @param <S>         数据源类型
     * @return true: 成功, false: 失败
     */
    <S> boolean transaction(Collection<S> source,
                            Function<Collection<S>, Boolean> addFunction,
                            String username,
                            ImportTable importTable);

    /**
     * 导入模块分批更新:
     * 建议关联关系的表放在一个事务内, ps: 车辆新增成功了，但是车辆与与企业绑定关系新增失败, 导致查询不到车辆信息
     * 这里包含事务提交和表锁;
     * @param handler handler
     * @return ImportResult, 包含成功失败, 和异常信息
     */
    JsonResultBean transaction(BaseImportHandler handler);
}
