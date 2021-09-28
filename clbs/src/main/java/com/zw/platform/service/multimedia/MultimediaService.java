package com.zw.platform.service.multimedia;

import com.zw.platform.basic.dto.BindDTO;
import com.zw.platform.domain.multimedia.MultimediaRetrieval;
import com.zw.platform.domain.multimedia.MultimediaUpload;
import com.zw.platform.domain.multimedia.Photograph;
import com.zw.platform.domain.multimedia.Record;
import com.zw.platform.domain.param.TelBack;

/**
 * Created by LiaoYuecai on 2017/3/31.
 */
public interface MultimediaService {
    /**
     * 拍照命令接口
     * @param mobile      终端手机号
     * @param photograph  拍照命令参数
     * @param monitorInfo 监控对象
     */
    void photograph(String deviceId, Photograph photograph, String mobile, Integer serialNumber, BindDTO monitorInfo);

    /**
     * 录音命令接口
     * @param record
     * @param mobile
     * @param deviceType
     */
    void record(String deviceId, Record record, String mobile, Integer serialNumber, String deviceType);

    /**
     * 电话回拨
     */
    void telListen(String deviceId, TelBack telBack, String simcardNumber, Integer serialNumber, String deviceType);

    /**
     * 多媒体检索
     */
    void multimediaRetrieval(String deviceId, MultimediaRetrieval multimediaRetrieval, String mobile,
        Integer serialNumber, String deviceType);

    /**
     * 多媒体上传
     */
    void multimediaUpload(MultimediaUpload multimediaUpload, String deviceId, String simcardNumber,
        Integer serialNumber, String deviceType);
}
