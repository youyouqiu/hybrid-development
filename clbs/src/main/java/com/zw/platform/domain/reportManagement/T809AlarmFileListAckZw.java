package com.zw.platform.domain.reportManagement;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class T809AlarmFileListAckZw implements Serializable {
    /**
     * 对应报警附件目录请求消息源子业务类型标识
     */
    private Integer sourceDataType;
    /**
     * 对应报警附件目录请求请求消息源报文序列号
     */
    private Integer sourceMsgSn;

    /**
     * 报警信息ID
     */
    private String infoId;

    /**
     * 附件数量
     */
    private Integer fileCount;

    /**
     * 附件信息列表
     */
    private List<WarnMsgFileInfoZw> fileInfos;

    public int getTotalFileLength() {
        int dataLength = 28;
        for (WarnMsgFileInfoZw media : fileInfos) {
            dataLength += (media.getFileUrlLength() + media.getFileNameLength());
        }
        return dataLength;
    }
}
