package com.zw.platform.domain.param;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * Created by FanLu on 2017/4/19.
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class InformationParam extends BaseFormBean implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final int FREQUENCY_45 = 0;
    public static final int FREQUENCY_60 = 1;
    public static final int FREQUENCY_75 = 2;
    private String vid;
    private Integer operationType;//操作类型0：删除终端全部信息项；1：更新菜单；2：追加菜单；3：修改菜单
    private Integer infoId;//信息id
    private String infoContent;//信息内容
    /**
     * 0:45;1:60:2:75;
     */
    private Integer sendFrequency;
    /**
     * 信息内容(0x8403)
     */
    private String messageContent;
}
