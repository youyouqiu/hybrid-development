package com.zw.platform.domain.realTimeVideo;

import com.zw.platform.util.common.BaseFormBean;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author zhangsq
 * @date 2018/3/22 16:58
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class VideoSpecialSetting extends BaseFormBean implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer videoPlayTime; //视频播放缺省时间（s）

    private Integer videoRequestTime; //视频空闲断开时间（s）

    private Integer ftpStorage; //ftp存储容量（GB）

    private Integer type; //存储控件满时处理方式：0 空间满自动覆盖，1 空间满停止录制


}
