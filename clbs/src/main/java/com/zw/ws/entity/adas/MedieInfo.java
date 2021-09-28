package com.zw.ws.entity.adas;

import lombok.Data;


/**
 * @author  Tdz
 * @create 2017-11-08 19:39
 **/
@Data
public class MedieInfo {
    private String id;
    private String pwd;
    private String carno;
    private Integer camera;
    private String starttime;
    private String endtime;
    private String filepath;
}
