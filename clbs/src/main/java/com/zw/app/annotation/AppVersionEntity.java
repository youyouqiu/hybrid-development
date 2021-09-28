package com.zw.app.annotation;

/***
 @Author gfw
 @Date 2018/12/11 19:57
 @Description APP版本控制实体
 @version 1.0
 **/
public class AppVersionEntity implements Comparable<AppVersionEntity> {

    /**
     * 版本
     */
    private Integer version;
    /**
     * 方法
     */
    private String method;

    public AppVersionEntity() {
    }

    public AppVersionEntity(Integer version, String method) {
        this.version = version;
        this.method = method;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public int compareTo(AppVersionEntity o) {
        return this.getVersion().compareTo(o.getVersion());
    }
}
