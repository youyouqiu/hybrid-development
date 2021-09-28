package com.zw.platform.util.spring;

import com.zw.platform.util.common.MethodLog;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;

import java.lang.reflect.Method;

public final class SpringAopLogHelper {
    private static final Logger log = LogManager.getLogger();

    public void log(JoinPoint joinPoint) {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        MethodLog ann = method.getAnnotation(MethodLog.class);
        if (ann != null) {
            log.debug("--位置：{} --方法：{} --描述：{}",
                new Object[] { joinPoint.getStaticPart(), ann.name(), ann.description() });
        }
    }

    public void logArg(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        MethodLog ann = method.getAnnotation(MethodLog.class);
        if (ann != null) {
            log.debug("--位置：{} --方法：{} --描述：{} --参数：{}",
                new Object[] { joinPoint.getStaticPart(), ann.name(), ann.description(), args });
        }
    }

    public void logArgAndReturn(JoinPoint joinPoint, Object returnObj) {
        Object[] args = joinPoint.getArgs();
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        MethodLog ann = method.getAnnotation(MethodLog.class);

        String returnObjClass = "null";
        if (returnObj != null) {
            returnObjClass = returnObj.getClass().toString();
        }

        if (ann != null) {
            log.debug("--位置：{} --方法：{} --描述：{} --参数：{} --返回结果类型：{}",
                new Object[] { joinPoint.getStaticPart(), ann.name(), ann.description(), args, returnObjClass });
        }
    }
}