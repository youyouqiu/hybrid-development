package com.zw.platform.domain.vas.alram.query;

import lombok.Data;

@Data
public class TerminalSet {
    /**
     * 保留项 12位
     */
    private byte[] keep1 = new byte[12];

    /**
     * 急加速报警阈值
     */
    private Integer speedUpAlarm = 0XFF;
    /**
     * 急减速报警阈值
     */
    private Integer speedCutAlarm = 0XFF;
    /**
     * 急转弯报警阈值
     */
    private Integer swerveAlarm = 0XFF;
    /**
     * 碰撞报警阈值
     */
    private Integer collisionAlarm = 0XFF;
    /**
     * 保留项 6位
     */
    private byte[] keep2 = new byte[4];
}
