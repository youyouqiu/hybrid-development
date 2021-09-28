package com.cb.platform.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;
import java.util.UUID;

/**
 * 车辆在线率月报表
 *
 * @author zhangsq
 * @date 2018/5/2 16:06
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class VehicleMileMonth implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id = UUID.randomUUID().toString();
    private byte[] vehicleId;
    private byte[] groupId;
    private Long monthDate;
    private double day1;
    private double day2;
    private double day3;
    private double day4;
    private double day5;
    private double day6;
    private double day7;
    private double day8;
    private double day9;
    private double day10;
    private double day11;
    private double day12;
    private double day13;
    private double day14;
    private double day15;
    private double day16;
    private double day17;
    private double day18;
    private double day19;
    private double day20;
    private double day21;
    private double day22;
    private double day23;
    private double day24;
    private double day25;
    private double day26;
    private double day27;
    private double day28;
    private double day29;
    private double day30;
    private double day31;
    private String groupName;
    private String plateColor;
    private String vehicleType;
    private String monitorName;
    private List<Double> monthEveryDayMile;
    private double mileCount;
}
