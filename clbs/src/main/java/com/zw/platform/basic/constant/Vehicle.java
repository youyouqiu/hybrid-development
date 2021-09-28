package com.zw.platform.basic.constant;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 车辆常量类
 * @author zhangjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class Vehicle {
    /**
     * 监控对象在clbs端的状态与在PASS端状态的映射关系
     */
    private static final Map<Integer, Integer> CLBS_PAAS_STATUS_MAP;

    static {
        //1、在线，2在线停车，3在线行驶，4报警，5超速报警,6未定位,7未上线,8离线,9心跳
        CLBS_PAAS_STATUS_MAP = new HashMap<>();
        CLBS_PAAS_STATUS_MAP.put(2, 4);
        CLBS_PAAS_STATUS_MAP.put(3, 10);
        CLBS_PAAS_STATUS_MAP.put(4, 5);
        CLBS_PAAS_STATUS_MAP.put(5, 9);
        CLBS_PAAS_STATUS_MAP.put(6, 2);
        CLBS_PAAS_STATUS_MAP.put(9, 11);
    }

    /**
     * 根据监控对象的在线状态获取在pass端的在线状态
     * @param onlineStatus 1、在线，2在线停车，3在线行驶，4报警，5超速报警,6未定位,7未上线,8离线,9心跳
     * @return 在PAAS端对应的状态
     */
    public static Integer getPassStatus(Integer onlineStatus) {
        return CLBS_PAAS_STATUS_MAP.get(onlineStatus);
    }

    /**
     * 车辆导入模板字段数
     */
    public static final short COMMON_IMPORT_CELL = 57;
    public static final short ENGINEER_IMPORT_CELL = 71;
    public static final short FREIGHT_IMPORT_CELL = 75;
    /**
     * 最大保养里程数
     */
    public static final int BIGEST_MAINTAIN_MEILAGE = 1000000;

    /**
     * 车辆返回未绑定数量限制
     */
    public static final Integer UNBIND_SELECT_SHOW_NUMBER = 20;

    /**
     * 经营权类型
     */
    public static final String[] MANAGEMENT_TYPES = { "国有", "集体", "私营", "个体", "联营", "股份制", "外商投资", "港澳台及其他" };
    /**
     * 运营状态
     */
    public static final String[] OPERATING_STATES = { "营运", "停运", "挂失", "报废", "歇业", "注销", "迁出(过户)", "迁出(转籍)", "其他" };

    /**
     * 车辆购置方式
     */
    public static final String[] PURCHASE_WAY = { "分期付款", "一次性付清" };

    /**
     * 区域属性
     */
    public static final String[] AREA_ATTRIBUTES = { "省内", "跨省", "进京报备" };

    /**
     * 电话是否校验
     */
    public static final String[] PHONE_CHECKS = { "未校验", "已校验" };

    /**
     * 维修状态
     */
    public static final String[] STATE_REPAIRS = { "否", "是" };

    /**
     * 启用状态
     */
    public static final String[] IS_START = { "启用", "停用" };

    /**
     * 需要维修的状态
     */
    public static final String NEED_REPAIR = "1";

    /**
     * 处于运营状态
     */
    public static final String IN_OPERATING_SATTE = "0";

    /**
     * 类别标准 0：通用；1：货运；2：工程机械
     */
    public static class Standard {
        public static final int COMMON = 0;
        public static final int FREIGHT_TRANSPORT = 1;
        public static final int ENGINEERING = 2;
    }

    /***
     * 监控对象的绑定类型 0 未绑定 1绑定
     */
    public static class BindType {
        public static final String UNBIND = "0";
        public static final String HAS_BIND = "1";
    }
}
