package com.zw.platform.util.imports.lock;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

/**
 * 导入锁AOP
 * @author create by zhouzongbo on 2020/8/28.
 */
@Slf4j
public class ImportLockHelper {

    public void checkTableLock(JoinPoint joinPoint) {
        final MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        final Method method = signature.getMethod();
        final ImportDaoLock importDaoLock = method.getAnnotation(ImportDaoLock.class);
        if (importDaoLock == null) {
            return;
        }
        final ImportTable importTable = importDaoLock.value();
        final ImportLockCounter counter = ImportLockCounter.getInstance();
        // 如果是Dao加的锁, 仅校验是否有锁, 并且是否为自己的锁即可
        counter.checkTableLock(importTable);
    }

    /**
     * 导入切面
     * @param pjp        pjp
     * @param importLock lock注解, 从中获取对应的值
     * @return obj
     */
    public Object doLock(ProceedingJoinPoint pjp, ImportLock importLock) throws Throwable {
        final ImportModule module = importLock.value();
        final ImportLockCounter counter = ImportLockCounter.getInstance();

        // 加锁, 这里方在try catch外面的原因是: (不可重入)
        // increment内部会有一个isLock的判断, 如果有人已经加锁了, 会抛出异常, 但还未执行到incrementModule方法, 如果放在try块里面,
        // 就会执行释放锁的操作, 数据会出现问题
        counter.lock(module);
        Object proceed = null;
        try {
            proceed = pjp.proceed();
        } catch (Throwable throwable) {
            log.error("导入异常", throwable);
            throw throwable;
        } finally {
            // 释放锁
            counter.unlock(module);
        }
        return proceed;
    }
}
