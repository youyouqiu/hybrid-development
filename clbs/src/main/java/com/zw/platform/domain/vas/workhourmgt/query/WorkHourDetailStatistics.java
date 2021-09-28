package com.zw.platform.domain.vas.workhourmgt.query;

/**
 * Created by LiaoYuecai on 2016/9/28.
 */
public class WorkHourDetailStatistics {

    private int No;//序号
    private String brand;//车牌号
    private String startTime;//开始时间
    private String endTime;//结束时间
    private Double workHours;//工作时间

    private String longtitude;//经度
    private String latitude;//纬度
    private String position;//位置

    public int getNo() {
        return No;
    }

    public String getBrand() {
        return brand;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public Double getWorkHours() {
        return workHours;
    }

    public String getLongtitude() {
        return longtitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getPosition() {
        return position;
    }

    public void setNo(int no) {
        No = no;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setWorkHours(Double workHours) {
        this.workHours = workHours;
    }

    public void setLongtitude(String longtitude) {
        this.longtitude = longtitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setPosition(String position) {
        this.position = position;
    }
}
