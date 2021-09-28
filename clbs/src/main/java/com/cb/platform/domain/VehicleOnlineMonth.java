package com.cb.platform.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.UUID;

/**
 * 车辆在线率月报表
 *
 * @author zhangsq
 * @date 2018/5/2 16:06
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class VehicleOnlineMonth implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id = UUID.randomUUID().toString();
    private byte[] vehicleId;
    private byte[] groupId;
    private Long monthDate;
    private Integer day1;
    private Integer day2;
    private Integer day3;
    private Integer day4;
    private Integer day5;
    private Integer day6;
    private Integer day7;
    private Integer day8;
    private Integer day9;
    private Integer day10;
    private Integer day11;
    private Integer day12;
    private Integer day13;
    private Integer day14;
    private Integer day15;
    private Integer day16;
    private Integer day17;
    private Integer day18;
    private Integer day19;
    private Integer day20;
    private Integer day21;
    private Integer day22;
    private Integer day23;
    private Integer day24;
    private Integer day25;
    private Integer day26;
    private Integer day27;
    private Integer day28;
    private Integer day29;
    private Integer day30;
    private Integer day31;
    private String groupName;
    private String plateColor;
    private String vehicleType;
    private String monitorName;

}
