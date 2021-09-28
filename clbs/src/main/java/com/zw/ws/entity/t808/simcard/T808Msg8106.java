package com.zw.ws.entity.t808.simcard;

import com.zw.protocol.msg.t808.T808MsgBody;
import com.zw.ws.entity.t808.parameter.ParamItem;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 获取指定参数信息
 * @author Tdz
 * @since 2017-02-21 15:18
 **/
@Data
public class T808Msg8106 implements T808MsgBody {
    private static final long serialVersionUID = 1L;
    private Integer paramSum;
    /**
     * 外设消息项列表
     */
    private List<Integer> paramItems = new ArrayList<>();

    public void setParamItems(List<ParamItem> paramItems) {
        this.paramItems = paramItems.stream().map(ParamItem::getParamId).collect(Collectors.toList());
    }

    public void setParamIds(List<Integer> paramIds) {
        this.paramItems = paramIds;
    }
}
