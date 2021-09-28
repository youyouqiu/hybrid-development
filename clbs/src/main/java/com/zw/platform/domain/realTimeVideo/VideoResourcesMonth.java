package com.zw.platform.domain.realTimeVideo;

import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * @author Administrator
 */
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
public class VideoResourcesMonth implements T808MsgBody {
    private static final long serialVersionUID = -241145449421787515L;
    /**
     * 多媒体类型
     */
    private Integer type;

    /**
     * 查询日期 yymm
     */
    private String date;

    public VideoResourcesMonth(Integer type, String date) {
        this.type = type;
        this.date = date;
    }
}
