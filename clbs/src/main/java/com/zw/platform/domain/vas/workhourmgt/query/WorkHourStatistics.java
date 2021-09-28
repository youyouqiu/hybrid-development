package com.zw.platform.domain.vas.workhourmgt.query;

/**
 * Created by LiaoYuecai on 2016/9/28.
 */
public class WorkHourStatistics {

    private Integer No;//序号
    private String team;//车队
    private String brand;//车牌号
    private Double workHours = 0d;//工作时间
    private Integer workTimes = 0;//工作次数


    public boolean isEmpty(){
        if (No == null)
            return true;
        return false;
    }

    public void addWorkHours(Double workHours){
        this.workHours += workHours;
    }

    public void addWorkTimes(Integer workTimes){
        this.workTimes += workTimes;
    }

    public void setNo(Integer no) {
        No = no;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setWorkHours(Double workHours) {
        this.workHours = workHours;
    }

    public void setWorkTimes(Integer workTimes) {
        this.workTimes = workTimes;
    }

    public int getNo() {
        return No;
    }

    public String getTeam() {
        return team;
    }

    public String getBrand() {
        return brand;
    }

    public Double getWorkHours() {
        return workHours;
    }

    public Integer getWorkTimes() {
        return workTimes;
    }
}
