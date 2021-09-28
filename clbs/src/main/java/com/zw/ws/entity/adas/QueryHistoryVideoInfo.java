package com.zw.ws.entity.adas;


import lombok.Data;


/**
 * @author  Tdz
 * @create 2017-11-09 11:04
 **/
@Data
public class QueryHistoryVideoInfo {
    protected String id;

    protected String pwd;

    protected String carno;

    protected String color;

    protected Integer camera;

    protected long starttime;

    protected long endtime;

    protected String devicenum;

    protected String risknum;

    protected String visitid;

    protected String realPath;

    protected int type;//0:视频；1：音频

    protected  String alarmUUID;
}
