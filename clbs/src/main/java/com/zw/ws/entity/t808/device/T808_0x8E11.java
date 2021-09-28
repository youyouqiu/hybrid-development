package com.zw.ws.entity.t808.device;

import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;

import java.util.List;

/**
 * @Description: 驾驶员识别管理下发指令 F3交互实体
 * @Author Tianzhangxu
 * @Date 2020/9/30 10:24
 */
@Data
public class T808_0x8E11<T> implements T808MsgBody {
    private static final long serialVersionUID = 1L;

    /**
     * 设置类型0：增加（全替换），
     *
     * 1：删除（全删除），
     *
     * 2：删除指定条目，
     *
     * 3：修改(如果设备存在人脸 id，那么替换当前设备的人脸图片。如果设备不存在人脸 id，那么新增人脸)
     */
    private Integer type;

    /**
     * 人脸信息
     */
    private List<FaceInfo> faceList;
}
