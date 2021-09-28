package com.zw.platform.domain.multimedia;

import com.zw.protocol.msg.t808.T808MsgBody;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 多媒体上传
 * @author  Tdz
 * @create 2017-04-24 10:13
 **/
@Data
@EqualsAndHashCode(callSuper = true)
public class MultimediaUpload extends MultimediaRetrieval implements T808MsgBody {

    private Integer deleteSign;

}
