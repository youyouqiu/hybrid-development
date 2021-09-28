package com.cb.platform.domain;

import lombok.Data;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

@Data
public class ItemNameEntity implements Serializable {
    private static final long serialVersionUID = 3887797924001701913L;

    private String id;
    /**
     * 品名
     */
    private String name;

    /**
     * 11:危险货物1类1项、
     * 12:危险货物1类2项、
     * 13:危险货物1类3项、
     * 14:危险货物1类4项、
     * 15:危险货物1类5项、
     * 16:危险货物1类6项、
     * 21:危险货物2类1项、
     * 22:危险货物2类2项、
     * 23：危险货物2类3项、
     * 3：危险货物3类、
     * 41：危险货物4类1项、
     * 42：危险货物4类2项、
     * 43：危险货物4类3项、
     * 51：危险货物5类1项、
     * 52：危险货物5类2项、
     * 61：危险货物6类1项、
     * 62：危险货物6类2项、
     * 7：危险货物7类、
     * 8：危险货物8类、
     * 9：危险货物9类
     */
    private Integer dangerType;
    /**
     * 单位：
     * 1：kg
     * 2:L
     */
    private Integer unit;

    private String remark;

    private Integer flag;
    private Date createDataTime;
    private String createDataUsername;
    private Date updateDataTime;
    private String updateDataUsername;

}
