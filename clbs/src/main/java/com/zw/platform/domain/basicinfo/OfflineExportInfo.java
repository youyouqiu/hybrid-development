package com.zw.platform.domain.basicinfo;

import com.alibaba.fastjson.JSONObject;
import com.cb.platform.domain.OffLineExportBusinessId;
import lombok.Data;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.TreeMap;

@Data
public class OfflineExportInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用户名
     */
    private String userName;

    /**
     * 模块名
     */
    private String module;

    /**
     * 业务编号
     */
    private String businessId;

    /**
     * 查询条件json串 treemap
     */
    private String queryCondition;

    /**
     * 创建时间
     */
    private Date createDateTime;

    /**
     * 摘要 导出摘要编号 查询条件排序后进行算法处理,这里采用sha256摘要编码
     */
    private String digestId;
    /**
     * 下载时候显示的文件名称
     */
    private String fileName;

    /**
     * 导入的状态 0 待执行 1 执行中 2成功 3 失败
     */
    private Integer status;

    /**
     * 完成时间
     */
    private Date finishTime;

    /**
     * 真实路径(服务器存储的地址)
     */
    private String realPath;

    /**
     * 文件大小(单位Byte)
     */
    private Integer fileSize;

    /**
     * 组装文件路径
     */
    private String assemblePath;

    /**
     * 文件大小KB
     */
    private Double doubleFileSize;

    public static OfflineExportInfo getInstance(String module, String fileName) {
        OfflineExportInfo data = new OfflineExportInfo();
        data.module = module;
        data.fileName = fileName;
        data.status = 0;
        data.createDateTime = new Date();
        return data;
    }

    public void assembleCondition(TreeMap<String, String> param, OffLineExportBusinessId businessId) {
        param.put("businessId", businessId.getBusinessId());
        //因为数据是实时的所以，需要加上一个标志，保证pass端每次都能够计算数据重新计算
        param.put("flag", System.currentTimeMillis() + "");
        this.businessId = businessId.getBusinessId();
        this.queryCondition = JSONObject.toJSONString(param);
        this.digestId = DigestUtils.sha256Hex(queryCondition);

    }

}
