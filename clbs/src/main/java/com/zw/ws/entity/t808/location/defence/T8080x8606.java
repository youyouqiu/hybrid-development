
package com.zw.ws.entity.t808.location.defence;

import com.zw.protocol.msg.t808.T808MsgBody;
import com.zw.ws.entity.line.LinePoints;
import lombok.Data;

import java.util.ArrayList;


/**
 * <p>
 * Title: T8080x8606.java
 * </p>
 * <p>
 * Copyright: Copyright (c) 2016
 * </p>
 * <p>
 * Company: ZhongWei
 * </p>
 * <p>
 * team: ZhongWeiTeam
 * </p>
 *
 * @version 1.0
 * @author: Jiangxiaoqiang
 * @date 2016年9月5日下午3:12:44
 */
@Data
public class T8080x8606 implements T808MsgBody {

    private static final long serialVersionUID = 1L;

    /// <summary>
    /// 路线ID
    /// </summary>
    private Integer lineID;

    /// <summary>
    /// 路线属性
    /// </summary>
    private Long lineParam;

    /// <summary>
    /// 起始时间
    /// </summary>
    private String startTime;

    /// <summary>
    /// 结束时间
    /// </summary>
    private String endTime;

    /// <summary>
    /// 路线总拐点数
    /// </summary>
    private Integer swervePortSum;

    private Integer packagePointsCount; // 包拐点数

    /// <summary>
    /// 路线名称长度,若区域属性15位为0 则没有该字段
    /// </summary>
    private byte routeNameLength;

    /// <summary>
    /// 路线名称,经GBK编码, 长度n 若路线属性15位为0 则没有该字段
    /// </summary>
    private String routeName;

    /**
     * 线路名称，2019协议调整
     */
    private String name;

    /// 拐点项
    /// </summary>
    private ArrayList<LinePoints> swervePortParams = new ArrayList<LinePoints>();

    public void initParam2019() {
        name = routeName;
    }
}
