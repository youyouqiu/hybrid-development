package com.zw.platform.util.imports.lock.interceptor;

import com.zw.platform.util.imports.lock.ImportDaoLock;
import com.zw.platform.util.imports.lock.ImportLockCounter;
import com.zw.platform.util.imports.lock.ImportTable;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import java.lang.reflect.Method;

/**
 * @author create by zhouzongbo on 2020/9/6.
 */
public class ImportLockDaoInterceptor implements MethodInterceptor {

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        final Method method = invocation.getMethod();
        final ImportDaoLock importDaoLock = method.getAnnotation(ImportDaoLock.class);
        if (importDaoLock == null) {
            return invocation.proceed();
        }

        final ImportTable importTable = importDaoLock.value();
        final ImportLockCounter counter = ImportLockCounter.getInstance();
        // 如果是Dao加的锁, 仅校验是否有锁, 并且是否为自己的锁即可
        counter.checkTableLock(importTable);

        return invocation.proceed();
    }
}
