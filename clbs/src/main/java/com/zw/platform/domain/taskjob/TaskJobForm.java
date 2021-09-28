package com.zw.platform.domain.taskjob;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;

/**
 * 功能描述: 任务的描述对象
 * @author zhengjc
 * @date 2019/9/27
 * @time 13:48
 */
@Data
public class TaskJobForm extends BaseFormBean {
    private static final long serialVersionUID = 1L;

    //升级类型
    private String upgradeType;

    private String vehicleId;

    /**
     * cron表达式
     */
    private String cronExpression;
    /**
     * 任务调用的方法名
     */
    private String methodName;
    /**
     * 任务是否有状态
     */
    private String isConcurrent;
    /**
     * 任务描述
     */
    private String description;

    /**
     * 任务执行时调用哪个类的方法 包名+类名
     */
    private String beanClass;

    /**
     * 任务状态
     */
    private String jobStatus;
    /**
     * 任务分组
     */
    private String jobGroup;

    /**
     * Spring bean
     */
    private String springBean;
    /**
     * 任务名
     */
    private String jobName;





}
