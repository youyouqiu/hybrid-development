package com.zw.platform.util.imports.lock;

/**
 * 导入模块
 * @author create by zhouzongbo on 2020-08-28.
 */
public enum ImportModule implements ImportType {
    /**
     * 信息配置
     */
    CONFIG("信息配置"),
    /**
     * 车辆管理
     */
    VEHICLE("车辆管理"),
    /**
     * 人员管理
     */
    PEOPLE("人员管理"),
    /**
     * 物品管理
     */
    THING("物品管理"),
    /**
     * 终端管理
     */
    DEVICE("终端管理"),
    /**
     * sim卡管理
     */
    SIM_CARD("sim卡管理"),
    /**
     * 从业人员
     */
    PROFESSIONAL("从业人员"),
    /**
     * 分组
     */
    ASSIGNMENT("分组"),

    /**
     * 对讲信息配置
     */
    INTERCOM("对讲信息配置");

    private final String value;

    ImportModule(String value) {
        this.value = value;
    }

    @Override
    public String value() {
        return value;
    }
}