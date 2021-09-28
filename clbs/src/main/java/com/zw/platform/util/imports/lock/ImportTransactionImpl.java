package com.zw.platform.util.imports.lock;

import com.zw.platform.util.common.JsonResultBean;
import com.zw.platform.util.imports.ProgressBar;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.function.Function;

/**
 * 导入手动提交事务
 * @author create by zhouzongbo on 2020/9/2.
 */
@Slf4j
@Component
public class ImportTransactionImpl implements ImportTransaction {

    /**
     * mybatis事务
     */
    @Resource(name = "transactionManager")
    private DataSourceTransactionManager transactionManager;

    /**
     * 分批更新
     * @param source      数据源
     * @param addFunction 新增function
     * @param <S>         数据源类型
     * @return true: 成功, false: 失败
     */
    @Override
    public <S> boolean transaction(Collection<S> source, Function<Collection<S>, Boolean> addFunction, String username,
        ImportTable importTable) {
        // todo 暂时不用
        return true;
    }

    @Override
    public JsonResultBean transaction(BaseImportHandler handler) {
        final ImportTable[] tables = handler.tables();
        // 对表进行加锁
        final ImportLockCounter instance = ImportLockCounter.getInstance();
        instance.lock(tables);
        DefaultTransactionDefinition transactionDefinition = new DefaultTransactionDefinition();
        // 每次都创建一个新的事务, 并暂定当前事务
        transactionDefinition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        final TransactionStatus transactionStatus = transactionManager.getTransaction(transactionDefinition);
        try {
            final ImportModule module = handler.module();
            final int stage = handler.stage();
            handler.getProgressBar().setStatus(ProgressBar.STATUS_RUNNING);
            // 1.数据校验
            log.info("[{}]阶段[{}]开始校验", module, stage);
            final boolean valid = handler.uniqueValid();
            log.info("[{}]阶段[{}]完成校验，是否通过：{}", module, stage, valid ? "是" : "否");
            if (!valid) {
                // 校验的数据, 返回给调用方, 用于组装下载模板
                return new JsonResultBean(JsonResultBean.FAULT);
            }
            log.info("[{}]阶段[{}]开始写入MySQL", module, stage);
            // 2.写入数据库
            handler.addMysql();
            log.info("[{}]阶段[{}]完成MySQL写入", module, stage);
            // 3.提交事务
            transactionManager.commit(transactionStatus);
            // 4.新增修改Redis
            log.info("[{}]阶段[{}]开始更新Redis", module, stage);
            handler.addOrUpdateRedis();
            log.info("[{}]阶段[{}]完成Redis更新", module, stage);
            handler.getProgressBar().setStatus(ProgressBar.STATUS_SUCCEED);
        } catch (Exception e) {
            // 事务回滚
            try {
                if (transactionStatus != null && !transactionStatus.isCompleted()) {
                    // 如果存redis报错，因为事务已经提交，这边回滚会报错，所有这里try一下把报错打出来，抛出存redis的报错
                    transactionManager.rollback(transactionStatus);
                }
            } catch (Exception e1) {
                log.error("事务回滚失败", e1);
            }
            if (log.isDebugEnabled()) {
                StringBuilder builder = new StringBuilder();
                for (ImportTable table : tables) {
                    builder.append(table.name()).append(";");
                }
                log.debug("{}-表导入失败, 执行事务回滚", builder.toString());
            }
            throw e;
        } finally {
            instance.unlock(tables);
        }

        return new JsonResultBean(JsonResultBean.SUCCESS);
    }
}
