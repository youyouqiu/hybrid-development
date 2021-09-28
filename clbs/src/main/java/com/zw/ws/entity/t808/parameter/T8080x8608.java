package com.zw.ws.entity.t808.parameter;

import com.zw.platform.domain.multimedia.form.OrderForm;
import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;

import java.util.List;


/***
 @Author zhengjc
 @Date 2019/5/24 15:42
 @Description 围栏查询
 @version 1.0
 **/
@Data
public class T8080x8608 implements T808MsgBody {

    /**
     * 围栏查询的类型：
     * 1=查询圆形区域数据，
     * 2=查询矩形区域数据，
     * 3=查询多边形区域数据，
     * 4=查询线路数据
     */
    Integer queryType;

    /**
     * 查询区域总数
     */
    private Integer countId = 0;

    /**
     * 区域ID集合
     */
    List<Integer> queryId;

    public static T8080x8608 getInstance(OrderForm orderForm) {
        T8080x8608 data = new T8080x8608();
        data.queryType = orderForm.getQueryType();
        data.countId = orderForm.getCountId();
        data.queryId = orderForm.getQueryId();
        return data;
    }
}
