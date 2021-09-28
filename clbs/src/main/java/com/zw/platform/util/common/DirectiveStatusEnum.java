package com.zw.platform.util.common;

/**
 * 指令下发状态Enum
 * @Author: Tianzhangxu
 * @Date: 2020/6/15 10:46
 */
public enum DirectiveStatusEnum {
    /**
     * 指令已生效
     */
    IS_EFFECTED(0),

    /**
     * 指令未生效
     */
    IS_NOT_EFFECTED(1),

    /**
     * 通讯错误
     */
    COMMUNICATION_ERROR(2),

    /**
     * 设备不支持
     */
    EQUIPMENT_NOT_SUPPORTED(3),

    /**
     * 指令已发出
     */
    ISSUED(4),

    /**
     * 终端处理中
     */
    DEVICE_IN_PROCESSING(7),

    /**
     * 参数下发失败
     */
    IS_FAILED(8);

    private final Integer num;

    DirectiveStatusEnum(Integer num) {
        this.num = num;
    }

    public Integer getNum() {
        return num;
    }
}
